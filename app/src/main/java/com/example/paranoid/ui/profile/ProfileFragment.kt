package com.example.paranoid.ui.profile

import android.os.Bundle
import android.view.View
import com.example.paranoid.R
import com.example.paranoid.databinding.NavigationProfileFragmentBinding
import com.example.paranoid.ui.base.BaseFragment

class ProfileFragment :
    BaseFragment<NavigationProfileFragmentBinding>(NavigationProfileFragmentBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.testTextView.text = getString(R.string.this_is_profile)
    }
}
