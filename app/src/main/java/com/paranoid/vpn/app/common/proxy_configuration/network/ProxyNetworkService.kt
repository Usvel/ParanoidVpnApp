package com.paranoid.vpn.app.common.proxy_configuration.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkService {
    private var mInstance: NetworkService? = null
    val instance: NetworkService?
        get() {
            if (mInstance == null) {
                mInstance = NetworkService
            }
            return mInstance
        }
}

class ProxyNetworkService private constructor() {
    private val mRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    companion object {
        private var mInstance: ProxyNetworkService? = null
        private const val BASE_URL = "https://www.proxyscan.io"
        val instance: ProxyNetworkService?
            get() {
                if (mInstance == null) {
                    mInstance = ProxyNetworkService()
                }
                return mInstance
            }
    }

    fun getProxyApi(): ProxyApi? {
        return mRetrofit.create(ProxyApi::class.java)
    }

}