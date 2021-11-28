package com.paranoid.vpn.app.vpn.ui.vpn_pager.proxy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.databinding.PageProxyListBinding
import com.paranoid.vpn.app.vpn.ui.VPNViewModel


class ProxyObjectFragment(private val oldViewModel: VPNViewModel) :
    BaseFragment<PageProxyListBinding, VPNViewModel>(PageProxyListBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLoaders()
        setListeners()
        setRecyclerViews()
    }

    private fun setLoaders() {
        val proxyPing = if (
            oldViewModel.getProxyPing() != Utils.getString(R.string.possible_ping)
        )
            oldViewModel.getProxyPing()
        else ""

        val proxyCountry = if (
            oldViewModel.getProxyCountry() != Utils.getString(R.string.countries)
        )
            oldViewModel.getProxyCountry()
        else ""

        if (proxyCountry != null) {
            if (proxyPing != null) {
                oldViewModel.loadAllProxiesFromNetwork(
                    proxyCountry,
                    proxyPing
                )
            }
        }
    }

    private fun setListeners() {
        binding.ivFilterProxies.setOnClickListener {
            showConfigDetails()
        }
    }

    private fun showConfigDetails() {
        val customAlertDialogView = LayoutInflater.from(context)
            .inflate(R.layout.proxy_filter_dialog, null, false)
        val materialAlertDialogBuilder = context?.let {
            MaterialAlertDialogBuilder(
                it,
                R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Background
            )
        }

        if(oldViewModel.getProxyPing() != "")
            customAlertDialogView.findViewById<TextInputEditText>(
                R.id.etEditProxyPing
            ).setText(oldViewModel.getProxyPing())

        if(oldViewModel.getProxyCountry() != "")
            customAlertDialogView.findViewById<TextInputEditText>(
                R.id.etEditCountry
            ).setText(oldViewModel.getProxyCountry())

        materialAlertDialogBuilder
            ?.setView(customAlertDialogView)
            ?.setTitle("Proxy filter")
            ?.setMessage("Current configuration details")
            ?.setPositiveButton("Ok") { dialog, _ ->
                oldViewModel.setProxyCountry(
                    customAlertDialogView.findViewById<TextInputEditText>(
                        R.id.etEditCountry
                    ).text.toString()
                )
                oldViewModel.setProxyPing(
                    customAlertDialogView.findViewById<TextInputEditText>(
                        R.id.etEditProxyPing
                    ).text.toString()
                )
                setLoaders()
                dialog.dismiss()
            }
            ?.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }?.show()
    }


    private fun setRecyclerViews() {
        val warningColor = MaterialColors.getColor(binding.root, R.attr.vpnButtonWarning)
        val errorColor = MaterialColors.getColor(binding.root, R.attr.vpnButtonError)

        oldViewModel.getAllProxiesFromNetwork().observe(viewLifecycleOwner) { value ->
            binding.rvAllProxy.layoutManager = LinearLayoutManager(context)
            val adapter = ProxyListAdapter(value, warningColor, errorColor) { id, code ->
                //when (code) {
                //ClickHandlers.GetConfiguration -> showConfigDetails(id)
                //}
            }
            binding.rvAllProxy.adapter = adapter
        }

    }

    override fun initViewModel() {
    }

}