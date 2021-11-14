package com.paranoid.vpn.app.common.vpn_configuration.domain.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

enum class Protocols {
    UTP, TCP
}

data class ForwardingRule(
    var protocol: Protocols,
    var ports: MutableList<String>,
    var target_ip: String?,
    var target_port: String
)

@Entity(tableName = "config")
data class VPNConfig(
    @PrimaryKey
    var id: Long = 0,
    @NonNull var primary_dns: String = "",
    var secondary_dns: String? = null,
    var proxy_ip: String? = null,
    @NonNull var local_ip: String = "",
    @NonNull var gateway: String = "",
    @NotNull var forwarding_rules: MutableList<ForwardingRule>
)
