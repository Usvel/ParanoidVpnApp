package com.paranoid.vpn.app.vpn.ui.vpn_pager.proxy.proxy_item

import android.os.Bundle
import android.view.View
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.GsonBuilder
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.proxy_configuration.domain.model.Location
import com.paranoid.vpn.app.common.proxy_configuration.domain.model.ProxyItem
import com.paranoid.vpn.app.common.proxy_configuration.domain.repository.ProxyRepository
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.common.utils.Utils
import com.paranoid.vpn.app.databinding.NavigationProxyAddFragmentBinding
import com.paranoid.vpn.app.vpn.ui.VPNViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProxyAddFragment :
    BaseFragment<NavigationProxyAddFragmentBinding, VPNViewModel>(
        NavigationProxyAddFragmentBinding::inflate
    ) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val proxyItemGson = arguments?.getString("proxyItem")
        val locationGson = arguments?.getString("location")
        var editProxy = false

        val navBar = activity?.findViewById<BottomNavigationView>(R.id.bottom_tab_bar)
        navBar?.visibility = View.GONE

        if (proxyItemGson != null) {
            if (locationGson != null) {
                editProxy = true
                val gson = GsonBuilder().create()
                val proxyItem: ProxyItem = gson.fromJson(proxyItemGson, ProxyItem::class.java)
                val location: Location = gson.fromJson(locationGson, Location::class.java)
                setEditTextData(proxyItem, location)
            }
        }
        setListeners(editProxy)

    }

    private fun setEditTextData(proxyItem: ProxyItem, location: Location) {
        binding.etProxyIp.setText(proxyItem.Ip)
        binding.etProxyPort.setText(proxyItem.Port.toString())
        binding.etProxyCity.setText(location.city)
        binding.etProxyCountry.setText(location.country)
        binding.etProxyContinent.setText(location.continent)
        binding.etProxyCountryCode.setText(location.countryCode)
        binding.etLat.setText(location.lat)
        binding.etLon.setText(location.lon)
        binding.etRegion.setText(location.region)

        val types = proxyItem.Type
        if (types != null) {
            for (type in types) {
                when (type) {
                    "http" -> binding.cHttp.isChecked = true
                    "https" -> binding.cHttps.isChecked = true
                    "socks4" -> binding.cSocks4.isChecked = true
                    "socks5" -> binding.cSocks5.isChecked = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val navBar = activity?.findViewById<BottomNavigationView>(R.id.bottom_tab_bar)
        navBar?.visibility = View.VISIBLE
    }

    override fun initViewModel() {
    }

    private fun setListeners(editProxy: Boolean) {
        binding.ivBack.setOnClickListener {
            it.findNavController().popBackStack()
        }

        binding.bSave.setOnClickListener {
            if (editProxy) {
                editProxy()
            } else
                addProxy()
        }
    }

    private fun editProxy() {
        val proxyRepository = ProxyRepository()
        CoroutineScope(Dispatchers.IO).launch {
            val gson = GsonBuilder().create()
            val proxyItemGson = arguments?.getString("proxyItem")
            val proxyItem: ProxyItem = gson.fromJson(proxyItemGson, ProxyItem::class.java)
            if (binding.etProxyIp.text != null)
                proxyItem.Ip = binding.etProxyIp.text.toString()
            if (binding.etProxyPort.text != null)
                proxyItem.Port = binding.etProxyPort.text.toString().toInt()
            if (binding.etProxyCity.text != null)
                proxyItem.Location.city = binding.etProxyCity.text.toString()
            if (binding.etProxyCountry.text != null)
                proxyItem.Location.country = binding.etProxyCountry.text.toString()
            if (binding.etProxyContinent.text != null)
                proxyItem.Location.continent = binding.etProxyContinent.text.toString()
            if (binding.etProxyCountryCode.text != null)
                proxyItem.Location.countryCode = binding.etProxyCountryCode.text.toString()
            if (binding.etLat.text != null)
                proxyItem.Location.lat = binding.etLat.text.toString()
            if (binding.etLon.text != null)
                proxyItem.Location.lon = binding.etLon.text.toString()
            if (binding.etRegion.text != null)
                proxyItem.Location.region = binding.etRegion.text.toString()
            val values: MutableList<String> = arrayListOf()
            values.add("HTTP")
            if (binding.cHttps.isChecked)
                values.add("HTTPS")
            if (binding.cSocks4.isChecked)
                values.add("SOCKS4")
            if (binding.cSocks5.isChecked)
                values.add("SOCKS5")
            proxyItem.Type = values
            proxyRepository.updateProxy(proxyItem)
        }
        context?.let { Utils.makeToast(it, "Successfully updated") }
        binding.root.findNavController().popBackStack()
    }

    private fun addProxy() {
        if ((binding.etProxyIp.text != null) && (binding.etProxyPort.text != null)) {
            val proxyRepository = ProxyRepository()
            CoroutineScope(Dispatchers.IO).launch {
                var proxyIp = ""
                var proxyPort = 0
                var proxyLocationCity = ""
                var proxyLocationCountry = ""
                var proxyLocationContinent = ""
                var proxyLocationCountryCode = ""
                var proxyLocationLat = ""
                var proxyLocationLon = ""
                var proxyLocationRegion = ""

                if (binding.etProxyIp.text != null)
                    proxyIp = binding.etProxyIp.text.toString()
                if (binding.etProxyPort.text != null)
                    proxyPort = binding.etProxyPort.text.toString().toInt()
                if (binding.etProxyCity.text != null)
                    proxyLocationCity = binding.etProxyCity.text.toString()
                if (binding.etProxyCountry.text != null)
                    proxyLocationCountry = binding.etProxyCountry.text.toString()
                if (binding.etProxyContinent.text != null)
                    proxyLocationContinent = binding.etProxyContinent.text.toString()
                if (binding.etProxyCountryCode.text != null)
                    proxyLocationCountryCode = binding.etProxyCountryCode.text.toString()
                if (binding.etLat.text != null)
                    proxyLocationLat = binding.etLat.text.toString()
                if (binding.etLon.text != null)
                    proxyLocationLon = binding.etLon.text.toString()
                if (binding.etRegion.text != null)
                    proxyLocationRegion = binding.etRegion.text.toString()

                val values: MutableList<String> = arrayListOf()
                values.add("HTTP")
                if (binding.cHttps.isChecked)
                    values.add("HTTPS")
                if (binding.cSocks4.isChecked)
                    values.add("SOCKS4")
                if (binding.cSocks5.isChecked)
                    values.add("SOCKS5")

                val proxyItem = ProxyItem(
                    Ip = proxyIp,
                    Port = proxyPort,
                    Type = values,
                    Location = Location(
                        city = proxyLocationCity,
                        country = proxyLocationCountry,
                        continent = proxyLocationContinent,
                        countryCode = proxyLocationCountryCode,
                        lat = proxyLocationLat,
                        lon = proxyLocationLon,
                        region = proxyLocationRegion
                    )
                )

                proxyRepository.addProxy(proxyItem)
            }
            context?.let { Utils.makeToast(it, "Successfully added") }
            binding.root.findNavController().popBackStack()
        } else
            context?.let { Utils.makeToast(it, "Please add Ip and port") }
    }


}
