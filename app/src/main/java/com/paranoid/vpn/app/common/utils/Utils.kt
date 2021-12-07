package com.paranoid.vpn.app.common.utils

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Patterns
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import com.paranoid.vpn.app.common.ui.base.MessageData
import retrofit2.HttpException
import java.io.BufferedReader
import java.io.File
import java.io.File.separator
import java.io.IOException
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.DecimalFormat
import java.util.regex.Pattern
import kotlin.math.roundToInt

private const val NETWORK_CODE_500 = 500

object Utils {
    private var application: Application? = null

    fun init(application: Application) {
        Utils.application = application
    }

    private const val K: Long = 1024
    private const val M = K * K
    private const val G = M * K
    private const val T = G * K

    fun convertToStringRepresentation(value: Long): String? {
        val dividers = longArrayOf(T, G, M, K, 1)
        val units = arrayOf("TB", "GB", "MB", "KB", "B")
        if (value < 1){
            return "0 B"
        }
        var result: String? = null
        for (i in dividers.indices) {
            val divider = dividers[i]
            if (value >= divider) {
                val speed = value.toDouble()/divider.toDouble()
                val df: DecimalFormat = DecimalFormat("#.00")
                result = "${df.format(speed)} ${units[i]}"
                break
            }
        }
        return result
    }

    fun generateFile(context: Context, fileName: String): File? {
        val csvFile = File(context.filesDir, fileName)
        csvFile.createNewFile()

        return if (csvFile.exists()) {
            csvFile
        } else {
            null
        }
    }

    fun goToFileIntent(context: Context, file: File): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val mimeType = context.contentResolver.getType(contentUri)
        intent.setDataAndType(contentUri, mimeType)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        return intent
    }

    fun readLines(fileName: String): List<String> {

        var reader: BufferedReader? = null
        val linesFromFile: MutableList<String> = arrayListOf()
        try {
            reader = BufferedReader(
                InputStreamReader(application?.assets?.open(fileName), "UTF-8")
            )

            var line: String?
            do {
                line = reader.readLine()
                linesFromFile.add(line)
            } while (line != null)

        } catch (e: IOException) {
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                }
            }
        }

        return linesFromFile
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

fun CharSequence?.isValidEmail() =
    !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun CharSequence?.isValidPassword(): Boolean {
    val passwordPattern: Pattern = Pattern.compile(
        "[a-zA-Z0-9]{8,24}"
    )

    return !isNullOrEmpty() && passwordPattern.matcher(this).matches()
}

fun CharSequence?.isValidName(): Boolean {
    val namePattern: Pattern = Pattern.compile(
        "[a-zA-Z0-9]{2,10}"
    )

    return isNullOrEmpty() || namePattern.matcher(this).matches()
}

fun CharSequence?.isValidUrl() = isNullOrEmpty() || Patterns.WEB_URL.matcher(this).matches()

enum class VPNState {
    CONNECTED, NOT_CONNECTED, ERROR
}

enum class UserLoggedState {
    USER_LOGGED_IN,
    USER_LOGGED_OUT
}

sealed class NetworkStatus<T>(
    val data: T? = null,
    val messageData: MessageData? = null
) {
    class Success<T>(data: T? = null) : NetworkStatus<T>(data)
    class Loading<T>(data: T? = null) : NetworkStatus<T>(data)
    class Error<T>(messageData: MessageData?, data: T? = null) : NetworkStatus<T>(data, messageData)
}

class Validators {
    companion object {
        private val PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$"
        )

        private fun validate(ip: String?): Boolean {
            return PATTERN.matcher(ip).matches()
        }

        fun validateIP(ips: List<String>): Boolean {
            for (ip in ips)
                if (!validate(ip))
                    return false
            return true
        }
    }
}

enum class ConfigurationClickHandlers {
    GetConfiguration, SetConfiguration, QRCode, Edit, Share, Like
}

enum class ProxyClickHandlers {
    Save, Set, Info
}
