package com.paranoid.vpn.app.vpn.ui.vpn_pager

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.paranoid.vpn.app.vpn.ui.vpn_pager.proxy.ProxyObjectFragment
import com.paranoid.vpn.app.vpn.ui.vpn_pager.traffic.TrafficObjectFragment
import com.paranoid.vpn.app.vpn.ui.vpn_pager.vpn.VPNObjectFragment


class VPNFragmentPagerAdapter(
    fragmentActivity: FragmentActivity?,
    private val favorite: Boolean = false,
    private val turnOnVPN: Boolean = false
) :
    FragmentStateAdapter(fragmentActivity!!) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            // 0 -> VPNObjectFragment()
            0 -> {
                val vpnObjectFragment = VPNObjectFragment();
                if (favorite) {
                    val bundle = Bundle()
                    bundle.putBoolean("favorite", true);
                    vpnObjectFragment.arguments = bundle;
                }
                if (turnOnVPN) {
                    val bundle = Bundle()
                    bundle.putBoolean("turnOnVPN", true);
                    vpnObjectFragment.arguments = bundle;
                }
                return vpnObjectFragment
            }
            1 -> ProxyObjectFragment()
            else -> TrafficObjectFragment()
        }
    }

    override fun getItemCount(): Int {
        return 3
    }
}
