package com.example.paranoid.ui.vpn.basic_client.handlers.udp

import android.net.VpnService
import android.util.Log
import com.example.paranoid.ui.vpn.basic_client.handlers.SuspendableRunnable
import com.example.paranoid.ui.vpn.basic_client.protocol.tcpip.Packet
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
        var readJob: Job? = null
        var writeJob: Job? = null
        try {
            val tunnelQueue: BlockingQueue<UdpTunnel> = ArrayBlockingQueue(100)
            selector = withContext(Dispatchers.IO) {
                Selector.open()
            }
            readJob = CoroutineScope(context).launch {
                UdpReadWorker(
                    selector,
                    networkToDeviceQueue,
                    tunnelQueue
                ).run()
            }
            readJob.start()

            writeJob = CoroutineScope(context).launch {
                UdpWriteWorker(
                    selector,
                    tunnelQueue,
                    queue,
                    udpSockets,
                    vpnService
                ).run()
            }
            writeJob.start()

            while (coroutineContext.isActive) {
                continue
            }

        } catch (e: Exception) {
            Log.v(TAG, "error")
            e.printStackTrace()
        }
        finally {
            Log.v(TAG, "closing resources in BioUdpHandler")
            closeResources()
            readJob?.cancelAndJoin()
            writeJob?.cancelAndJoin()
            coroutineContext.cancel()
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
