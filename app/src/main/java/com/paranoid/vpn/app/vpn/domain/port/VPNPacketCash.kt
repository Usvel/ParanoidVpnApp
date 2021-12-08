package com.paranoid.vpn.app.vpn.domain.port

import com.paranoid.vpn.app.vpn.domain.EntityPacket
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface VPNPacketCash {
    fun addPacket(packet: EntityPacket)
    fun listPackets(): List<EntityPacket>
}