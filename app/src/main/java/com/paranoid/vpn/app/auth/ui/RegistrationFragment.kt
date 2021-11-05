package com.paranoid.vpn.app.auth.ui

import android.os.Bundle
import android.view.View
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.databinding.NavigationRegistrationFragmentBinding

class RegistrationFragment: BaseFragment<NavigationRegistrationFragmentBinding>(NavigationRegistrationFragmentBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
    }

    private fun setListeners() {

    }

    override fun initViewModel() {

    }

    override fun initBottomNav() {
        setProceedBottomNav {}
    }
}