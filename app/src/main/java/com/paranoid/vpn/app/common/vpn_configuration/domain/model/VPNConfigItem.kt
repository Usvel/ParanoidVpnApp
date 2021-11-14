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