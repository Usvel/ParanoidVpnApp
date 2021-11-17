package com.paranoid.vpn.app.settings.ui.main

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.paranoid.vpn.app.common.ui.base.BaseFragmentViewModel
import com.paranoid.vpn.app.common.vpn_configuration.domain.database.VPNConfigDatabase
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.VPNConfigItem
import com.paranoid.vpn.app.common.vpn_configuration.domain.repository.VPNConfigRepository
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : BaseFragmentViewModel() {
    private val vpnConfigRepository: VPNConfigRepository = VPNConfigRepository(application)

    private val allConfigs = vpnConfigRepository.readAllData

    private val _configsLiveData = MutableLiveData<List<VPNConfigItem>>()
    val configsLiveData: LiveData<List<VPNConfigItem>> = _configsLiveData

    override fun getCurrentData() {
        // TODO
    }

    fun getAllConfigs(): LiveData<List<VPNConfigItem>> {
        return allConfigs
    }

    suspend fun insertConfigToDataBase(vpnConfigItem: VPNConfigItem){
        vpnConfigRepository.insert(vpnConfigItem)
    }
}
