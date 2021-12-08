package com.paranoid.vpn.app.vpn.ui.vpn_pager.traffic

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.common.ui.base.BaseFragmentViewModel
import com.paranoid.vpn.app.common.ui.base.rv.BaseRVItem
import com.paranoid.vpn.app.databinding.PageTrafficBinding
import com.paranoid.vpn.app.vpn.domain.usecase.LoadListPacket
import com.paranoid.vpn.app.vpn.remote.VPNPacketMemoryCache

class TrafficObjectFragment :
    BaseFragment<PageTrafficBinding, BaseFragmentViewModel>(PageTrafficBinding::inflate) {
    private val loadList = LoadListPacket(VPNPacketMemoryCache)
    private var rvAdapter: TrafficRVAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvAdapter = TrafficRVAdapter()
        binding.rvPacket.adapter = rvAdapter

        binding.srlPacketContainer.setOnRefreshListener {
            updateAdapter()
            binding.srlPacketContainer.isRefreshing = false
        }

        updateAdapter()
    }

    private fun updateAdapter() {
        val resultList = arrayListOf<RVPacketItem>()
        val list = loadList.execute()
        if (list.isNotEmpty()) {
            for (id in list.indices) {
                resultList.add(
                    RVPacketItem(
                        id = id.toString(),
                        packet = list[list.size - 1 - id],
                    )
                )
            }
        }
        rvAdapter?.update(resultList)
    }

    override fun initViewModel() {
        viewModel = ViewModelProvider(this)[TrafficObjectFragmentViewModel::class.java]
    }
}