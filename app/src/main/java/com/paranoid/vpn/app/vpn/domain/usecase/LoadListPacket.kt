package com.paranoid.vpn.app.vpn.domain.usecase

import com.paranoid.vpn.app.vpn.domain.EntityPacket
import com.paranoid.vpn.app.vpn.domain.port.VPNPacketCash
import javax.inject.Inject

class LoadListPacket (private val vpnPacketCash: VPNPacketCash) {
    fun execute(): List<EntityPacket> {
        return vpnPacketCash.listPackets()
    }
}