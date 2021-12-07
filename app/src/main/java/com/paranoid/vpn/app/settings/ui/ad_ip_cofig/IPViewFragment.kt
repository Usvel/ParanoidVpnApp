package com.paranoid.vpn.app.settings.ui.ad_ip_cofig

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ad_block_configuration.domain.database.IpDatabase
import com.paranoid.vpn.app.common.ad_block_configuration.domain.model.AdBlockIpItem
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.common.utils.Validators
import com.paranoid.vpn.app.databinding.NavigationAdvertFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class IPViewFragment :
    BaseFragment<NavigationAdvertFragmentBinding, IpViewViewModel>(
        NavigationAdvertFragmentBinding::inflate
    ) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        initViewModel()
        setRecyclerViews()
        setObservers()
    }

    override fun initViewModel() {
        viewModel = ViewModelProvider(
            this,
            IpViewViewModelFactory(requireActivity().application)
        )[IpViewViewModel::class.java]
    }

    private fun setListeners() {
        binding.viewAddIp.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val ip = binding.etProxyIp.text.toString()
                if (Validators.validateIP(arrayListOf(ip))) {
                    viewModel!!.insertToDataBase(
                        AdBlockIpItem(
                            Ip = binding.etProxyIp.text.toString(),
                            IsLocal = false
                        )
                    )
                }
                else
                    withContext(Dispatchers.Main) {
                        context?.let { it1 -> Utils.makeToast(it1, "Ip is not correct") }
                    }
            }
        }
        binding.viewDeleteIp.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val ip = binding.etProxyIp.text.toString()
                if (Validators.validateIP(arrayListOf(ip))) {
                    val ipItem = viewModel!!.getItemByIp(ip)
                    if (ipItem != null){
                        viewModel!!.deleteFromDataBase(ipItem)
                    }
                }
                else
                    withContext(Dispatchers.Main) {
                        context?.let { it1 -> Utils.makeToast(it1, "Ip is not correct") }
                    }
            }
        }
        binding.viewLoadFromDatabase.setOnClickListener {
            IpDatabase.populateDatabase()
            setProgressVisibility(true)
        }
        binding.viewDeleteFromDatabase.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel?.deleteAllLocalIps()
            }
        }
    }

    private fun setRecyclerViews() {
        viewModel?.getAddedIPs()?.observe(viewLifecycleOwner) { value ->
            setProgressVisibility(true)
            binding.rvAdBlockIPs.layoutManager = LinearLayoutManager(context)
            val adapter = IPsListAdapter(value) { adBlockItem ->
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel!!.deleteFromDataBase(adBlockItem)
                }
            }
            binding.rvAdBlockIPs.adapter = adapter
            setProgressVisibility(false)
        }
    }

    private fun setObservers() {
        viewModel?.getIpsSize()?.observe(viewLifecycleOwner) { value ->
            binding.tvAddressCount.text =
                resources.getQuantityString(
                    R.plurals.ips_number,
                    value,
                    value.toString()
                )
        }
    }
}
