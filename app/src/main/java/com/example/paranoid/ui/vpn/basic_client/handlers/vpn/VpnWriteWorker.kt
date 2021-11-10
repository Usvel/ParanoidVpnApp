package com.example.paranoid.ui.vpn.basic_client.handlers.vpn

import android.util.Log
import com.example.paranoid.ui.vpn.VPNFragment
import com.example.paranoid.ui.vpn.basic_client.LocalVPNService2
import com.example.paranoid.ui.vpn.basic_client.config.Config
import com.example.paranoid.ui.vpn.basic_client.handlers.SuspendableRunnable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.BlockingQueue
import kotlin.coroutines.coroutineContext

class VpnWriteWorker(
    var vpnOutput: FileChannel,
    private val networkToDeviceQueue: BlockingQueue<ByteBuffer>
): SuspendableRunnable {
    override suspend fun run() {
        while (coroutineContext.isActive) {
            try {
                val bufferFromNetwork = networkToDeviceQueue.take()
                bufferFromNetwork.flip()
                while (bufferFromNetwork.hasRemaining()) {
                    val w = vpnOutput.write(bufferFromNetwork)
                    if (w > 0) {
                        VPNFragment.downByte.addAndGet(w.toLong())
                    }
                    if (Config.logRW) {
                        Log.d(LocalVPNService2.TAG, "vpn write $w")
                    }
                }
            } catch (e: Exception) {
                Log.i(LocalVPNService2.TAG, "WriteVpnThread fail", e)
                coroutineContext.cancel()
            }
        }
    }
}