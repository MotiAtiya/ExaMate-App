package com.example.examate

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.example.examate.databinding.ActivityMainBinding

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = layoutInflater.inflate(R.layout.activity_main, null)

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        windowManager.addView(overlayView, layoutParams)
        Log.d("OverlayService", "Overlay view added")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
            Log.d("OverlayService", "Overlay view removed")
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
