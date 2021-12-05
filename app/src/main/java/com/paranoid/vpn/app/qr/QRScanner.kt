package com.paranoid.vpn.app.qr

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.paranoid.vpn.app.common.ui.base.BaseFragment
import com.paranoid.vpn.app.common.vpn_configuration.domain.model.VPNConfigItem
import com.paranoid.vpn.app.common.vpn_configuration.domain.repository.VPNConfigRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.paranoid.vpn.app.databinding.QrScannerBinding
import com.paranoid.vpn.app.settings.ui.main.QRScannerViewModel

class QRScanner : BaseFragment<QrScannerBinding, QRScannerViewModel>(QrScannerBinding::inflate) {

    private lateinit var codescanner: CodeScanner

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) } == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA),123)
        } else {
            Log.println(Log.INFO, "project", "onViewCreated")
            startScanning()
        }
    }

    private fun startScanning() {
        val scannerView: CodeScannerView = binding.scannerView
        codescanner = context?.let { CodeScanner(it,scannerView) }!!
        codescanner.camera = CodeScanner.CAMERA_BACK
        codescanner.formats = CodeScanner.ALL_FORMATS

        codescanner.autoFocusMode = AutoFocusMode.SAFE
        codescanner.scanMode = ScanMode.SINGLE
        codescanner.isAutoFocusEnabled = true
        codescanner.isFlashEnabled = false

        codescanner.decodeCallback = DecodeCallback {
            requireActivity().runOnUiThread {
                try {
                    val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                    val configItem: VPNConfigItem = gson.fromJson(it.text, VPNConfigItem::class.java)
                    CoroutineScope(Dispatchers.IO).launch {
                        VPNConfigRepository(requireActivity().application).addConfig(configItem)
                    }
                    binding.root.findNavController().popBackStack()
                } catch (e: JsonSyntaxException) {
                    Toast.makeText(context, "Bad QR-code", Toast.LENGTH_SHORT).show()
                }
            }
        }
        codescanner.errorCallback = ErrorCallback {
            requireActivity().runOnUiThread {
                Toast.makeText(context, "Camera initialization error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(context, "Camera permission granted", Toast.LENGTH_SHORT).show()
                startScanning()
            }else{
                Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::codescanner.isInitialized){
            codescanner?.startPreview()
        }
    }

    override fun onPause() {

        if (::codescanner.isInitialized){
            codescanner?.releaseResources()
        }
        super.onPause()
    }

    override fun initViewModel() {}
}