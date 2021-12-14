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
import androidx.cardview.widget.CardView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.GsonBuilder
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.common.utils.ConfigurationClickHandlers
import com.paranoid.vpn.app.common.utils.DebouncedOnClickListener
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.common.utils.VPNState
import com.paranoid.vpn.app.common.vpn_configuration.domain.repository.VPNConfigRepository
import com.paranoid.vpn.app.databinding.PageVpnButtonBinding
import com.paranoid.vpn.app.qr.QRCreator
import com.paranoid.vpn.app.vpn.core.LocalVPNService2
import com.paranoid.vpn.app.vpn.ui.*
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.Collectors


class VPNObjectFragment :
    BaseFragment<PageVpnButtonBinding, VPNViewModel>(PageVpnButtonBinding::inflate) {

    private var textUpdater: Job? = null
    private val VPN_REQUEST_CODE = 0x0F
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var oldViewModel: VPNViewModel
    private var favoriteData = false

    /** Defines callbacks for service binding, passed to bindService()  */
    private var connection = VPNServiceConnection()

    companion object {
        @JvmStatic
        var downByte: AtomicLong = AtomicLong(0)

        @JvmStatic
        var upByte: AtomicLong = AtomicLong(0)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewModel()
        loadMainConfiguration()
        setListeners()
        initBottomSheetDialog()
        setRecyclerViews()
        setObservers()
        analyzeNetworkState()

        if (arguments?.isEmpty != true) {
            arguments?.getBoolean("turnOnVPN")?.let {
                if(it)
                    oldViewModel.changeVpnState()
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            oldViewModel.getConfig()?.let {
                updateConfigText(configName = it.name)
                withContext(Dispatchers.Main) {
                    binding.ivEditIcon.visibility = View.VISIBLE
                    binding.ivShareIcon.visibility = View.VISIBLE
                    binding.ivQrIcon.visibility = View.VISIBLE
                }
            }
            if (oldViewModel.getConfig() == null)
                withContext(Dispatchers.Main) {
                    binding.ivEditIcon.visibility = View.GONE
                    binding.ivShareIcon.visibility = View.GONE
                    binding.ivQrIcon.visibility = View.GONE
                }
        }
        textUpdater = lifecycleScope.launch(Dispatchers.Default) {
            while (true) {
                if (oldViewModel.vpnStateOn.value == VPNState.CONNECTED) {
                    updateText()
                    withContext(Dispatchers.Main) {
                        binding.llConnectionStatus.visibility = View.VISIBLE
                    }
                }
                else
                    withContext(Dispatchers.Main) {
                        binding.llConnectionStatus.visibility = View.GONE
                    }
                delay(500)
            }
        }
    }

    private fun setRecyclerViews(filter: String? = null, favorite: Boolean = false) {

        val rvAllConfigs = bottomSheetDialog.findViewById<RecyclerView>(R.id.rvAllConfigs)
        val tvNoData = bottomSheetDialog.findViewById<TextView>(R.id.tvNoData)

        rvAllConfigs!!.layoutManager = LinearLayoutManager(context)
        if ((filter == null) || (filter == "")) {
            oldViewModel.getAllConfigs(favorite).observe(viewLifecycleOwner) { value ->
                val adapter = VPNConfigAdapter(value) { id, code ->
                    when (code) {
                        ConfigurationClickHandlers.GetConfiguration -> showConfigDetails(id)
                        ConfigurationClickHandlers.SetConfiguration -> {
                            lifecycleScope.launch(Dispatchers.IO) {
                                oldViewModel.setConfig(id)
                                if (connection.isBound)
                                    setVpnConfig()
                                oldViewModel.getConfig()
                                    ?.let { updateConfigText(configName = it.name) }
                                withContext(Dispatchers.Main) {
                                    hideBottomSheetDialog()
                                }
                            }
                        }
                        ConfigurationClickHandlers.QRCode -> CoroutineScope(Dispatchers.IO).launch {
                            showQRCode(id)
                        }
                        ConfigurationClickHandlers.Share -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                val config = VPNConfigRepository()
                                    .getConfig(id)
                                val gson =
                                    GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                                shareConfiguration(gson.toJson(config))
                            }
                        }
                        ConfigurationClickHandlers.Edit -> openConfigEditingFragment(id)
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
        } else {
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
                                        lifecycleScope.launch(Dispatchers.IO) {
                                            oldViewModel.setConfig(id)
                                            if (connection.isBound)
                                                setVpnConfig()
                                            oldViewModel.getConfig()?.let { updateConfigText(configName = it.name) }
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

        if (arguments?.isEmpty != true) {
            arguments?.getBoolean("favorite")?.let {
                if(it) {
                    favoriteData = true
                    setRecyclerViews(favorite = favoriteData)
                    viewFavoriteImage?.setImageResource(R.drawable.ic_favorite_full)
                    bottomSheetDialog.show()
                }
            }
        }
    }


    private fun loadMainConfiguration() {
        binding.llConnectionStatus.visibility = View.GONE
        binding.tvMainConfigurationText.text = Utils.getString(R.string.test_first_configuration)
        binding.cvMainConfigurationCard.setOnClickListener {
            val navBar =
                activity?.findViewById<BottomNavigationView>(R.id.bottom_tab_bar)
            Utils.makeSnackBar(
                binding.root,
                Utils.getString(R.string.test_first_configuration),
                navBar
            )
        }
    }

    private suspend fun updateText() = withContext(Dispatchers.Main) {
        binding.tvIsConnected.text =
            resources.getString(
                R.string.connection_speed,
                Utils.convertToStringRepresentation(upByte.toLong()),
                Utils.convertToStringRepresentation(downByte.toLong())
            )

        when {
            (upByte.toLong() < 1024) or (downByte.toLong() < 1024) -> {
                binding.ivWifiIcon.setImageResource(R.drawable.ic_bad_connection)
                binding.viewWifiIcon.background.setTint(getVpnButtonColor(R.attr.vpnButtonWarning))
                binding.tvConnectionQuality.text = Utils.getString(R.string.bad_connection)
            }
            (upByte.toLong() < 10) or (downByte.toLong() < 10) -> {
                binding.ivWifiIcon.setImageResource(R.drawable.ic_no_connection)
                binding.viewWifiIcon.background.setTint(getVpnButtonColor(R.attr.vpnButtonError))
                binding.tvConnectionQuality.text = Utils.getString(R.string.no_connection)
            }
            else -> {
                binding.viewWifiIcon.background.setTint(getVpnButtonColor(R.attr.iconBackground))
                binding.ivWifiIcon.setImageResource(R.drawable.ic_stable_connection)
                binding.tvConnectionQuality.text = Utils.getString(R.string.stable_connection)
            }
        }

    }

    private fun setListeners() {
        binding.cvSettingsIcon.setOnClickListener {
            showConfigDetails(oldViewModel.getConfigId())
        }

        binding.llCurrentConfiguration.setOnLongClickListener {
            showConfigDetails(oldViewModel.getConfigId())
            return@setOnLongClickListener false
        }

        val debouncedOnClickListener = DebouncedOnClickListener {
            showBottomSheetDialog()
        }

        binding.cvMainConfigurationCard.setOnClickListener(debouncedOnClickListener)

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
            oldViewModel.changeVpnState()
        }
    }

    private fun setObservers() {
        oldViewModel.vpnStateOn.observe(viewLifecycleOwner) { value ->
            when (value) {
                VPNState.CONNECTED -> {
                    vpnButtonConnected()
                    startVpn()
                }
                VPNState.NOT_CONNECTED -> {
                    vpnButtonDisable()
                    stopVpn()
                }
                else -> {
                    vpnButtonDisable()
                    stopVpn()
                }
            }
        }
    }

    private fun analyzeNetworkState() {
        when (oldViewModel.vpnStateOn.value) {
            VPNState.CONNECTED -> vpnButtonConnected()
            VPNState.ERROR -> vpnButtonError()
            VPNState.NOT_CONNECTED -> vpnButtonDisable()
            else -> vpnButtonDisable()
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
        binding.llConnectionStatus.visibility = View.VISIBLE
        binding.imTurnOnVPN.setImageResource(R.drawable.ic_power)
        //binding.tvIsConnected.text = getString(R.string.connected)
        binding.cvVpnButtonBackground.background.setTint(getVpnButtonColor(R.attr.vpnButtonConnected))
    }

    private fun vpnButtonError() {
        binding.llConnectionStatus.visibility = View.GONE
        binding.imTurnOnVPN.setImageResource(R.drawable.ic_dino)
        binding.tvIsConnected.text = Utils.getString(R.string.error)
        binding.cvVpnButtonBackground.background.setTint(getVpnButtonColor(R.attr.vpnButtonError))
    }

    private fun setVpnConfig() {
        val config = oldViewModel.getConfig().let {
            val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
            gson.toJson(it)
        }
        val configIntent = Intent(context, LocalVPNService2::class.java).apply {
            action = "config"
            putExtra(Intent.EXTRA_TEXT, config)
            type = "text/plain"
        }
        context?.let { ContextCompat.startForegroundService(it, configIntent) }
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
            CoroutineScope(Dispatchers.IO).launch {
                val config = oldViewModel.getConfig().let {
                    val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                    return@let gson.toJson(it)
                }
                val intent = Intent(context, LocalVPNService2::class.java).apply {
                    action = "start"
                    putExtra("config", config)
                    type = "text/plain"
                }
                withContext(Dispatchers.Main) {
                    context?.let { ContextCompat.startForegroundService(it, intent) }
                }
            }


        }
    }

    private fun stopVpn() {
        if (!connection.isBound)
            return
        activity?.unbindService(connection)
        connection.isBound = false
        downByte.set(0)
        upByte.set(0)
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