package com.paranoid.vpn.app.vpn.core.handlers.udp

import android.net.VpnService
import android.util.Log
import com.paranoid.vpn.app.vpn.core.config.Config
import com.paranoid.vpn.app.vpn.core.handlers.SuspendableRunnable
import com.paranoid.vpn.app.vpn.core.protocol.tcpip.Packet
import kotlinx.coroutines.*
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.DatagramChannel
import java.nio.channels.Selector
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.coroutineContext

class UdpWriteWorker(
    private var selector: Selector?,
    private var tunnelQueue: BlockingQueue<UdpTunnel>,
    private var queue: BlockingQueue<Packet>,
    private var udpSockets: ConcurrentHashMap<String?, DatagramChannel?>,
    private var vpnService: VpnService,
): SuspendableRunnable {

    override suspend fun run() {
        try {
            while (coroutineContext.isActive) {
                val packet = runInterruptible {
                    queue.take()
                }
                val destinationAddress = packet.ip4Header.destinationAddress
                val header = packet.udpHeader

                //Log.d(TAG, String.format("get pack %d udp %d ", packet.packId, header.length));
                val destinationPort = header.destinationPort
                val sourcePort = header.sourcePort
                val ipAndPort =
                    destinationAddress.hostAddress + ":" + destinationPort + ":" + sourcePort
                if (!udpSockets.containsKey(ipAndPort)) {
                    val outputChannel = runInterruptible {
                        DatagramChannel.open()
                    }
                    vpnService.protect(outputChannel.socket())
                    try {
                        runInterruptible {
                            outputChannel.socket().bind(null)
                            outputChannel.connect(
                                InetSocketAddress(
                                    destinationAddress,
                                    destinationPort
                                )
                            )
                            outputChannel.configureBlocking(false)
                        }
                    } catch (e: IOException) {
                        Log.e(BioUdpHandler.TAG, "udp write error", e)
                        runInterruptible {
                            outputChannel?.close()
                        }
                        return
                    }

                    val tunnel = UdpTunnel()
                    tunnel.local =
                        InetSocketAddress(packet.ip4Header.sourceAddress, header.sourcePort)
                    tunnel.remote = InetSocketAddress(
                        packet.ip4Header.destinationAddress,
                        header.destinationPort
                    )
                    tunnel.channel = outputChannel
                    tunnelQueue.offer(tunnel)
                    selector?.wakeup()
                    udpSockets[ipAndPort] = outputChannel
                }
                val outputChannel = udpSockets[ipAndPort]
                val buffer = packet.backingBuffer
                try {
                    while (packet.backingBuffer.hasRemaining()) {
                        val w = runInterruptible {
                            outputChannel!!.write(buffer)
                        }
                        if (Config.logRW) {
                            Log.d(
                                BioUdpHandler.TAG,
                                String.format(
                                    "write udp pack %d len %d %s ",
                                    packet.packId,
                                    w,
                                    ipAndPort
                                )
                            )
                        }
                    }
                } catch (e: IOException) {
                    Log.v(TAG, "udp write error", e)
                    runInterruptible {
                        outputChannel?.close()
                    }
                    udpSockets.remove(ipAndPort)
                }
            }
        } catch (e: Exception) {
            Log.v(TAG, "error", e)
        } finally {
            Log.d(TAG, "UdpWriteWorker quit")
        }

    }

    companion object {
        val TAG: String = UdpWriteWorker::class.java.simpleName
    }
}