package com.paranoid.vpn.app.vpn.ui

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.content.ContextCompat.startForegroundService
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayoutMediator
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.common.utils.VPNState
import com.paranoid.vpn.app.databinding.NavigationVpnFragmentBinding
import com.paranoid.vpn.app.vpn.core.LocalVPNService2
import com.paranoid.vpn.app.vpn.ui.vpn_pager.VPNFragmentPagerAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong

class VPNFragment :
    BaseFragment<NavigationVpnFragmentBinding, VPNViewModel>(NavigationVpnFragmentBinding::inflate) {

    companion object {
        @JvmStatic
        var downByte: AtomicLong = AtomicLong(0)

        @JvmStatic
        var upByte: AtomicLong = AtomicLong(0)
    }

    private var textUpdater: Job? = null
    private val VPN_REQUEST_CODE = 0x0F
    private lateinit var bottomSheetDialog: BottomSheetDialog

    /** Defines callbacks for service binding, passed to bindService()  */
    private var connection = VPNServiceConnection()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Just for test
        setListeners()
        setObservers()

        Intent(context, LocalVPNService2::class.java).also { intent ->
            activity?.bindService(intent, connection, 0)
        }

        initTabLayout()
    }

    private fun initTabLayout() {
        val vpnFragmentPagerAdapter = viewModel?.let { VPNFragmentPagerAdapter(activity, it) }
        binding.vpVpnPager.adapter = vpnFragmentPagerAdapter

        TabLayoutMediator(binding.tlTabLayout, binding.vpVpnPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = Utils.getString(R.string.tab_vpn)
                    tab.icon = context?.let { getDrawable(it, R.drawable.ic_outline_vpn_key) }
                }
                1 -> {
                    tab.text = Utils.getString(R.string.tab_proxy)
                    tab.icon = context?.let { getDrawable(it, R.drawable.ic_outline_router) }
                }
                2 -> {
                    tab.text = Utils.getString(R.string.tab_traffic)
                    tab.icon =
                        context?.let { getDrawable(it, R.drawable.ic_outline_data_exploration) }
                }
            }
        }.attach()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        lifecycleScope.launch(Dispatchers.Default) {
            textUpdater?.cancel()
        }
        if (connection.isBound)
            activity?.unbindService(connection)
    }

    private fun setListeners() {

        binding.cvHelpButton.setOnClickListener {
            context?.let { context_ ->
                Utils.makeToast(
                    context_,
                    Utils.getString(R.string.help_info)
                )
            }
        }
    }

    private fun setObservers() {
        viewModel?.vpnStateOn?.observe(viewLifecycleOwner) { value ->
            when (value) {
                VPNState.CONNECTED -> {
                    if (viewModel?.isConnected?.value == true)
                        startVpn()
                }
                VPNState.NOT_CONNECTED -> stopVpn()
                else -> stopVpn()
            }

        }
    }

    private fun startVpn() {
        if (connection.isBound)
            return
        val vpnIntent = VpnService.prepare(context)
        if (vpnIntent != null) startActivityForResult(
            vpnIntent,
            VPN_REQUEST_CODE
        ) else onActivityResult(VPN_REQUEST_CODE, Activity.RESULT_OK, null)

        Intent(context, LocalVPNService2::class.java).also { intent ->
            activity?.bindService(intent, connection, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val intent = Intent(context, LocalVPNService2::class.java)
            intent.action = "start"
            context?.let { startForegroundService(it, intent) }
        }
    }

    private fun stopVpn() {
        if (!connection.isBound)
            return
        activity?.unbindService(connection)
        connection.isBound = false
        val stopIntent = Intent(context, LocalVPNService2::class.java)
        stopIntent.action = "stop"
        context?.let { startForegroundService(it, stopIntent) }
    }

    override fun initViewModel() {
        viewModel = ViewModelProvider(
            this,
            VPNViewModelFactory(requireActivity().application)
        )[VPNViewModel::class.java]
    }
}
