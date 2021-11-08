package com.paranoid.vpn.app.settings.ui

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.databinding.NavigationSettingsFragmentBinding

class SettingsFragment :
    BaseFragment<NavigationSettingsFragmentBinding, SettingsViewModel>(
        NavigationSettingsFragmentBinding::inflate
    ) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.testTextView.text = Utils.getString(R.string.this_is_settings)
    }

    override fun initViewModel() {
        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
    }
}
