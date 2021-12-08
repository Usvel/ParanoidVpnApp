package com.paranoid.vpn.app.vpn.ui.vpn_pager.traffic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.rv.BaseListAdapter
import com.paranoid.vpn.app.common.ui.base.rv.BaseRVItem
import com.paranoid.vpn.app.common.ui.base.rv.BaseViewHolder
import com.paranoid.vpn.app.common.utils.Utils

class TrafficRVAdapter : BaseListAdapter<BaseRVItem, BaseViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return PacketViewHolder(view)
    }

    private inner class PacketViewHolder(view: View) : BaseViewHolder(view) {
        private val cvContainer = view.findViewById<CardView>(R.id.cv_ip)
        private val ipSource = view.findViewById<TextView>(R.id.ipSource)
        private val ipSourcePort = view.findViewById<TextView>(R.id.ipSourcePort)
        private val ipDestination = view.findViewById<TextView>(R.id.ipDestination)
        private val ipDestinationPort = view.findViewById<TextView>(R.id.ipDestinationPort)
        private val ipProtocol = view.findViewById<TextView>(R.id.ipProtocol)

        override fun bind() {
            super.bind()
            val item = getItem(adapterPosition) as RVPacketItem
            ipSource.text = item.packet.ip4.sourceAddress.toString()
            ipDestination.text = item.packet.ip4.destinationAddress.toString()
            if (item.packet.tcp != null) {
                ipSourcePort.text = item.packet.tcp.sourcePort.toString()
                ipDestinationPort.text = item.packet.tcp.destinationPort.toString()
                ipProtocol.text = Utils.getString(R.string.traffic_rv_adapter_tcp)
            } else {
                ipSourcePort.text = item.packet.udp!!.sourcePort.toString()
                ipDestinationPort.text = item.packet.udp.destinationPort.toString()
                ipProtocol.text = Utils.getString(R.string.traffic_rv_adapter_udp)
            }
        }
    }
}