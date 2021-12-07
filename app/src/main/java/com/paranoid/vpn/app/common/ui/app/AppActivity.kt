package com.paranoid.vpn.app.common.ui.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.view.isVisible
import com.paranoid.vpn.app.common.ui.base.BaseActivity
import com.paranoid.vpn.app.databinding.ActivityMainBinding
import com.paranoid.vpn.app.qr.QRScanner


class AppActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setUpBottomNav(binding.bottomTabBar)
        val extras = intent.extras
        if (extras != null) {
            if (extras.containsKey("toQr")) {
                val intent = Intent(this, QRScanner::class.java)
                startActivity(intent)
            }
            if (extras.containsKey("toTurn")) {
                Log.println(Log.DEBUG, "logs", "toTurn")
            }
            if (extras.containsKey("toService")) {
                Log.println(Log.DEBUG, "logs", "toService")
            }
            if (extras.containsKey("toProxy")) {
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
