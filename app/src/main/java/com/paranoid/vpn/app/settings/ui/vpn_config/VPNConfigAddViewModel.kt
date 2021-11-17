package com.paranoid.vpn.app.settings.ui.vpn_config

import android.app.Application
import com.paranoid.vpn.app.common.ui.base.BaseFragmentViewModel
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.VPNConfigItem
import com.paranoid.vpn.app.common.vpn_configuration.domain.repository.VPNConfigRepository

class VPNConfigAddViewModel(application: Application) : BaseFragmentViewModel() {
    private val vpnConfigRepository: VPNConfigRepository = VPNConfigRepository(application)

    override fun getCurrentData() {
        // TODO
    }

    suspend fun insertConfigToDataBase(vpnConfigItem: VPNConfigItem){
        vpnConfigRepository.insert(vpnConfigItem)
    }

}
