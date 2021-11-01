package com.example.paranoid

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.paranoid.qr.QRCreateActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun goToQr(view: View){
        val QRIntent = Intent(this, QRCreateActivity::class.java)
        startActivity(QRIntent)
    }
}
