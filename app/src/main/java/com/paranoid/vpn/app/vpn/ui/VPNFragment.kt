package com.paranoid.vpn.app.vpn.ui

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startForegroundService
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
            viewModel.getConfig()?.let { updateConfigText(configName = it.name) }
        }


        analyzeNetworkState()

        textUpdater = lifecycleScope.launch(Dispatchers.Default) {
            while (true) {
                if (viewModel.vpnStateOn.value == VPNState.CONNECTED
                    && viewModel.isConnected.value == true
                )
                    updateText()
                delay(500)
            }
        }

        Intent(context, LocalVPNService2::class.java).also { intent ->
            activity?.bindService(intent, connection, 0)
        }
    }

    private fun analyzeNetworkState() {
        when(viewModel.vpnStateOn.value) {
            VPNState.CONNECTED -> vpnButtonConnected()
            VPNState.ERROR -> vpnButtonError()
            VPNState.NOT_CONNECTED -> vpnButtonDisable()
        }
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

        viewModel.getAllConfigs().observe(viewLifecycleOwner) { value ->
            val adapter = VPNConfigAdapter(value) { id, code ->
                when (code) {
                    ClickHandlers.GetConfiguration -> showConfigDetails(id)
                    ClickHandlers.SetConfiguration -> {
                        lifecycleScope.launch(Dispatchers.IO) {
                            viewModel.setConfig(id)
                            LocalVPNService2.currentConfig = viewModel.getConfig()
                            viewModel.getConfig()?.let { updateConfigText(configName = it.name) }
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

    private suspend fun updateText() = withContext(Dispatchers.Main) {
        binding.isConnected.text = "up: $upByte B, down: $downByte B"
    }

    private suspend fun updateConfigText(configName: String) = withContext(Dispatchers.Main) {
        binding.tvMainConfigurationText.text = configName
    }

    private fun loadMainConfiguration() {
        binding.tvMainConfigurationText.text = Utils.getString(R.string.test_first_configuration)
        binding.mainConfigurationCard.setOnClickListener {
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
            showConfigDetails(viewModel.getConfigId())
        }

        binding.llCurrentConfiguration.setOnLongClickListener {
            showConfigDetails(viewModel.getConfigId())
            return@setOnLongClickListener false
        }

        binding.mainConfigurationCard.setOnClickListener {
            showBottomSheetDialog()
        }

        binding.vpnButtonBackground.setOnClickListener {
            when (viewModel.vpnStateOn.value) {
                VPNState.CONNECTED -> {
                    vpnButtonDisable()
                    viewModel.changeVpnState()
                }

                VPNState.NOT_CONNECTED -> {
                    vpnButtonConnected()
                    if (viewModel.isConnected.value == true)
                        viewModel.changeVpnState()
                }
                VPNState.ERROR -> vpnButtonDisable()
            }
        }

        binding.helpButton.setOnClickListener {
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
                    showQRCode(viewModel.getConfigId())
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
        viewModel.isConnected.observe(viewLifecycleOwner) { value ->
            when (value) {
                false -> {
                    vpnButtonDisable()
                    stopVpn()
                }

                true -> {
                    // Not starting service automatically yet
                }
            }
        }

        viewModel.vpnStateOn.observe(viewLifecycleOwner) { value ->
            when (value) {
                VPNState.CONNECTED -> {
                    if (viewModel.isConnected.value == true)
                        startVpn()
                }
                VPNState.NOT_CONNECTED -> stopVpn()
            }

        }
    }

    private fun getVpnButtonColor(vpnButtonStateAttr: Int): Int {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(vpnButtonStateAttr, typedValue, true)
        return typedValue.data
    }

    private fun vpnButtonDisable() {
        binding.connectionStatus.visibility = View.GONE
        binding.turnOnVPN.setImageResource(R.drawable.ic_power)
        binding.isConnected.text = getString(R.string.not_connected)
        binding.connectionStatus.visibility = View.GONE
        binding.vpnButtonBackground.background.setTint(getVpnButtonColor(R.attr.vpnButtonDisabled))
    }

    private fun vpnButtonConnected() {
        // TODO: Remove Toasts
        when (viewModel.isConnected.value) {
            false ->
                Toast.makeText(requireContext(), "Error: connectivity is off!", Toast.LENGTH_SHORT)
                    .show()
            true -> {
                binding.connectionStatus.visibility = View.VISIBLE
                binding.turnOnVPN.setImageResource(R.drawable.ic_power)
                //binding.isConnected.text = getString(R.string.connected)
                binding.vpnButtonBackground.background.setTint(getVpnButtonColor(R.attr.vpnButtonConnected))
            }
        }
    }

    private fun vpnButtonError() {
        binding.turnOnVPN.setImageResource(R.drawable.ic_dino)
        binding.connectionStatus.visibility = View.GONE
        binding.isConnected.text = Utils.getString(R.string.error)
        binding.vpnButtonBackground.background.setTint(getVpnButtonColor(R.attr.vpnButtonError))
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
