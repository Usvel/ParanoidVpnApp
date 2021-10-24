package com.example.paranoid

import android.content.Context
import android.widget.Toast

class Utils {
    companion object {
        fun makeToast(context: Context, string: String) {
            Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
        }
    }
}
