package com.paranoid.vpn.app.common.ad_block_configuration.domain.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.paranoid.vpn.app.common.ad_block_configuration.domain.model.AdBlockIpItem
import com.paranoid.vpn.app.common.proxy_configuration.domain.model.ProxyItem


@Dao
interface IpDao {
    @Query("SELECT * from ad_ip_item")
    fun getAll(): LiveData<List<AdBlockIpItem>>

    @Query("SELECT * FROM ad_ip_item WHERE id = :id")
    fun getById(id: Long): AdBlockIpItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(objects: List<AdBlockIpItem>)

    @Insert
    suspend fun insert(ipItem: AdBlockIpItem?)

    @Update
    suspend fun update(ipItem: AdBlockIpItem?)

    @Delete
    suspend fun delete(ipItem: AdBlockIpItem?)

    @Query("DELETE FROM ad_ip_item")
    suspend fun deleteAllIps()
}
