package com.paranoid.vpn.app.common.vpn_configuration.domain.repository

import androidx.lifecycle.LiveData
import com.paranoid.vpn.app.common.vpn_configuration.domain.database.VPNConfigDao
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.VPNConfig


class ConfigRepository(private val vpnConfigDao: VPNConfigDao) {
    val readAllData : LiveData<List<VPNConfig>> =  vpnConfigDao.getAll()

    fun getConfig(id: Long): VPNConfig? = vpnConfigDao.getById(id)

    suspend fun addConfig(configItem: VPNConfig) {
        vpnConfigDao.insert(configItem)
    }

    suspend fun updateConfig(configItem: VPNConfig) {
        vpnConfigDao.update(configItem)
    }
    suspend fun deleteConfig(configItem: VPNConfig) {
        vpnConfigDao.delete(configItem)
    }

    suspend fun deleteAllConfigs() {
        vpnConfigDao.deleteAllConfigs()
    }

}