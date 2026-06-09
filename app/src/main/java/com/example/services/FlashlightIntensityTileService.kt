package com.example.services

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import android.service.quicksettings.Tile

class FlashlightIntensityTileService : BaseTileService() {
    private var intensityLevel = 0 // 0=Off, 1=Low, 2=Medium, 3=High

    override fun onStartListening() {
        super.onStartListening()
        updateState()
    }

    override fun onClick() {
        super.onClick()
        triggerHapticClick()
        
        intensityLevel = (intensityLevel + 1) % 4
        
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = cameraManager.cameraIdList.firstOrNull()
            if (cameraId != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                    val maxLevel = characteristics.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 1
                    
                    if (intensityLevel == 0) {
                        cameraManager.setTorchMode(cameraId, false)
                    } else {
                        val mappedLevel = maxOf(1, (intensityLevel / 3f * maxLevel).toInt())
                        try {
                            cameraManager.turnOnTorchWithStrengthLevel(cameraId, mappedLevel)
                        } catch (e: Exception) {
                            cameraManager.setTorchMode(cameraId, true)
                        }
                    }
                } else {
                    cameraManager.setTorchMode(cameraId, intensityLevel > 0)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        updateState()
    }

    private fun updateState() {
        val state = if (intensityLevel > 0) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        val subtitle = when(intensityLevel) {
            0 -> "Off"
            1 -> "Low"
            2 -> "Medium"
            3 -> "High"
            else -> "Off"
        }
        updateTileState(state, "Flashlight", subtitle)
    }
}
