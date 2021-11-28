package com.paranoid.vpn.app.common.proxy_configuration.domain.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.paranoid.vpn.app.common.proxy_configuration.domain.database.ProxyDatabase
import com.paranoid.vpn.app.common.proxy_configuration.domain.model.ProxyItem


class ProxyRepository(application: Application) {
    private val proxyDatabase = ProxyDatabase.getInstance()
    private val proxyDao = proxyDatabase.ProxyDao()
    val readAllData: LiveData<List<ProxyItem>> = proxyDao.getAll()

    fun getConfig(id: Long) = proxyDao.getById(id)

    suspend fun addProxy(proxyItem: ProxyItem) {
        proxyDao.insert(proxyItem)
    }

    suspend fun updateProxy(proxyItem: ProxyItem) {
        proxyDao.update(proxyItem)
    }

    suspend fun deleteProxy(proxyItem: ProxyItem) {
        proxyDao.delete(proxyItem)
    }

    suspend fun deleteAllProxies() {
        proxyDao.deleteAllProxies()
    }

    suspend fun insert(proxyItem: ProxyItem) {
        proxyDao.insert(proxyItem)
    }

}