package com.paranoid.vpn.app.vpn.ui

import android.app.Application
import android.content.Context
import android.net.*
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.paranoid.vpn.app.common.ui.base.BaseFragmentViewModel
import com.paranoid.vpn.app.common.utils.VPNState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VPNViewModel(
    application: Application
) :
    BaseFragmentViewModel() {
    private val connectivityManager =
        application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _vpnStateOn = MutableLiveData(VPNState.NOT_CONNECTED)
    val vpnStateOn: LiveData<VPNState> = _vpnStateOn

    private val _isConnected = MutableLiveData(isOnline())
    val isConnected: LiveData<Boolean> = _isConnected

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun getCurrentData() {
        networkCallback = getNetworkCallBack()
        connectivityManager.registerNetworkCallback(
            getNetworkRequest(),
            networkCallback!!
        )
    }

    private fun isOnline(): Boolean {
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) or
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) or
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            )
                return true
        }
        return false
    }

    fun changeVpnState() {
        when (vpnStateOn.value) {
            VPNState.CONNECTED ->
                _vpnStateOn.value = VPNState.NOT_CONNECTED
            VPNState.NOT_CONNECTED ->
                _vpnStateOn.value = VPNState.CONNECTED
            VPNState.ERROR -> {
                //TODO: Error state handling
            }
        }
    }

    private fun getNetworkCallBack(): ConnectivityManager.NetworkCallback {
        return object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)

                _isConnected.postValue(true)
            }

            override fun onLost(network: Network) {
                super.onLost(network)

                if (vpnStateOn.value == VPNState.CONNECTED)
                    viewModelScope.launch(Dispatchers.Main) {
                        changeVpnState()
                    }
                _isConnected.postValue(false)
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
}
