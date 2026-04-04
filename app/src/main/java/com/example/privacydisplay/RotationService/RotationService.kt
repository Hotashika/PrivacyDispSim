package com.example.privacydisplay.RotationService

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi

class RotationService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var rotation: Sensor? = null

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // Kalibrasyon offset'leri
    private var firstBoot = true
    private var yawOffset   = 0f
    private var pitchOffset = 0f
    private var rollOffset  = 0f

    companion object {
        const val ACTION_ROTATION_DATA = "com.example.privacydisplay.ROTATION_DATA"
        const val PITCH = "pitch"
        const val YAW = "yaw"
        const val ROLL = "roll"
        const val ACTION_RECALIBRATE = "com.example.privacydisplay.RECALIBRATE"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        rotation = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)

        if (rotation == null) {
            stopSelf()
            return
        }

        // Recalibrate broadcast'ini dinle
        registerReceiver(
            recalibrateReceiver,
            IntentFilter(ACTION_RECALIBRATE),
            RECEIVER_NOT_EXPORTED
        )

        sensorManager.registerListener(this, rotation, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_RECALIBRATE) {
            firstBoot = true
        }
        return START_NOT_STICKY
    }

    private fun normalizeAngle(angle: Float): Float {
        var a = angle % 360f
        if (a > 180f) a -= 360f
        if (a < -180f) a += 360f
        return a
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        if (event.sensor.type != Sensor.TYPE_GAME_ROTATION_VECTOR) return

        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        val rawYaw   = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
        val rawPitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
        val rawRoll  = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

        if (firstBoot) {
            yawOffset   = rawYaw
            pitchOffset = rawPitch
            rollOffset  = rawRoll
            firstBoot   = false
        }

        val yaw   = normalizeAngle(rawYaw - yawOffset)
        val pitch = normalizeAngle(rawPitch - pitchOffset)
        val roll  = normalizeAngle(rawRoll - rollOffset)

        sendBroadcast(Intent(ACTION_ROTATION_DATA).apply {
            putExtra(YAW, yaw)
            putExtra(PITCH, pitch)
            putExtra(ROLL, roll)
        })
    }

    // ReCalibrate
    private val recalibrateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            firstBoot = true
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        unregisterReceiver(recalibrateReceiver)
    }
}