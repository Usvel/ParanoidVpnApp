package com.paranoid.vpn.app.profile.ui.edit

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.common.utils.*
import com.paranoid.vpn.app.databinding.NavigationEditUserFragmentBinding

class EditUserFragment : BaseFragment<NavigationEditUserFragmentBinding, EditUserViewModel>(
    NavigationEditUserFragmentBinding::inflate
) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservers()
        setProceedBottomNav {
            if (validateInfoUser()) return@setProceedBottomNav

            viewModel?.onProceed(
                userName = binding.etEditUserName.text.toString(),
                imageUrl = binding.etEditUserUrl.text.toString(),
                email = binding.etEditUserEmail.text.toString()
            )
        }
    }

    override fun onDestroyView() {
        setUpBottomNav()
        super.onDestroyView()
    }

    override fun initViewModel() {
        viewModel = ViewModelProvider(this)[EditUserViewModel::class.java]
    }

    private fun setObservers() {
        viewModel?.networkStateUser?.observe(viewLifecycleOwner) {
            it?.let { networkState ->
                when (networkState) {
                    is NetworkStatus.Success -> {
                        setProgressVisibility(false)
                        var start: Int
                        if (it.data?.name != null) {
                            start = binding.etEditUserName.selectionStart
                            binding.etEditUserName.text?.insert(start, it.data.name.toString())
                        }

                        if (it.data?.email != null) {
                            start = binding.etEditUserEmail.selectionStart
                            binding.etEditUserEmail.text?.insert(start, it.data.email.toString())
                        }

                        if (it.data?.photoUrl != null) {
                            start = binding.etEditUserUrl.selectionStart
                            binding.etEditUserUrl.text?.insert(start, it.data.photoUrl.toString())
                        }
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
        viewModel?.networkStateUpdateUser?.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkStatus.Error -> {
                    setProgressVisibility(false)
                    it.messageData?.let { messageData -> showMessage(messageData) }
                }
                is NetworkStatus.Loading -> {
                    setProgressVisibility(true)
                }
                is NetworkStatus.Success -> {
                    setProgressVisibility(false)
                    this.findNavController()
                        .navigate(R.id.action_edit_user_fragment_to_profile_fragment)
                }
            }
        }
        viewModel?.networkStateReauthenticate?.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkStatus.Error -> {
                    setProgressVisibility(false)
                    it.messageData?.let { messageData -> showMessage(messageData) }
                }
                is NetworkStatus.Loading -> {
                    setProgressVisibility(true)
                }
                is NetworkStatus.Success -> {
                    setProgressVisibility(false)
                }
            }
        }
        viewModel?.statePasswordUser?.observe(viewLifecycleOwner) {
            if (it) {
                setProceedBottomNav {
                    if (!binding.etEditUserPassword.text.isValidPassword()) {
                        Log.d(TAG, binding.etEditUserPassword.text.toString())
                        binding.tilEditUserPassword.error =
                            Utils.getString(R.string.authentication_fragment_invalid_password)
                        binding.etEditUserPassword.error =
                            Utils.getString(R.string.authentication_fragment_about_password)
                        return@setProceedBottomNav
                    }
                    viewModel?.onReauthenticate(
                        binding.etEditUserPassword.text.toString(),
                        binding.etEditUserEmail.text.toString()
                    )
                }
                binding.gEditTextInfo.isVisible = false
                binding.tilEditUserPassword.isVisible = true
            }
        }
    }

    private fun validateInfoUser(): Boolean {
        var validate = false

        if (!binding.etEditUserEmail.text.isValidEmail()) {
            binding.tilEditUserEmail.error =
                Utils.getString(R.string.edit_user_fragment_invalid_email)
            validate = true
        }

        if (!binding.etEditUserUrl.text.isValidUrl()) {
            binding.tilEditUserUrl.error = Utils.getString(R.string.edit_user_fragment_invalid_url)
            validate = true
        }

        if (!binding.etEditUserName.text.isValidName()) {
            binding.tilEditUserName.error =
                Utils.getString(R.string.edit_user_fragment_invalid_name)
            validate = true
        }
        return validate
    }

    companion object {
        const val TAG = "EditUserFragment"
    }
}
