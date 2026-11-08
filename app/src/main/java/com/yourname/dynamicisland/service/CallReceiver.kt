package com.yourname.dynamicisland.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

        if (state == TelephonyManager.EXTRA_STATE_RINGING) {
            val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: "ناشناس"

            val serviceIntent = Intent(context, IslandOverlayService::class.java).apply {
                action = "SHOW_CALL"
                putExtra("number", number)
            }
            context.startService(serviceIntent)
        }
    }
}
