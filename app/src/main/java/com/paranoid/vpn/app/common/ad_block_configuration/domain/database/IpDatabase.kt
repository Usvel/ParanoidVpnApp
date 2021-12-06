package com.paranoid.vpn.app.common.ad_block_configuration.domain.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.paranoid.vpn.app.common.ad_block_configuration.domain.model.AdBlockIpDataGenerator
import com.paranoid.vpn.app.common.ad_block_configuration.domain.model.AdBlockIpItem
import com.paranoid.vpn.app.common.utils.IP_DB_NAME
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
                            IpDatabase::class.java, IP_DB_NAME
                        ).build()
                    }
                }
            }
            return INSTANCE
        }

        @Synchronized
        fun getInstance(): IpDatabase {
            return INSTANCE!!
        }

        fun populateDatabase() {
            val ipDao = getInstance().IpDao()
            CoroutineScope(IO).launch {
                val data = AdBlockIpDataGenerator.getAdBlockIpItems()
                ipDao.insertAll(data)
            }
        }
    }
}

