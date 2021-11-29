package com.paranoid.vpn.app.vpn.ui.vpn_pager.proxy

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.proxy_configuration.domain.model.ProxyItem
import com.paranoid.vpn.app.common.utils.ProxyClickHandlers


class ProxyOnlineListAdapter(
    private val mList: List<ProxyItem>,
    private val warningColor: Int,
    private val errorColor: Int,
    private val onItemClicked: (ProxyItem, ProxyClickHandlers) -> Unit,
) : RecyclerView.Adapter<ProxyOnlineListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.proxy_item_online, parent, false)

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
        val currentPing = configItem.Ping
        holder.ping.text = "${currentPing}ms"
        if (currentPing > 100)
            holder.ping.setTextColor(warningColor)
        else if (currentPing > 500)
            holder.ping.setTextColor(errorColor)
        if (configItem.Type != null) {
            if (configItem.Type?.size == 2)
                holder.protocol.text = "SOCKS4/5"
            else
                holder.protocol.text = configItem.Type?.joinToString(separator = ", ") ?: "None"
        }

        holder.infoButton.setOnClickListener {
            onItemClicked(configItem, ProxyClickHandlers.Info)
        }
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(
        ItemView: View,
        onItemClicked: (Int) -> Unit
    ) : RecyclerView.ViewHolder(ItemView) {
        val countryName: TextView = itemView.findViewById(R.id.tvCountryName)
        val ipAddress: TextView = itemView.findViewById(R.id.tvProxyIp)
        val ping: TextView = itemView.findViewById(R.id.tvPing)
        val protocol: TextView = itemView.findViewById(R.id.tvProtocol)
        val proxy: CardView = itemView.findViewById(R.id.cvProxy)
        val editButton: ImageView = itemView.findViewById(R.id.imEditIcon)
        val infoButton: ImageView = itemView.findViewById(R.id.imInfoIcon)

        init {
            infoButton.setOnClickListener {
                onItemClicked(bindingAdapterPosition)
            }
        }
    }
}