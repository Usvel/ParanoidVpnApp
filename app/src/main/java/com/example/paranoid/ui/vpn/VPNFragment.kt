package com.example.paranoid.ui.vpn

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.compose.runtime.sourceInformationMarkerEnd
import androidx.core.content.ContextCompat.startForegroundService
import androidx.lifecycle.lifecycleScope
import com.example.paranoid.R
import com.example.paranoid.databinding.NavigationVpnFragmentBinding
import com.example.paranoid.ui.base.BaseFragment
import com.example.paranoid.utils.Utils
import java.util.concurrent.atomic.AtomicLong
import com.example.paranoid.ui.vpn.basic_client.LocalVPNService2
import kotlinx.coroutines.*


class VPNFragment :
    BaseFragment<NavigationVpnFragmentBinding>(NavigationVpnFragmentBinding::inflate) {

    private var vpnStateOn: Boolean = false

    private val VPN_REQUEST_CODE = 0x0F

    private var textUpdater: Job? = null

    companion object {
        @JvmStatic
        var downByte: AtomicLong = AtomicLong(0)
        @JvmStatic
        var upByte: AtomicLong = AtomicLong(0)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Just for test
        loadMainConfiguration()

        binding.vpnButtonBackground.setOnClickListener {
            when(vpnStateOn){
                true -> vpnButtonDisable()
                false -> vpnButtonConnected()
            }
        }
        textUpdater = lifecycleScope.launch(Dispatchers.Default) {
            while (true) {
                if (vpnStateOn)
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

    private fun loadMainConfiguration() {
        binding.mainConfigurationText.text = getString(R.string.test_first_configuration)
        binding.mainConfigurationCard.setOnClickListener {
            context?.let { context_ ->
                Utils.makeToast(context_, getString(R.string.test_first_configuration))
            }
        }
    }

    private fun getVpnButtonColor(vpnButtonStateAttr: Int): Int {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(vpnButtonStateAttr, typedValue, true)
        return typedValue.data
    }

    private fun vpnButtonDisable() {
        vpnStateOn = false
        binding.connectionStatus.visibility = View.GONE
        binding.isConnected.text = getString(R.string.not_connected)
        binding.vpnButtonBackground.background.setTint(getVpnButtonColor(R.attr.vpnButtonDisabled))
        stopVpn()
    }

    private suspend fun updateText() = withContext(Dispatchers.Main) {
        binding.isConnected.text = "up: $upByte B, down: $downByte B"
    }

    private fun vpnButtonConnected() {
        vpnStateOn = true
        binding.connectionStatus.visibility = View.VISIBLE
        // binding.isConnected.text = getString(R.string.connected)
        binding.vpnButtonBackground.background.setTint(getVpnButtonColor(R.attr.vpnButtonConnected))
        startVpn()
    }


    private fun vpnButtonError() {
        binding.connectionStatus.visibility = View.GONE
        binding.isConnected.text = getString(R.string.error)
        binding.vpnButtonBackground.background.setTint(getVpnButtonColor(R.attr.vpnButtonError))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val intent = Intent(context, LocalVPNService2::class.java)
            intent.action = "start"
            context?.let { startForegroundService(it, intent) }
        }
    }

    private fun startVpn() {
        val vpnIntent = VpnService.prepare(context)
        if (vpnIntent != null) startActivityForResult(
            vpnIntent,
            VPN_REQUEST_CODE
        ) else onActivityResult(VPN_REQUEST_CODE, Activity.RESULT_OK, null)
    }

    private fun stopVpn() {
        val stopIntent = Intent(context, LocalVPNService2::class.java)
        stopIntent.action = "stop"
        context?.let { startForegroundService(it, stopIntent) }
    }

}
