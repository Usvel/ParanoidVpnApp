package com.paranoid.vpn.app.settings.ui.ad_ip_cofig

import android.app.Application
import androidx.lifecycle.LiveData
import com.paranoid.vpn.app.common.ad_block_configuration.domain.model.AdBlockIpItem
import com.paranoid.vpn.app.common.ad_block_configuration.domain.repository.IpRepository
import com.paranoid.vpn.app.common.ui.base.BaseFragmentViewModel

class IpViewViewModel(application: Application) : BaseFragmentViewModel() {
    private val ipRepository: IpRepository = IpRepository()
    private val allIp = ipRepository.readAllData
    private val allAdded = ipRepository.readAddedData

    override fun getCurrentData() {
        // TODO
    }

    fun getIPs(): LiveData<List<AdBlockIpItem>> = allIp

    fun getIpsSize(): LiveData<Int> = ipRepository.allAddressCount

    fun getAddedIPs(): LiveData<List<AdBlockIpItem>> = allAdded

    suspend fun insertToDataBase(ipItem: AdBlockIpItem) {
        if (ipItem.Ip?.let { ipRepository.getConfigCount(it) } == 0)
            ipRepository.insert(ipItem)
    }

    suspend fun deleteFromDataBase(ipItem: AdBlockIpItem) {
        ipRepository.deleteItem(ipItem)
    }

    suspend fun deleteAllLocalIps() {
        ipRepository.deleteAllLocalIps()
    }

}
