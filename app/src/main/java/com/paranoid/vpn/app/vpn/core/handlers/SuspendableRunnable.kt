package com.paranoid.vpn.app.vpn.core.handlers

interface SuspendableRunnable {
    suspend fun run()
}