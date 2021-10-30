package com.example.paranoid.ui.vpn.basic_client.bio

import android.net.VpnService
import android.util.Log
import com.example.paranoid.ui.vpn.basic_client.bio.BioTcpHandler.*
import com.example.paranoid.ui.vpn.basic_client.config.Config
import com.example.paranoid.ui.vpn.basic_client.protocol.tcpip.IpUtil
import com.example.paranoid.ui.vpn.basic_client.protocol.tcpip.Packet
import com.example.paranoid.ui.vpn.basic_client.protocol.tcpip.Packet.TCPHeader
import com.example.paranoid.ui.vpn.basic_client.protocol.tcpip.TCBStatus
import com.example.paranoid.ui.vpn.basic_client.util.ByteBufferPool
import com.example.paranoid.ui.vpn.basic_client.util.ProxyException
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ClosedChannelException
import java.nio.channels.SocketChannel
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger


class BioTcpHandler(
    private var queue: BlockingQueue<Packet>,
    private var networkToDeviceQueue: BlockingQueue<ByteBuffer>,
    private val vpnService: VpnService
) :
    Runnable {
    private var tunnels: ConcurrentHashMap<String?, TcpTunnel?> = ConcurrentHashMap<String?, TcpTunnel?>()

    class TcpTunnel {
        val tunnelId = tunnelIds.addAndGet(1)
        var mySequenceNum: Long = 0
        var theirSequenceNum: Long = 0
        var myAcknowledgementNum: Long = 0
        var theirAcknowledgementNum: Long = 0
        var tcbStatus = TCBStatus.SYN_SENT
        var tunnelInputQueue: BlockingQueue<Packet> = ArrayBlockingQueue(1024)
        var sourceAddress: InetSocketAddress? = null
        var destinationAddress: InetSocketAddress? = null
        var destSocket: SocketChannel? = null
        var vpnService: VpnService? = null
        var networkToDeviceQueue: BlockingQueue<ByteBuffer>? = null
        var packId = 1
        var upActive = true
        var downActive = true
        var tunnelKey: String? = null
        var tunnelCloseMsgQueue: BlockingQueue<String?>? = null

        companion object {
            var tunnelIds = AtomicInteger(0)
        }
    }

    private class UpStreamWorker(var tunnel: TcpTunnel) : Runnable {
        private fun startDownStream() {
            val t = Thread(DownStreamWorker(tunnel))
            t.start()
        }

        private fun connectRemote() {
            try {
                //connect
                val remote = SocketChannel.open()
                tunnel.vpnService!!.protect(remote.socket())
                val address = tunnel.destinationAddress
                val ts = System.currentTimeMillis()
                remote.socket().connect(address, 5000)
                val te = System.currentTimeMillis()
                Log.i(
                    TAG,
                    String.format(
                        "connectRemote %d cost %d  remote %s",
                        tunnel.tunnelId,
                        te - ts,
                        tunnel.destinationAddress.toString()
                    )
                )
                tunnel.destSocket = remote
                startDownStream()
            } catch (e: Exception) {
                Log.e(TAG, e.message, e)
                throw ProxyException("connectRemote fail" + tunnel.destinationAddress.toString())
            }
        }

        var synCount = 0
        private fun handleSyn(packet: Packet?) {
            if (tunnel.tcbStatus == TCBStatus.SYN_SENT) {
                tunnel.tcbStatus = TCBStatus.SYN_RECEIVED
            }
            Log.i(TAG, String.format("handleSyn  %d %d", tunnel.tunnelId, packet!!.packId))
            val tcpHeader = packet.tcpHeader
            if (synCount == 0) {
                tunnel.mySequenceNum = 1
                tunnel.theirSequenceNum = tcpHeader.sequenceNumber
                tunnel.myAcknowledgementNum = tcpHeader.sequenceNumber + 1
                tunnel.theirAcknowledgementNum = tcpHeader.acknowledgementNumber
                sendTcpPack(
                    tunnel, (TCPHeader.SYN or TCPHeader.ACK).toByte(), null
                )
            } else {
                tunnel.myAcknowledgementNum = tcpHeader.sequenceNumber + 1
            }
            synCount += 1
        }

        @Throws(IOException::class)
        private fun writeToRemote(buffer: ByteBuffer) {
            if (tunnel.upActive) {
                val payloadSize = buffer.remaining()
                val write = tunnel.destSocket!!.write(buffer)
            }
        }

        @Throws(IOException::class)
        private fun handleAck(packet: Packet?) {
            if (tunnel.tcbStatus == TCBStatus.SYN_RECEIVED) {
                tunnel.tcbStatus = TCBStatus.ESTABLISHED
            }
            if (Config.logAck) {
                Log.d(TAG, String.format("handleAck %d ", packet!!.packId))
            }
            val tcpHeader = packet!!.tcpHeader
            val payloadSize = packet.backingBuffer.remaining()
            if (payloadSize == 0) {
                return
            }
            val newAck = tcpHeader.sequenceNumber + payloadSize
            if (newAck <= tunnel.myAcknowledgementNum) {
                if (Config.logAck) {
                    Log.d(
                        TAG,
                        String.format(
                            "handleAck duplicate ack",
                            tunnel.myAcknowledgementNum,
                            newAck
                        )
                    )
                }
                return
            }
            tunnel.myAcknowledgementNum = tcpHeader.sequenceNumber
            tunnel.theirAcknowledgementNum = tcpHeader.acknowledgementNumber
            tunnel.myAcknowledgementNum += payloadSize.toLong()
            writeToRemote(packet.backingBuffer)
            sendTcpPack(
                tunnel,
                TCPHeader.ACK.toByte(), null
            )
            System.currentTimeMillis()
        }

        private fun handleFin(packet: Packet?) {
            Log.i(TAG, String.format("handleFin %d", tunnel.tunnelId))
            tunnel.myAcknowledgementNum = packet!!.tcpHeader.sequenceNumber + 1
            tunnel.theirAcknowledgementNum = packet.tcpHeader.acknowledgementNumber
            sendTcpPack(
                tunnel,
                TCPHeader.ACK.toByte(), null
            )
            //closeTunnel(tunnel);
            //closeDownStream();
            closeUpStream(
                tunnel
            )
            tunnel.tcbStatus = TCBStatus.CLOSE_WAIT
        }

        private fun handleRst(packet: Packet?) {
            Log.i(TAG, String.format("handleRst %d", tunnel.tunnelId))
            try {
                synchronized(tunnel) {
                    if (tunnel.destSocket != null) {
                        tunnel.destSocket!!.close()
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "close error", e)
            }
            synchronized(tunnel) {
                tunnel.upActive = false
                tunnel.downActive = false
                tunnel.tcbStatus = TCBStatus.CLOSE_WAIT
            }
        }

        private fun loop() {
            loop@ while (true) {
                var packet: Packet? = null
                try {
                    packet = tunnel.tunnelInputQueue.take()

                    //Log.i(TAG, "lastIdentification " + tunnel.lastIdentification);
                    synchronized(tunnel) {
                        var end = false
                        val tcpHeader = packet.tcpHeader
                        if (tcpHeader.isSYN) {
                            handleSyn(packet)
                            end = true
                        }
                        if (!end && tcpHeader.isRST) {
                            //
                            //Log.i(TAG, String.format("handleRst %d", tunnel.tunnelId));
                            //tunnel.destSocket.close();
                            handleRst(packet)
                            Log.i(TAG, String.format("UpStreamWorker quit"))
                            return
                            //return
                        }
                        if (!end && tcpHeader.isFIN) {
                            handleFin(packet)
                            end = true
                        }
                        if (!end && tcpHeader.isACK) {
                            handleAck(packet)
                        }
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                    return
                }
            }
            // Log.i(TAG, String.format("UpStreamWorker quit"))
        }

        override fun run() {
            try {
                connectRemote()
                loop()
            } catch (e: ProxyException) {
                //closeTotalTunnel();
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private class DownStreamWorker(var tunnel: TcpTunnel) : Runnable {
        override fun run() {
            val buffer = ByteBuffer.allocate(4 * 1024)
            var quitType = "rst"
            try {
                while (true) {
                    buffer.clear()
                    if (tunnel.destSocket == null) {
                        throw ProxyException("tunnel maybe closed")
                    }
                    val n = BioUtil.read(tunnel.destSocket, buffer)
                    synchronized(tunnel) {
                        if (n == -1) {
                            quitType = "fin"
                            //break
                            return
                        } else {
                            if (tunnel.tcbStatus != TCBStatus.CLOSE_WAIT) {
                                buffer.flip()
                                val data = ByteArray(buffer.remaining())
                                buffer[data]
                                sendTcpPack(
                                    tunnel, TCPHeader.ACK.toByte(), data
                                )
                            }
                        }
                    }
                }
            } catch (e: ClosedChannelException) {
                Log.w(TAG, String.format("channel closed %s", e.message))
                quitType = "rst"
            } catch (e: IOException) {
                Log.e(TAG, e.message, e)
                quitType = "rst"
            } catch (e: Exception) {
                quitType = "rst"
                Log.e(TAG, "DownStreamWorker fail", e)
            }
            //Log.i(TAG, String.format("DownStreamWorker quit %d", tunnel.tunnelId));
            synchronized(tunnel) {
                if (quitType == "fin") {
                    closeDownStream(
                        tunnel
                    )
                    //closeUpStream(tunnel);
                    //closeRst(tunnel);
                } else if (quitType == "rst") {
                    closeRst(
                        tunnel
                    )
                }
            }
        }
    }

    private fun initTunnel(packet: Packet): TcpTunnel {
        val tunnel = TcpTunnel()
        tunnel.sourceAddress =
            InetSocketAddress(packet.ip4Header.sourceAddress, packet.tcpHeader.sourcePort)
        tunnel.destinationAddress =
            InetSocketAddress(packet.ip4Header.destinationAddress, packet.tcpHeader.destinationPort)
        tunnel.vpnService = vpnService
        tunnel.networkToDeviceQueue = networkToDeviceQueue
        tunnel.tunnelCloseMsgQueue = tunnelCloseMsgQueue
        val t = Thread(UpStreamWorker(tunnel))
        t.start()
        return tunnel
    }

    private var tunnelCloseMsgQueue: BlockingQueue<String?> = ArrayBlockingQueue(1024)
    override fun run() {
        while (true) {
            try {
                val currentPacket = queue.take()
                val destinationAddress = currentPacket.ip4Header.destinationAddress
                val tcpHeader = currentPacket.tcpHeader
                //Log.d(TAG, String.format("get pack %d tcp " + tcpHeader.printSimple() + " ", currentPacket.packId));
                val destinationPort = tcpHeader.destinationPort
                val sourcePort = tcpHeader.sourcePort
                val ipAndPort = destinationAddress.hostAddress + ":" +
                        destinationPort + ":" + sourcePort
                //
                while (true) {
                    val s = tunnelCloseMsgQueue.poll()
                    if (s == null) {
                        break
                    } else {
                        tunnels.remove(ipAndPort)
                        Log.i(TAG, String.format("remove tunnel %s", ipAndPort))
                    }
                }
                //
                if (!tunnels.containsKey(ipAndPort)) {
                    val tcpTunnel = initTunnel(currentPacket)
                    tcpTunnel.tunnelKey = ipAndPort
                    tunnels[ipAndPort] = tcpTunnel
                }
                val tcpTunnel = tunnels[ipAndPort]
                //
                tcpTunnel!!.tunnelInputQueue.offer(currentPacket)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val HEADER_SIZE = Packet.IP4_HEADER_SIZE + Packet.TCP_HEADER_SIZE
        private val TAG = BioTcpHandler::class.java.simpleName
        private fun sendTcpPack(tunnel: TcpTunnel, flag: Byte, data: ByteArray?) {
//
//        if(true){
//            return;
//        }
            var dataLen = 0
            if (data != null) {
                dataLen = data.size
            }
            val packet = IpUtil.buildTcpPacket(
                tunnel.destinationAddress, tunnel.sourceAddress, flag,
                tunnel.myAcknowledgementNum, tunnel.mySequenceNum, tunnel.packId
            )
            tunnel.packId += 1
            val byteBuffer = ByteBufferPool.acquire()
            //
            byteBuffer.position(HEADER_SIZE)
            if (data != null) {
                if (byteBuffer.remaining() < data.size) {
                    System.currentTimeMillis()
                }
                byteBuffer.put(data)
            }
            packet.updateTCPBuffer(
                byteBuffer,
                flag,
                tunnel.mySequenceNum,
                tunnel.myAcknowledgementNum,
                dataLen
            )
            byteBuffer.position(HEADER_SIZE + dataLen)
            tunnel.networkToDeviceQueue!!.offer(byteBuffer)
            if (flag.toInt() and TCPHeader.SYN != 0) {
                tunnel.mySequenceNum += 1
            }
            if (flag.toInt() and TCPHeader.FIN != 0) {
                tunnel.mySequenceNum += 1
            }
            if (flag.toInt() and TCPHeader.ACK != 0) {
                tunnel.mySequenceNum += dataLen.toLong()
            }
        }

        private fun isClosedTunnel(tunnel: TcpTunnel): Boolean {
            return !tunnel.upActive && !tunnel.downActive
        }

        private fun closeDownStream(tunnel: TcpTunnel) {
            synchronized(tunnel) {
                Log.i(
                    TAG,
                    String.format("closeDownStream %d", tunnel.tunnelId)
                )
                try {
                    if (tunnel.destSocket != null && tunnel.destSocket!!.isOpen) {
                        tunnel.destSocket!!.shutdownInput()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                sendTcpPack(
                    tunnel,
                    (TCPHeader.FIN or TCPHeader.ACK).toByte(),
                    null
                )
                tunnel.downActive = false
                if (isClosedTunnel(
                        tunnel
                    )
                ) {
                    tunnel.tunnelCloseMsgQueue!!.add(tunnel.tunnelKey)
                }
            }
        }

        private fun closeUpStream(tunnel: TcpTunnel) {
            synchronized(tunnel) {
                Log.i(
                    TAG,
                    String.format("closeUpStream %d", tunnel.tunnelId)
                )
                try {
                    if (tunnel.destSocket != null && tunnel.destSocket!!.isOpen) {
                        tunnel.destSocket!!.shutdownOutput()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                Log.i(
                    TAG,
                    String.format("closeUpStream %d", tunnel.tunnelId)
                )
                tunnel.upActive = false
                if (isClosedTunnel(
                        tunnel
                    )
                ) {
                    tunnel.tunnelCloseMsgQueue!!.add(tunnel.tunnelKey)
                }
            }
        }

        private fun closeRst(tunnel: TcpTunnel) {
            synchronized(tunnel) {
                Log.i(
                    TAG,
                    String.format("closeRst %d", tunnel.tunnelId)
                )
                try {
                    if (tunnel.destSocket != null && tunnel.destSocket!!.isOpen) {
                        tunnel.destSocket!!.close()
                        tunnel.destSocket = null
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                sendTcpPack(
                    tunnel,
                    TCPHeader.RST.toByte(), null
                )
                tunnel.upActive = false
                tunnel.downActive = false
            }
        }
    }
}