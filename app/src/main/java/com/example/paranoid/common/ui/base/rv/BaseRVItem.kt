package com.example.paranoid.common.ui.base.rv

interface BaseRVItem {
    val itemViewType: Int

    fun getItemId(): Int
}
