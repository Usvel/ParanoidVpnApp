package com.paranoid.vpn.app.settings.ui.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.common.utils.DBFileProvider
import com.paranoid.vpn.app.common.utils.IP_DB_NAME
import com.paranoid.vpn.app.common.utils.PROXY_DB_NAME
import com.paranoid.vpn.app.common.utils.VPN_CONFIG_DB_NAME
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
            binding.tvConfigurationNumber.text = "Already added $configSize configuration(s)"
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

        binding.viewBackup.setOnClickListener {
            backupDatabase(activity)
        }

        binding.viewRestore.setOnClickListener {

        }
    }

    private fun backupDatabase(activity: Activity?) {
        val uriVpnConfig: Uri? =
            DBFileProvider().getDatabaseURI(activity!!, VPN_CONFIG_DB_NAME)
        val uriProxyConfig: Uri? = DBFileProvider().getDatabaseURI(activity, PROXY_DB_NAME)
        val uriIP: Uri? = DBFileProvider().getDatabaseURI(activity, IP_DB_NAME)
        if (uriVpnConfig != null) {
            shareDB(uriVpnConfig)
        }
    }

    private fun shareDB(attachment: Uri) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, attachment)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun sendEmail(activity: AppCompatActivity, attachment: Uri) {
        val emailIntent = Intent(Intent.ACTION_SEND)
        //Set type to email
        emailIntent.type = "vnd.android.cursor.dir/email"
        val toEmail = "whatever@gmail.com"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, toEmail)
        emailIntent.putExtra(Intent.EXTRA_STREAM, attachment)
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Data for Training Log")
        activity.startActivity(Intent.createChooser(emailIntent, "Send Email"))
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
