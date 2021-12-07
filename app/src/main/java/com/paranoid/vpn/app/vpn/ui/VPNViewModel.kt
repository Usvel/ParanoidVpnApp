package com.paranoid.vpn.app.vpn.ui

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.proxy_configuration.domain.model.ProxyItem
import com.paranoid.vpn.app.common.proxy_configuration.domain.repository.ProxyRepository
import com.paranoid.vpn.app.common.proxy_configuration.network.ProxyNetworkService
import com.paranoid.vpn.app.common.ui.base.BaseFragmentViewModel
import com.paranoid.vpn.app.common.utils.Utils.getString
import com.paranoid.vpn.app.common.utils.VPNState
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.VPNConfigItem
import com.paranoid.vpn.app.common.vpn_configuration.domain.repository.VPNConfigRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val DEFAULT_CONFIG_ID = 1L

class VPNViewModel(
    application: Application
) :
    BaseFragmentViewModel() {

    /////////////////////////////////  Repository part
    private val sharedPref: SharedPreferences = application.getSharedPreferences(
        getString(R.string.config_sp), Context.MODE_PRIVATE
    )
    private val vpnConfigRepository: VPNConfigRepository = VPNConfigRepository()
    private val proxyRepository: ProxyRepository = ProxyRepository()

    private var currentConfig: VPNConfigItem? = null
    private val allConfigs = vpnConfigRepository.readAllData
    private val allConfigsFavorite = vpnConfigRepository.readAllDataFavorite

    private var currentProxy: ProxyItem? = null
    private var allProxies = proxyRepository.readAllData

    private var allNetworkProxies: MutableLiveData<List<ProxyItem>> = MutableLiveData()


    private val proxyApi = ProxyNetworkService.instance?.getProxyApi()

    fun getConfigId(): Long {
        return sharedPref.getLong(
            getString(R.string.config_sp_tag_ID), DEFAULT_CONFIG_ID
        )
    }

    fun getProxyType(): String? {
        return sharedPref.getString(
            getString(R.string.proxy_type_sp_tag_ID), ""
        )
    }

    fun setProxyType(type: String) {
        with(sharedPref.edit()) {
            putString(getString(R.string.proxy_type_sp_tag_ID), type)
            apply()
        }
    }

    fun getProxyPing(): String? {
        return sharedPref.getString(
            getString(R.string.proxy_ping_sp_tag_ID), getString(R.string.possible_ping)
        )
    }

    fun setProxyPing(ping: String) {
        with(sharedPref.edit()) {
            putString(getString(R.string.proxy_ping_sp_tag_ID), ping)
            apply()
        }
    }

    fun getProxyCountry(): String? {
        return sharedPref.getString(
            getString(R.string.proxy_country_sp_tag_ID), getString(R.string.countries)
        )
    }

    fun setProxyCountry(country: String) {
        with(sharedPref.edit()) {
            putString(getString(R.string.proxy_country_sp_tag_ID), country)
            apply()
        }
    }

    fun getConfig(): VPNConfigItem? {
        if (currentConfig == null)
            currentConfig = vpnConfigRepository.getConfig(
                sharedPref.getLong(
                    getString(R.string.config_sp_tag_ID), DEFAULT_CONFIG_ID
                )
            )
        return currentConfig
    }

    fun getAllConfigs(favorite: Boolean = false): LiveData<List<VPNConfigItem>> {
        return if (favorite)
            allConfigsFavorite
        else
            allConfigs
    }

    fun getConfigByName(name: String): VPNConfigItem? {
        return vpnConfigRepository.getConfigByName(name)
    }

    suspend fun updateConfig(config: VPNConfigItem) {
        return vpnConfigRepository.updateConfig(config)
    }

    fun getAllProxies(): LiveData<List<ProxyItem>> {
        return allProxies
    }

    fun loadAllProxiesFromNetwork(
        country: String = "",
        ping: String = "",
        proxyType: String = "",
    ) {
        proxyApi?.getProxies(
            limit = 20,
            country = country,
            ping = ping,
            type = proxyType
        )
            ?.enqueue(object : Callback<List<ProxyItem>> {
                override fun onResponse(
                    call: Call<List<ProxyItem>>,
                    response: Response<List<ProxyItem>>
                ) {
                    val proxyItems: List<ProxyItem>? = response.body()
                    if (proxyItems != null) {
                        allNetworkProxies.value = proxyItems
                    }
                }

                override fun onFailure(call: Call<List<ProxyItem>>, t: Throwable) {
                    t.printStackTrace()
                }
            })
    }

    fun getAllProxiesFromNetwork(): LiveData<List<ProxyItem>> {
        return allNetworkProxies
    }


    fun setConfig(newId: Long) {
        with(sharedPref.edit()) {
            putLong(getString(R.string.config_sp_tag_ID), newId)
            apply()
        }
        currentConfig = vpnConfigRepository.getConfig(newId)
    }
    /////////////////////////////////

    private val _vpnStateOn = MutableLiveData(VPNState.NOT_CONNECTED)
    val vpnStateOn: LiveData<VPNState> = _vpnStateOn

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
}
