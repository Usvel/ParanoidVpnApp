package com.paranoid.vpn.app.common.ad_block_configuration.domain.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.paranoid.vpn.app.common.ad_block_configuration.domain.model.AdBlockIpDataGenerator
import com.paranoid.vpn.app.common.ad_block_configuration.domain.model.AdBlockIpItem
import com.paranoid.vpn.app.common.proxy_configuration.domain.model.ProxyDataGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch


@Database(entities = [AdBlockIpItem::class], version = 1, exportSchema = false)
abstract class IpDatabase : RoomDatabase() {

    abstract fun IpDao(): IpDao

    companion object {

        private var INSTANCE: IpDatabase? = null


        fun setInstance(context: Context): IpDatabase? {
            if (INSTANCE == null) {
                CoroutineScope(IO).launch {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            IpDatabase::class.java, "ip_database"
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
        fun getInstance(): IpDatabase {
            return INSTANCE!!
        }

        private fun populateDatabase(db: IpDatabase) {
            val ipDao = db.IpDao()
            CoroutineScope(IO).launch {
                ipDao.insertAll(AdBlockIpDataGenerator.getAdBlockIpItems())
            }
        }
    }
}

