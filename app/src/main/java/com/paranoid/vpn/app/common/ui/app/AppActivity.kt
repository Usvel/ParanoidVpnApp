package com.paranoid.vpn.app.common.ui.app

import android.os.Bundle
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.auth.ui.RegistrationFragment
import com.paranoid.vpn.app.common.ui.base.BaseActivity
import com.paranoid.vpn.app.databinding.ActivityMainBinding

class AppActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBottomNav(binding.bottomTabBar)
        setUpBottomNav()
        supportFragmentManager.beginTransaction().replace(R.id.my_nav_host_fragment, RegistrationFragment()).commit()
    }
}
