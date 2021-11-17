package com.paranoid.vpn.app.profile.ui.registration

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.common.utils.NetworkStatus
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.common.utils.isValidEmail
import com.paranoid.vpn.app.common.utils.isValidPassword
import com.paranoid.vpn.app.databinding.NavigationAuthenticationFragmentBinding

class RegistrationFragment :
    BaseFragment<NavigationAuthenticationFragmentBinding, RegistrationViewModel>(
        NavigationAuthenticationFragmentBinding::inflate
    ) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setProceedBottomNav {
            if (validateText()) return@setProceedBottomNav
            viewModel?.createUser(
                binding.etAuthenticationName.text.toString(),
                binding.etAuthenticationPassword.text.toString()
            )
        }
        setObservers()
        initView()
    }

    override fun onDestroyView() {
        setUpBottomNav()
        super.onDestroyView()
    }

    override fun initViewModel() {
        viewModel = ViewModelProvider(this)[RegistrationViewModel::class.java]
    }

    private fun setObservers() {
        viewModel?.networkState?.observe(viewLifecycleOwner) {
            it?.let { networkState ->
                when (networkState) {
                    is NetworkStatus.Success -> {
                        setProgressVisibility(false)
                        view?.findNavController()
                            ?.navigate(R.id.action_registration_fragment_to_profile_fragment)
                    }
                    is NetworkStatus.Loading -> {
                        setProgressVisibility(true)
                    }
                    is NetworkStatus.Error -> {
                        setProgressVisibility(false)
                        it.messageData?.let { messageData -> showMessage(messageData) }
                    }
                }
            }
        }
    }

    private fun initView() {
        binding.bAuthenticationForgot.isVisible = false
        binding.tvAuthenticationEntry.text =
            Utils.getString(R.string.authentication_fragment_entry_registration)

        binding.etAuthenticationName.requestFocus()

        binding.etAuthenticationName.addTextChangedListener {
            binding.tilAuthenticationName.error = null
        }

        binding.etAuthenticationPassword.addTextChangedListener {
            binding.tilAuthenticationPassword.error = null
        }
    }

    private fun validateText(): Boolean {
        var validate = false

        if (!binding.etAuthenticationName.text.isValidEmail()) {
            binding.tilAuthenticationName.error =
                Utils.getString(R.string.authentication_fragment_invalid_email)
            validate = true
        }

        if (!binding.etAuthenticationPassword.text.isValidPassword()) {
            binding.tilAuthenticationPassword.error =
                Utils.getString(R.string.authentication_fragment_invalid_password)
            binding.etAuthenticationPassword.error =
                Utils.getString(R.string.authentication_fragment_about_password)
            validate = true
        }
        return validate
    }
}
