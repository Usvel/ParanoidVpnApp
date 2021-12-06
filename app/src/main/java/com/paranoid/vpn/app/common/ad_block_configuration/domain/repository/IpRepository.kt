package com.paranoid.vpn.app.common.ad_block_configuration.domain.repository

import androidx.lifecycle.LiveData
import com.paranoid.vpn.app.common.ad_block_configuration.domain.database.IpDatabase
import com.paranoid.vpn.app.common.ad_block_configuration.domain.model.AdBlockIpItem


class IpRepository {
    private val ipDatabase = IpDatabase.getInstance()
    private val ipDao = ipDatabase.IpDao()
    val readAllData: LiveData<List<AdBlockIpItem>> = ipDao.getAll()
    val readAddedData: LiveData<List<AdBlockIpItem>> = ipDao.getAdded()
    val allAddressCount: LiveData<Int> = ipDao.getAllSize()

    fun getConfig(id: Long) = ipDao.getById(id)

    fun getConfigCount(ip: String) = ipDao.getByIp(ip)

    suspend fun addIp(proxyItem: AdBlockIpItem) {
        ipDao.insert(proxyItem)
    }

    suspend fun updateIp(proxyItem: AdBlockIpItem) {
        ipDao.update(proxyItem)
    }

    suspend fun deleteItem(proxyItem: AdBlockIpItem) {
        ipDao.delete(proxyItem)
    }

    suspend fun deleteAllIps() {
        ipDao.deleteAllIps()
    }

    suspend fun deleteAllLocalIps() {
        ipDao.deleteAllLocalIps()
    }

    suspend fun insert(proxyItem: AdBlockIpItem) {
        ipDao.insert(proxyItem)
    }

}