package com.paranoid.vpn.app.common.vpn_configuration.domain.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.VPNConfig


@Dao
interface VPNConfigDao {
    @Query("SELECT * from config")
    fun getAll(): LiveData<List<VPNConfig>>

    @Query("SELECT * FROM config WHERE id = :id")
    fun getById(id: Long): VPNConfig?

    @Insert
    suspend fun insert(VPNConfig: VPNConfig?)

    @Update
    suspend fun update(VPNConfig: VPNConfig?)

    @Delete
    suspend fun delete(VPNConfig: VPNConfig?)

    @Query("DELETE FROM config")
    suspend fun deleteAllConfigs()
}
