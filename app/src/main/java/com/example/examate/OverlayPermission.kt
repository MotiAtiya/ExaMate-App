//package com.example.examate
//
//import android.content.Context
//import android.content.Intent
//import android.net.Uri
//import android.os.Build
//import android.provider.Settings
//
//object OverlayPermission {
//    fun checkPermission(context: Context): Boolean {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            Settings.canDrawOverlays(context)
//        } else {
//            true
//        }
//    }
//
//    fun requestPermission(context: Context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.packageName))
//            (context as MainActivity).startActivityForResult(intent, MainActivity.REQUEST_OVERLAY_PERMISSION)
//        }
//    }
//}
