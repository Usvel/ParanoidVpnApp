package com.paranoid.vpn.app.settings.ui

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.databinding.NavigationSettingsFragmentBinding


class SettingsFragment :
    BaseFragment<NavigationSettingsFragmentBinding, SettingsViewModel>(
        NavigationSettingsFragmentBinding::inflate
    ) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonRotationSet()
        setConfigNumber()
        setListeners()
    }

    override fun initViewModel() {
        viewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(requireActivity().application)
        )[SettingsViewModel::class.java]
    }

    private fun setConfigNumber() {
        binding.configurationNumber.text = String()
            .format(
                resources.getString(R.string.configuration_number),
                viewModel.configsLiveData
            )
    }

    private fun setListeners() {
        binding.addConfigurationButton.setOnClickListener {
        }

        binding.addConfigurationButtonByQr.setOnClickListener {
            Toast.makeText(context, "QR code scanning here", Toast.LENGTH_SHORT).show()

            //CoroutineScope(Dispatchers.IO).launch {
            // viewModel.insertConfigToDataBase()
            //}
        }
    }

    // Animations

    private fun buttonRotationSet() {
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
}
