package com.example.paranoid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.paranoid.databinding.NavigationVpnFragmentBinding

private lateinit var binding: NavigationVpnFragmentBinding


class VPNFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.navigation_vpn_fragment, container, false)

        binding = NavigationVpnFragmentBinding.inflate(layoutInflater)

        return view
    }
}