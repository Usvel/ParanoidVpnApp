package com.example.paranoid.ui.vpn

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.startForegroundService
import com.example.paranoid.R
import com.example.paranoid.databinding.NavigationVpnFragmentBinding
import com.example.paranoid.ui.base.BaseFragment
import com.example.paranoid.ui.vpn.basic_client.LocalVPNService
import com.example.paranoid.utils.Utils
import java.util.concurrent.atomic.AtomicLong
import androidx.localbroadcastmanager.content.LocalBroadcastManager




class VPNFragment :
    BaseFragment<NavigationVpnFragmentBinding>(NavigationVpnFragmentBinding::inflate) {

    private var vpnStateOn: Boolean = false

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
    }


    private fun vpnButtonConnected() {
        vpnStateOn = true
        binding.connectionStatus.visibility = View.VISIBLE
        binding.isConnected.text = getString(R.string.connected)
        binding.vpnButtonBackground.background.setTint(getVpnButtonColor(R.attr.vpnButtonConnected))
        startVpn()
    }


    private fun vpnButtonError() {
        binding.connectionStatus.visibility = View.GONE
        binding.isConnected.text = getString(R.string.error)
        binding.vpnButtonBackground.background.setTint(getVpnButtonColor(R.attr.vpnButtonError))
    }

    private val VPN_REQUEST_CODE = 0x0F
    private val TAG = "paranoid"

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val intent = Intent(context, LocalVPNService::class.java)
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
}
