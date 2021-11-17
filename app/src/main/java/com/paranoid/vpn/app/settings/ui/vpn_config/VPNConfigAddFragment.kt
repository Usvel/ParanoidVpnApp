package com.paranoid.vpn.app.settings.ui.vpn_config

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.common.utils.Validators.Companion.validateIP
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.ForwardingRule
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.Protocols
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.VPNConfigItem
import com.paranoid.vpn.app.databinding.NavigationVpnConfigAddFragmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class VPNConfigAddFragment :
    BaseFragment<NavigationVpnConfigAddFragmentBinding, VPNConfigAddViewModel>(
        NavigationVpnConfigAddFragmentBinding::inflate
    ) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonRotationSet()
        setListeners()
    }

    override fun initViewModel() {
        viewModel = ViewModelProvider(
            this,
            VPNConfigAddViewModelFactory(requireActivity().application)
        )[VPNConfigAddViewModel::class.java]
    }

    private fun setListeners() {
        binding.addConfigurationButton.setOnClickListener {
            val name = binding.etConfigName.text.toString()
            val primaryDNS: String = binding.etPrimaryDNS.text.toString()
            val secondaryDNS = binding.etSecondaryDNS.text.toString()
            val localIP = binding.etLocalIP.text.toString()
            val gateway = binding.etGateway.text.toString()
            if(validateIP(listOf(primaryDNS, secondaryDNS, localIP, gateway))) {
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.insertConfigToDataBase(
                        VPNConfigItem(
                            name = name,
                            primary_dns = primaryDNS,
                            secondary_dns = secondaryDNS,
                            proxy_ip = arrayListOf("123.123.123.123"), // Coming son
                            local_ip = localIP,
                            gateway = gateway,
                            forwarding_rules = arrayListOf()
                        )
                    )
                }
                it.findNavController().navigate(R.id.action_vpn_config_add_element_to_settings_fragment)
            }
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
