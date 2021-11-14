package com.paranoid.vpn.app.vpn.core.handlers.udp

import android.util.Log
import com.paranoid.vpn.app.vpn.core.handlers.SuspendableRunnable
import com.paranoid.vpn.app.vpn.core.protocol.tcpip.IpUtil
import com.paranoid.vpn.app.vpn.core.util.ByteBufferPool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.util.concurrent.BlockingQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.coroutineContext

class UdpReadWorker(
    private var selector: Selector?,
    private var networkToDeviceQueue: BlockingQueue<ByteBuffer>,
    private var tunnelQueue: BlockingQueue<UdpTunnel>
): SuspendableRunnable {

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
        byteBuffer.position(BioUdpHandler.HEADER_SIZE)
        if (data != null) {
            if (byteBuffer.remaining() < data.size) {
                System.currentTimeMillis()
            }
            byteBuffer.put(data)
        }
        packet.updateUDPBuffer(byteBuffer, dataLen)
        byteBuffer.position(BioUdpHandler.HEADER_SIZE + dataLen)
        networkToDeviceQueue.offer(byteBuffer)
    }

    override suspend fun run() {
        try {
            while (coroutineContext.isActive) {
                val readyChannels = withContext(Dispatchers.IO) {
                    selector!!.select()
                }
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
                            Log.d(BioUdpHandler.TAG, "register fail", e)
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
                            withContext(Dispatchers.IO) {
                                inputChannel.read(receiveBuffer)
                            }
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
        } finally {
            Log.d(TAG, "BioUdpHandler quit")
            coroutineContext.cancel()
        }
    }

    companion object {
        private val ipId = AtomicInteger()
        val TAG: String = UdpReadWorker::class.java.simpleName
    }
}