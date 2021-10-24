package com.example.paranoid.common.utils

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import retrofit2.HttpException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

private const val NETWORK_CODE_500 = 500

object Utils {

    private var application: Application? = null

    fun init(application: Application) {
        Utils.application = application
    }

    fun Exception.isNetworkError(): Boolean {
        return this is SocketTimeoutException ||
            this is ConnectException ||
            this is NoRouteToHostException ||
            this is UnknownHostException ||
            this is HttpException &&
            this.code() >= NETWORK_CODE_500
    }

    fun getString(@StringRes id: Int, vararg parameters: Any): String {
        return application?.getString(id, parameters)
            ?: throw IllegalStateException(
                "Application context in Utils not initialized.Please " +
                    "call method init in your Application instance"
            )
    }

    fun makeToast(context: Context, string: String) {
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
    }
}

enum class VPNState {
    CONNECTED, NOT_CONNECTED, ERROR
}
