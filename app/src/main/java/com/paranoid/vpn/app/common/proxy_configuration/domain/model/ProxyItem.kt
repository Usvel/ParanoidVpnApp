package com.paranoid.vpn.app.common.proxy_configuration.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.reflect.TypeToken
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.ArrayConverter
import org.jetbrains.annotations.NotNull

data class Location(
    var city: String?,
    var continent: String?,
    var country: String?,
    var countryCode: String?,
    var ipName: String? = "no information",
    var ipType: String? = "no information",
    var isp: String? = "no information",
    var lat: String?,
    var lon: String?,
    var org: String? = "no information",
    var query: String? = "no information",
    var region: String?,
    var status: String? = "no information",
)

@Entity(tableName = "proxy_item")
data class ProxyItem(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @Expose()
    var Ip: String,
    @Expose()
    var Port: Int,
    @Expose()
    var Time: Int? = -1,
    @Expose()
    var Ping: Int? = -1,
    @Expose()
    var Failed: Boolean? = false,
    @Expose()
    var Anonymity: String? = "no information",
    @Expose()
    var Uptime: Double? = -1.0,
    @Expose()
    var RecheckCount: Int? = -1,
    @Expose()
    var WorkingCount: Int? = -1,
    @TypeConverters(ArrayConverter::class)
    @Expose()
    var Type: MutableList<String>? = null,
    @TypeConverters(LocationConverter::class)
    @Expose()
    @NotNull var Location: Location,
)

class LocationConverter {
    @TypeConverter
    fun fromLocation(value: Location): String {
        val gson = Gson()
        val type = object : TypeToken<Location>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toLocation(value: String): Location {
        val gson = Gson()
        val type = object : TypeToken<Location>() {}.type
        return gson.fromJson(value, type)
    }
}


class ProxyDataGenerator {
    companion object {
        fun getProxyItem(): ProxyItem {
            return ProxyItem(
                id = 1L,
                Ip = "206.126.57.190",
                Port = 5678,
                Ping = 24,
                Time = 1638086336,
                Failed = false,
                Anonymity = "Anonymous",
                WorkingCount = 1,
                Uptime = 100.0,
                RecheckCount = 1,
                Type = arrayListOf("SOCK4"),
                Location = Location(
                    city = "Houston",
                    continent = null,
                    country = "United States",
                    countryCode = "US",
                    ipName = null,
                    ipType = null,
                    isp = "Networks On-Line",
                    lat = "29.8005",
                    lon = "-95.5591",
                    org = "",
                    query = "206.126.57.190",
                    region = "TX",
                    status = "success"
                )
            )
        }
    }

}