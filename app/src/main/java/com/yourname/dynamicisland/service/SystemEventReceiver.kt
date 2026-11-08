package com.yourname.dynamicisland.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.BatteryManager
import android.os.PowerManager

class SystemEventReceiver(private val callback: (EventType) -> Unit) : BroadcastReceiver() {

    enum class EventType {
        CHARGING,
        FULL_BATTERY,
        LOW_BATTERY,
        SILENT_ON,
        SILENT_OFF,
        BATTERY_SAVER_ON,
        BATTERY_SAVER_OFF
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {

            Intent.ACTION_BATTERY_CHANGED -> {
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)

                when (status) {
                    BatteryManager.BATTERY_STATUS_CHARGING -> callback(EventType.CHARGING)
                    BatteryManager.BATTERY_STATUS_FULL -> callback(EventType.FULL_BATTERY)
                }

                if (level in 1..15) {
                    callback(EventType.LOW_BATTERY)
                }
            }

            AudioManager.RINGER_MODE_CHANGED_ACTION -> {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                when (audioManager.ringerMode) {
                    AudioManager.RINGER_MODE_SILENT,
                    AudioManager.RINGER_MODE_VIBRATE -> callback(EventType.SILENT_ON)
                    AudioManager.RINGER_MODE_NORMAL -> callback(EventType.SILENT_OFF)
                }
            }

            PowerManager.ACTION_POWER_SAVE_MODE_CHANGED -> {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                if (powerManager.isPowerSaveMode) {
                    callback(EventType.BATTERY_SAVER_ON)
                } else {
                    callback(EventType.BATTERY_SAVER_OFF)
                }
            }
        }
    }

    companion object {
        fun getIntentFilter(): IntentFilter {
            return IntentFilter().apply {
                addAction(Intent.ACTION_BATTERY_CHANGED)
                addAction(AudioManager.RINGER_MODE_CHANGED_ACTION)
                addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
            }
        }
    }
}
