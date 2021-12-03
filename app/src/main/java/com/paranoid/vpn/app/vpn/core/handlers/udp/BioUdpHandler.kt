package com.paranoid.vpn.app.vpn.core.handlers.udp

import android.net.VpnService
import android.util.Log
import com.paranoid.vpn.app.vpn.core.handlers.SuspendableRunnable
import com.paranoid.vpn.app.vpn.core.protocol.tcpip.Packet
import kotlinx.coroutines.*
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.channels.Selector
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class BioUdpHandler(
    private var queue: BlockingQueue<Packet>,
    private var networkToDeviceQueue: BlockingQueue<ByteBuffer>,
    private var vpnService: VpnService,
    private val context: CoroutineContext
): SuspendableRunnable {
    private var selector: Selector? = null
    private var udpSockets = ConcurrentHashMap<String?, DatagramChannel?>()

    override suspend fun run() {
        var readJob: Deferred<Unit>? = null
        var writeJob: Deferred<Unit>? = null
        try {
            val tunnelQueue: BlockingQueue<UdpTunnel> = ArrayBlockingQueue(100)
            selector = runInterruptible {
                Selector.open()
            }
            readJob = CoroutineScope(context).async {
                UdpReadWorker(
                    selector,
                    networkToDeviceQueue,
                    tunnelQueue
                ).run()
            }

            writeJob = CoroutineScope(context).async {
                UdpWriteWorker(
                    selector,
                    tunnelQueue,
                    queue,
                    udpSockets,
                    vpnService
                ).run()
            }

            readJob.await()
            writeJob.await()

        } catch (e: Exception) {
            Log.v(TAG, "error")
            e.printStackTrace()
        }
        finally {
            Log.v(TAG, "closing resources in BioUdpHandler")
            closeResources()
            readJob?.cancelAndJoin()
            writeJob?.cancelAndJoin()
        }
    }

    private fun closeResources() {
        udpSockets.forEach {
            it.value?.close()
        }
        selector?.close()
    }

    companion object {
        const val HEADER_SIZE = Packet.IP4_HEADER_SIZE + Packet.UDP_HEADER_SIZE
        val TAG = BioUdpHandler::class.java.simpleName
    }
}
