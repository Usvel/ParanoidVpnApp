package com.example.paranoid.vpn.ui

import android.os.Bundle
import android.view.View
import com.example.paranoid.R
import com.example.paranoid.databinding.NavigationVpnFragmentBinding
import com.example.paranoid.common.ui.base.BaseFragment

class VPNFragment :
    BaseFragment<NavigationVpnFragmentBinding>(NavigationVpnFragmentBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.testTextView.text = getString(R.string.this_is_vpn)
    }

    override fun initViewModule() {
        TODO("Not yet implemented")
    }
}
