package com.paranoid.vpn.app.common

import android.app.Application
import android.os.StrictMode
import androidx.viewbinding.BuildConfig
import com.paranoid.vpn.app.common.remote.FirebaseServiceFactory
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.common.vpn_configuration.domain.database.VPNConfigDatabase

class Application : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseServiceFactory.makeFirebase()

        Utils.init(this)

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    // .detectDiskReads()
                    // .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .penaltyDeath()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build()
            )
        }
    }
}

// private AppDatabase database;
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        instance = this;
//        database = Room.databaseBuilder(this, AppDatabase.class, "database")
//                .build();
//    }
//
//    public static App getInstance() {
//        return instance;
//    }
//
//    public AppDatabase getDatabase() {
//        return database;
//    }