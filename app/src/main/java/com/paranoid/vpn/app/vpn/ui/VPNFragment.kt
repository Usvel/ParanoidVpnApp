package com.paranoid.vpn.app.vpn.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getDrawable
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.databinding.NavigationVpnFragmentBinding
import com.paranoid.vpn.app.intro.ParanoidIntroScreen
import com.paranoid.vpn.app.qr.QRScanner
import com.paranoid.vpn.app.vpn.ui.vpn_pager.VPNFragmentPagerAdapter

class VPNFragment :
    BaseFragment<NavigationVpnFragmentBinding, VPNViewModel>(NavigationVpnFragmentBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        setListeners()
        initTabLayout()
    }

    private fun initTabLayout() {
        var favorite = false
        var turnOnVPN = false
        if (arguments?.isEmpty != true) {
            arguments?.getBoolean("favoriteFlag")?.let {
                if (it)
                    favorite = true
            }
            arguments?.getBoolean("turnOnVPN")?.let {
                if (it)
                    turnOnVPN = true
            }
        }
        val vpnFragmentPagerAdapter =
            viewModel?.let { VPNFragmentPagerAdapter(activity, favorite, turnOnVPN) }
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
            val intent = Intent(context, ParanoidIntroScreen::class.java)
            startActivity(intent)
            //context?.let { context_ ->
              //  Utils.makeToast(
            //        context_,
            //        Utils.getString(R.string.help_info)
             //   )
            //}
        }
    }

    override fun initViewModel() {
        viewModel = ViewModelProvider(
            this,
            VPNViewModelFactory(requireActivity().application)
        )[VPNViewModel::class.java]
    }
}
