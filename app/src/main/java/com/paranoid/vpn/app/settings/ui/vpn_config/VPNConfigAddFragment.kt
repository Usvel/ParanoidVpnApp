package com.paranoid.vpn.app.settings.ui.vpn_config

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.GsonBuilder
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.common.utils.Validators.Companion.validateIP
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.ForwardingRule
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.Protocols
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.VPNConfigItem
import com.paranoid.vpn.app.databinding.NavigationVpnConfigAddFragmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

        val vpnConfigGson = arguments?.getString("vpnConfig")
        var editConfig = false
        if (vpnConfigGson != null) {
            editConfig = true
            val gson = GsonBuilder().create()
            val vpnConfig: VPNConfigItem = gson.fromJson(vpnConfigGson, VPNConfigItem::class.java)
            setEditTextData(vpnConfig)
        }

        val navBar = activity?.findViewById<BottomNavigationView>(R.id.bottom_tab_bar)
        navBar?.visibility = View.GONE
        setRecyclerViews()
        setListeners(editConfig)
    }

    private fun setEditTextData(vpnConfig: VPNConfigItem) {
        binding.etConfigName.setText(vpnConfig.name)
        binding.etPrimaryDNS.setText(vpnConfig.primary_dns)
        binding.etSecondaryDNS.setText(vpnConfig.secondary_dns)
        binding.etLocalIP.setText(vpnConfig.local_ip)
        binding.etProxy.setText(vpnConfig.proxy_ip?.joinToString())
        binding.etGateway.setText(vpnConfig.gateway)

        for (forwardingRule in vpnConfig.forwarding_rules) {
            rulesList.add(forwardingRule)
        }

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

    private fun setListeners(editConfig: Boolean) {
        if (!editConfig) {
            binding.deleteConfigurationButton.visibility = View.GONE
        }
        binding.ivBack.setOnClickListener {
            it.findNavController().popBackStack()
        }
        binding.addConfigurationButton.setOnClickListener {
            if (editConfig) {
                editConfig()
            } else
                addConfig()
        }
        binding.addRuleButton.setOnClickListener {
            addRule()
        }
        binding.deleteConfigurationButton.setOnClickListener {
            deleteConfig()
        }
    }

    private fun deleteConfig() {
        val vpnConfigGson = arguments?.getString("vpnConfig")
        val gson = GsonBuilder().create()
        val vpnConfig: VPNConfigItem = gson.fromJson(vpnConfigGson, VPNConfigItem::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            viewModel?.deleteConfigFromDataBase(vpnConfig)
        }
        val navBar =
            activity?.findViewById<BottomNavigationView>(R.id.bottom_tab_bar)
        Utils.makeSnackBar(
            binding.root,
            Utils.getString(R.string.snackbar_delete),
            navBar
        )
        binding.root.findNavController().popBackStack()
    }

    private fun addConfig() {
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
            val navBar =
                activity?.findViewById<BottomNavigationView>(R.id.bottom_tab_bar)
            Utils.makeSnackBar(
                binding.root,
                Utils.getString(R.string.snackbar_cofig_added),
                navBar
            )
            binding.root.findNavController().popBackStack()
        } else {
            val navBar =
                activity?.findViewById<BottomNavigationView>(R.id.bottom_tab_bar)
            Utils.makeSnackBar(
                binding.root,
                Utils.getString(R.string.validation_failed),
                navBar
            )
        }
    }

    private fun editConfig() {
        val vpnConfigGson = arguments?.getString("vpnConfig")
        val gson = GsonBuilder().create()
        val vpnConfig: VPNConfigItem = gson.fromJson(vpnConfigGson, VPNConfigItem::class.java)
        vpnConfig.forwarding_rules = rulesList

        if (binding.etConfigName.text != null) {
            vpnConfig.name = binding.etConfigName.text.toString()
        }
        if (binding.etPrimaryDNS.text != null) {
            vpnConfig.primary_dns = binding.etPrimaryDNS.text.toString()
        }
        if (binding.etSecondaryDNS.text != null) {
            vpnConfig.secondary_dns = binding.etSecondaryDNS.text.toString()
        }
        if (binding.etLocalIP.text != null) {
            vpnConfig.local_ip = binding.etLocalIP.text.toString()
        }
        if (binding.etGateway.text != null) {
            vpnConfig.gateway = binding.etGateway.text.toString()
        }
        if (binding.etProxy.text != null) {
            vpnConfig.proxy_ip = binding.etProxy.text.toString().split(",").toMutableList()
        }
        CoroutineScope(Dispatchers.IO).launch {
            viewModel?.updateConfigInDataBase(vpnConfig)
        }
        val navBar =
            activity?.findViewById<BottomNavigationView>(R.id.bottom_tab_bar)
        Utils.makeSnackBar(
            binding.root,
            Utils.getString(R.string.snackbar_delete),
            navBar
        )
        binding.root.findNavController().popBackStack()

    }

    private fun setRecyclerViews() {
        binding.rvForwardingRules.layoutManager = LinearLayoutManager(context)
        rulesAdapter = context?.let { ForwardingRulesAdapter(it, rulesList) }
        binding.rvForwardingRules.adapter = rulesAdapter!!
    }

    private fun addRule() {
        val inflater = LayoutInflater.from(context)
        val v = inflater.inflate(R.layout.add_rule_dialog, null)

    val protoName = v.findViewById<TextInputEditText>(R.id.etProtocolName)
    val sourcePort = v.findViewById<TextInputEditText>(R.id.etSourcePort)
    val targetIp = v.findViewById<TextInputEditText>(R.id.etTargetIp)
    val targetPort = v.findViewById<TextInputEditText>(R.id.etTargetPort)
    val addDialog = MaterialAlertDialogBuilder(context!!)
    addDialog.setView(v)

    addDialog.setPositiveButton("Ok") { dialog, _ ->
        try {
            val proto = Protocols.valueOf(protoName.text.toString().uppercase(Locale.getDefault()))
            val sourcePortDialog = sourcePort.text.toString()
            val targetIpDialog = targetIp.text.toString()
            val targetPortDialog = targetPort.text.toString()
            rulesList.add(
                ForwardingRule(
                    protocol = proto,
                    ports = mutableListOf(sourcePortDialog),
                    target_ip = targetIpDialog,
                    target_port = targetPortDialog
                )
            )
            //rulesAdapter?.notifyDataSetChanged()
            rulesAdapter?.notifyItemInserted(rulesList.size-1)
        } catch (e: IllegalArgumentException) {
            val navBar =
                activity?.findViewById<BottomNavigationView>(R.id.bottom_tab_bar)
            Utils.makeSnackBar(
                binding.root,
                Utils.getString(R.string.validation_value),
                navBar
            )
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
}
