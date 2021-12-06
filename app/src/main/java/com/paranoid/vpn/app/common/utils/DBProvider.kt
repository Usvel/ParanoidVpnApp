package com.paranoid.vpn.app.common.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

const val VPN_CONFIG_DB_NAME = "vpn_config_database"
const val PROXY_DB_NAME = "proxy_database"
const val IP_DB_NAME = "ip_database"

class DBFileProvider : FileProvider() {

    fun getDatabaseURI(c: Context, dbName: String?): Uri? {
        val file: File = c.getDatabasePath(dbName)
        return getFileUri(c, file)
    }

    private fun getFileUri(context: Context, file: File): Uri? {
        return getUriForFile(context, "com.paranoid.vpn.provider", file)
    }

}