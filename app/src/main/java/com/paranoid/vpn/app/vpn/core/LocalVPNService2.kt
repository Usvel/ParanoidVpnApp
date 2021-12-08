package com.paranoid.vpn.app.vpn.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Binder
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.VPNConfigDataGenerator
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.VPNConfigItem
import com.paranoid.vpn.app.vpn.core.handlers.udp.BioUdpHandler
import com.paranoid.vpn.app.vpn.core.handlers.tcp.NioSingleThreadTcpHandler
import com.paranoid.vpn.app.vpn.core.config.Config
import com.paranoid.vpn.app.vpn.core.handlers.vpn.VpnReadWorker
import com.paranoid.vpn.app.vpn.core.protocol.tcpip.Packet
import kotlinx.coroutines.*
import java.io.*
import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

class LocalVPNService2 : VpnService() {

    val addPacketUseCase = AddPacketUseCase(VPNPacketMemoryCache)

    private val binder = LocalBinder()

    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private var vpnInterface: ParcelFileDescriptor? = null
    private var pendingIntent: PendingIntent? = null

    private var deviceToNetworkUDPQueue: BlockingQueue<Packet>? = null
    private var deviceToNetworkTCPQueue: BlockingQueue<Packet>? = null
    private var networkToDeviceQueue: BlockingQueue<ByteBuffer>? = null

    private var bioUdpHandlerJob: Job? = null
    private var nioSingleThreadTcpHandlerJob: Job? = null
    private var VPNRunnableJob: Job? = null

    private var watchDogJob: Job? = null

