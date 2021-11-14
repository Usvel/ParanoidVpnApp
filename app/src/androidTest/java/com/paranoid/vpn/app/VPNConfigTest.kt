package com.paranoid.vpn.app

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.paranoid.vpn.app.common.vpn_configuration.domain.database.VPNConfigDao
import com.paranoid.vpn.app.common.vpn_configuration.domain.database.VPNConfigDatabase
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.ForwardingRule
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.Protocols
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.VPNConfigItem
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
        val configItem = VPNConfigItem(
            id = 1L,
            primary_dns = "8.8.8.8",
            secondary_dns = "8.8.4.4",
            proxy_ip = arrayListOf("123.123.123.123", "10.10.10.1"),
            local_ip = "10.10.10.1",
            gateway = "192.168.0.1",
            forwarding_rules = arrayListOf(
                ForwardingRule(
                    protocol = Protocols.UDP,
                    ports = arrayListOf("888", "1234", "8080"),
                    target_ip = "10.10.10.5",
                    target_port = "80"
                ),
                ForwardingRule(
                    protocol = Protocols.TCP,
                    ports = arrayListOf("888", "1234", "8080"),
                    target_ip = null,
                    target_port = "80"
                )
            )
        )

        configDao.insert(configItem)
        val configItemGot = configDao.getById(1)
        assertEquals(configItemGot?.id, 1L)
    }
}