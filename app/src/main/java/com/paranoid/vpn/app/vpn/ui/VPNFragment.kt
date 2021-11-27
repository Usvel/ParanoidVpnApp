package com.paranoid.vpn.app.vpn.ui

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.content.ContextCompat.startForegroundService
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.GsonBuilder
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.common.utils.ClickHandlers
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.common.utils.VPNState
import com.paranoid.vpn.app.common.vpn_configuration.domain.repository.VPNConfigRepository
import com.paranoid.vpn.app.databinding.NavigationVpnFragmentBinding
import com.paranoid.vpn.app.qr.QRCreator
import com.paranoid.vpn.app.vpn.core.LocalVPNService2
import com.paranoid.vpn.app.vpn.ui.vpn_pager.VPNFragmentPagerAdapter
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.Collectors

class VPNFragment :
    BaseFragment<NavigationVpnFragmentBinding, VPNViewModel>(NavigationVpnFragmentBinding::inflate) {

    companion object {
        @JvmStatic
        var downByte: AtomicLong = AtomicLong(0)

        @JvmStatic
        var upByte: AtomicLong = AtomicLong(0)
    }

    private var textUpdater: Job? = null
    private val VPN_REQUEST_CODE = 0x0F
    private lateinit var bottomSheetDialog: BottomSheetDialog

    /** Defines callbacks for service binding, passed to bindService()  */
    private var connection = VPNServiceConnection()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Just for test
        loadMainConfiguration()
        setListeners()
        initBottomSheetDialog()
        setRecyclerViews()
        setObservers()

        CoroutineScope(Dispatchers.IO).launch {
            viewModel?.getConfig()?.let { updateConfigText(configName = it.name) }
        }

        Intent(context, LocalVPNService2::class.java).also { intent ->
            activity?.bindService(intent, connection, 0)
        }

        initTabLayout()
    }

    private fun initTabLayout() {
        val vpnFragmentPagerAdapter = viewModel?.let { VPNFragmentPagerAdapter(activity, it) }
        binding.vpVpnPager.adapter = vpnFragmentPagerAdapter

        TabLayoutMediator(binding.tlTabLayout, binding.vpVpnPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = Utils.getString(R.string.tab_vpn)
                    tab.icon = context?.let { getDrawable(it, R.drawable.ic_outline_vpn_key) }
                }
                1 -> {
                    tab.text = Utils.getString(R.string.tab_proxy)
                    tab.icon = context?.let { getDrawable(it, R.drawable.ic_outline_router) }
                }
                2 -> {
                    tab.text = Utils.getString(R.string.tab_traffic)
                    tab.icon =
                        context?.let { getDrawable(it, R.drawable.ic_outline_data_exploration) }
                }
            }
        }.attach()
    }

    private fun initBottomSheetDialog() {
        bottomSheetDialog =
            context?.let { BottomSheetDialog(it, R.style.AppBottomSheetDialogTheme) }!!
        bottomSheetDialog.setContentView(R.layout.vpn_bottom_sheet_dialog_layout)
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun setRecyclerViews() {
        val rvAllConfigs = bottomSheetDialog.findViewById<RecyclerView>(R.id.rvAllConfigs)

        rvAllConfigs!!.layoutManager = LinearLayoutManager(context)

        viewModel?.getAllConfigs()?.observe(viewLifecycleOwner) { value ->
            val adapter = VPNConfigAdapter(value) { id, code ->
                when (code) {
                    ClickHandlers.GetConfiguration -> showConfigDetails(id)
                    ClickHandlers.SetConfiguration -> {
                        if (connection.isBound) {
                            Toast.makeText(
                                context,
                                "Cannot set config if service is running!",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@VPNConfigAdapter
                        }
                        lifecycleScope.launch(Dispatchers.IO) {
                            viewModel?.setConfig(id)
                            LocalVPNService2.currentConfig = viewModel?.getConfig()
                            viewModel?.getConfig()?.let { updateConfigText(configName = it.name) }
                            withContext(Dispatchers.Main) {
                                hideBottomSheetDialog()
                            }
                        }
                    }
                    ClickHandlers.QRCode -> CoroutineScope(Dispatchers.IO).launch { showQRCode(id) }
                    else -> showConfigDetails(id)
                }
            }
            rvAllConfigs.adapter = adapter
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        lifecycleScope.launch(Dispatchers.Default) {
            textUpdater?.cancel()
        }
        if (connection.isBound)
            activity?.unbindService(connection)
    }

    private suspend fun updateConfigText(configName: String) = withContext(Dispatchers.Main) {
        binding.tvMainConfigurationText.text = configName
    }

    private fun loadMainConfiguration() {
        binding.tvMainConfigurationText.text = Utils.getString(R.string.test_first_configuration)
        binding.cvMainConfigurationCard.setOnClickListener {
            context?.let { context_ ->
                Utils.makeToast(
                    context_,
                    Utils.getString(R.string.test_first_configuration)
                )
            }
        }
    }

    private fun showConfigDetails(config_id: Long) {
        val customAlertDialogView = LayoutInflater.from(context)
            .inflate(R.layout.config_details_dialog, null, false)
        CoroutineScope(Dispatchers.IO).launch {
            val config = VPNConfigRepository(requireActivity().application)
                .getConfig(config_id)
            withContext(Dispatchers.Main) {
                val materialAlertDialogBuilder = context?.let {
                    MaterialAlertDialogBuilder(
                        it,
                        R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Background
                    )
                }
                customAlertDialogView.findViewById<TextView>(R.id.tvPrimaryDNS).text =
                    config?.primary_dns
                customAlertDialogView.findViewById<TextView>(R.id.tvSecondaryDNS).text =
                    config?.secondary_dns
                customAlertDialogView.findViewById<TextView>(R.id.tvLocalIp).text =
                    config?.local_ip
                customAlertDialogView.findViewById<TextView>(R.id.tvGateway).text =
                    config?.gateway
                customAlertDialogView.findViewById<TextView>(R.id.tvProxyIp).text =
                    config?.proxy_ip?.stream()?.collect(Collectors.joining(", "))
                materialAlertDialogBuilder
                    ?.setView(customAlertDialogView)
                    ?.setTitle(config?.name)
                    ?.setMessage("Current configuration details")
                    ?.setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }?.show()

            }
        }
    }

    private fun setListeners() {
        binding.cvSettingsIcon.setOnClickListener {
            viewModel?.getConfigId()?.let { it1 -> showConfigDetails(it1) }
        }

        binding.llCurrentConfiguration.setOnLongClickListener {
            viewModel?.getConfigId()?.let { it1 -> showConfigDetails(it1) }
            return@setOnLongClickListener false
        }

        binding.cvMainConfigurationCard.setOnClickListener {
            showBottomSheetDialog()
        }

        binding.cvHelpButton.setOnClickListener {
            context?.let { context_ ->
                Utils.makeToast(
                    context_,
                    Utils.getString(R.string.help_info)
                )
            }
        }

        binding.ivShareIcon.setOnClickListener {
            context?.let { context_ ->
                Utils.makeToast(
                    context_,
                    Utils.getString(R.string.share_configuration)
                )
            }
        }

        binding.ivQrIcon.setOnClickListener {
            context?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel?.getConfigId()?.let { it1 -> showQRCode(it1) }
                }
            }
        }
    }

    private fun showQRCode(id: Long) {
        val config = VPNConfigRepository(requireActivity().application)
            .getConfig(id)
        val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
        val intent = Intent(context, QRCreator::class.java)
        intent.putExtra("config", gson.toJson(config))
        startActivity(intent)
    }

    private fun showBottomSheetDialog() {
        bottomSheetDialog.show()
    }

    private fun hideBottomSheetDialog() {
        bottomSheetDialog.hide()
    }

    private fun setObservers() {
        viewModel?.vpnStateOn?.observe(viewLifecycleOwner) { value ->
            when (value) {
                VPNState.CONNECTED -> {
                    if (viewModel?.isConnected?.value == true)
                        startVpn()
                }
                VPNState.NOT_CONNECTED -> stopVpn()
                else -> stopVpn()
            }

        }
    }

    private fun startVpn() {
        if (connection.isBound)
            return
        val vpnIntent = VpnService.prepare(context)
        if (vpnIntent != null) startActivityForResult(
            vpnIntent,
            VPN_REQUEST_CODE
        ) else onActivityResult(VPN_REQUEST_CODE, Activity.RESULT_OK, null)

        Intent(context, LocalVPNService2::class.java).also { intent ->
            activity?.bindService(intent, connection, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val intent = Intent(context, LocalVPNService2::class.java)
            intent.action = "start"
            context?.let { startForegroundService(it, intent) }
        }
    }

    private fun stopVpn() {
        if (!connection.isBound)
            return
        activity?.unbindService(connection)
        connection.isBound = false
        val stopIntent = Intent(context, LocalVPNService2::class.java)
        stopIntent.action = "stop"
        context?.let { startForegroundService(it, stopIntent) }
    }

    override fun initViewModel() {
        viewModel = ViewModelProvider(
            this,
            VPNViewModelFactory(requireActivity().application)
        )[VPNViewModel::class.java]
    }
}
