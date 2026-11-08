package com.yourname.dynamicisland.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaSessionManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn ?: return

        // موسیقی
        checkMediaSession()

        // پیام‌ها
        val packageName = sbn.packageName
        if (packageName.contains("whatsapp") ||
            packageName.contains("telegram") ||
            packageName.contains("messaging") ||
            packageName.contains("sms")) {

            val extras = sbn.notification.extras
            val title = extras.getString("android.title") ?: "پیام جدید"
            val text = extras.getCharSequence("android.text")?.toString() ?: ""

            val intent = Intent(this, IslandOverlayService::class.java).apply {
                action = "SHOW_MESSAGE"
                putExtra("sender", title)
                putExtra("message", text)
            }
            startService(intent)
        }
    }

    private fun checkMediaSession() {
        try {
            val mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            val controllers = mediaSessionManager.getActiveSessions(
                ComponentName(this, NotificationListener::class.java)
            )

            if (controllers.isNotEmpty()) {
                val metadata = controllers[0].metadata
                val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "در حال پخش"
                val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: ""

                val intent = Intent(this, IslandOverlayService::class.java).apply {
                    action = "UPDATE_MUSIC"
                    putExtra("title", title)
                    putExtra("artist", artist)
                }
                startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
