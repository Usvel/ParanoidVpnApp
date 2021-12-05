package com.paranoid.vpn.app.vpn.ui.vpn_pager.proxy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.GsonBuilder
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.proxy_configuration.domain.model.ProxyItem
import com.paranoid.vpn.app.common.proxy_configuration.domain.repository.ProxyRepository
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.common.utils.ProxyClickHandlers
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.databinding.PageProxyListBinding
import com.paranoid.vpn.app.vpn.ui.VPNViewModel
import com.paranoid.vpn.app.vpn.ui.VPNViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProxyObjectFragment() :
    BaseFragment<PageProxyListBinding, VPNViewModel>(PageProxyListBinding::inflate) {

    private lateinit var oldViewModel: VPNViewModel

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

        val proxyType = oldViewModel.getProxyType()

        if (proxyCountry != null) {
            if (proxyPing != null) {
                if (proxyType != null) {
                    oldViewModel.loadAllProxiesFromNetwork(
                        proxyCountry,
                        proxyPing,
                        proxyType
                    )
                }
            }
        }
    }

    private fun setListeners() {
        binding.ivFilterProxies.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun showFilterDialog() {
        val customAlertDialogView = LayoutInflater.from(context)
            .inflate(R.layout.proxy_filter_dialog, null, false)
        val materialAlertDialogBuilder = MaterialAlertDialogBuilder(context!!)
        materialAlertDialogBuilder.setView(customAlertDialogView)

        if (oldViewModel.getProxyPing() != "")
            customAlertDialogView.findViewById<TextInputEditText>(
                R.id.etEditProxyPing
            ).setText(oldViewModel.getProxyPing())

        if (oldViewModel.getProxyCountry() != "")
            customAlertDialogView.findViewById<TextInputEditText>(
                R.id.etEditCountry
            ).setText(oldViewModel.getProxyCountry())

        val types = oldViewModel.getProxyType()?.split(",")
        if (types != null) {
            for (type in types) {
                when (type) {
                    "http" -> customAlertDialogView.findViewById<Chip>(
                        R.id.cHttp
                    ).isChecked = true
                    "https" -> customAlertDialogView.findViewById<Chip>(
                        R.id.cHttps
                    ).isChecked = true
                    "socks4" -> customAlertDialogView.findViewById<Chip>(
                        R.id.cSocks4
                    ).isChecked = true
                    "socks5" -> customAlertDialogView.findViewById<Chip>(
                        R.id.cSocks5
                    ).isChecked = true
                }
            }
        }

        materialAlertDialogBuilder
            .setView(customAlertDialogView)
            .setTitle("Proxy filter")
            .setMessage("Current configuration details")
            .setPositiveButton("Ok") { dialog, _ ->
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

                var typesFromDialog = ""

                if (customAlertDialogView.findViewById<Chip>(
                        R.id.cHttp
                    ).isChecked
                )
                    typesFromDialog += "http,"

                if (customAlertDialogView.findViewById<Chip>(
                        R.id.cHttps
                    ).isChecked
                )
                    typesFromDialog += "https,"

                if (customAlertDialogView.findViewById<Chip>(
                        R.id.cSocks4
                    ).isChecked
                )
                    typesFromDialog += "socks4,"

                if (customAlertDialogView.findViewById<Chip>(
                        R.id.cSocks5
                    ).isChecked
                )
                    typesFromDialog += "socks5,"

                oldViewModel.setProxyType(
                    typesFromDialog
                )

                setLoaders()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }


    private fun setRecyclerViews() {
        val warningColor = MaterialColors.getColor(binding.root, R.attr.vpnButtonWarning)
        val errorColor = MaterialColors.getColor(binding.root, R.attr.vpnButtonError)

        oldViewModel.getAllProxiesFromNetwork().observe(viewLifecycleOwner) { value ->
            binding.rvOnlineProxy.layoutManager = LinearLayoutManager(context)
            val adapter =
                ProxyOnlineListAdapter(value, warningColor, errorColor) { proxyItem, code ->
                    when (code) {
                        ProxyClickHandlers.Info -> openProxyInFragment(proxyItem, false)
                        ProxyClickHandlers.Save -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                ProxyRepository(requireActivity().application).addProxy(proxyItem)
                            }
                            context?.let { it1 -> Utils.makeToast(it1, "Proxy added") }
                        }
                        else -> {}
                    }
                }
            binding.rvOnlineProxy.adapter = adapter
        }

        oldViewModel.getAllProxies().observe(viewLifecycleOwner) { value ->
            binding.rvLocalProxy.layoutManager = LinearLayoutManager(context)
            val adapter = ProxyLocalListAdapter(value) { proxyItem, code ->
                when (code) {
                    ProxyClickHandlers.Info -> openProxyInFragment(proxyItem, true)
                    else -> {}
                }
            }
            binding.rvLocalProxy.adapter = adapter
        }

    }

    private fun openProxyInFragment(proxyItem: ProxyItem, local: Boolean) {
        val gson = GsonBuilder().create()
        val bundle = Bundle()
        bundle.putString(
            "proxyItem", gson.toJson(proxyItem)
        )

        bundle.putString(
            "location", gson.toJson(proxyItem.Location)
        )

        bundle.putBoolean(
            "local", local
        )

        view?.findNavController()?.navigate(
            R.id.action_vpn_fragment_to_proxy_view_fragment,
            bundle
        )
    }

    override fun initViewModel() {
        viewModel = ViewModelProvider(
            this,
            VPNViewModelFactory(requireActivity().application)
        )[VPNViewModel::class.java]
        oldViewModel = viewModel as VPNViewModel
    }

}