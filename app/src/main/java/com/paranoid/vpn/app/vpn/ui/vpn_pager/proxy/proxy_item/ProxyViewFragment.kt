package com.paranoid.vpn.app.vpn.ui.vpn_pager.proxy.proxy_item

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.navigation.findNavController
import com.google.gson.GsonBuilder
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.proxy_configuration.domain.model.Location
import com.paranoid.vpn.app.common.proxy_configuration.domain.model.ProxyItem
import com.paranoid.vpn.app.common.proxy_configuration.domain.repository.ProxyRepository
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.databinding.NavigationProxyViewFragmentBinding
import com.paranoid.vpn.app.vpn.ui.VPNViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProxyViewFragment :
    BaseFragment<NavigationProxyViewFragmentBinding, VPNViewModel>(
        NavigationProxyViewFragmentBinding::inflate
    ) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val proxyItemGson = arguments?.getString("proxyItem")
        val locationGson = arguments?.getString("location")
        val local = arguments?.getBoolean("local", false)
        val gson = GsonBuilder().create()
        val proxyItem: ProxyItem = gson.fromJson(proxyItemGson, ProxyItem::class.java)
        val location: Location = gson.fromJson(locationGson, Location::class.java)
        setProxyItem(proxyItem, location, local)
        if (proxyItemGson != null) {
            if (locationGson != null) {
                setListeners(proxyItemGson, locationGson, proxyItem, local)
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun setProxyItem(proxyItem: ProxyItem, location: Location, local: Boolean?) {

        if (local == true){
            binding.tvProxyDescription.text =
                Utils.getString(R.string.view_information_about_local_proxy)
            binding.tvProxyTime.visibility = View.GONE
            binding.tvProxyPing.visibility = View.GONE
            binding.tvProxyFailed.visibility = View.GONE
            binding.tvProxyUpTime.visibility = View.GONE
            binding.tvProxyRecheckCount.visibility = View.GONE
            binding.tvProxyWorkingCount.visibility = View.GONE
        }

        binding.tvProxyIp.text = "IP: ${proxyItem.Ip}"
        binding.tvProxyPort.text = "Port: ${proxyItem.Port}"
        binding.tvProxyTime.text = "Time: ${proxyItem.Time}"
        binding.tvProxyPing.text = "Ping: ${proxyItem.Ping}"
        binding.tvProxyFailed.text = "Failed: ${proxyItem.Failed}"
        binding.tvProxyAnonymity.text = "Anonymity: ${proxyItem.Anonymity}"
        binding.tvProxyUpTime.text = "Uptime: ${proxyItem.Uptime}"
        binding.tvProxyRecheckCount.text = "RecheckCount: ${proxyItem.RecheckCount}"
        binding.tvProxyWorkingCount.text = "WorkingCount: ${proxyItem.WorkingCount}"
        binding.tvType.text = "Type: ${proxyItem.Type}"

        binding.tvProxyLocationCity.text = "City: ${location.city}"
        binding.tvProxyLocationContinent.text = "Continent: ${location.continent}"
        binding.tvProxyLocationCountryCode.text = "Country code: ${location.countryCode}"
        binding.tvProxyLocationCountry.text = "Country: ${location.country}"
        binding.tvProxyLocationIpName.text = "Ip name: ${location.ipName}"
        binding.tvProxyLocationIpType.text = "Ip Type: ${location.ipType}"
        binding.tvProxyLocationISP.text = "ISP: ${location.isp}"
        binding.tvProxyLocationLat.text = "LAT: ${location.lat}"
        binding.tvProxyLocationLan.text = "LON: ${location.lon}"
        binding.tvProxyLocationOrg.text = "ORG: ${location.org}"
        binding.tvProxyLocationQuery.text = "Query: ${location.query}"
        binding.tvProxyLocationRegion.text = "Region: ${location.region}"
        binding.tvProxyLocationStatus.text = "Status: ${location.status}"
    }

    private fun setListeners(
        proxyItemGson: String,
        locationGson: String,
        proxyItem: ProxyItem,
        local: Boolean?
    ) {
        if (local == true) {
            binding.ibSaveProxy.visibility = View.GONE
        }

        if (local == false) {
            binding.ibEditProxy.visibility = View.GONE
            binding.ibDeleteProxy.visibility = View.GONE
        }

        binding.ibSaveProxy.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                ProxyRepository().addProxy(proxyItem)
            }
            context?.let { it1 -> Utils.makeToast(it1, "Proxy added") }
            it.findNavController().popBackStack()
        }
        binding.ibSetProxy.setOnClickListener {
            //
        }
        binding.ibDeleteProxy.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                ProxyRepository().deleteProxy(proxyItem)
            }
            context?.let { it1 -> Utils.makeToast(it1, "Proxy removed") }
            it.findNavController().popBackStack()
        }
        binding.ibEditProxy.setOnClickListener {
            openProxyEditingFragment()
        }
        binding.ibShareProxy.setOnClickListener {
            shareProxy(proxyItemGson + locationGson)
        }

    }

    private fun openProxyEditingFragment() {
        val proxyItem = arguments?.getString("proxyItem")
        val location = arguments?.getString("location")
        val bundle = Bundle()
        bundle.putString(
            "proxyItem", proxyItem
        )

        bundle.putString(
            "location", location
        )

        view?.findNavController()?.navigate(
            R.id.action_proxy_view_fragment_to_proxy_add_fragment,
            bundle
        )
    }

    private fun shareProxy(proxyItemGson: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, proxyItemGson)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    override fun initViewModel() {
    }
}