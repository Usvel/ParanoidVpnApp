package com.paranoid.vpn.app.common.ui.app

import android.os.Build
import android.os.Bundle
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.paranoid.vpn.app.common.ui.base.BaseActivity
import com.paranoid.vpn.app.databinding.ActivityMainBinding
import android.view.WindowManager




class AppActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpBottomNav(binding.bottomTabBar)

        // TODO
        //val w: Window = window // in Activity's onCreate() for instance

        //w.setFlags(
        //    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        //    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        //)

        // supportFragmentManager.beginTransaction().replace(R.id.my_nav_host_fragment, RegistrationFragment()).commit()
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    fun setProceedBottomNav(next: () -> Unit) {
        binding.flAppProceed.isVisible = true
        binding.bottomTabBar.isVisible = false
        binding.cvAppProceed.setOnClickListener {
            next()
        }
    }

    fun setUpBottomNav() {
        binding.flAppProceed.isVisible = false
        binding.bottomTabBar.isVisible = true
    }
}
