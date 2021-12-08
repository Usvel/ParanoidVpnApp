package com.paranoid.vpn.app.vpn.ui.vpn_pager.traffic

import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.rv.BaseRVItem
import com.paranoid.vpn.app.vpn.domain.EntityPacket

const val VIEW_TYPE_PACKET_ITEM = R.layout.rv_packet_item

data class RVPacketItem(
    val id: String,
    val packet: EntityPacket,
    override val itemViewType: Int = VIEW_TYPE_PACKET_ITEM
) : BaseRVItem {
    override fun getItemId(): Int {
        return id.hashCode()
    }
}
