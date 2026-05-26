package com.refassistant.app.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object Haptics {

    private fun vibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun expired(context: Context) {
        val v = vibrator(context) ?: return
        val pattern = longArrayOf(0, 120, 80, 120, 80, 240)
        v.vibrate(VibrationEffect.createWaveform(pattern, -1))
    }

    fun defaulted(context: Context) {
        val v = vibrator(context) ?: return
        val pattern = longArrayOf(0, 300, 150, 300, 150, 500)
        v.vibrate(VibrationEffect.createWaveform(pattern, -1))
    }

    fun tick(context: Context) {
        val v = vibrator(context) ?: return
        v.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun rolloverWarning(context: Context) {
        val v = vibrator(context) ?: return
        val pattern = longArrayOf(0, 80, 60, 80, 60, 200)
        v.vibrate(VibrationEffect.createWaveform(pattern, -1))
    }
}
