package com.example.paranoid.ui.vpn.basic_client.handlers.vpn

import android.util.Log
import com.example.paranoid.ui.vpn.VPNFragment
import com.example.paranoid.ui.vpn.basic_client.LocalVPNService2
import com.example.paranoid.ui.vpn.basic_client.config.Config
import com.example.paranoid.ui.vpn.basic_client.handlers.SuspendableRunnable
import com.example.paranoid.ui.vpn.basic_client.protocol.tcpip.Packet
import com.example.paranoid.ui.vpn.basic_client.util.ByteBufferPool
import kotlinx.coroutines.*
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.BlockingQueue
import kotlin.coroutines.coroutineContext

class VpnReadWorker(
    private val vpnFileDescriptor: FileDescriptor,
    private val deviceToNetworkUDPQueue: BlockingQueue<Packet>,
    private val deviceToNetworkTCPQueue: BlockingQueue<Packet>,
    private val networkToDeviceQueue: BlockingQueue<ByteBuffer>,
): SuspendableRunnable {

    override suspend fun run() {
        Log.i(LocalVPNService2.TAG, "VPNRunnable Started")
        val vpnInput = FileInputStream(
            vpnFileDescriptor
        ).channel
        val vpnOutput = FileOutputStream(
            vpnFileDescriptor
        ).channel
        val job = CoroutineScope(coroutineContext).launch {
            VpnWriteWorker(
                vpnOutput,
                networkToDeviceQueue
            ).run()
        }
        job.start()
        try {
            var bufferToNetwork: ByteBuffer?
            while (coroutineContext.isActive) {
                bufferToNetwork = ByteBufferPool.acquire()
                val readBytes = vpnInput.read(bufferToNetwork)
                VPNFragment.upByte.addAndGet(readBytes.toLong())
                if (readBytes > 0) {
                    bufferToNetwork.flip()
                    val packet = Packet(bufferToNetwork)
                    when {
                        packet.isUDP() -> {
                            if (Config.logRW) {
                                Log.i(LocalVPNService2.TAG, "read udp$readBytes")
                            }
                            deviceToNetworkUDPQueue.offer(packet)
                        }
                        packet.isTCP() -> {
                            if (Config.logRW) {
                                Log.i(LocalVPNService2.TAG, "read tcp $readBytes")
                            }
                            deviceToNetworkTCPQueue.offer(packet)
                        }
                        else -> {
                            Log.w(
                                LocalVPNService2.TAG,
                                String.format(
                                    "Unknown packet protocol type %d",
                                    packet.ip4Header.protocolNum
                                )
                            )
                        }
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
            Log.w(LocalVPNService2.TAG, e.toString(), e)
            coroutineContext.cancel()
        } finally {
            LocalVPNService2.closeResources(vpnInput, vpnOutput)
            job.cancelAndJoin()
        }
    }
}