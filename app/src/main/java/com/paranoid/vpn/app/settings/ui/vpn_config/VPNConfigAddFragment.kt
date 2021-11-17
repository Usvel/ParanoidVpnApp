package com.paranoid.vpn.app.settings.ui.vpn_config

import android.app.AlertDialog
import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.compose.ui.text.toUpperCase
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.common.utils.ClickHandlers
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.common.utils.Validators.Companion.validateIP
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.ForwardingRule
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.Protocols
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.VPNConfigItem
import com.paranoid.vpn.app.databinding.NavigationVpnConfigAddFragmentBinding
import com.paranoid.vpn.app.vpn.core.LocalVPNService2
import com.paranoid.vpn.app.vpn.ui.VPNConfigAdapter
import com.squareup.moshi.internal.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.collections.ArrayList


class VPNConfigAddFragment :
    BaseFragment<NavigationVpnConfigAddFragmentBinding, VPNConfigAddViewModel>(
        NavigationVpnConfigAddFragmentBinding::inflate
    ) {

    private var rulesList: ArrayList<ForwardingRule> = ArrayList()
    private var rulesAdapter: ForwardingRulesAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navBar = activity?.findViewById<BottomNavigationView>(R.id.bottom_tab_bar)
        navBar?.visibility = View.GONE
        buttonRotationSet()
        setRecyclerViews()
        setListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val navBar = activity?.findViewById<BottomNavigationView>(R.id.bottom_tab_bar)
        navBar?.visibility = View.VISIBLE
    }

    override fun initViewModel() {
        viewModel = ViewModelProvider(
            this,
            VPNConfigAddViewModelFactory(requireActivity().application)
        )[VPNConfigAddViewModel::class.java]
    }

    private fun setListeners() {
        binding.addConfigurationButton.setOnClickListener {
            val name = binding.etConfigName.text.toString()
            val primaryDNS: String = binding.etPrimaryDNS.text.toString()
            val secondaryDNS = binding.etSecondaryDNS.text.toString()
            val localIP = binding.etLocalIP.text.toString()
            val gateway = binding.etGateway.text.toString()
            val proxyIP = binding.etProxy.text.toString()
        if (validateIP(listOf(primaryDNS, secondaryDNS, localIP, gateway))) {
            CoroutineScope(Dispatchers.IO).launch {
                viewModel?.insertConfigToDataBase(
                    VPNConfigItem(
                        name = name,
                        primary_dns = primaryDNS,
                        secondary_dns = secondaryDNS,
                        proxy_ip = arrayListOf(proxyIP), // Coming son
                        local_ip = localIP,
                        gateway = gateway,
                        forwarding_rules = rulesList
                    )
                )
            }
            it.findNavController().navigate(R.id.action_vpn_config_add_element_to_settings_fragment)
        } else
            context?.let { ct -> Utils.makeToast(ct, "Validation of ip is failed!") }
    }
    binding.addRuleButton.setOnClickListener {
        addRule()
    }
}

private fun setRecyclerViews() {
    binding.rvForwardingRules.layoutManager = LinearLayoutManager(context)
    rulesAdapter = context?.let { ForwardingRulesAdapter(it, rulesList) }
    binding.rvForwardingRules.adapter = rulesAdapter!!
}

private fun addRule() {
    val inflater = LayoutInflater.from(context)
    val v = inflater.inflate(R.layout.add_rule, null)

    val protoName = v.findViewById<TextInputEditText>(R.id.etProtocolName)
    val sourcePort = v.findViewById<TextInputEditText>(R.id.etSourcePort)
    val targetIp = v.findViewById<TextInputEditText>(R.id.etTargetIp)
    val targetPort = v.findViewById<TextInputEditText>(R.id.etTargetPort)
    val addDialog = AlertDialog.Builder(context)
    addDialog.setView(v)

    addDialog.setPositiveButton("Ok") { dialog, _ ->
        try {
            val proto = Protocols.valueOf(protoName.text.toString().toUpperCase())
            val source_port = sourcePort.text.toString()
            val target_ip = targetIp.text.toString()
            val target_port = targetPort.text.toString()
            rulesList.add(
                ForwardingRule(
                    protocol = proto,
                    ports = mutableListOf(source_port),
                    target_ip = target_ip,
                    target_port = target_port
                )
            )
            rulesAdapter?.notifyDataSetChanged()
        } catch (e: IllegalArgumentException) {
            context?.let { Utils.makeToast(it, "Incorrect value passed!") }
        }
        dialog.dismiss()
    }
    addDialog.setNegativeButton("Cancel") { dialog, _ ->
        dialog.dismiss()
        Toast.makeText(context, "Cancel", Toast.LENGTH_SHORT).show()

    }
    addDialog.create()
    addDialog.show()

}

// Animations

private fun buttonRotationSet() {
    val rotate = RotateAnimation(
        0F,
        180F,
        Animation.RELATIVE_TO_SELF,
        0.5f,
        Animation.RELATIVE_TO_SELF,
        0.5f
    )
    rotate.duration = 5000
    rotate.interpolator = LinearInterpolator()
    binding.settingsIcon.startAnimation(rotate)
}
}
