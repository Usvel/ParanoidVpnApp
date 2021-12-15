package com.paranoid.vpn.app.vpn.ui.vpn_pager.traffic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.rv.BaseListAdapter
import com.paranoid.vpn.app.common.ui.base.rv.BaseRVItem
import com.paranoid.vpn.app.common.ui.base.rv.BaseViewHolder
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.vpn.remote.InternalStorageImpl

class TrafficRVAdapter : BaseListAdapter<BaseRVItem, BaseViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return PacketViewHolder(view, onClickListener)
    }

    private inner class PacketViewHolder(view: View, var listener: OnClickListener?) :
        BaseViewHolder(view), View.OnClickListener {
        private val cvContainer = view.findViewById<CardView>(R.id.cv_ip)
        private val ipSource = view.findViewById<TextView>(R.id.ipSource)
        private val ipSourcePort = view.findViewById<TextView>(R.id.ipSourcePort)
        private val ipDestination = view.findViewById<TextView>(R.id.ipDestination)
        private val ipDestinationPort = view.findViewById<TextView>(R.id.ipDestinationPort)
        private val ipProtocol = view.findViewById<TextView>(R.id.ipProtocol)
        private val ipGroup = view.findViewById<Group>(R.id.gIpPacket)
        private val ipSizePacket = view.findViewById<TextView>(R.id.ipSizePacket)
        private val btnSave = view.findViewById<CardView>(R.id.ipSavePcap)

        init {
            btnSave.setOnClickListener(this)
        }

        override fun bind() {
            super.bind()
            val item = getItem(bindingAdapterPosition) as RVPacketItem
            ipSource.text = item.packets.last().ip4.sourceAddress.toString().removeRange(0, 1)
            ipDestination.text = item.ip
            if (item.packets.last().tcp != null) {
                ipSourcePort.text = item.packets.last().tcp!!.sourcePort.toString()
                ipDestinationPort.text = item.packets.last().tcp!!.destinationPort.toString()
                ipProtocol.text = Utils.getString(R.string.traffic_rv_adapter_tcp)
            } else {
                ipSourcePort.text = item.packets.last().udp!!.sourcePort.toString()
                ipDestinationPort.text = item.packets.last().udp!!.destinationPort.toString()
                ipProtocol.text = Utils.getString(R.string.traffic_rv_adapter_udp)
            }
            ipGroup.isVisible = item.isOpen
            cvContainer.setOnClickListener {
                item.isOpen = !item.isOpen
                notifyItemChanged(bindingAdapterPosition)
            }

            ipSizePacket.text = "${item.sizePackets} packets"
        }

        override fun onClick(v: View) {
            val position = bindingAdapterPosition
            if (position == RecyclerView.NO_POSITION) return
            listener?.onClick(v, position)
        }
    }
}