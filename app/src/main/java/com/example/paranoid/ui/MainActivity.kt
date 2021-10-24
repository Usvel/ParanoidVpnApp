package com.example.paranoid.ui

import android.os.Bundle
import com.example.paranoid.databinding.ActivityMainBinding
import com.example.paranoid.ui.base.BaseActivity

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpBottomNav(navController, binding.bottomTabBar)
    }
}