    private val context = Dispatchers.IO

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()

        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = getNetworkCallBack()
        connectivityManager!!.registerNetworkCallback(
            getNetworkRequest(),
            networkCallback!!
        )
    }

    private fun setupVPN(config: VPNConfigItem) {
        try {
            if (vpnInterface == null) {
                val builder = Builder()
                    .addAddress(config.local_ip, 32)
                    .addRoute(VPN_ROUTE, 0)
                    .addDnsServer(config.primary_dns)
                config.secondary_dns?.let { builder.addDnsServer(it) }
                if (Config.testLocal) {
                    builder.addAllowedApplication(packageName)
                }
                vpnInterface = builder.setSession(packageName).establish()
            }
        } catch (e: Exception) {
            Log.e(TAG, "error", e)
        }
    }

    private fun startUdpJob() {
        bioUdpHandlerJob = CoroutineScope(context).launch {
            BioUdpHandler(
                deviceToNetworkUDPQueue as ArrayBlockingQueue<Packet>,
                networkToDeviceQueue as ArrayBlockingQueue<ByteBuffer>,
                this@LocalVPNService2,
                context
            ).run()
        }
        bioUdpHandlerJob!!.start()
    }

    private fun startTcpJob() {
        nioSingleThreadTcpHandlerJob = CoroutineScope(context).launch {
            NioSingleThreadTcpHandler(
                deviceToNetworkTCPQueue as ArrayBlockingQueue<Packet>,
                networkToDeviceQueue as ArrayBlockingQueue<ByteBuffer>,
                this@LocalVPNService2
            ).run()
        }
        nioSingleThreadTcpHandlerJob!!.start()
    }

    private fun startJobs() {
        deviceToNetworkUDPQueue = ArrayBlockingQueue(1000)
        deviceToNetworkTCPQueue = ArrayBlockingQueue(1000)
        networkToDeviceQueue = ArrayBlockingQueue(1000)

        startUdpJob()
        startTcpJob()

        VPNRunnableJob = CoroutineScope(context).launch {
            VpnReadWorker(
                vpnInterface!!.fileDescriptor,
                deviceToNetworkUDPQueue as ArrayBlockingQueue<Packet>,
                deviceToNetworkTCPQueue as ArrayBlockingQueue<Packet>,
                networkToDeviceQueue as ArrayBlockingQueue<ByteBuffer>,
                networkToDeviceQueue as ArrayBlockingQueue<ByteBuffer>,
                addPacketUseCase
            ).run()
        }
        VPNRunnableJob!!.start()
    }

    private fun startWatchDog() {
        watchDogJob = CoroutineScope(context).launch {
            while (coroutineContext.isActive) {
                if (bioUdpHandlerJob?.isActive != true) {
                    Log.i(TAG, "watchDogJob cancelling bioUdpHandlerJob")
                    bioUdpHandlerJob?.cancelAndJoin()
                    Log.i(TAG, "watchDogJob starting bioUdpHandlerJob")
                    startUdpJob()
                }

                if (nioSingleThreadTcpHandlerJob?.isActive != true) {
                    Log.i(TAG, "watchDogJob canceling nioSingleThreadTcpHandlerJob")
                    nioSingleThreadTcpHandlerJob?.cancelAndJoin()
                    Log.i(TAG, "watchDogJob starting nioSingleThreadTcpHandlerJob")
                    startTcpJob()
                }

                delay(5000)
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        when (intent.action) {
            "start" -> {
                createNotificationChannel()
                pendingIntent =
                    PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
                val notification = NotificationCompat.Builder(this, "Channel_id1")
                    .setContentTitle("Example")
                    .setContentText("App is running")
                    .setContentIntent(pendingIntent).build()
                startForeground(1, notification)
                val config = intent.getSerializableExtra("config").toString()
                val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                val currentConfig = gson.fromJson(config, VPNConfigItem::class.java)
                setupVPN(currentConfig)
                startJobs()
            }
            "stop" -> {
                CoroutineScope(context).launch {
                    stopVPN()
                }
            }
            "config" -> {
                CoroutineScope(context).launch {
                    cleanup()
                    val config = intent.getSerializableExtra(Intent.EXTRA_TEXT).toString()
                    val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                    val configItem: VPNConfigItem = gson.fromJson(config, VPNConfigItem::class.java)
                    setupVPN(configItem)
                    startJobs()
                }
            }
        }
        return START_STICKY
    }

    // Needed for Android 8.1 and above
    private fun createNotificationChannel() {
        Log.d(TAG, "createNotificationChannel")
        val notificationChannel = NotificationChannel(
            "Channel_id1", "Foreground service", NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(
            NotificationManager::class.java
        )
        manager.createNotificationChannel(notificationChannel)
    }

    private suspend fun stopVPN() {
        stopForeground(true)
        stopSelf()

        networkCallback?.let { connectivityManager?.unregisterNetworkCallback(it) }
        cleanup()
    }

    private suspend fun cleanup() {
        // watchDogJob?.cancelAndJoin()
        // Log.i(TAG, "Vpnservice after watchDogJob.cancelAndJoin")
        bioUdpHandlerJob?.cancelAndJoin()
        Log.i(TAG, "Vpnservice after bioUdpHandlerJob.cancelAndJoin")
        nioSingleThreadTcpHandlerJob?.cancelAndJoin()
        Log.i(TAG, "Vpnservice after nioSingleThreadTcpHandlerJob.cancelAndJoin")
        VPNRunnableJob?.cancelAndJoin()
        Log.i(TAG, "Vpnservice after all cancelAndJoin")
        context.cancel()
        Log.i(TAG, "Vpnservice after cancel context")
        CoroutineScope(context).coroutineContext.cancelChildren()
        Log.i(TAG, "Vpnservice after cancelChildren")
        deviceToNetworkTCPQueue = null
        deviceToNetworkUDPQueue = null
        networkToDeviceQueue = null
        closeResources(vpnInterface!!)
        vpnInterface = null
    }

    private fun isConnected(): Boolean {
        val capabilities =
            connectivityManager?.getNetworkCapabilities(connectivityManager?.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) or
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) or
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            )
                return true
        }
        return false
    }

    private fun getNetworkCallBack(): ConnectivityManager.NetworkCallback {
        return object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)

                if (nioSingleThreadTcpHandlerJob != null && nioSingleThreadTcpHandlerJob?.isActive != true)
                    startTcpJob()
                if (bioUdpHandlerJob != null && bioUdpHandlerJob?.isActive != true)
                    startUdpJob()
//                if (watchDogJob?.isActive != true)
//                    startWatchDog()
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                CoroutineScope(context).launch {
                    // watchDogJob?.cancelAndJoin()
                    // Log.i(TAG, "onLost after watchDogJob.cancelAndJoin")
                    bioUdpHandlerJob?.cancelAndJoin()
                    Log.i(TAG, "onLost after bioUdpHandlerJob.cancelAndJoin")
                    nioSingleThreadTcpHandlerJob?.cancelAndJoin()
                    Log.i(TAG, "onLost after nioSingleThreadTcpHandlerJob.cancelAndJoin")
                }
            }

        }
    }

    private fun getNetworkRequest(): NetworkRequest {
        return NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .build()
    }

    companion object {
        val TAG: String = LocalVPNService2::class.java.simpleName
        private val VPN_ADDRESS = "10.0.0.2" // Only IPv4 support for now
        private val VPN_ROUTE = "0.0.0.0" // Intercept everything

        // TODO: Move this to a "utils" class for reuse
        fun closeResources(vararg resources: Closeable) {
            for (resource in resources) {
                try {
                    resource.close()
                } catch (e: IOException) {
                    // Ignore
                }
            }
        }
    }

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): LocalVPNService2 = this@LocalVPNService2
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }
}