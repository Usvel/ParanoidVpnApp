package com.example.paranoid.utils

import android.content.Context
import android.widget.Toast

object Utils {
    fun makeToast(context: Context, string: String) {
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
    }
}
