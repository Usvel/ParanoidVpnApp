package com.paranoid.vpn.app.qr

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.databinding.QrCreatorBinding
import com.paranoid.vpn.app.settings.ui.main.QRCreatorViewModel
import com.paranoid.vpn.app.settings.ui.main.QRScannerViewModel

class QRCreator : BaseFragment<QrCreatorBinding, QRCreatorViewModel>(QrCreatorBinding::inflate) {
    private lateinit var ivQRcode: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivQRcode = binding.ivQRCode
        val data = arguments?.getString("qr_creator")
        val writer = QRCodeWriter()

        try {
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            ivQRcode.setImageBitmap(bmp)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    override fun initViewModel() {
    }
}