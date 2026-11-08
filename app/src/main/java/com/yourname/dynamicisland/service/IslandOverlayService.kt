package com.yourname.dynamicisland.service

import android.app.*
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.view.animation.OvershootInterpolator
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

    private var titleText: TextView? = null
    private var artistText: TextView? = null
    private var coverImage: ImageView? = null
    private var controlsLayout: LinearLayout? = null

    private var isExpanded = false
    private var systemEventReceiver: SystemEventReceiver? = null
    private var currentEvent: SystemEventReceiver.EventType? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification())
        showIsland()

        systemEventReceiver = SystemEventReceiver { event ->
            showSystemEvent(event)
        }
        registerReceiver(systemEventReceiver, SystemEventReceiver.getIntentFilter())
    }

    private fun showIsland() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        islandView = LayoutInflater.from(this).inflate(R.layout.island_overlay, null)

        titleText = islandView?.findViewById(R.id.tvTitle)
        artistText = islandView?.findViewById(R.id.tvArtist)
        coverImage = islandView?.findViewById(R.id.ivCover)
        controlsLayout = islandView?.findViewById(R.id.controlsLayout)

        islandView?.setOnClickListener { toggleExpand() }

        // دکمه‌های کنترل موسیقی
        islandView?.findViewById<ImageView>(R.id.btnPlay)?.setOnClickListener {
            sendMediaAction("play")
        }
        islandView?.findViewById<ImageView>(R.id.btnPause)?.setOnClickListener {
            sendMediaAction("pause")
        }
        islandView?.findViewById<ImageView>(R.id.btnNext)?.setOnClickListener {
            sendMediaAction("next")
        }

        params = WindowManager.LayoutParams(
            160, 48,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 36
        }

        windowManager?.addView(islandView, params)

        islandView?.scaleX = 0.3f
        islandView?.scaleY = 0.3f
        islandView?.alpha = 0f
        islandView?.animate()
            ?.scaleX(1f)?.scaleY(1f)?.alpha(1f)
            ?.setDuration(400)
            ?.setInterpolator(OvershootInterpolator(1.4f))
            ?.start()
    }

    private fun sendMediaAction(action: String) {
        val intent = Intent("com.yourname.dynamicisland.MEDIA_ACTION")
        intent.putExtra("action", action)
        sendBroadcast(intent)
    }

    private fun toggleExpand() {
        if (isExpanded) collapseIsland() else expandIsland()
    }

    private fun expandIsland() {
        params?.width = 340
        params?.height = 110
        windowManager?.updateViewLayout(islandView, params)
        controlsLayout?.visibility = View.VISIBLE

        islandView?.scaleX = 0.85f
        islandView?.scaleY = 0.85f
        islandView?.animate()
            ?.scaleX(1f)?.scaleY(1f)
            ?.setDuration(320)
            ?.setInterpolator(OvershootInterpolator(1.2f))
            ?.start()
        isExpanded = true
    }

    private fun collapseIsland() {
        islandView?.animate()
            ?.scaleX(0.9f)?.scaleY(0.9f)
            ?.setDuration(250)
            ?.withEndAction {
                params?.width = 160
                params?.height = 48
                controlsLayout?.visibility = View.GONE
                windowManager?.updateViewLayout(islandView, params)
                isExpanded = false
            }?.start()
    }

    private fun showSystemEvent(event: SystemEventReceiver.EventType) {
        currentEvent = event
        if (!isExpanded) expandIsland()

        when (event) {
            SystemEventReceiver.EventType.CHARGING -> {
                titleText?.text = "در حال شارژ"
                artistText?.text = "شارژر متصل شد"
                coverImage?.setImageResource(R.drawable.ic_charging)
            }
            SystemEventReceiver.EventType.FULL_BATTERY -> {
                titleText?.text = "شارژ کامل"
                artistText?.text = "۱۰۰٪"
                coverImage?.setImageResource(R.drawable.ic_battery_full)
            }
            SystemEventReceiver.EventType.LOW_BATTERY -> {
                titleText?.text = "باتری ضعیف"
                artistText?.text = "شارژ کنید"
                coverImage?.setImageResource(R.drawable.ic_battery_low)
            }
            SystemEventReceiver.EventType.SILENT_ON -> {
                titleText?.text = "حالت بی‌صدا"
                artistText?.text = "اعلان‌ها خاموش"
                coverImage?.setImageResource(R.drawable.ic_silent)
            }
            SystemEventReceiver.EventType.SILENT_OFF -> {
                titleText?.text = "صدا روشن"
                artistText?.text = "اعلان‌ها فعال"
                coverImage?.setImageResource(R.drawable.ic_sound)
            }
            SystemEventReceiver.EventType.BATTERY_SAVER_ON -> {
                titleText?.text = "صرفه‌جویی باتری"
                artistText?.text = "فعال شد"
                coverImage?.setImageResource(R.drawable.ic_battery_saver)
            }
            SystemEventReceiver.EventType.BATTERY_SAVER_OFF -> {
                titleText?.text = "صرفه‌جویی باتری"
                artistText?.text = "خاموش شد"
                coverImage?.setImageResource(R.drawable.ic_battery)
            }
        }

        islandView?.postDelayed({
            if (isExpanded && currentEvent == event) collapseIsland()
        }, 3000)
    }

    fun updateMusicInfo(title: String, artist: String) {
        titleText?.text = title
        artistText?.text = artist
        coverImage?.setImageResource(R.drawable.ic_music)
    }

    fun showCall(number: String) {
        if (!isExpanded) expandIsland()
        titleText?.text = "تماس ورودی"
        artistText?.text = number
        coverImage?.setImageResource(R.drawable.ic_call)
    }

    fun showMessage(sender: String, message: String) {
        if (!isExpanded) expandIsland()
        titleText?.text = sender
        artistText?.text = message
        coverImage?.setImageResource(R.drawable.ic_message)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "UPDATE_MUSIC" -> {
                val title = intent.getStringExtra("title") ?: "در حال پخش"
                val artist = intent.getStringExtra("artist") ?: ""
                updateMusicInfo(title, artist)
            }
            "SHOW_CALL" -> {
                val number = intent.getStringExtra("number") ?: "ناشناس"
                showCall(number)
            }
            "SHOW_MESSAGE" -> {
                val sender = intent.getStringExtra("sender") ?: "پیام جدید"
                val message = intent.getStringExtra("message") ?: ""
                showMessage(sender, message)
            }
        }
        return START_STICKY
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
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

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
        systemEventReceiver?.let { unregisterReceiver(it) }
        islandView?.let { windowManager?.removeView(it) }
    }
}
