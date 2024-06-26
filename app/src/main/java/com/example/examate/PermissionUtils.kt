package com.example.examate

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

object PermissionUtils {
    const val DND_PERMISSION_REQUEST_CODE = 1001

    fun checkDoNotDisturbEnabled(activity: Activity, onEnabled: () -> Unit) {
        val notificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Check if Do Not Disturb mode is enabled
        if (notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE) {
            onEnabled()
        } else {
            showEnableDoNotDisturbDialog(activity)
        }
    }

    private fun showEnableDoNotDisturbDialog(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.permission_required))
            .setMessage(activity.getString(R.string.dnd_enable_required))
            .setPositiveButton(activity.getString(R.string.go_to_settings)) { dialog, _ ->
                val intent = Intent(Settings.ACTION_SOUND_SETTINGS) // Open Sound settings to enable Do Not Disturb
                activity.startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton(activity.getString(R.string.cancel)) { dialog, _ ->
                Toast.makeText(activity, activity.getString(R.string.permission_needed_to_proceed), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .show()
    }

    fun checkDoNotDisturbPermission(activity: Activity, onPermissionGranted: () -> Unit) {
        val notificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Check if the app has Do Not Disturb access
        if (notificationManager.isNotificationPolicyAccessGranted) {
            onPermissionGranted()
        } else {
            showRequestDoNotDisturbPermissionDialog(activity)
        }
    }

    private fun showRequestDoNotDisturbPermissionDialog(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.permission_required))
            .setMessage(activity.getString(R.string.dnd_permission_required))
            .setPositiveButton(activity.getString(R.string.go_to_settings)) { dialog, _ ->
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                activity.startActivityForResult(intent, DND_PERMISSION_REQUEST_CODE)
                dialog.dismiss()
            }
            .setNegativeButton(activity.getString(R.string.cancel)) { dialog, _ ->
                Toast.makeText(activity, activity.getString(R.string.permission_needed_to_proceed), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .show()
    }

}
