package com.example.paranoid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.paranoid.databinding.NavigationSettingsFragmentBinding

private lateinit var binding: NavigationSettingsFragmentBinding


class SettingsFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.navigation_settings_fragment, container, false)

        binding = NavigationSettingsFragmentBinding.inflate(layoutInflater)

        return view
    }
}