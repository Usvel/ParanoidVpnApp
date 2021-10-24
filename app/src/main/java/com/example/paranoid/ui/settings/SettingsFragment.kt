package com.example.paranoid.ui.settings

import android.os.Bundle
import android.view.View
import com.example.paranoid.R
import com.example.paranoid.databinding.NavigationSettingsFragmentBinding
import com.example.paranoid.ui.base.BaseFragment

class SettingsFragment :
    BaseFragment<NavigationSettingsFragmentBinding>(NavigationSettingsFragmentBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.testTextView.text = getString(R.string.this_is_settings)
    }
}
