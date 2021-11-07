package com.paranoid.vpn.app.common.ui.base

import androidx.lifecycle.ViewModel

abstract class BaseFragmentViewModel : ViewModel() {
    abstract fun getCurrentData()
}
