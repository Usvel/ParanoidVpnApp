package com.paranoid.vpn.app.settings.ui

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
    private val vpnConfigDatabase = application.applicationContext?.let { VPNConfigDatabase.getInstance() }
    val vpnConfigDao = vpnConfigDatabase?.VPNConfigDao()
    private val vpnConfigRepository: VPNConfigRepository = VPNConfigRepository(vpnConfigDao!!)

    private val _configsLiveData = MutableLiveData<List<VPNConfigItem>>()
    val configsLiveData: LiveData<List<VPNConfigItem>> = _configsLiveData

    override fun getCurrentData() {
        // TODO
    }

    fun getAllConfigs(email: String, password: String)= viewModelScope.launch{
        val configs = vpnConfigRepository.readAllData
        _configsLiveData.postValue(configs.value)

    }

    suspend fun insertConfigToDataBase(vpnConfigItem: VPNConfigItem){
        vpnConfigRepository.insert(vpnConfigItem)
    }
}
