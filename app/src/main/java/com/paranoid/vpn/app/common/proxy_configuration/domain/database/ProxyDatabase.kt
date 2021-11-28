package com.paranoid.vpn.app.common.proxy_configuration.domain.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.paranoid.vpn.app.common.proxy_configuration.domain.model.LocationConverter
import com.paranoid.vpn.app.common.proxy_configuration.domain.model.ProxyDataGenerator
import com.paranoid.vpn.app.common.proxy_configuration.domain.model.ProxyItem
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.ArrayConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch


@Database(entities = [ProxyItem::class], version = 1, exportSchema = false)
@TypeConverters(ArrayConverter::class, LocationConverter::class)
abstract class ProxyDatabase : RoomDatabase() {

    abstract fun ProxyDao(): ProxyDao

    companion object {

        private var INSTANCE: ProxyDatabase? = null


        fun setInstance(context: Context): ProxyDatabase? {
            if (INSTANCE == null) {
                CoroutineScope(IO).launch {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            ProxyDatabase::class.java, "proxy_database"
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
        fun getInstance(): ProxyDatabase {
            return INSTANCE!!
        }

        private fun populateDatabase(db: ProxyDatabase) {
            val proxyDao = db.ProxyDao()
            CoroutineScope(IO).launch {
                proxyDao.insert(ProxyDataGenerator.getProxyItem())
            }
        }
    }
}

