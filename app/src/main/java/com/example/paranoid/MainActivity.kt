package com.example.paranoid

import android.os.Bundle
import com.example.paranoid.databinding.ActivityMainBinding


class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpBottomNav(navController, binding.bottomTabBar)
    }
}
