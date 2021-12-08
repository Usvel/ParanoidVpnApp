package com.paranoid.vpn.app.vpn.domain

import com.paranoid.vpn.app.vpn.core.protocol.tcpip.Packet.IP4Header.TransportProtocol
import java.net.InetAddress

data class EntityPacket(
    val ip4: IP4,
    val udp: UDP?,
    val tcp: TCP?
)

data class IP4(
    var version: Byte = 0,
    var IHL: Byte = 0,
    var headerLength: Int = 0,
    var typeOfService: Short = 0,
    var totalLength: Int = 0,
    var identificationAndFlagsAndFragmentOffset: Int = 0,
    var TTL: Short = 0,
    var protocolNum: Short = 0,
    var headerChecksum: Int = 0,
    var sourceAddress: InetAddress? = null,
    var destinationAddress: InetAddress? = null
)

data class UDP(
    var sourcePort: Int = 0,
    var destinationPort: Int = 0,
    var length: Int = 0,
    var checksum: Int = 0
)

data class TCP(
    var sourcePort: Int = 0,
    var destinationPort: Int = 0,
    var sequenceNumber: Long = 0,
    var acknowledgementNumber: Long = 0,
    var dataOffsetAndReserved: Byte = 0,
    var headerLength: Int = 0,
    var flags: Byte = 0,
    var window: Int = 0,
    var checksum: Int = 0,
    var urgentPointer: Int = 0
)
