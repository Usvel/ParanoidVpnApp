package com.paranoid.vpn.app.vpn.core.protocol.tcpip;

public enum TCPStatus {
    SYN_SENT,
    SYN_RECEIVED,
    ESTABLISHED,
    CLOSE_WAIT,
    LAST_ACK,
    //new
    CLOSED,
}
