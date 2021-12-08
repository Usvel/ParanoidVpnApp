package com.paranoid.vpn.app.common.ui.app

import android.os.Bundle
import androidx.core.view.isVisible
import com.paranoid.vpn.app.common.ui.base.BaseActivity
import com.paranoid.vpn.app.databinding.ActivityMainBinding

class AppActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setUpBottomNav(binding.bottomTabBar)
        // TODO
        // supportFragmentManager.beginTransaction().replace(R.id.my_nav_host_fragment, RegistrationFragment()).commit()
        //window.setFlags(
        //    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        //    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        //)


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

    companion object{
        const val TAG = "AppActivityTest"
    }
}
