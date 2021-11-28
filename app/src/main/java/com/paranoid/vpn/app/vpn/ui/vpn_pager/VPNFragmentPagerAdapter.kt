package com.paranoid.vpn.app.vpn.ui.vpn_pager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.paranoid.vpn.app.vpn.ui.VPNViewModel
import com.paranoid.vpn.app.vpn.ui.vpn_pager.proxy.ProxyObjectFragment
import com.paranoid.vpn.app.vpn.ui.vpn_pager.traffic.TrafficObjectFragment
import com.paranoid.vpn.app.vpn.ui.vpn_pager.vpn.VPNObjectFragment


class VPNFragmentPagerAdapter(fragmentActivity: FragmentActivity?, private val oldViewModel: VPNViewModel) :
    FragmentStateAdapter(fragmentActivity!!) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> VPNObjectFragment(oldViewModel)
            1 -> ProxyObjectFragment(oldViewModel)
            else -> TrafficObjectFragment()
        }
    }

    override fun getItemCount(): Int {
        return 3
    }
}
