package com.paranoid.vpn.app.common.ad_block_configuration.domain.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.paranoid.vpn.app.common.ad_block_configuration.domain.database.IpDatabase
import com.paranoid.vpn.app.common.ad_block_configuration.domain.model.AdBlockIpItem


class IpRepository(application: Application) {
    private val ipDatabase = IpDatabase.getInstance()
    private val ipDao = ipDatabase.IpDao()
    val readAllData: LiveData<List<AdBlockIpItem>> = ipDao.getAll()


    fun getConfig(id: Long) = ipDao.getById(id)

    suspend fun addProxy(proxyItem: AdBlockIpItem) {
        ipDao.insert(proxyItem)
    }

    suspend fun updateProxy(proxyItem: AdBlockIpItem) {
        ipDao.update(proxyItem)
    }

    suspend fun deleteProxy(proxyItem: AdBlockIpItem) {
        ipDao.delete(proxyItem)
    }

    suspend fun deleteAllIps() {
        ipDao.deleteAllIps()
    }

    suspend fun insert(proxyItem: AdBlockIpItem) {
        ipDao.insert(proxyItem)
    }

}