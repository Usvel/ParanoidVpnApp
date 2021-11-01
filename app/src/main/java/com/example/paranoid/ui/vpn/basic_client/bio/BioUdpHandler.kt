package com.example.paranoid.ui.vpn.basic_client.bio
import android.net.VpnService
import android.util.Log
import com.example.paranoid.ui.vpn.basic_client.config.Config
import com.example.paranoid.ui.vpn.basic_client.protocol.tcpip.IpUtil
import com.example.paranoid.ui.vpn.basic_client.protocol.tcpip.Packet
import com.example.paranoid.ui.vpn.basic_client.util.ByteBufferPool
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess

class BioUdpHandler(
    private var queue: BlockingQueue<Packet>,
    private var networkToDeviceQueue: BlockingQueue<ByteBuffer>,
    private var vpnService: VpnService
) {
    private var selector: Selector? = null

    private class UdpDownWorker(
        var selector: Selector?,
        var networkToDeviceQueue: BlockingQueue<ByteBuffer>,
        var tunnelQueue: BlockingQueue<UdpTunnel>
    ) {
        @Throws(IOException::class)
        private fun sendUdpPack(
            tunnel: UdpTunnel,
            source: InetSocketAddress,
            data: ByteArray?
        ) {
            var dataLen = 0
            if (data != null) {
                dataLen = data.size
            }
            val packet = IpUtil.buildUdpPacket(tunnel.remote, tunnel.local, ipId.addAndGet(1))
            val byteBuffer = ByteBufferPool.acquire()
            //
            byteBuffer.position(HEADER_SIZE)
            if (data != null) {
                if (byteBuffer.remaining() < data.size) {
                    System.currentTimeMillis()
                }
                byteBuffer.put(data)
            }
            packet.updateUDPBuffer(byteBuffer, dataLen)
            byteBuffer.position(HEADER_SIZE + dataLen)
            networkToDeviceQueue.offer(byteBuffer)
        }

        suspend fun run() {
            try {
                while (true) {
                    val readyChannels = selector!!.select()
                    while (true) {
                        val tunnel = tunnelQueue.poll()
                        if (tunnel == null) {
                            break
                        } else {
                            try {
                                val key = tunnel.channel!!.register(
                                    selector, SelectionKey.OP_READ, tunnel
                                )
                                key.interestOps(SelectionKey.OP_READ)
                            } catch (e: IOException) {
                                Log.d(TAG, "register fail", e)
                            }
                        }
                    }
                    if (readyChannels == 0) {
                        selector!!.selectedKeys().clear()
                        continue
                    }
                    val keys = selector!!.selectedKeys()
                    val keyIterator = keys.iterator()
                    while (keyIterator.hasNext()) {
                        val key = keyIterator.next()
                        keyIterator.remove()
                        if (key.isValid && key.isReadable) {
                            try {
                                val inputChannel = key.channel() as DatagramChannel
                                val receiveBuffer = ByteBufferPool.acquire()
                                receiveBuffer.flip()
                                val data = ByteArray(receiveBuffer.remaining())
                                receiveBuffer[data]
                                sendUdpPack(
                                    key.attachment() as UdpTunnel,
                                    inputChannel.localAddress as InetSocketAddress,
                                    data
                                )
                            } catch (e: IOException) {
                                Log.e(TAG, "error", e)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "error", e)
                exitProcess(0)
            } finally {
                Log.d(TAG, "BioUdpHandler quit")
            }
        }

        companion object {
            private val ipId = AtomicInteger()
        }
    }

    private var udpSockets: MutableMap<String?, DatagramChannel?> = HashMap<String?, DatagramChannel?>()

    private class UdpTunnel {
        var local: InetSocketAddress? = null
        var remote: InetSocketAddress? = null
        var channel: DatagramChannel? = null
    }

    @DelicateCoroutinesApi
    suspend fun run() {
        try {
            val tunnelQueue: BlockingQueue<UdpTunnel> = ArrayBlockingQueue(100)
            selector = Selector.open()
            GlobalScope.launch(Dispatchers.IO) {
                runCatching {
                    UdpDownWorker(
                        selector,
                        networkToDeviceQueue,
                        tunnelQueue
                    ).run()
                }
            }
            while (true) {
                val packet = queue.take()
                val destinationAddress = packet.ip4Header.destinationAddress
                val header = packet.udpHeader

                //Log.d(TAG, String.format("get pack %d udp %d ", packet.packId, header.length));
                val destinationPort = header.destinationPort
                val sourcePort = header.sourcePort
                val ipAndPort =
                    destinationAddress.hostAddress + ":" + destinationPort + ":" + sourcePort
                if (!udpSockets.containsKey(ipAndPort)) {
                    val outputChannel = DatagramChannel.open()
                    vpnService.protect(outputChannel.socket())
                    outputChannel.socket().bind(null)
                    outputChannel.connect(InetSocketAddress(destinationAddress, destinationPort))
                    outputChannel.configureBlocking(false)
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
                        val w = outputChannel!!.write(buffer)
                        if (Config.logRW) {
                            Log.d(
                                TAG,
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
                    Log.e(TAG, "udp write error", e)
                    outputChannel!!.close()
                    udpSockets.remove(ipAndPort)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "error", e)
            exitProcess(0)
        }
    }

    companion object {
        private const val HEADER_SIZE = Packet.IP4_HEADER_SIZE + Packet.UDP_HEADER_SIZE
        private val TAG = BioUdpHandler::class.java.simpleName
    }
}
