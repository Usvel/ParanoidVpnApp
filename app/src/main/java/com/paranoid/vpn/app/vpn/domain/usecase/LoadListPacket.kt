package com.paranoid.vpn.app.vpn.domain.usecase

import com.paranoid.vpn.app.vpn.domain.entity.EntityPacket
import com.paranoid.vpn.app.vpn.domain.port.VPNPacketCash

class LoadListPacket (private val vpnPacketCash: VPNPacketCash) {
    fun execute(): List<EntityPacket> {
        return vpnPacketCash.listPackets()
    }
}