package com.paranoid.vpn.app.vpn.ui.vpn_pager.proxy

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.databinding.PageProxyListBinding
import com.paranoid.vpn.app.vpn.ui.VPNViewModel

class ProxyObjectFragment(private val oldViewModel: VPNViewModel) :
    BaseFragment<PageProxyListBinding, VPNViewModel>(PageProxyListBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLoaders()
        setRecyclerViews()
    }

    private fun setLoaders() {
        oldViewModel.loadAllProxiesFromNetwork()
    }

    private fun setRecyclerViews() {
        oldViewModel.getAllProxiesFromNetwork().observe(viewLifecycleOwner) { value ->
            binding.rvAllProxy.layoutManager = LinearLayoutManager(context)
            val adapter = ProxyListAdapter(value) { id, code ->
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