package com.paranoid.vpn.app.settings.ui.main

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.paranoid.vpn.app.common.ad_block_configuration.domain.model.AdBlockIpItem
import com.paranoid.vpn.app.common.ad_block_configuration.domain.repository.IpRepository
import com.paranoid.vpn.app.common.proxy_configuration.domain.model.ProxyItem
import com.paranoid.vpn.app.common.proxy_configuration.domain.repository.ProxyRepository
import com.paranoid.vpn.app.common.ui.base.BaseFragmentViewModel
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.VPNConfigItem
import com.paranoid.vpn.app.common.vpn_configuration.domain.repository.VPNConfigRepository
import java.io.File

class SettingsViewModel(application: Application) : BaseFragmentViewModel() {
    private val vpnConfigRepository: VPNConfigRepository = VPNConfigRepository()
    private val proxyRepository: ProxyRepository = ProxyRepository()
    private val ipRepository: IpRepository = IpRepository()

    private val allConfigs = vpnConfigRepository.readAllData
    private val allProxies = proxyRepository.readAllData
    private val allIPs = ipRepository.readAllData

    private val _configsLiveData = MutableLiveData<List<VPNConfigItem>>()
    val configsLiveData: LiveData<List<VPNConfigItem>> = _configsLiveData

    override fun getCurrentData() {
        // TODO
    }

    fun getAllIPs(): LiveData<List<AdBlockIpItem>> {
        return allIPs
    }

    fun getAllConfigs(): LiveData<List<VPNConfigItem>> {
        return allConfigs
    }

    fun getAllProxies(): LiveData<List<ProxyItem>> {
        return allProxies
    }

    suspend fun insertConfigToDataBase(vpnConfigItem: VPNConfigItem) {
        vpnConfigRepository.insert(vpnConfigItem)
    }

    fun exportVpnDBToCSVFile(csvFile: File, configList: List<VPNConfigItem>) {
        csvWriter().open(csvFile, append = false) {
            writeRow(
                listOf(
                    "index",
                    "id",
                    "name",
                    "favorite",
                    "local_ip",
                    "proxy_ip",
                    "primary_dns",
                    "secondary_dns",
                    "gateway"
                )
            )
            configList.forEachIndexed { index, config ->
                writeRow(
                    listOf(
                        index,
                        config.id,
                        config.name,
                        config.favorite,
                        config.local_ip,
                        config.proxy_ip.toString(),
                        config.primary_dns,
                        config.secondary_dns,
                        config.gateway
                    )
                )
            }
        }
    }

    fun exportIpDBToCSVFile(csvFile: File, proxyList: List<AdBlockIpItem>) {
        csvWriter().open(csvFile, append = false) {
            writeRow(
                listOf(
                    "index",
                    "id",
                    "Ip",
                    "IsDomain",
                    "Domain"
                )
            )
            proxyList.forEachIndexed { index, ip ->
                writeRow(
                    listOf(
                        index,
                        ip.id,
                        ip.Ip,
                        ip.IsDomain,
                        ip.Domain
                    )
                )
            }
        }
    }

    fun exportProxyDBToCSVFile(csvFile: File, proxyList: List<ProxyItem>) {
        csvWriter().open(csvFile, append = false) {
            writeRow(
                listOf(
                    "index",
                    "id",
                    "Ip",
                    "Port",
                    "Type"
                )
            )
            proxyList.forEachIndexed { index, proxy ->
                writeRow(
                    listOf(
                        index,
                        proxy.id,
                        proxy.Ip,
                        proxy.Port,
                        proxy.Type.toString()
                    )
                )
            }
        }
    }
}
