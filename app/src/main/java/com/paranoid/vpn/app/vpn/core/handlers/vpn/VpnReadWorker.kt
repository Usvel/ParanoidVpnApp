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
import com.paranoid.vpn.app.vpn.domain.EntityPacket
import com.paranoid.vpn.app.vpn.domain.IP4
import com.paranoid.vpn.app.vpn.domain.TCP
import com.paranoid.vpn.app.vpn.domain.UDP
import com.paranoid.vpn.app.vpn.domain.usecase.AddPacketUseCase
import com.paranoid.vpn.app.vpn.ui.vpn_pager.vpn.VPNObjectFragment.Companion.upByte
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
    private val addPacketUseCase: AddPacketUseCase
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
                    val ip4 = IP4(
                        version = packet.ip4Header.version,
                        IHL = packet.ip4Header.IHL,
                        headerLength = packet.ip4Header.headerLength,
                        typeOfService = packet.ip4Header.typeOfService,
                        totalLength = packet.ip4Header.totalLength,
                        identificationAndFlagsAndFragmentOffset = packet.ip4Header.identificationAndFlagsAndFragmentOffset,
                        TTL = packet.ip4Header.TTL,
                        protocolNum = packet.ip4Header.protocolNum,
                        headerChecksum = packet.ip4Header.headerChecksum,
                        sourceAddress = packet.ip4Header.sourceAddress,
                        destinationAddress = packet.ip4Header.destinationAddress
                    )
                    when (true) {
                        packet.isUDP() -> {
                            if (Config.logRW) {
                                Log.i(LocalVPNService2.TAG, "read udp$readBytes")
                            }
                            deviceToNetworkUDPQueue.offer(packet)
                            Log.d("Packet - UDP", packet.toString())
                            val udp = UDP(
                                sourcePort = packet.udpHeader.sourcePort,
                                destinationPort = packet.udpHeader.destinationPort,
                                length = packet.udpHeader.length,
                                checksum = packet.udpHeader.checksum
                            )
                            addPacketUseCase.execute(
                                EntityPacket(
                                    ip4 = ip4,
                                    udp = udp,
                                    tcp = null
                                )
                            )
                        }
                        packet.isTCP() -> {
                            if (Config.logRW) {
                                Log.i(LocalVPNService2.TAG, "read tcp $readBytes")
                            }
                            deviceToNetworkTCPQueue.offer(packet)
                            Log.d("Packet - TCP", packet.toString())
                            val tcp = TCP(
                                sourcePort = packet.tcpHeader.sourcePort,
                                destinationPort = packet.tcpHeader.destinationPort,
                                sequenceNumber = packet.tcpHeader.sequenceNumber,
                                acknowledgementNumber = packet.tcpHeader.acknowledgementNumber,
                                dataOffsetAndReserved = packet.tcpHeader.dataOffsetAndReserved,
                                headerLength = packet.tcpHeader.headerLength,
                                flags = packet.tcpHeader.flags,
                                window = packet.tcpHeader.window,
                                checksum = packet.tcpHeader.checksum,
                                urgentPointer = packet.tcpHeader.urgentPointer
                            )
                            addPacketUseCase.execute(
                                EntityPacket(
                                    ip4 = ip4,
                                    udp = null,
                                    tcp = tcp
                                )
                            )
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