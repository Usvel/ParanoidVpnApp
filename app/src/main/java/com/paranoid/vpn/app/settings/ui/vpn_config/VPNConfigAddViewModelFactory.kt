package com.paranoid.vpn.app.settings.ui.vpn_config

import android.app.Application
import androidx.lifecycle.ViewModel

import androidx.lifecycle.ViewModelProvider

class VPNConfigAddViewModelFactory(
    private val application: Application
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return VPNConfigAddViewModel(application) as T
    }
}