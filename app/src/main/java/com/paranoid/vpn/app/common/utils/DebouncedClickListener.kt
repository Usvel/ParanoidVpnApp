package com.paranoid.vpn.app.common.utils

import android.os.SystemClock
import android.view.View
import java.util.*

private const val minimumInterval = 100L

class DebouncedOnClickListener(private val onClick: (view: View) -> Unit) : View.OnClickListener {
    private val lastClickMap: MutableMap<View, Long> = WeakHashMap()

    override fun onClick(clickedView: View) {
        val previousClickTimestamp = lastClickMap[clickedView]
        val currentTimestamp = SystemClock.uptimeMillis()

        lastClickMap[clickedView] = currentTimestamp
        if (previousClickTimestamp == null || currentTimestamp - previousClickTimestamp.toLong() > minimumInterval) {
            onClick.invoke(clickedView)
        }
    }
}