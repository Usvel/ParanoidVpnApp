package com.paranoid.vpn.app.profile.ui.profile

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.common.ui.base.MessageData
import com.paranoid.vpn.app.common.utils.NetworkStatus
import com.paranoid.vpn.app.common.utils.UserLoggedState
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.common.utils.isValidEmail
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

    override fun onStop() {
        setUpBottomNav()
        super.onStop()
    }

    private fun setListeners() {
        binding.cvProfileRegistration.setOnClickListener {
            it.findNavController().navigate(R.id.action_profile_fragment_to_registration_fragment)
        }
        binding.cvProfileLogin.setOnClickListener {
            it.findNavController().navigate(R.id.action_profile_fragment_to_login_fragment)
        }
        binding.cvProfileBasket.setOnClickListener {
            viewModel?.deleteUser()
        }
        binding.cvProfileHange.setOnClickListener {
            it.findNavController().navigate(R.id.action_profile_fragment_to_edit_user_fragment)
        }
        binding.cvProfileOut.setOnClickListener {
            viewModel?.singOutUser()
        }
    }

    private fun setObservers() {
        viewModel?.userState?.observe(viewLifecycleOwner) {
            it?.let {
                when (it) {
                    UserLoggedState.USER_LOGGED_IN -> {
                        binding.gProfileNotEnter.isVisible = true
                        binding.gProfileButton.isVisible = false
                        binding.tilProfilePassword.isVisible = false
                    }
                    UserLoggedState.USER_LOGGED_OUT -> {
                        binding.gProfileNotEnter.isVisible = false
                        binding.gProfileButton.isVisible = true
                        binding.tilProfilePassword.isVisible = false
                    }
                }
            }
        }
        viewModel?.user?.observe(viewLifecycleOwner) {
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
            if (it.photoUrl.isNullOrEmpty()) {
                binding.ivProfileImage.setImageResource(R.drawable.ic_image_profile)
            } else {
                Glide.with(this).load(it.photoUrl).listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.ivProfileImage.setImageResource(R.drawable.ic_image_profile)
                        return true
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                }).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .apply(RequestOptions.circleCropTransform()).into(binding.ivProfileImage)
            }
        }
        viewModel?.networkStateDeleteUser?.observe(viewLifecycleOwner) {
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
                    setUpBottomNav()
                    viewModel?.getCurrentData()
                }
            }
        }
        viewModel?.statePasswordUser?.observe(viewLifecycleOwner) {
            if (it) {
                binding.gProfileNotEnter.isVisible = false
                binding.gProfileButton.isVisible = false
                binding.tilProfilePassword.isVisible = true

                setProceedBottomNav {
                    if (binding.etProfilePassword.text.isValidEmail()) {
                        binding.tilProfilePassword.error =
                            Utils.getString(R.string.edit_user_fragment_invalid_email)
                        return@setProceedBottomNav
                    }

                    viewModel?.onReauthenticate(binding.etProfilePassword.text.toString())
                }
            }
        }
        viewModel?.networkStateReauthenticate?.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkStatus.Error -> {
                    setProgressVisibility(false)
                    it.messageData?.let { messageData -> showMessage(messageData) }
                    viewModel?.getCurrentData()
                }
                is NetworkStatus.Loading -> {
                    setProgressVisibility(true)
                }
                is NetworkStatus.Success -> {
                    setProgressVisibility(false)
                    viewModel?.getCurrentData()
                }
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
