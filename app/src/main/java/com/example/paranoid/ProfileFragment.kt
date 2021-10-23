package com.example.paranoid

import android.os.Bundle
import android.view.View
import com.example.paranoid.databinding.NavigationProfileFragmentBinding

class ProfileFragment :
    BaseFragment<NavigationProfileFragmentBinding>(NavigationProfileFragmentBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.testTextView.text = getString(R.string.this_is_profile)
    }

}