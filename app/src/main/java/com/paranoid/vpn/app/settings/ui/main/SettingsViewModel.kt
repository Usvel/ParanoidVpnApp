package com.paranoid.vpn.app.settings.ui.main

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.paranoid.vpn.app.common.proxy_configuration.domain.model.ProxyItem
import com.paranoid.vpn.app.common.proxy_configuration.domain.repository.ProxyRepository
import com.paranoid.vpn.app.common.ui.base.BaseFragmentViewModel
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.VPNConfigItem
import com.paranoid.vpn.app.common.vpn_configuration.domain.repository.VPNConfigRepository

class SettingsViewModel(application: Application) : BaseFragmentViewModel() {
    private val vpnConfigRepository: VPNConfigRepository = VPNConfigRepository()
    private val proxyRepository: ProxyRepository = ProxyRepository()

    private val allConfigs = vpnConfigRepository.readAllData
    private val allProxies = proxyRepository.readAllData

    private val _configsLiveData = MutableLiveData<List<VPNConfigItem>>()
    val configsLiveData: LiveData<List<VPNConfigItem>> = _configsLiveData

    override fun getCurrentData() {
        // TODO
    }

    fun getAllConfigs(): LiveData<List<VPNConfigItem>> {
        return allConfigs
    }

    fun getAllProxies(): LiveData<List<ProxyItem>> {
        return allProxies
    }

    suspend fun insertConfigToDataBase(vpnConfigItem: VPNConfigItem){
        vpnConfigRepository.insert(vpnConfigItem)
    }
}
