package com.yourname.dynamicisland.service

import android.app.*
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.FrameLayout
import androidx.core.app.NotificationCompat
import com.yourname.dynamicisland.MainActivity
import com.yourname.dynamicisland.R

class IslandOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var islandView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification())
        showIsland()
    }

    private fun showIsland() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // ساخت ویوی ساده جزیره
        islandView = FrameLayout(this).apply {
            setBackgroundResource(R.drawable.island_background)
            // بعداً محتوای موسیقی و تماس رو اینجا اضافه می‌کنیم
        }

        val params = WindowManager.LayoutParams(
            180, // عرض
            60,  // ارتفاع
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.y = 40 // فاصله از بالای صفحه

        windowManager?.addView(islandView, params)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "island_channel",
                "Dynamic Island",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "island_channel")
            .setContentTitle("Dynamic Island فعال است")
            .setContentText("جزیره در حال اجراست")
            .setSmallIcon(R.drawable.ic_island)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        islandView?.let { windowManager?.removeView(it) }
    }
}
