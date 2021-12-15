package com.paranoid.vpn.app.vpn.domain.port

import com.paranoid.vpn.app.vpn.domain.entity.EntityPacket

interface VPNPacketCash {
    fun addPacket(packet: EntityPacket)
    fun listPackets(): List<EntityPacket>
}