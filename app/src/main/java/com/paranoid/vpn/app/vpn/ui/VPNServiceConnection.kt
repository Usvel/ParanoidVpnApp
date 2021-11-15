package com.paranoid.vpn.app.vpn.ui

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import com.paranoid.vpn.app.vpn.core.LocalVPNService2

class VPNServiceConnection: ServiceConnection {

    private var vpnService: LocalVPNService2? = null

    var isBound = false

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as LocalVPNService2.LocalBinder
        vpnService = binder.getService()
        isBound = true
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        vpnService = null
        isBound = false
    }
}