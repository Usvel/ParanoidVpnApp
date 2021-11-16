package com.paranoid.vpn.app.vpn.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.lifecycle.ViewModel

import androidx.lifecycle.ViewModelProvider

class VPNViewModelFactory(
    private val application: Application
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VPNViewModel::class.java)) {
            return VPNViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}