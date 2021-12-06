package com.paranoid.vpn.app.common.vpn_configuration.domain.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.ArrayConverter
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.ForwardingRuleConverter
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.VPNConfigDataGenerator
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.VPNConfigItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

@Database(entities = [VPNConfigItem::class], version = 2, exportSchema = false)
@TypeConverters(ArrayConverter::class, ForwardingRuleConverter::class)
abstract class VPNConfigDatabase : RoomDatabase() {

    abstract fun  VPNConfigDao(): VPNConfigDao

    companion object {

        private var INSTANCE: VPNConfigDatabase? = null


        fun setInstance(context: Context): VPNConfigDatabase? {
            if (INSTANCE == null) {
                CoroutineScope(IO).launch {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            VPNConfigDatabase::class.java, "vpn_config_database"
                        ).addCallback(object : RoomDatabase.Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                populateDatabase(INSTANCE!!)
                            }
                        })
                            .build()
                    }
                }
            }
            return INSTANCE
        }

        @Synchronized
        fun getInstance(): VPNConfigDatabase {
            return INSTANCE!!
        }

        private fun populateDatabase(db: VPNConfigDatabase) {
            val vpnConfigDao = db.VPNConfigDao()
            CoroutineScope(IO).launch {
                vpnConfigDao.insert(VPNConfigDataGenerator.getVPNConfigItem())
            }
        }
    }
}

