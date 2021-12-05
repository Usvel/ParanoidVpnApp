package com.paranoid.vpn.app.settings.ui.ad_ip_cofig

import android.app.Application
import androidx.lifecycle.LiveData
import com.paranoid.vpn.app.common.ad_block_configuration.domain.model.AdBlockIpItem
import com.paranoid.vpn.app.common.ad_block_configuration.domain.repository.IpRepository
import com.paranoid.vpn.app.common.ui.base.BaseFragmentViewModel

class IpViewViewModel(application: Application) : BaseFragmentViewModel() {
    private val ipRepository: IpRepository = IpRepository()
    private val allIp = ipRepository.readAllData

    override fun getCurrentData() {
        // TODO
    }

    fun getIPs(): LiveData<List<AdBlockIpItem>> {
        return allIp
    }

    suspend fun insertToDataBase(ipItem: AdBlockIpItem) {
        ipRepository.insert(ipItem)
    }


    suspend fun deleteFromDataBase(ipItem: AdBlockIpItem) {
        ipRepository.deleteItem(ipItem)
    }

}
