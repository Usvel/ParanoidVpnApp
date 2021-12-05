package com.paranoid.vpn.app.vpn.ui.vpn_pager.vpn

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.GsonBuilder
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.common.utils.ConfigurationClickHandlers
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.common.utils.VPNState
import com.paranoid.vpn.app.common.vpn_configuration.domain.repository.VPNConfigRepository
import com.paranoid.vpn.app.databinding.PageVpnButtonBinding
import com.paranoid.vpn.app.qr.QRCreator
import com.paranoid.vpn.app.vpn.core.LocalVPNService2
import com.paranoid.vpn.app.vpn.ui.*
import kotlinx.coroutines.*
import java.util.stream.Collectors


class VPNObjectFragment() :
    BaseFragment<PageVpnButtonBinding, VPNViewModel>(PageVpnButtonBinding::inflate) {

    private var textUpdater: Job? = null
    private val VPN_REQUEST_CODE = 0x0F
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var oldViewModel: VPNViewModel
    private var favoriteData = false

    /** Defines callbacks for service binding, passed to bindService()  */
    private var connection = VPNServiceConnection()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        loadMainConfiguration()
        setListeners()
        initBottomSheetDialog()
        setRecyclerViews()
        setObservers()
        analyzeNetworkState()


        CoroutineScope(Dispatchers.IO).launch {
            oldViewModel.getConfig()?.let { updateConfigText(configName = it.name) }
        }
        textUpdater = lifecycleScope.launch(Dispatchers.Default) {
            while (coroutineContext.isActive) {
                if (oldViewModel.vpnStateOn.value == VPNState.CONNECTED
                    && oldViewModel.isConnected.value == true
                )
                    updateText()
                delay(500)
            }
        }
    }

    private fun setRecyclerViews(filter: String? = null, favorite: Boolean = false) {

        val rvAllConfigs = bottomSheetDialog.findViewById<RecyclerView>(R.id.rvAllConfigs)
        val tvNoData = bottomSheetDialog.findViewById<TextView>(R.id.tvNoData)

        rvAllConfigs!!.layoutManager = LinearLayoutManager(context)

        if ((filter == null) || (filter == ""))
            oldViewModel.getAllConfigs(favorite).observe(viewLifecycleOwner) { value ->
                val adapter = VPNConfigAdapter(value) { id, code ->
                    when (code) {
                        ConfigurationClickHandlers.GetConfiguration -> showConfigDetails(id)
                        ConfigurationClickHandlers.SetConfiguration -> {
                            if (connection.isBound) {
                                Toast.makeText(
                                    context,
                                    "Cannot set config if service is running!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@VPNConfigAdapter
                            }
                            lifecycleScope.launch(Dispatchers.IO) {
                                oldViewModel.setConfig(id)
                                LocalVPNService2.currentConfig = oldViewModel.getConfig()
                                oldViewModel.getConfig()
                                    ?.let { updateConfigText(configName = it.name) }
                                withContext(Dispatchers.Main) {
                                    hideBottomSheetDialog()
                                }
                            }
                        }
                        ConfigurationClickHandlers.QRCode -> CoroutineScope(Dispatchers.IO).launch {
                            showQRCode(
                                id
                            )
                        }
                        ConfigurationClickHandlers.Edit -> openConfigEditingFragment(id)
                        ConfigurationClickHandlers.Share -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                val config = VPNConfigRepository()
                                    .getConfig(id)
                                val gson =
                                    GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                                shareConfiguration(gson.toJson(config))
                            }
                        }
                        ConfigurationClickHandlers.Like -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                val config = VPNConfigRepository()
                                    .getConfig(id)
                                if (config != null) {
                                    config.favorite = !config.favorite
                                }
                                if (config != null) {
                                    oldViewModel.updateConfig(config)
                                }
                            }
                        }
                    }
                }
                rvAllConfigs.adapter = adapter
                if (adapter.itemCount == 0)
                    tvNoData?.visibility = View.VISIBLE
                else
                    tvNoData?.visibility = View.GONE

            }
        else {
            lifecycleScope.launch(Dispatchers.IO) {
                val result = oldViewModel.getConfigByName(filter)
                if (result != null) {
                    withContext(Dispatchers.Main) {
                        val data = mutableListOf(result)
                        val adapter =
                            VPNConfigAdapter(data) { id, code ->
                                when (code) {
                                    ConfigurationClickHandlers.GetConfiguration -> showConfigDetails(
                                        id
                                    )
                                    ConfigurationClickHandlers.SetConfiguration -> {
                                        if (connection.isBound) {
                                            Toast.makeText(
                                                context,
                                                "Cannot set config if service is running!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            return@VPNConfigAdapter
                                        }
                                        lifecycleScope.launch(Dispatchers.IO) {
                                            oldViewModel.setConfig(id)
                                            LocalVPNService2.currentConfig =
                                                oldViewModel.getConfig()
                                            oldViewModel.getConfig()
                                                ?.let { updateConfigText(configName = it.name) }
                                            withContext(Dispatchers.Main) {
                                                hideBottomSheetDialog()
                                            }
                                        }
                                    }
                                    ConfigurationClickHandlers.QRCode -> CoroutineScope(Dispatchers.IO).launch {
                                        showQRCode(
                                            id
                                        )
                                    }
                                    ConfigurationClickHandlers.Edit -> openConfigEditingFragment(id)
                                    ConfigurationClickHandlers.Share -> {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val config = VPNConfigRepository()
                                                .getConfig(id)
                                            val gson =
                                                GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                                                    .create()
                                            shareConfiguration(gson.toJson(config))
                                        }
                                    }
                                    else -> showConfigDetails(id)
                                }
                            }
                        rvAllConfigs.adapter = adapter
                        if (adapter.itemCount == 0)
                            tvNoData?.visibility = View.VISIBLE
                        else
                            tvNoData?.visibility = View.GONE
                    }
                }
            }
        }

    }

    private fun openConfigEditingFragment(id: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val config = VPNConfigRepository()
                .getConfig(id)
            val gson = GsonBuilder().create()
            val bundle = Bundle()
            bundle.putString(
                "vpnConfig", gson.toJson(config)
            )
            withContext(Dispatchers.Main) {
                bottomSheetDialog.hide()
                view?.findNavController()?.navigate(
                    R.id.action_vpn_fragment_to_vpn_config_add_element,
                    bundle
                )
            }

        }


    }

    private suspend fun updateConfigText(configName: String) = withContext(Dispatchers.Main) {
        binding.tvMainConfigurationText.text = configName
    }


    private fun initBottomSheetDialog() {
        bottomSheetDialog =
            context?.let { BottomSheetDialog(it, R.style.AppBottomSheetDialogTheme) }!!
        bottomSheetDialog.setContentView(R.layout.vpn_bottom_sheet_dialog_layout)

        val tilAdProxyIp = bottomSheetDialog.findViewById<TextInputLayout>(R.id.tilAdProxyIp)

        val etProxyIp = bottomSheetDialog.findViewById<TextInputEditText>(R.id.etProxyIp)

        etProxyIp?.setOnEditorActionListener { _, actionId, _ ->
            val handled = false
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                setRecyclerViews(filter = etProxyIp.text.toString())
            }
            handled
        }
        tilAdProxyIp?.setEndIconOnClickListener {
            setRecyclerViews()
            etProxyIp?.setText("")
        }

        val viewFavorite = bottomSheetDialog.findViewById<CardView>(R.id.viewFavorite)
        val viewFavoriteImage = bottomSheetDialog.findViewById<ImageView>(R.id.ivFavorite)
        viewFavorite?.setOnClickListener {
            favoriteData = !favoriteData
            setRecyclerViews(favorite = favoriteData)
            if (favoriteData)
                viewFavoriteImage?.setImageResource(R.drawable.ic_favorite_full)
            else
                viewFavoriteImage?.setImageResource(R.drawable.ic_baseline_favorite)

        }

        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
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

    private suspend fun updateText() = withContext(Dispatchers.Main) {
        binding.tvIsConnected.text = "up: ${VPNFragment.upByte} B, down: ${VPNFragment.downByte} B"
    }

    private fun setListeners() {
        binding.cvSettingsIcon.setOnClickListener {
            showConfigDetails(oldViewModel.getConfigId())
        }

        binding.llCurrentConfiguration.setOnLongClickListener {
            showConfigDetails(oldViewModel.getConfigId())
            return@setOnLongClickListener false
        }

        binding.cvMainConfigurationCard.setOnClickListener {
            showBottomSheetDialog()
        }

        binding.ivEditIcon.setOnClickListener {
            openConfigEditingFragment(oldViewModel.getConfigId())
        }

        binding.ivShareIcon.setOnClickListener {
            context?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    val config = VPNConfigRepository()
                        .getConfig(oldViewModel.getConfigId())
                    val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                    shareConfiguration(gson.toJson(config))
                }
            }
        }

        binding.ivQrIcon.setOnClickListener {
            context?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    showQRCode(oldViewModel.getConfigId())
                }
            }
        }

        binding.cvVpnButtonBackground.setOnClickListener {
            when (oldViewModel.vpnStateOn.value) {
                VPNState.CONNECTED -> {
                    vpnButtonDisable()
                    oldViewModel.changeVpnState()
                }

                VPNState.NOT_CONNECTED -> {
                    vpnButtonConnected()
                    if (oldViewModel.isConnected.value == true)
                        oldViewModel.changeVpnState()
                }
                VPNState.ERROR -> vpnButtonDisable()
            }
        }
    }

    private fun setObservers() {
        oldViewModel.isConnected.observe(viewLifecycleOwner) { value ->
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

        oldViewModel.vpnStateOn.observe(viewLifecycleOwner) { value ->
            when (value) {
                VPNState.CONNECTED -> {
                    if (oldViewModel.isConnected.value == true)
                        startVpn()
                }
                VPNState.NOT_CONNECTED -> stopVpn()
                else -> stopVpn()
            }

        }
    }

    private fun analyzeNetworkState() {
        when (oldViewModel.vpnStateOn.value) {
            VPNState.CONNECTED -> vpnButtonConnected()
            VPNState.ERROR -> vpnButtonError()
            VPNState.NOT_CONNECTED -> vpnButtonDisable()
        }
    }

    private fun getVpnButtonColor(vpnButtonStateAttr: Int): Int {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(vpnButtonStateAttr, typedValue, true)
        return typedValue.data
    }

    private fun vpnButtonDisable() {
        binding.llConnectionStatus.visibility = View.GONE
        binding.imTurnOnVPN.setImageResource(R.drawable.ic_power)
        binding.tvIsConnected.text = getString(R.string.not_connected)
        binding.cvVpnButtonBackground.background.setTint(getVpnButtonColor(R.attr.vpnButtonDisabled))
    }

    private fun vpnButtonConnected() {
        // TODO: Remove Toasts
        when (oldViewModel.isConnected.value) {
            false ->
                Toast.makeText(requireContext(), "Error: connectivity is off!", Toast.LENGTH_SHORT)
                    .show()
            true -> {
                binding.llConnectionStatus.visibility = View.VISIBLE
                binding.imTurnOnVPN.setImageResource(R.drawable.ic_power)
                //binding.tvIsConnected.text = getString(R.string.connected)
                binding.cvVpnButtonBackground.background.setTint(getVpnButtonColor(R.attr.vpnButtonConnected))
            }
        }
    }

    private fun vpnButtonError() {
        binding.llConnectionStatus.visibility = View.GONE
        binding.imTurnOnVPN.setImageResource(R.drawable.ic_dino)
        binding.tvIsConnected.text = Utils.getString(R.string.error)
        binding.cvVpnButtonBackground.background.setTint(getVpnButtonColor(R.attr.vpnButtonError))
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
            context?.let { ContextCompat.startForegroundService(it, intent) }
        }
    }

    private fun stopVpn() {
        if (!connection.isBound)
            return
        activity?.unbindService(connection)
        connection.isBound = false
        val stopIntent = Intent(context, LocalVPNService2::class.java)
        stopIntent.action = "stop"
        context?.let { ContextCompat.startForegroundService(it, stopIntent) }
    }

    private fun showConfigDetails(config_id: Long) {
        val customAlertDialogView = LayoutInflater.from(context)
            .inflate(R.layout.config_details_dialog, null, false)
        CoroutineScope(Dispatchers.IO).launch {
            val config = VPNConfigRepository()
                .getConfig(config_id)
            withContext(Dispatchers.Main) {
                val materialAlertDialogBuilder = MaterialAlertDialogBuilder(context!!)
                materialAlertDialogBuilder.setView(customAlertDialogView)
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
                    .setView(customAlertDialogView)
                    .setTitle(config?.name)
                    .setMessage("Current configuration details")
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }.show()

            }
        }
    }

    private fun shareConfiguration(config: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, config)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun showQRCode(id: Long) {
        val config = VPNConfigRepository()
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

    override fun initViewModel() {
        viewModel = ViewModelProvider(
            this,
            VPNViewModelFactory(requireActivity().application)
        )[VPNViewModel::class.java]
        oldViewModel = viewModel as VPNViewModel
    }
}