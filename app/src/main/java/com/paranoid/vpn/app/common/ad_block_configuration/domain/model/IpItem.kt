package com.paranoid.vpn.app.common.ad_block_configuration.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.reflect.TypeToken
import com.paranoid.vpn.app.common.proxy_configuration.domain.model.ProxyItem
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.ArrayConverter
import org.jetbrains.annotations.NotNull
import java.io.File

@Entity(tableName = "ad_ip_item")
data class AdBlockIpItem(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @Expose()
    var Ip: String? = "",
    @Expose()
    var Domain: String? = "",
    @Expose()
    var IsDomain: Boolean = false,
)

class AdBlockIpDataGenerator {
    companion object {
        fun getAdBlockIpItems(): MutableList<AdBlockIpItem> {
            val adBlockItems: MutableList<AdBlockIpItem> = arrayListOf()
            var id = 1L
            File("ipsList").forEachLine {
                adBlockItems.add(AdBlockIpItem(
                    id = id++,
                    Ip = it
                ))
            }
            return adBlockItems
        }
    }

}