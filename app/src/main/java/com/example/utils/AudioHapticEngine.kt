package com.example.utils

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object AudioHapticEngine {
    fun triggerSuccess(context: Context) {
        val intensity = getIntensity(context)
        if (intensity == 0) return
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 40 * intensity.toLong(), 60, 20 * intensity.toLong()), -1))
        }
        playSynthClick(1500.0, 30, 0.4)
    }

    fun triggerError(context: Context) {
        val intensity = getIntensity(context)
        if (intensity == 0) return
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100 * intensity.toLong(), 50, 100 * intensity.toLong()), -1))
        }
        playSynthClick(600.0, 80, 0.5)
    }

    fun triggerClick(context: Context) {
        val intensity = getIntensity(context)
        if (intensity == 0) return
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val amplitude = when (intensity) {
             1 -> 50
             3 -> 255
             else -> VibrationEffect.DEFAULT_AMPLITUDE
        }
        val duration = when (intensity) {
             1 -> 15L
             3 -> 40L
             else -> 25L
        }
        
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
        } else {
            vibrator.vibrate(duration)
        }
        playSynthClick(1800.0, 15, 0.35)
    }

    private fun getIntensity(context: Context): Int {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("haptic_intensity", 2)
    }

    private fun playSynthClick(frequencyHz: Double, durationMs: Int, volume: Double) {
        Thread {
            try {
                val sampleRate = 44100
                val numSamples = (sampleRate * durationMs / 1000)
                val generatedSnd = ByteArray(2 * numSamples)
                
                for (i in 0 until numSamples) {
                    val angle = 2.0 * Math.PI * i / (sampleRate / frequencyHz)
                    // Synthesize linear/exponential decay envelope
                    val fade = (numSamples - i).toDouble() / numSamples
                    val valSample = (Math.sin(angle) * 32767.0 * volume * fade).toInt()
                    
                    generatedSnd[2 * i] = (valSample and 0x00ff).toByte()
                    generatedSnd[2 * i + 1] = ((valSample and 0xff00) ushr 8).toByte()
                }
                
                val audioTrack = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    generatedSnd.size,
                    AudioTrack.MODE_STATIC
                )
                audioTrack.write(generatedSnd, 0, generatedSnd.size)
                audioTrack.play()
                
                Thread.sleep(durationMs.toLong() + 15)
                try {
                    audioTrack.stop()
                } catch (e: Exception) {}
                audioTrack.release()
            } catch (e: Exception) {}
        }.start()
    }
}

