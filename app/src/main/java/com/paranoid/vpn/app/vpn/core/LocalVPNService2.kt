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
    private val binder = LocalBinder()

    private var vpnInterface: ParcelFileDescriptor? = null
    private var pendingIntent: PendingIntent? = null
    private var deviceToNetworkUDPQueue: BlockingQueue<Packet>? = null
    private var deviceToNetworkTCPQueue: BlockingQueue<Packet>? = null
    private var networkToDeviceQueue: BlockingQueue<ByteBuffer>? = null
    private var bioUdpHandlerJob: Job? = null
    private var nioSingleThreadTcpHandlerJob: Job? = null
    private var VPNRunnableJob: Job? = null

    private val context = Dispatchers.IO

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
        setupVPN(currentConfig ?: VPNConfigDataGenerator.getVPNConfigItem())

        deviceToNetworkUDPQueue = ArrayBlockingQueue(1000)
        deviceToNetworkTCPQueue = ArrayBlockingQueue(1000)
        networkToDeviceQueue = ArrayBlockingQueue(1000)

        bioUdpHandlerJob = CoroutineScope(context).launch {
            BioUdpHandler(
                deviceToNetworkUDPQueue as ArrayBlockingQueue<Packet>,
                networkToDeviceQueue as ArrayBlockingQueue<ByteBuffer>,
                this@LocalVPNService2,
                context
            ).run()
        }
        bioUdpHandlerJob!!.start()

        nioSingleThreadTcpHandlerJob = CoroutineScope(context).launch {
            NioSingleThreadTcpHandler(
                deviceToNetworkTCPQueue as ArrayBlockingQueue<Packet>,
                networkToDeviceQueue as ArrayBlockingQueue<ByteBuffer>,
                this@LocalVPNService2
            ).run()
        }
        nioSingleThreadTcpHandlerJob!!.start()

        VPNRunnableJob = CoroutineScope(context).launch {
            VpnReadWorker(
                vpnInterface!!.fileDescriptor,
                deviceToNetworkUDPQueue as ArrayBlockingQueue<Packet>,
                deviceToNetworkTCPQueue as ArrayBlockingQueue<Packet>,
                networkToDeviceQueue as ArrayBlockingQueue<ByteBuffer>
            ).run()
        }
        VPNRunnableJob!!.start()
    }

    private fun setupVPN(config: VPNConfigItem) {
        try {
            if (vpnInterface == null) {
                val builder = Builder()
                builder.addAddress(config.local_ip, 32)
                builder.addRoute(VPN_ROUTE, 0)
                builder.addDnsServer(config.primary_dns)
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
            }
            "stop" -> {
                CoroutineScope(context).launch {
                    stopVPN()
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
        cleanup()
    }

    private suspend fun cleanup() {
        bioUdpHandlerJob?.cancelAndJoin()
        Log.i(TAG, "Vpnservice after bioUdpHandlerJob.cancelAndJoin")
        nioSingleThreadTcpHandlerJob?.cancelAndJoin()
        Log.i(TAG, "Vpnservice after nioSingleThreadTcpHandlerJob.cancelAndJoin")
        VPNRunnableJob?.cancelAndJoin()
        Log.i(TAG, "Vpnservice after all cancelAndJoin")
        context.cancel()
        Log.i(LocalVPNService2.TAG, "Vpnservice after cancel context")
        CoroutineScope(context).coroutineContext.cancelChildren()
        Log.i(LocalVPNService2.TAG, "Vpnservice after cancelChildren")
        deviceToNetworkTCPQueue = null
        deviceToNetworkUDPQueue = null
        networkToDeviceQueue = null
        closeResources(vpnInterface!!)
    }

    companion object {
        var currentConfig: VPNConfigItem? = null

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