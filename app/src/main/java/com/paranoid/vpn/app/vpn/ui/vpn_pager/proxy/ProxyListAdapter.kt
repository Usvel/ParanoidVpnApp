package com.paranoid.vpn.app.vpn.ui.vpn_pager.proxy

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.proxy_configuration.domain.model.ProxyItem
import com.paranoid.vpn.app.common.utils.ClickHandlers


class ProxyListAdapter(
    private val mList: List<ProxyItem>,
    private val warningColor: Int,
    private val errorColor: Int,
    private val onItemClicked: (Long, ClickHandlers) -> Unit,
) : RecyclerView.Adapter<ProxyListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.proxy_item_view, parent, false)

        return ViewHolder(view) {
            onItemClicked(
                mList[it].id, ClickHandlers.GetConfiguration
            )
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val configItem = mList[position]

        holder.countryName.text = configItem.Location.country
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

        //holder.proxy.setOnClickListener {
        //    onItemClicked(configItem.id, ClickHandlers.GetConfiguration)
        //}
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

        init {
            //itemView.setOnClickListener {
            //   onItemClicked(bindingAdapterPosition)
            //}
        }
    }
}