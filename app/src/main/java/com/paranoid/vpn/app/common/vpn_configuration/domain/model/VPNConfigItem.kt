package com.paranoid.vpn.app.common.vpn_configuration.domain.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jetbrains.annotations.NotNull
import java.util.stream.Collectors

enum class Protocols {
    UDP, TCP
}

data class ForwardingRule(
    var protocol: Protocols,
    var ports: MutableList<String>,
    var target_ip: String?,
    var target_port: String
)

@Entity(tableName = "config")
data class VPNConfigItem(
    @PrimaryKey
    var id: Long = 0,
    var name: String,
    @NonNull var primary_dns: String = "",
    var secondary_dns: String? = null,
    @TypeConverters(ArrayConverter::class)
    var proxy_ip: MutableList<String>? = null,
    @NonNull var local_ip: String = "",
    @NonNull var gateway: String = "",
    @TypeConverters(ForwardingRuleConverter::class)
    @NotNull var forwarding_rules: MutableList<ForwardingRule>,
)

class ForwardingRuleConverter {
    @TypeConverter
    fun fromForwardingRule(value: MutableList<ForwardingRule>): String {
        val gson = Gson()
        val type = object : TypeToken<MutableList<ForwardingRule>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toForwardingRule(value: String): MutableList<ForwardingRule> {
        val gson = Gson()
        val type = object : TypeToken<MutableList<ForwardingRule>>() {}.type
        return gson.fromJson(value, type)
    }
}

class ArrayConverter {
    @TypeConverter
    fun fromArray(array: MutableList<String>): String {
        return array.stream().collect(Collectors.joining(","))
    }

    @TypeConverter
    fun toArray(data: String): MutableList<String> {
        return data.split(",".toRegex()).toTypedArray().toMutableList()
    }
}

class VPNConfigDataGenerator {

    companion object {
        fun getVPNConfigItem(): VPNConfigItem {
            return VPNConfigItem(
                id = 1L,
                name = "Test config",
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
        }
    }

}