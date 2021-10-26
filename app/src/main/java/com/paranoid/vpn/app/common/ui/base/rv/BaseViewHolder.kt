package com.paranoid.vpn.app.common.ui.base.rv

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    open fun bind() {}
}
