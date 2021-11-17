package com.paranoid.vpn.app.settings.ui.vpn_config

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.ForwardingRule

class ForwardingRulesAdapter(
    private val context: Context,
    private val rulesList: ArrayList<ForwardingRule>
) :
    RecyclerView.Adapter<ForwardingRulesAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.mTitle)
        var mMenus: ImageView = itemView.findViewById(R.id.mMenus)

        init {
            // mbNum = itemView.findViewById<TextView>(R.id.mSubTitle)
            mMenus.setOnClickListener { popupMenus(it) }
        }

        private fun popupMenus(v: View) {
            val position = rulesList[absoluteAdapterPosition]
            val popupMenus = PopupMenu(context, v)
            popupMenus.inflate(R.menu.show_menu)
            popupMenus.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.delete -> {
                        rulesList.removeAt(absoluteAdapterPosition)
                        notifyDataSetChanged()
                        true
                    }
                    else -> true
                }

            }
            popupMenus.show()
            val popup = PopupMenu::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true
            val menu = popup.get(popupMenus)
            menu.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(menu, true)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.add_string_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = "Rule ${position + 1}"
    }

    override fun getItemCount(): Int {
        return rulesList.size
    }
}