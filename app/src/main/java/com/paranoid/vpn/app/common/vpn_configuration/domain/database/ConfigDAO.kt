package com.paranoid.vpn.app.common.vpn_configuration.domain.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.VPNConfigItem


@Dao
interface VPNConfigDao {
    @Query("SELECT * from config")
    fun getAll(): LiveData<List<VPNConfigItem>>

    @Query("SELECT * FROM config WHERE favorite = 1")
    fun getFavorite(): LiveData<List<VPNConfigItem>>

    @Query("SELECT * FROM config WHERE id = :id")
    fun getById(id: Long): VPNConfigItem?

    @Query("SELECT * FROM config WHERE name = :name")
    fun getByName(name: String): VPNConfigItem?

    @Insert
    suspend fun insert(VPNConfig: VPNConfigItem?)

    @Update
    suspend fun update(VPNConfig: VPNConfigItem?)

    @Delete
    suspend fun delete(VPNConfig: VPNConfigItem?)

    @Query("DELETE FROM config")
    suspend fun deleteAllConfigs()
}
