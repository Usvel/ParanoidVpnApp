package com.paranoid.vpn.app.vpn.ui.vpn_pager.traffic

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.common.ui.base.BaseFragmentViewModel
import com.paranoid.vpn.app.common.ui.base.rv.BaseListAdapter
import com.paranoid.vpn.app.databinding.PageTrafficBinding
import com.paranoid.vpn.app.vpn.domain.usecase.LoadListPacket
import com.paranoid.vpn.app.vpn.remote.InternalStorageImpl
import com.paranoid.vpn.app.vpn.remote.VPNPacketMemoryCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import java.text.SimpleDateFormat
import java.util.*

class TrafficObjectFragment :
    BaseFragment<PageTrafficBinding, BaseFragmentViewModel>(PageTrafficBinding::inflate),
    BaseListAdapter.OnClickListener {
    private val loadList = LoadListPacket(VPNPacketMemoryCache)
    private var rvAdapter: TrafficRVAdapter? = null

    private val resultList = arrayListOf<RVPacketItem>()

    private val storage = InternalStorageImpl
    private var formatter: SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvAdapter = TrafficRVAdapter()
        rvAdapter?.onClickListener = this
        binding.rvPacket.adapter = rvAdapter
        binding.srlPacketContainer.setOnRefreshListener {
            updateAdapter()
            binding.srlPacketContainer.isRefreshing = false
        }

        updateAdapter()
    }

    override fun onClick(view: View, position: Int) {
        savePacket(position)
    }

    private fun savePacket(position: Int) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            val date = Date()
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                resultList[position].packets.forEachIndexed { index, item ->
                    withContext(Dispatchers.IO) {
                        storage.writeFileOnInternalStorage(
                            requireActivity(),
                            "${formatter.format(date)} + ${index}",
                            item.byteBuffer
                        )
                    }
                }
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    CODE
                )
            }
        }
    }

    private fun updateAdapter() {
        resultList.clear()
        var resultSize = -1
        val list = loadList.execute()
        var sizePackets = 0
        var lastIp = "0.0.0.0"
        if (list.isNotEmpty()) {
            for (id in list.indices) {
                val packet = list[list.size - 1 - id]
                val ipPacket = packet.ip4.destinationAddress.toString().removeRange(0, 1)
                if (lastIp == ipPacket) {
                    sizePackets++
                    resultList[resultSize].apply {
                        packets.add(packet)
                        this.sizePackets = sizePackets
                    }
                } else {
                    resultSize++
                    sizePackets = 1
                    resultList.add(
                        RVPacketItem(
                            id = resultSize.toString(),
                            packets = mutableListOf(
                                packet
                            ),
                            ip = ipPacket,
                            isOpen = false,
                            sizePackets = sizePackets
                        )
                    )
                    lastIp = ipPacket
                }
            }
        }
        rvAdapter?.update(resultList)
    }

    override fun initViewModel() {
        viewModel = ViewModelProvider(this)[TrafficObjectFragmentViewModel::class.java]
    }

    companion object {
        private const val CODE = 500
    }
}
