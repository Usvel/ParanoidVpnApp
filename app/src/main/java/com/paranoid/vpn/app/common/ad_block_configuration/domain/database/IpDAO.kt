package com.paranoid.vpn.app.common.ad_block_configuration.domain.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.paranoid.vpn.app.common.ad_block_configuration.domain.model.AdBlockIpItem


@Dao
interface IpDao {
    @Query("SELECT * FROM ad_ip_item")
    fun getAll(): LiveData<List<AdBlockIpItem>>

    @Query("SELECT COUNT(Ip) FROM ad_ip_item")
    fun getAllSize(): LiveData<Int>

    @Query("SELECT * FROM ad_ip_item WHERE NOT IsLocal")
    fun getAdded(): LiveData<List<AdBlockIpItem>>

    @Query("SELECT * FROM ad_ip_item WHERE id = :id")
    fun getById(id: Long): AdBlockIpItem?

    @Query("SELECT * FROM ad_ip_item WHERE Ip = :ip")
    fun getItemByIp(ip: String): AdBlockIpItem?

    @Query("SELECT COUNT(Ip) FROM ad_ip_item WHERE Ip = :ip")
    fun getByIp(ip: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(objects: List<AdBlockIpItem>)

    @Insert
    suspend fun insert(ipItem: AdBlockIpItem?)

    @Update
    suspend fun update(ipItem: AdBlockIpItem?)

    @Delete
    suspend fun delete(ipItem: AdBlockIpItem?)

    @Query("DELETE FROM ad_ip_item")
    suspend fun deleteAllIps()

    @Query("DELETE FROM ad_ip_item WHERE IsLocal")
    suspend fun deleteAllLocalIps()
}
