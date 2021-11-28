package com.paranoid.vpn.app.common.proxy_configuration.network

import com.paranoid.vpn.app.common.proxy_configuration.domain.model.ProxyItem
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

// sample: https://github.com/ShiftyTR/Proxy-List
// https://www.proxyscan.io/api/proxy?last_check=3800&country=fr,us&uptime=50&ping=100&limit=10&type=socks4,socks5

interface ProxyApi {
    @GET("/api/proxy?format=json")
    fun getProxy(@Path("id") id: Int): Call<ProxyItem>

    @GET("/api/proxy?limit=20")
    fun getProxies(): Call<List<ProxyItem>>

}