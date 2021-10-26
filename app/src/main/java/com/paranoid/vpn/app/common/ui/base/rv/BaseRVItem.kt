package com.paranoid.vpn.app.common.ui.base.rv

interface BaseRVItem {
    val itemViewType: Int

    fun getItemId(): Int
}
