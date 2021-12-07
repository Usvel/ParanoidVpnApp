package com.paranoid.vpn.app.vpn.ui.vpn_pager.proxy

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.proxy_configuration.domain.model.ProxyItem
import com.paranoid.vpn.app.common.utils.ProxyClickHandlers


class ProxyLocalListAdapter(
    private val mList: List<ProxyItem>,
    private val onItemClicked: (ProxyItem, ProxyClickHandlers) -> Unit,
) : RecyclerView.Adapter<ProxyLocalListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.proxy_item_local, parent, false)

        return ViewHolder(view) {
            onItemClicked(
                mList[it], ProxyClickHandlers.Info
            )
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val configItem = mList[position]

        holder.countryName.text = "${configItem.Location.country} · ${configItem.Location.city}"
        holder.ipAddress.text = configItem.Ip
        if (configItem.Type != null) {
            if (configItem.Type?.size == 2)
                holder.protocol.text = "SOCKS4/5"
            else
                holder.protocol.text = configItem.Type?.joinToString(separator = ", ") ?: "None"
        }

        holder.infoButton.setOnClickListener {
            onItemClicked(configItem, ProxyClickHandlers.Info)
        }

        setAnimation(holder.itemView, position)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    private var lastPosition = -1

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastPosition) {
            val animation: Animation = AnimationUtils.loadAnimation(
                viewToAnimate.context,
                R.anim.scale_fade_anim
            )
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

    class ViewHolder(
        ItemView: View,
        onItemClicked: (Int) -> Unit
    ) : RecyclerView.ViewHolder(ItemView) {
        val countryName: TextView = itemView.findViewById(R.id.tvCountryName)
        val ipAddress: TextView = itemView.findViewById(R.id.tvProxyIp)
        val protocol: TextView = itemView.findViewById(R.id.tvProtocol)
        val proxy: CardView = itemView.findViewById(R.id.cvProxy)
        val setButton: ImageView = itemView.findViewById(R.id.imUploadIcon)
        val infoButton: ImageView = itemView.findViewById(R.id.imInfoIcon)

        init {
            infoButton.setOnClickListener {
                onItemClicked(bindingAdapterPosition)
            }
        }
    }
}