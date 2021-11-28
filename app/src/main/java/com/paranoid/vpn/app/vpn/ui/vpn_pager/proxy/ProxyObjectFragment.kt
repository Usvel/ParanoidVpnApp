package com.paranoid.vpn.app.vpn.ui.vpn_pager.proxy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.paranoid.vpn.app.databinding.PageProxyListBinding
import com.paranoid.vpn.app.vpn.ui.VPNViewModel

class ProxyObjectFragment(private val oldViewModel: VPNViewModel) : Fragment() {

    private var _binding: PageProxyListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PageProxyListBinding.inflate(inflater, container, false)
        return binding.root
    }

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

}