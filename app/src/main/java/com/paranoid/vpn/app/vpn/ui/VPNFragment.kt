package com.paranoid.vpn.app.vpn.ui

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.ConnectivityManager
import android.net.VpnService
import android.os.Bundle
import android.os.IBinder
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat.startForegroundService
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.common.utils.VPNState
import com.paranoid.vpn.app.common.vpn_configuration.domain.database.VPNConfigDatabase
import com.paranoid.vpn.app.databinding.NavigationVpnFragmentBinding
import com.paranoid.vpn.app.vpn.core.LocalVPNService2
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicLong

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

    /** Defines callbacks for service binding, passed to bindService()  */
    private var mBound: Boolean = false
    private lateinit var mService: LocalVPNService2
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as LocalVPNService2.LocalBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onStart() {
        super.onStart()

        Intent(context, LocalVPNService2::class.java).also { intent ->
            activity?.bindService(intent, connection, 0)
        }
    }


    override fun onStop() {
        super.onStop()

        activity?.unbindService(connection)
        mBound = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Just for test
        loadMainConfiguration()
        setListeners()
        setObservers()

        CoroutineScope(Dispatchers.IO).launch {
            val db = context?.let { VPNConfigDatabase.getInstance() }
            val vpnConfigDao = db?.VPNConfigDao()
            val config = vpnConfigDao?.getById(1L)
            if (config != null) {
                updateConfigText(configName = config.name)
            }
        }

        textUpdater = lifecycleScope.launch(Dispatchers.Default) {
            while (true) {
                if (viewModel.vpnStateOn.value == VPNState.CONNECTED
                    && viewModel.isConnected.value == true
                )
                    updateText()
                delay(500)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        lifecycleScope.launch(Dispatchers.Default) {
            textUpdater?.cancel()
        }
    }

    private suspend fun updateText() = withContext(Dispatchers.Main) {
        binding.isConnected.text = "up: $upByte B, down: $downByte B"
    }

    private suspend fun updateConfigText(configName: String) = withContext(Dispatchers.Main) {
        binding.mainConfigurationText.text = configName
    }

    private fun loadMainConfiguration() {
        binding.mainConfigurationText.text = Utils.getString(R.string.test_first_configuration)
        binding.mainConfigurationCard.setOnClickListener {
            context?.let { context_ ->
                Utils.makeToast(
                    context_,
                    Utils.getString(R.string.test_first_configuration)
                )
            }
        }
    }

    private fun setListeners() {
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

        binding.shareIcon.setOnClickListener {
            context?.let { context_ ->
                Utils.makeToast(
                    context_,
                    Utils.getString(R.string.share_configuration)
                )
            }
        }

        binding.qrIcon.setOnClickListener {
            context?.let { context_ ->
                CoroutineScope(Dispatchers.IO).launch {
                    val db = context?.let { VPNConfigDatabase.getInstance() }
                    val vpnConfigDao = db?.VPNConfigDao()
                    val config = vpnConfigDao?.getById(1L)
                }
                Utils.makeToast(
                    context_,
                    Utils.getString(R.string.scan_qr_code)
                )
            }
        }
    }

    private fun setObservers() {
        viewModel.isConnected.observe(viewLifecycleOwner) { value ->
            when (value) {
                false -> {
                    vpnButtonDisable()
                    if (viewModel.vpnStateOn.value == VPNState.NOT_CONNECTED)
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
                VPNState.NOT_CONNECTED -> {
                    if (viewModel.isConnected.value == true)
                        stopVpn()
                }
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
        if (mBound)
            return
        val vpnIntent = VpnService.prepare(context)
        if (vpnIntent != null) startActivityForResult(
            vpnIntent,
            VPN_REQUEST_CODE
        ) else onActivityResult(VPN_REQUEST_CODE, Activity.RESULT_OK, null)
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
        if (!mBound)
            return
        val stopIntent = Intent(context, LocalVPNService2::class.java)
        stopIntent.action = "stop"
        context?.let { startForegroundService(it, stopIntent) }
    }

    private fun getConnectivityManager() =
        requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun initViewModel() {
        viewModel = ViewModelProvider(
            this,
            VPNViewModelFactory(requireActivity().application)
        )[VPNViewModel::class.java]
    }
}
