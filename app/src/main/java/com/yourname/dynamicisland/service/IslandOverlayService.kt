package com.yourname.dynamicisland.service

import android.app.*
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.yourname.dynamicisland.MainActivity
import com.yourname.dynamicisland.R

class IslandOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var islandView: View? = null
    private var params: WindowManager.LayoutParams? = null

    // ویوهای داخل جزیره
    private var titleText: TextView? = null
    private var artistText: TextView? = null
    private var coverImage: ImageView? = null

    private var isExpanded = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification())
        showIsland()
    }

    private fun showIsland() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // ساخت ویوی جزیره
        islandView = LayoutInflater.from(this).inflate(R.layout.island_overlay, null)

        titleText = islandView?.findViewById(R.id.tvTitle)
        artistText = islandView?.findViewById(R.id.tvArtist)
        coverImage = islandView?.findViewById(R.id.ivCover)

        // کلیک برای باز و بسته شدن
        islandView?.setOnClickListener {
            toggleExpand()
        }

        params = WindowManager.LayoutParams(
            160,
            48,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 36
        }

        windowManager?.addView(islandView, params)

        // انیمیشن ورود اولیه
        islandView?.scaleX = 0.3f
        islandView?.scaleY = 0.3f
        islandView?.alpha = 0f
        islandView?.animate()
            ?.scaleX(1f)
            ?.scaleY(1f)
            ?.alpha(1f)
            ?.setDuration(400)
            ?.setInterpolator(OvershootInterpolator(1.4f))
            ?.start()
    }

    private fun toggleExpand() {
        if (isExpanded) {
            // جمع شدن
            islandView?.animate()
                ?.scaleX(1f)
                ?.scaleY(1f)
                ?.setDuration(280)
                ?.setInterpolator(OvershootInterpolator(0.9f))
                ?.withEndAction {
                    params?.width = 160
                    params?.height = 48
                    windowManager?.updateViewLayout(islandView, params)
                }
                ?.start()
        } else {
            // باز شدن
            params?.width = 320
            params?.height = 90
            windowManager?.updateViewLayout(islandView, params)

            islandView?.scaleX = 0.85f
            islandView?.scaleY = 0.85f
            islandView?.animate()
                ?.scaleX(1f)
                ?.scaleY(1f)
                ?.setDuration(320)
                ?.setInterpolator(OvershootInterpolator(1.2f))
                ?.start()
        }
        isExpanded = !isExpanded
    }

    // این متد بعداً از NotificationListener صدا زده می‌شه
    fun updateMusicInfo(title: String, artist: String) {
        titleText?.text = title
        artistText?.text = artist
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
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
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
