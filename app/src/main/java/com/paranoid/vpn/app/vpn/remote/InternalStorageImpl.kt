package com.paranoid.vpn.app.vpn.remote

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.nio.ByteBuffer
import android.os.Environment




object InternalStorageImpl {
    fun writeFileOnInternalStorage(mcoContext: Context, sFileName: String, sBody: ByteBuffer?) {
        val root = mcoContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        try {
            val gpxfile = File(root, sFileName)
            val stream = FileOutputStream(gpxfile).channel
            stream.write(sBody)
            stream.close()
            Log.d("File-Load", gpxfile.path)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}