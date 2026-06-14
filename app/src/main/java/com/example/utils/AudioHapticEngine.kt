package com.example.utils

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object AudioHapticEngine {
    var vibrateIntensityMultiplier: Float = 1.0f
    var currentRhythm: String = "Crisp"

    fun triggerSuccess(context: Context) {
        val intensity = (2 * vibrateIntensityMultiplier).toInt().coerceIn(0, 3)
        if (intensity == 0) return
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 40 * intensity.toLong(), 60, 20 * intensity.toLong()), -1))
        }
        playSynthClick(1500.0, 30, 0.4 * vibrateIntensityMultiplier)
    }

    fun triggerError(context: Context) {
        val intensity = (2 * vibrateIntensityMultiplier).toInt().coerceIn(0, 3)
        if (intensity == 0) return
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100 * intensity.toLong(), 50, 100 * intensity.toLong()), -1))
        }
        playSynthClick(600.0, 80, 0.5 * vibrateIntensityMultiplier)
    }

    fun triggerClick(context: Context) {
        val intensity = (2 * vibrateIntensityMultiplier).toInt().coerceIn(0, 3)
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
        playSynthClick(1800.0, 15, 0.35 * vibrateIntensityMultiplier)
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

    // PREMIUM HAVEN AUDIO/HAPTIC IDENTITY
    fun triggerFocusStarted(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            val waveform = when (currentRhythm) {
                "Smooth" -> longArrayOf(0, 100, 100, 100)
                "Bouncy" -> longArrayOf(0, 20, 50, 40, 50, 60)
                else -> longArrayOf(0, 50, 100, 50) // Crisp
            }
            vibrator.vibrate(VibrationEffect.createWaveform(waveform, -1))
        }
        playSynthClick(800.0, 40, 0.4)
        Thread.sleep(80)
        playSynthClick(1200.0, 60, 0.4)
    }

    fun triggerFocusCompleted(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            val waveform = when (currentRhythm) {
                "Smooth" -> longArrayOf(0, 80, 80, 80, 80, 100)
                "Bouncy" -> longArrayOf(0, 20, 30, 20, 30, 40, 50, 60)
                else -> longArrayOf(0, 40, 50, 40, 50, 80) // Crisp
            }
            vibrator.vibrate(VibrationEffect.createWaveform(waveform, -1))
        }
        playSynthClick(1500.0, 30, 0.3)
        Thread.sleep(60)
        playSynthClick(1800.0, 40, 0.3)
        Thread.sleep(80)
        playSynthClick(2200.0, 60, 0.4)
    }

    fun triggerAchievement(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            val waveform = when (currentRhythm) {
                "Smooth" -> longArrayOf(0, 60, 60, 60, 60, 120)
                "Bouncy" -> longArrayOf(0, 15, 30, 15, 30, 15, 30, 100)
                else -> longArrayOf(0, 30, 50, 30, 50, 100) // Crisp
            }
            vibrator.vibrate(VibrationEffect.createWaveform(waveform, -1))
        }
        playSynthClick(2500.0, 40, 0.4)
        Thread.sleep(50)
        playSynthClick(3000.0, 80, 0.5)
    }
    
    fun triggerAINotification(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 20, 100, 20), -1))
        }
        playSynthClick(3000.0, 40, 0.2)
        Thread.sleep(60)
        playSynthClick(2000.0, 60, 0.3)
    }

    fun triggerDeepWorkActivated(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 20, 200), -1))
        }
        playSynthClick(400.0, 150, 0.6)
    }
}

