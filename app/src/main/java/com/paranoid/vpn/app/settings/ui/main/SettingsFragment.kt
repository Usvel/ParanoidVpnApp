package com.paranoid.vpn.app.settings.ui.main

import android.annotation.SuppressLint
import android.content.Intent
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
import com.paranoid.vpn.app.qr.QRScanner


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

    @SuppressLint("SetTextI18n")
    private fun updateConfigNumber(){
        viewModel?.getAllConfigs()?.observe(viewLifecycleOwner) { value ->
            val configSize = value.size
            binding.configurationNumber.text = "Already added $configSize configuration(s)"
        }

        viewModel?.getAllProxies()?.observe(viewLifecycleOwner) { value ->
            val configSize = value.size
            binding.proxyConfigurationNumber.text = "Already added $configSize proxies(s)"
        }
    }

    private fun setListeners(){
        binding.viewAdIpConfigurationButton.setOnClickListener{
            it.findNavController().navigate(R.id.action_settings_fragment_to_advert_fragment)
        }

        binding.addConfigurationButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_settings_fragment_to_vpn_config_add_element)
        }

        binding.addProxyConfigurationButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_settings_fragment_to_proxy_add_fragment)
        }

        binding.addConfigurationButtonByQr.setOnClickListener {
            val intent = Intent(context, QRScanner::class.java)
            startActivity(intent)
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
