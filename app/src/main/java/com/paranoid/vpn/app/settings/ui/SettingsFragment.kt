package com.paranoid.vpn.app.settings.ui

import android.os.Bundle
import android.view.View
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.databinding.NavigationSettingsFragmentBinding

class SettingsFragment :
    BaseFragment<NavigationSettingsFragmentBinding>(NavigationSettingsFragmentBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpBottomNav()
        binding.testTextView.text = getString(R.string.this_is_settings)
    }

    override fun initViewModel() {
        // TODO
    }

    override fun initBottomNav() {
        setUpBottomNav()
    }
}
