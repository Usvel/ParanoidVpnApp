package com.paranoid.vpn.app.vpn.core.handlers.vpn

import android.util.Log
import com.paranoid.vpn.app.vpn.ui.VPNFragment
import com.paranoid.vpn.app.vpn.core.LocalVPNService2
import com.paranoid.vpn.app.vpn.core.config.Config
import com.paranoid.vpn.app.vpn.core.handlers.SuspendableRunnable
import kotlinx.coroutines.*
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.BlockingQueue
import kotlin.coroutines.coroutineContext

class VpnWriteWorker(
    private var vpnOutput: FileChannel,
    private val networkToDeviceQueue: BlockingQueue<ByteBuffer>
): SuspendableRunnable {
    override suspend fun run() {
        while (coroutineContext.isActive) {
            try {
                val bufferFromNetwork = runInterruptible {
                    networkToDeviceQueue.take()
                }
                bufferFromNetwork.flip()
                while (bufferFromNetwork.hasRemaining()) {
                    val w = runInterruptible { vpnOutput.write(bufferFromNetwork) }
                    if (w > 0) {
                        VPNFragment.downByte.addAndGet(w.toLong())
                    }
                    if (Config.logRW) {
                        Log.d(LocalVPNService2.TAG, "vpn write $w")
                    }
                }
            } catch (e: Exception) {
                Log.i(LocalVPNService2.TAG, "WriteVpnThread fail", e)
            }
        }
    }
}