package com.paranoid.vpn.app.settings.ui

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.databinding.NavigationSettingsFragmentBinding
import android.view.animation.Animation

import android.view.animation.LinearInterpolator

import android.view.animation.RotateAnimation
import com.paranoid.vpn.app.R


class SettingsFragment :
    BaseFragment<NavigationSettingsFragmentBinding, SettingsViewModel>(
        NavigationSettingsFragmentBinding::inflate
    ) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.testTextView.text = Utils.getString(R.string.this_is_settings)

        val rotate = RotateAnimation(
            0F,
            180F,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        rotate.duration = 5000
        rotate.interpolator = LinearInterpolator()
        binding.settingsIcon.startAnimation(rotate)

    }

    override fun initViewModel() {
        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
    }
}
