package com.paranoid.vpn.app.vpn.ui

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.content.ContextCompat.startForegroundService
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayoutMediator
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.common.utils.VPNState
import com.paranoid.vpn.app.databinding.NavigationVpnFragmentBinding
import com.paranoid.vpn.app.vpn.core.LocalVPNService2
import com.paranoid.vpn.app.vpn.ui.vpn_pager.VPNFragmentPagerAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong

class VPNFragment :
    BaseFragment<NavigationVpnFragmentBinding, VPNViewModel>(NavigationVpnFragmentBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        initTabLayout()
    }

    private fun initTabLayout() {
        val vpnFragmentPagerAdapter = viewModel?.let { VPNFragmentPagerAdapter(activity, it) }
        binding.vpVpnPager.adapter = vpnFragmentPagerAdapter

        TabLayoutMediator(binding.tlTabLayout, binding.vpVpnPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = Utils.getString(R.string.tab_vpn)
                    tab.icon = context?.let { getDrawable(it, R.drawable.ic_outline_vpn_key) }
                }
                1 -> {
                    tab.text = Utils.getString(R.string.tab_proxy)
                    tab.icon = context?.let { getDrawable(it, R.drawable.ic_outline_router) }
                }
                2 -> {
                    tab.text = Utils.getString(R.string.tab_traffic)
                    tab.icon =
                        context?.let { getDrawable(it, R.drawable.ic_outline_data_exploration) }
                }
            }
        }.attach()
    }

    private fun setListeners() {

        binding.cvHelpButton.setOnClickListener {
            context?.let { context_ ->
                Utils.makeToast(
                    context_,
                    Utils.getString(R.string.help_info)
                )
            }
        }
    }

    override fun initViewModel() {
        viewModel = ViewModelProvider(
            this,
            VPNViewModelFactory(requireActivity().application)
        )[VPNViewModel::class.java]
    }
}
