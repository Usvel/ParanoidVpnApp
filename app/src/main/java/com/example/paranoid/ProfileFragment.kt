package com.example.paranoid


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.paranoid.databinding.NavigationProfileFragmentBinding
import com.example.paranoid.databinding.NavigationSettingsFragmentBinding

private var _binding: NavigationProfileFragmentBinding? = null
private val binding get() = _binding!!

class ProfileFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = NavigationProfileFragmentBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}