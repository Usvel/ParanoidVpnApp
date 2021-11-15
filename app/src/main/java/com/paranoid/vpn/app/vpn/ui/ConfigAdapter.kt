package com.paranoid.vpn.app.vpn.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.VPNConfigItem

class ConfigAdapter(private val mList: List<VPNConfigItem>) :
    RecyclerView.Adapter<ConfigAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.config_item_view, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val configItem = mList[position]

        // sets the text to the textview from our itemHolder class
        holder.configName.text = configItem.name

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val configName: TextView = itemView.findViewById(R.id.tvConfigurationName)
    }
}