package com.paranoid.vpn.app.vpn.ui.vpn_pager.vpn

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.common.utils.VPNState
import com.paranoid.vpn.app.databinding.PageVpnButtonBinding
import com.paranoid.vpn.app.vpn.core.LocalVPNService2
import com.paranoid.vpn.app.vpn.ui.VPNFragment
import com.paranoid.vpn.app.vpn.ui.VPNServiceConnection
import com.paranoid.vpn.app.vpn.ui.VPNViewModel
import kotlinx.coroutines.*

class VPNObjectFragment(private val oldViewModel: VPNViewModel) : Fragment() {

    private var _binding: PageVpnButtonBinding? = null
    private val binding get() = _binding!!

    private var textUpdater: Job? = null
    private val VPN_REQUEST_CODE = 0x0F
    private lateinit var bottomSheetDialog: BottomSheetDialog

    /** Defines callbacks for service binding, passed to bindService()  */
    private var connection = VPNServiceConnection()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PageVpnButtonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        setObservers()
        analyzeNetworkState()

        textUpdater = lifecycleScope.launch(Dispatchers.Default) {
            while (true) {
                if (oldViewModel.vpnStateOn.value == VPNState.CONNECTED
                    && oldViewModel.isConnected.value == true
                )
                    updateText()
                delay(500)
            }
        }
    }


    private suspend fun updateText() = withContext(Dispatchers.Main) {
        binding.tvIsConnected.text = "up: ${VPNFragment.upByte} B, down: ${VPNFragment.downByte} B"
    }

    private fun setListeners() {
        binding.cvVpnButtonBackground.setOnClickListener {
            when (oldViewModel.vpnStateOn?.value) {
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
}