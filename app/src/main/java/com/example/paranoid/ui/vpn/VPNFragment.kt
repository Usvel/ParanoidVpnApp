package com.example.paranoid.ui.vpn

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.*
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat.startForegroundService
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.paranoid.R
import com.example.paranoid.databinding.NavigationVpnFragmentBinding
import com.example.paranoid.ui.base.BaseFragment
import com.example.paranoid.ui.vpn.basic_client.LocalVPNService2
import com.example.paranoid.utils.Utils
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.properties.Delegates
import androidx.core.content.ContextCompat.getSystemService

import android.app.ActivityManager
import androidx.core.content.ContextCompat


class VPNFragment :
    BaseFragment<NavigationVpnFragmentBinding>(NavigationVpnFragmentBinding::inflate) {

    private var vpnStateOn: Boolean = false

    val networkCallback = getNetworkCallBack()
    val networkReq = getNetworkRequest()

    private var isConnected = false

    private val VPN_REQUEST_CODE = 0x0F

    private var textUpdater: Job? = null

    //TODO: registerNetworkCallback on MainActivity
    private fun getConnectivityManager() =
        requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

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
            when (vpnStateOn) {
                true -> {
                    vpnButtonDisable()
                    changeVpnState()
                }
                false -> {
                    vpnButtonConnected()
                    if (isConnected)
                        changeVpnState()
                }
            }
        }
        textUpdater = lifecycleScope.launch(Dispatchers.Default) {
            while (true) {
                if (vpnStateOn)
                    updateText()
                delay(500)
            }
        }

        isConnected = isOnline()

        getConnectivityManager().registerNetworkCallback(networkReq, networkCallback)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("vpnStateOn", vpnStateOn)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            vpnStateOn = savedInstanceState.getBoolean("vpnStateOn")
            when (vpnStateOn) {
                true -> vpnButtonConnected()
                false ->vpnButtonDisable()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        lifecycleScope.launch(Dispatchers.Default) {
            textUpdater?.cancel()
        }
        getConnectivityManager().unregisterNetworkCallback(networkCallback)
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

    private fun isOnline(): Boolean {
        val capabilities =
            getConnectivityManager().getNetworkCapabilities(getConnectivityManager().activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) or
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) or
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            )
                return true
        }
        return false
    }

    private fun getNetworkCallBack(): ConnectivityManager.NetworkCallback {
        return object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)

                Toast.makeText(requireContext(), "Connectivity is on!", Toast.LENGTH_SHORT)
                    .show()
                if (vpnStateOn)
                    startVpn()
                isConnected = true
            }

            override fun onLost(network: Network) {
                super.onLost(network)

                Toast.makeText(requireContext(), "Connectivity is off!", Toast.LENGTH_SHORT)
                    .show()
                if (vpnStateOn)
                    stopVpn()
                isConnected = false
            }
        }
    }

    private fun getNetworkRequest(): NetworkRequest {
        return NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .build()
    }

    private suspend fun updateText() = withContext(Dispatchers.Main) {
        binding.isConnected.text = "up: $upByte B, down: $downByte B"
    }

    private fun vpnButtonConnected() {
        // TODO: Remove Toasts
        if (!isConnected) {
            Toast.makeText(requireContext(), "Error: connectivity is off!", Toast.LENGTH_SHORT)
                .show()
            return
        }
        binding.connectionStatus.visibility = View.VISIBLE
        //binding.isConnected.text = getString(R.string.connected)
        binding.vpnButtonBackground.background.setTint(getVpnButtonColor(R.attr.vpnButtonConnected))
    }

    private fun changeVpnState() {
        when (vpnStateOn) {
            true -> {
                vpnStateOn = false
                stopVpn()
            }
            false -> {
                vpnStateOn = true
                startVpn()
            }
        }
    }

    private fun vpnButtonDisable() {
        binding.connectionStatus.visibility = View.GONE
        binding.isConnected.text = getString(R.string.not_connected)
        binding.vpnButtonBackground.background.setTint(getVpnButtonColor(R.attr.vpnButtonDisabled))
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