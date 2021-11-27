package com.paranoid.vpn.app.vpn.ui.vpn_pager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.paranoid.vpn.app.vpn.ui.VPNViewModel


class VPNFragmentPagerAdapter(fragmentActivity: FragmentActivity?, private val oldViewModel: VPNViewModel) :
    FragmentStateAdapter(fragmentActivity!!) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> VPNObjectFragment(oldViewModel)
            1 -> PoxyObjectFragment()
            else -> TrafficObjectFragment()
        }
    }

    override fun getItemCount(): Int {
        return 3
    }
}
