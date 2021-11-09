package com.example.paranoid.ui.vpn.basic_client.handlers.udp

import java.net.InetSocketAddress
import java.nio.channels.DatagramChannel

data class UdpTunnel(
    var local: InetSocketAddress? = null,
    var remote: InetSocketAddress? = null,
    var channel: DatagramChannel? = null
)
