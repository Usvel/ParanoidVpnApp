package com.paranoid.vpn.app.common.vpn_configuration.domain.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.paranoid.vpn.app.common.vpn_configuration.domain.database.VPNConfigDatabase
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.VPNConfigItem


class VPNConfigRepository(application: Application) {
    private val vpnConfigDatabase = VPNConfigDatabase.getInstance()
    private val vpnConfigDao = vpnConfigDatabase.VPNConfigDao()
    val readAllData: LiveData<List<VPNConfigItem>> = vpnConfigDao.getAll()

    fun getConfig(id: Long) = vpnConfigDao.getById(id)

    suspend fun addConfig(configItem: VPNConfigItem) {
        vpnConfigDao.insert(configItem)
    }

    suspend fun updateConfig(configItem: VPNConfigItem) {
        vpnConfigDao.update(configItem)
    }
    suspend fun deleteConfig(configItem: VPNConfigItem) {
        vpnConfigDao.delete(configItem)
    }

    suspend fun deleteAllConfigs() {
        vpnConfigDao.deleteAllConfigs()
    }

    suspend fun insert(configItem: VPNConfigItem){
        vpnConfigDao.insert(configItem)
    }

}