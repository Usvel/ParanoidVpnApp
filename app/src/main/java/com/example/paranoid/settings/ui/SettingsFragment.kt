package com.example.paranoid.settings.ui

import android.os.Bundle
import android.view.View
import com.example.paranoid.R
import com.example.paranoid.databinding.NavigationSettingsFragmentBinding
import com.example.paranoid.common.ui.base.BaseFragment

class SettingsFragment :
    BaseFragment<NavigationSettingsFragmentBinding>(NavigationSettingsFragmentBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.testTextView.text = getString(R.string.this_is_settings)
    }

    override fun initViewModule() {
        TODO("Not yet implemented")
    }
}
