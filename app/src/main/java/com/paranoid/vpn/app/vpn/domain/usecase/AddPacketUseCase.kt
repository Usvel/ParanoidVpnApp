package com.paranoid.vpn.app.vpn.domain.usecase

import com.paranoid.vpn.app.vpn.domain.entity.EntityPacket
import com.paranoid.vpn.app.vpn.domain.port.VPNPacketCash

class AddPacketUseCase(val vpnPacketCash: VPNPacketCash) {
    fun execute(packet: EntityPacket) {
        vpnPacketCash.addPacket(packet)
    }
}