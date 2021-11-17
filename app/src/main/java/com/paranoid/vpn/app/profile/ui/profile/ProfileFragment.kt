package com.paranoid.vpn.app.profile.ui.profile

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.common.utils.UserLoggedState
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.databinding.NavigationProfileFragmentBinding

class ProfileFragment :
    BaseFragment<NavigationProfileFragmentBinding, ProfileViewModel>(
        NavigationProfileFragmentBinding::inflate
    ) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservers()
        setListeners()
    }

    private fun setListeners() {
        binding.cvProfileRegistration.setOnClickListener {
            it.findNavController().navigate(R.id.action_profile_fragment_to_registration_fragment)
        }
        binding.cvProfileLogin.setOnClickListener {
            it.findNavController().navigate(R.id.action_profile_fragment_to_login_fragment)
        }
        binding.cvProfileLogOut.setOnClickListener {
            viewModel.singOutUser()
        }
    }

    private fun setObservers() {
        viewModel.userState.observe(viewLifecycleOwner) {
            it?.let {
                when (it) {
                    UserLoggedState.USER_LOGGED_IN -> {
                        binding.gProfileNotEnter.isVisible = true
                        binding.gProfileButton.isVisible = false
                    }
                    UserLoggedState.USER_LOGGED_OUT -> {
                        binding.gProfileNotEnter.isVisible = false
                        binding.gProfileButton.isVisible = true
                    }
                }
            }
        }
        viewModel.user.observe(viewLifecycleOwner) {
            if (it.name.isNullOrEmpty()) {
                binding.tvProfileName.text = Utils.getString(R.string.name_user)
            } else {
                binding.tvProfileName.text = it.name
            }
            if (it.email.isNullOrEmpty()) {
                binding.gProfileEmail.isVisible = false
            } else {
                binding.gProfileEmail.isVisible = true
                binding.tvProfileEmailProfile.text = it.email
            }
        }
    }

    override fun initViewModel() {
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
    }

    companion object {
        const val TAG = "ProfileFragment"
    }
}
