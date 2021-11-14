package com.paranoid.vpn.app.common.vpn_configuration.domain.repository

import androidx.lifecycle.LiveData
import com.paranoid.vpn.app.common.vpn_configuration.domain.database.VPNConfigDao
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.VPNConfigItem


class ConfigRepository(private val vpnConfigDao: VPNConfigDao) {
    val readAllData : LiveData<List<VPNConfigItem>> =  vpnConfigDao.getAll()

    fun getConfig(id: Long): VPNConfigItem? = vpnConfigDao.getById(id)

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

}