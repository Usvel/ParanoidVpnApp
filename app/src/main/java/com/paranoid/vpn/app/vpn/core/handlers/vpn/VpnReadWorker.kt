package com.paranoid.vpn.app.vpn.core.handlers.vpn

import android.util.Log
import com.paranoid.vpn.app.common.ad_block_configuration.domain.model.AdBlockIpItem
import com.paranoid.vpn.app.common.ad_block_configuration.domain.repository.IpRepository
import com.paranoid.vpn.app.vpn.core.LocalVPNService2
import com.paranoid.vpn.app.vpn.core.config.Config
import com.paranoid.vpn.app.vpn.core.handlers.SuspendableRunnable
import com.paranoid.vpn.app.vpn.core.protocol.tcpip.Packet
import com.paranoid.vpn.app.vpn.core.util.ByteBufferPool
import com.paranoid.vpn.app.vpn.ui.vpn_pager.vpn.VPNObjectFragment
import kotlinx.coroutines.*
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.Inet4Address
import java.nio.ByteBuffer
import java.util.concurrent.BlockingQueue
import kotlin.coroutines.coroutineContext

class VpnReadWorker(
    private val vpnFileDescriptor: FileDescriptor,
    private val deviceToNetworkUDPQueue: BlockingQueue<Packet>,
    private val deviceToNetworkTCPQueue: BlockingQueue<Packet>,
    private val networkToDeviceQueue: BlockingQueue<ByteBuffer>,
) : SuspendableRunnable {

    private var advBlockList: List<AdBlockIpItem>? = null

    override suspend fun run() {
        Log.i(LocalVPNService2.TAG, "VPNRunnable Started")

        withContext(Dispatchers.Main) {
            IpRepository().readAllData.observeForever {
                advBlockList = it
            }
        }
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
                val readBytes = runInterruptible { vpnInput.read(bufferToNetwork) }
                VPNObjectFragment.upByte.addAndGet(readBytes.toLong())
                if (readBytes > 0) {
                    bufferToNetwork.flip()
                    val packet = runInterruptible { Packet(bufferToNetwork) }
                    if (advBlockList
                            ?.filter {
                                Inet4Address.getByName(it.Ip) == packet.ip4Header.destinationAddress ||
                                        Inet4Address.getByName(it.Ip) == packet.ip4Header.sourceAddress
                            }
                            .isNullOrEmpty()
                    ) {
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
                    } else
                        Log.d(
                            LocalVPNService2.TAG,
                            "blocked ip: ${packet.ip4Header.destinationAddress}"
                        )

                }
            }
        } catch (e: IOException) {
            Log.w(LocalVPNService2.TAG, e.toString(), e)
        } finally {
            LocalVPNService2.closeResources(vpnInput, vpnOutput)
            job.cancelAndJoin()
        }
    }
}