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
import com.paranoid.vpn.app.common.ad_block_configuration.domain.model.AdBlockIpItem
import com.paranoid.vpn.app.common.proxy_configuration.domain.model.ProxyItem
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.VPNConfigItem
import com.paranoid.vpn.app.databinding.NavigationSettingsFragmentBinding
import com.paranoid.vpn.app.qr.QRScanner

private const val VPN_DB_NAME = "vpn_config_database"
private const val PROXY_DB_NAME = "proxy_database"
private const val IP_DB_NAME = "ip_database"


class SettingsFragment :
    BaseFragment<NavigationSettingsFragmentBinding, SettingsViewModel>(
        NavigationSettingsFragmentBinding::inflate
    ) {


    private lateinit var configList: List<VPNConfigItem>
    private lateinit var proxyList: List<ProxyItem>
    private lateinit var ipList: List<AdBlockIpItem>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonRotationSet()
        updateDataNumber()
        setListeners()
    }

    override fun initViewModel() {
        viewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(requireActivity().application)
        )[SettingsViewModel::class.java]
    }

    @SuppressLint("SetTextI18n")
    private fun updateDataNumber() {
        viewModel?.getAllConfigs()?.observe(viewLifecycleOwner) { value ->
            val configSize = value.size
            configList = value
            binding.tvConfigurationNumber.text = "Already added $configSize configuration(s)"
        }

        viewModel?.getAllProxies()?.observe(viewLifecycleOwner) { value ->
            val configSize = value.size
            proxyList = value
            binding.proxyConfigurationNumber.text = "Already added $configSize proxies(s)"
        }

        viewModel?.getAllIPs()?.observe(viewLifecycleOwner) { value ->
            ipList = value
        }
    }

    private fun setListeners(){
        binding.viewAdIpConfigurationButton.setOnClickListener{
            it.findNavController().navigate(R.id.action_settings_fragment_to_advert_fragment)
        }

        binding.viewAddConfigurationButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_settings_fragment_to_vpn_config_add_element)
        }

        binding.addProxyConfigurationButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_settings_fragment_to_proxy_add_fragment)
        }

        binding.viewAddConfigurationButtonByQr.setOnClickListener {
            val intent = Intent(context, QRScanner::class.java)
            startActivity(intent)
        }

        binding.viewVpnBackup.setOnClickListener {
            exportDatabaseToCSVFile(VPN_DB_NAME)
        }

        binding.viewVpnRestore.setOnClickListener {

        }

        binding.viewProxyBackup.setOnClickListener {
            exportDatabaseToCSVFile(PROXY_DB_NAME)
        }

        binding.viewProxyRestore.setOnClickListener {

        }

        binding.viewIpBackup.setOnClickListener {
            exportDatabaseToCSVFile(IP_DB_NAME)
        }

        binding.viewIpRestore.setOnClickListener {

        }
    }

    private fun exportDatabaseToCSVFile(database: String) {
        val csvFile = context?.let { Utils.generateFile(it, "$database.csv") }
        if (csvFile != null) {

            when (database) {
                VPN_DB_NAME -> {
                    viewModel?.exportVpnDBToCSVFile(csvFile, configList)
                }
                PROXY_DB_NAME -> {
                    viewModel?.exportProxyDBToCSVFile(csvFile, proxyList)
                }
                IP_DB_NAME -> {
                    viewModel?.exportIpDBToCSVFile(csvFile, ipList)
                }
            }

            context?.let { Utils.makeToast(it, "Imported") }
            val intent = context?.let { Utils.goToFileIntent(it, csvFile) }
            startActivity(intent)
        } else {
            context?.let { Utils.makeToast(it, "Failed") }
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
        binding.ivMainSettingsIcon.startAnimation(rotate)
    }
}
