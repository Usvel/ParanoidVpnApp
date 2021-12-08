package com.paranoid.vpn.app.vpn.remote

import com.paranoid.vpn.app.vpn.domain.EntityPacket
import com.paranoid.vpn.app.vpn.domain.port.VPNPacketCash
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

object VPNPacketMemoryCache : VPNPacketCash {
    private val packets = arrayListOf<EntityPacket>()

    override fun addPacket(packet: EntityPacket) {
        packets.add(packet)
    }

    override fun listPackets(): List<EntityPacket> {
        return packets
    }
}
