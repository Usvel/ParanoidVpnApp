package com.example.paranoid


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.paranoid.databinding.NavigationProfileFragmentBinding

private lateinit var binding: NavigationProfileFragmentBinding


class ProfileFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.navigation_profile_fragment, container, false)

        binding = NavigationProfileFragmentBinding.inflate(layoutInflater)

        return view
    }
}