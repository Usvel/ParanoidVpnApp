package com.paranoid.vpn.app

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.paranoid.vpn.app.common.vpn_configuration.domain.database.VPNConfigDao
import com.paranoid.vpn.app.common.vpn_configuration.domain.database.VPNConfigDatabase
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.VPNConfigDataGenerator
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


@RunWith(AndroidJUnit4::class)
class VPNConfigTest {

    private lateinit var configDao: VPNConfigDao
    private lateinit var db: VPNConfigDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        db = Room.inMemoryDatabaseBuilder(context, VPNConfigDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        configDao = db.VPNConfigDao()
    }

    @After
    @Throws(IOException::class)
    fun deleteDb() {
        //db.close()
    }

    @Test
    @Throws(IOException::class)
    fun insertAndGetTodo() = runBlocking {
        val configItem = VPNConfigDataGenerator.getVPNConfigItem()

        configDao.insert(configItem)
        val configItemGot = configDao.getById(1)
        assertEquals(configItemGot?.id, 1L)
    }
}