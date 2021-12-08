package com.paranoid.vpn.app.vpn.domain.usecase

import com.paranoid.vpn.app.vpn.domain.EntityPacket
import com.paranoid.vpn.app.vpn.domain.port.VPNPacketCash
import javax.inject.Inject

class AddPacketUseCase(val vpnPacketCash: VPNPacketCash) {
    fun execute(packet: EntityPacket) {
        vpnPacketCash.addPacket(packet)
    }
}