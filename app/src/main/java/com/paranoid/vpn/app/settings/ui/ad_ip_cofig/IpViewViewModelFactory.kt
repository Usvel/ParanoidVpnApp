package com.paranoid.vpn.app.settings.ui.ad_ip_cofig

import android.app.Application
import androidx.lifecycle.ViewModel

import androidx.lifecycle.ViewModelProvider

class IpViewViewModelFactory(
    private val application: Application
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return IpViewViewModel(application) as T
    }
}