package com.paranoid.vpn.app.common.vpn_configuration.domain.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.ArrayConverter
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.ForwardingRuleConverter
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.VPNConfigItem


@Database(entities = [VPNConfigItem::class], version = 1, exportSchema = false)
@TypeConverters(ArrayConverter::class, ForwardingRuleConverter::class)
abstract class VPNConfigDatabase : RoomDatabase() {

    abstract fun  VPNConfigDao(): VPNConfigDao

    companion object {

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