package com.example.paranoid.ui.vpn.basic_client

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.paranoid.ui.vpn.VPNFragment.Companion.downByte
import com.example.paranoid.ui.vpn.VPNFragment.Companion.upByte
import com.example.paranoid.ui.vpn.basic_client.handlers.udp.BioUdpHandler
import com.example.paranoid.ui.vpn.basic_client.handlers.NioSingleThreadTcpHandler
import com.example.paranoid.ui.vpn.basic_client.config.Config
import com.example.paranoid.ui.vpn.basic_client.handlers.vpn.VpnReadWorker
import com.example.paranoid.ui.vpn.basic_client.protocol.tcpip.Packet
import com.example.paranoid.ui.vpn.basic_client.util.ByteBufferPool
import kotlinx.coroutines.*
import java.io.*
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import kotlin.coroutines.coroutineContext

class LocalVPNService2 : VpnService() {
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
        setupVPN()

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

    private fun setupVPN() {
        try {
            if (vpnInterface == null) {
                val builder = Builder()
                builder.addAddress(VPN_ADDRESS, 32)
                builder.addRoute(VPN_ROUTE, 0)
                builder.addDnsServer(Config.dns)
                if (Config.testLocal) {
                    builder.addAllowedApplication("com.example.paranoid")
                }
                vpnInterface = builder.setSession("com.example.paranoid").establish()
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
        nioSingleThreadTcpHandlerJob?.cancel()
        VPNRunnableJob?.cancel()
        context.cancel()
        CoroutineScope(context).coroutineContext.cancelChildren()
        deviceToNetworkTCPQueue = null
        deviceToNetworkUDPQueue = null
        networkToDeviceQueue = null
        closeResources(vpnInterface!!)
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
}