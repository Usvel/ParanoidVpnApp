package com.paranoid.vpn.app.vpn.ui

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getDrawable
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.databinding.NavigationVpnFragmentBinding
import com.paranoid.vpn.app.vpn.ui.vpn_pager.VPNFragmentPagerAdapter

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
        if (!arguments?.isEmpty!!) {
            arguments?.getInt("pageNumber")?.let { binding.vpVpnPager.currentItem = it }
        }
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
