package com.paranoid.vpn.app.settings.ui

import android.app.Application
import androidx.lifecycle.ViewModel

import androidx.lifecycle.ViewModelProvider

class SettingsViewModelFactory(
    private val application: Application
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SettingsViewModel(application) as T
    }
}