package com.paranoid.vpn.app.common.proxy_configuration.domain.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.paranoid.vpn.app.common.proxy_configuration.domain.model.ProxyItem


@Dao
interface ProxyDao {
    @Query("SELECT * from proxy_item")
    fun getAll(): LiveData<List<ProxyItem>>

    @Query("SELECT * FROM proxy_item WHERE id = :id")
    fun getById(id: Long): ProxyItem?

    @Insert
    suspend fun insert(proxy: ProxyItem?)

    @Update
    suspend fun update(proxy: ProxyItem?)

    @Delete
    suspend fun delete(proxy: ProxyItem?)

    @Query("DELETE FROM proxy_item")
    suspend fun deleteAllProxies()
}
