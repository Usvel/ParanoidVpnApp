package com.example.paranoid

import android.os.Bundle
import android.view.View
import com.example.paranoid.databinding.NavigationSettingsFragmentBinding

class SettingsFragment :
    BaseFragment<NavigationSettingsFragmentBinding>(NavigationSettingsFragmentBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.testTextView.text = getString(R.string.this_is_settings)
    }
}
