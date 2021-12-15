package com.paranoid.vpn.app.vpn.remote

import com.paranoid.vpn.app.vpn.domain.entity.EntityPacket
import com.paranoid.vpn.app.vpn.domain.port.VPNPacketCash

object VPNPacketMemoryCache : VPNPacketCash {
    private val packets = arrayListOf<EntityPacket>()

    override fun addPacket(packet: EntityPacket) {
        packets.add(packet)
    }

    override fun listPackets(): List<EntityPacket> {
        return packets
    }
}
