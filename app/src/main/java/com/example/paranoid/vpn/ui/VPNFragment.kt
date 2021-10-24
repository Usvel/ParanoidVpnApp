package com.example.paranoid.vpn.ui

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import com.example.paranoid.R
import com.example.paranoid.common.ui.base.BaseFragment
import com.example.paranoid.common.utils.Utils
import com.example.paranoid.common.utils.VPNState
import com.example.paranoid.databinding.NavigationVpnFragmentBinding

class VPNFragment :
    BaseFragment<NavigationVpnFragmentBinding>(NavigationVpnFragmentBinding::inflate) {

    private var vpnStateOn: VPNState = VPNState.NOT_CONNECTED

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Just for test
        loadMainConfiguration()

        binding.vpnButtonBackground.setOnClickListener {
            when (vpnStateOn) {
                VPNState.CONNECTED -> vpnButtonDisable()
                VPNState.NOT_CONNECTED -> vpnButtonConnected()
                VPNState.ERROR -> vpnButtonDisable()
            }
        }

        binding.helpButton.setOnClickListener {
            context?.let { context_ ->
                Utils.makeToast(
                    context_,
                    getString(R.string.help_info)
                )
            }
        }

        binding.shareIcon.setOnClickListener {
            context?.let { context_ ->
                Utils.makeToast(
                    context_,
                    getString(R.string.share_configuration)
                )
            }
        }

        binding.qrIcon.setOnClickListener {
            context?.let { context_ ->
                Utils.makeToast(
                    context_,
                    getString(R.string.scan_qr_code)
                )
            }
        }
    }

    private fun loadMainConfiguration() {
        binding.mainConfigurationText.text = getString(R.string.test_first_configuration)
        binding.mainConfigurationCard.setOnClickListener {
            context?.let { context_ ->
                Utils.makeToast(
                    context_,
                    getString(R.string.test_first_configuration)
                )
            }
        }
    }

    private fun getVpnButtonColor(vpnButtonStateAttr: Int): Int {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(vpnButtonStateAttr, typedValue, true)
        return typedValue.data
    }

    private fun vpnButtonDisable() {
        vpnStateOn = VPNState.NOT_CONNECTED
        binding.turnOnVPN.setImageResource(R.drawable.ic_power)
        binding.connectionStatus.visibility = View.GONE
        binding.isConnected.text = getString(R.string.not_connected)
        binding.vpnButtonBackground.background.setTint(getVpnButtonColor(R.attr.vpnButtonDisabled))
    }

    private fun vpnButtonConnected() {
        vpnStateOn = VPNState.CONNECTED
        binding.turnOnVPN.setImageResource(R.drawable.ic_power)
        binding.connectionStatus.visibility = View.VISIBLE
        binding.isConnected.text = getString(R.string.connected)
        binding.vpnButtonBackground.background.setTint(getVpnButtonColor(R.attr.vpnButtonConnected))
    }

    private fun vpnButtonError() {
        vpnStateOn = VPNState.ERROR
        binding.turnOnVPN.setImageResource(R.drawable.ic_dino)
        binding.connectionStatus.visibility = View.GONE
        binding.isConnected.text = getString(R.string.error)
        binding.vpnButtonBackground.background.setTint(getVpnButtonColor(R.attr.vpnButtonError))
    }

    override fun initViewModule() {
        // TODO
    }
}
