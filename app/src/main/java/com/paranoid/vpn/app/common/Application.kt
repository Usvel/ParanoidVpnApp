package com.paranoid.vpn.app.common

import android.app.Application
import android.os.StrictMode
import androidx.viewbinding.BuildConfig
import com.paranoid.vpn.app.common.ad_block_configuration.domain.database.IpDatabase
import com.paranoid.vpn.app.common.proxy_configuration.domain.database.ProxyDatabase
import com.paranoid.vpn.app.common.remote.FirebaseServiceFactory
import com.paranoid.vpn.app.common.di.component.AppComponent
import com.paranoid.vpn.app.common.di.component.DaggerAppComponent
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.common.vpn_configuration.domain.database.VPNConfigDatabase

class Application : Application() {
    private var appComponent: AppComponent? = null

    override fun onCreate() {
        super.onCreate()

        VPNConfigDatabase.setInstance(this)
        ProxyDatabase.setInstance(this)
        IpDatabase.setInstance(this)

        Utils.init(this)

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    // .detectDiskReads()
                    // .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    // .penaltyDeath()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    // .penaltyDeath()
                    .build()
            )
        }
    }

    fun getAppComponent(): AppComponent {
        return appComponent ?: DaggerAppComponent.factory().create(applicationContext).also {
            appComponent = it
        }
    }

    init {
        instance = this
    }

    companion object {
        lateinit var instance: Application
            private set
    }
}
