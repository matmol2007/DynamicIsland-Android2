package com.yourname.dynamicisland.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        checkMediaSession()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        checkMediaSession()
    }

    private fun checkMediaSession() {
        try {
            val mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            val controllers = mediaSessionManager.getActiveSessions(
                ComponentName(this, NotificationListener::class.java)
            )

            if (controllers.isNotEmpty()) {
                val controller = controllers[0]
                val metadata = controller.metadata

                val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "در حال پخش"
                val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: ""

                // ارسال اطلاعات به سرویس Overlay
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
