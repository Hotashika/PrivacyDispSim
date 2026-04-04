package com.example.privacydisplay.RotationService

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat
import kotlin.math.abs

class DemoAreaBckCalc(
    private val context: Context,
    private val onBalanceUpdated: (Float) -> Unit
) {
    private var pitch = 0.0f
    private var yaw = 0.0f
    private var roll = 0.0f


    private var isDynamic = false

    fun setDynamic(value: Boolean) {
        isDynamic = value
        if (!isDynamic) onBalanceUpdated(1f)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                RotationService.ACTION_ROTATION_DATA -> {
                    pitch = intent.getFloatExtra(RotationService.PITCH, 0.0f)
                    yaw = intent.getFloatExtra(RotationService.YAW, 0.0f)
                    roll = intent.getFloatExtra(RotationService.ROLL, 0.0f)
                }
            }
            calculateWbBalance()
        }
    }

    fun register() {
        val filter = IntentFilter().apply {
            addAction(RotationService.ACTION_ROTATION_DATA)
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
    }

    fun unregister() {
        try {
            context.unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) { }
    }

    private fun calculateWbBalance() {
        if (!isDynamic) {
            onBalanceUpdated(0f)
            return
        }

        var balance_x : Float = abs(pitch)
        var balance_y : Float = abs(roll)
        val normalizedX = (abs(balance_x) / 50f).coerceIn(0f, 1f)
        val normalizedY = (abs(balance_y) / 50f).coerceIn(0f, 1f)

        val balance = (normalizedX + normalizedY - normalizedX * normalizedY).coerceIn(0f, 1f)
        Log.d("Balance", "Balance: ${balance}, Balance_X_PITCH: ${balance_x}, Balance_Y_YAW: ${balance_y}")
        onBalanceUpdated(balance)

    }
}