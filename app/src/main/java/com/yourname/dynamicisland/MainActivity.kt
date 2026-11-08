package com.yourname.dynamicisland

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yourname.dynamicisland.service.IslandOverlayService

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnPermission = findViewById<Button>(R.id.btnPermission)

        btnPermission.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            } else {
                Toast.makeText(this, "دسترسی Overlay قبلاً داده شده", Toast.LENGTH_SHORT).show()
            }
        }

        btnStart.setOnClickListener {
            if (Settings.canDrawOverlays(this)) {
                startForegroundService(Intent(this, IslandOverlayService::class.java))
                Toast.makeText(this, "Dynamic Island فعال شد", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "اول دسترسی Overlay رو بده", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
