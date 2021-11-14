package com.paranoid.vpn.app.common.vpn_configuration.domain.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.paranoid.vpn.app.vpn.core.config.Config

@Database(entities = [Config::class], version = 1, exportSchema = false)
abstract class VPNConfigDatabase : RoomDatabase() {

    abstract val VPNConfigDao: VPNConfigDao

    companion object {

        @Volatile
        private var INSTANCE: VPNConfigDatabase? = null

        fun getInstance(context: Context): VPNConfigDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        VPNConfigDatabase::class.java,
                        "vpn_config_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}