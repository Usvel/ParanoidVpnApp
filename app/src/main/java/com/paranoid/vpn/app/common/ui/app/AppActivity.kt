package com.paranoid.vpn.app.common.ui.app

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.paranoid.vpn.app.common.ui.base.BaseActivity
import com.paranoid.vpn.app.databinding.ActivityMainBinding

class AppActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        Log.println(Log.DEBUG, "logs", "starter")
        setUpBottomNav(binding.bottomTabBar)
        val extras = intent.extras
        if (extras != null) {
            Log.println(Log.DEBUG, "logs", "stARTER2")
            if (extras.get("toQr") as Boolean) {
                Log.println(Log.DEBUG, "logs", "toQr")
            }
            if (extras.get("toTurn") as Boolean) {
                Log.println(Log.DEBUG, "logs", "toTurn")
            }
            if (extras.get("toService") as Boolean) {
                Log.println(Log.DEBUG, "logs", "toService")
            }
            if (extras.get("toProxy") as Boolean) {
                Log.println(Log.DEBUG, "logs", "toProxy")
            }
        }
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
