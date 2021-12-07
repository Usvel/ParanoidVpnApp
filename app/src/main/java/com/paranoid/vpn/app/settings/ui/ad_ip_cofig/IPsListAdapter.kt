package com.paranoid.vpn.app.settings.ui.ad_ip_cofig

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ad_block_configuration.domain.model.AdBlockIpItem

class IPsListAdapter(
    private val mList: List<AdBlockIpItem>,
    private val onItemClicked: (AdBlockIpItem) -> Unit,
) : RecyclerView.Adapter<IPsListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ad_ip_item, parent, false)

        return ViewHolder(view) {
            onItemClicked(
                mList[it]
            )
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val adBlockItem = mList[position]

        if (adBlockItem.IsDomain){
            holder.tvIp.text = adBlockItem.Domain
            holder.tvType.text = "Domain"
        } else {
            holder.tvIp.text = adBlockItem.Ip
            holder.tvType.text = "IP"
        }

        holder.imDelete.setOnClickListener {
            onItemClicked(adBlockItem)
        }

        setAnimation(holder.itemView, position)
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

    override fun getItemCount(): Int {
        return mList.size
    }

    class ViewHolder(
        ItemView: View,
        onItemClicked: (Int) -> Unit
    ) : RecyclerView.ViewHolder(ItemView) {
        var tvType: TextView = itemView.findViewById(R.id.tvType)
        var tvIp: TextView = itemView.findViewById(R.id.tvIp)
        val imDelete: ImageView = itemView.findViewById(R.id.imDelete)

        init {
            imDelete.setOnClickListener {
                onItemClicked(bindingAdapterPosition)
            }
        }
    }
}
