package com.example.paranoid.common.ui.app

import android.os.Bundle
import com.example.paranoid.databinding.ActivityMainBinding
import com.example.paranoid.common.ui.base.BaseActivity

class AppActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpBottomNav(navController, binding.bottomTabBar)
    }
}
