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
import com.example.paranoid.ui.vpn.basic_client.bio.BioUdpHandler
import com.example.paranoid.ui.vpn.basic_client.bio.NioSingleThreadTcpHandler
import com.example.paranoid.ui.vpn.basic_client.config.Config
import com.example.paranoid.ui.vpn.basic_client.protocol.tcpip.Packet
import com.example.paranoid.ui.vpn.basic_client.util.ByteBufferPool
import kotlinx.coroutines.*
import java.io.*
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class LocalVPNService2 : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private var pendingIntent: PendingIntent? = null
    private var deviceToNetworkUDPQueue: BlockingQueue<Packet>? = null
    private var deviceToNetworkTCPQueue: BlockingQueue<Packet>? = null
    private var networkToDeviceQueue: BlockingQueue<ByteBuffer>? = null

    private var _dispatcher: ExecutorCoroutineDispatcher? = null
    private val dispatcher get() = _dispatcher!!

    private val context = Dispatchers.IO + SupervisorJob()

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
        setupVPN()

        _dispatcher = Executors.newFixedThreadPool(10).asCoroutineDispatcher()

        deviceToNetworkUDPQueue = ArrayBlockingQueue(1000)
        deviceToNetworkTCPQueue = ArrayBlockingQueue(1000)
        networkToDeviceQueue = ArrayBlockingQueue(1000)


        CoroutineScope(context).launch {
            BioUdpHandler(
                deviceToNetworkUDPQueue as ArrayBlockingQueue<Packet>,
                networkToDeviceQueue as ArrayBlockingQueue<ByteBuffer>,
                this@LocalVPNService2,
                context
            ).run()
        }

        CoroutineScope(context).launch {
            NioSingleThreadTcpHandler(
                deviceToNetworkTCPQueue as ArrayBlockingQueue<Packet>,
                networkToDeviceQueue as ArrayBlockingQueue<ByteBuffer>,
                this@LocalVPNService2).run()
        }

        CoroutineScope(context).launch {
            VPNRunnable(
                vpnInterface!!.fileDescriptor,
                deviceToNetworkUDPQueue as ArrayBlockingQueue<Packet>,
                deviceToNetworkTCPQueue as ArrayBlockingQueue<Packet>,
                networkToDeviceQueue as ArrayBlockingQueue<ByteBuffer>
            ).run()
        }
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
            System.exit(0)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        createNotificationChannel()
        //val intent = Intent(this, VPNFragment::class.java)
        when (intent.action) {
            "start" -> {
                pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
                val notification = NotificationCompat.Builder(this, "Channel_id1")
                    .setContentTitle("Example")
                    .setContentText("App is running")
                    .setContentIntent(pendingIntent).build()
                startForeground(1, notification)
            }
            "stop" -> {
                stopVPN()
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

    private fun stopVPN() {
        stopForeground(true)
        stopSelf()
        cleanup()
    }

    private fun cleanup() {
        context.cancel()
        deviceToNetworkTCPQueue = null
        deviceToNetworkUDPQueue = null
        networkToDeviceQueue = null
        closeResources(vpnInterface!!)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        cleanup()
    }

    private class VPNRunnable(
        private val vpnFileDescriptor: FileDescriptor,
        private val deviceToNetworkUDPQueue: BlockingQueue<Packet>,
        private val deviceToNetworkTCPQueue: BlockingQueue<Packet>,
        private val networkToDeviceQueue: BlockingQueue<ByteBuffer>,
    ) {
        class WriteVpnThread(
            var vpnOutput: FileChannel,
            private val networkToDeviceQueue: BlockingQueue<ByteBuffer>
        ) {
            suspend fun run() {
                while (coroutineContext.isActive) {
                    try {
                        val bufferFromNetwork = networkToDeviceQueue.take()
                        bufferFromNetwork.flip()
                        while (bufferFromNetwork.hasRemaining()) {
                            val w = vpnOutput.write(bufferFromNetwork)
                            if (w > 0) {
                                downByte.addAndGet(w.toLong())
                            }
                            if (Config.logRW) {
                                Log.d(TAG, "vpn write $w")
                            }
                        }
                    } catch (e: Exception) {
                        Log.i(TAG, "WriteVpnThread fail", e)
                        coroutineContext.cancel()
                    }
                }
            }
        }

        suspend fun run() {
            Log.i(TAG, "VPNRunnable Started")
            val vpnInput = FileInputStream(
                vpnFileDescriptor
            ).channel
            val vpnOutput = FileOutputStream(
                vpnFileDescriptor
            ).channel
            CoroutineScope(coroutineContext).launch{
                WriteVpnThread(
                    vpnOutput,
                    networkToDeviceQueue
                ).run()
            }
            try {
                var bufferToNetwork: ByteBuffer?
                while (coroutineContext.isActive) {
                    bufferToNetwork = ByteBufferPool.acquire()
                    val readBytes = vpnInput.read(bufferToNetwork)
                    upByte.addAndGet(readBytes.toLong())
                    if (readBytes > 0) {
                        bufferToNetwork.flip()
                        val packet = Packet(bufferToNetwork)
                        if (packet.isUDP()) {
                            if (Config.logRW) {
                                Log.i(TAG, "read udp$readBytes")
                            }
                            deviceToNetworkUDPQueue.offer(packet)
                        } else if (packet.isTCP()) {
                            if (Config.logRW) {
                                Log.i(TAG, "read tcp $readBytes")
                            }
                            deviceToNetworkTCPQueue.offer(packet)
                        } else {
                            Log.w(
                                TAG,
                                String.format(
                                    "Unknown packet protocol type %d",
                                    packet.ip4Header.protocolNum
                                )
                            )
                        }
                    } else {
                        try {
                            delay(10)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: IOException) {
                Log.w(TAG, e.toString(), e)
                coroutineContext.cancel()
            } finally {
                closeResources(vpnInput, vpnOutput)
            }
        }
    }

    companion object {
        private val TAG = LocalVPNService::class.java.simpleName
        private val VPN_ADDRESS = "10.0.0.2" // Only IPv4 support for now
        private val VPN_ROUTE = "0.0.0.0" // Intercept everything

        // TODO: Move this to a "utils" class for reuse
        private fun closeResources(vararg resources: Closeable) {
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