package com.paranoid.vpn.app.settings.ui.main

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
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
        updateConfigNumber()
        setListeners()
    }

    override fun initViewModel() {
        viewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(requireActivity().application)
        )[SettingsViewModel::class.java]
    }

    private fun updateConfigNumber(){
        viewModel?.getAllConfigs()?.observe(viewLifecycleOwner){ value ->
            val configSize = value.size
            binding.configurationNumber.text = "Already added $configSize configuration(s)"
        }


        //binding.configurationNumber.text = String().format(resources.getString(R.string.configuration_number), viewModel.configsLiveData)
    }

    private fun setListeners(){
        binding.addConfigurationButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_settings_fragment_to_vpn_config_add_element)
        }

        binding.addConfigurationButtonByQr.setOnClickListener {
            it.findNavController().navigate(R.id.action_settings_fragment_to_qr_scanner)
        }
    }

    // Animations

    private fun buttonRotationSet(){
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
