import android.content.Intent
import android.provider.Settings
import android.app.Activity
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.examate.R

object PermissionUtils {
    fun checkDoNotDisturbPermission(activity: Activity, onPermissionGranted: () -> Unit) {
        val zenMode = Settings.Global.getInt(activity.contentResolver, "zen_mode", 0)
        if (zenMode != 0) {
            onPermissionGranted()
        } else {
            showRequestDNDPermission(activity)
        }
    }

    private fun showRequestDNDPermission(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.permission_required))
            .setMessage(activity.getString(R.string.dnd_permission_required))
            .setPositiveButton(activity.getString(R.string.go_to_settings)) { dialog, _ ->
                val intent = Intent(Settings.ACTION_NOTIFICATION_ASSISTANT_SETTINGS)
                activity.startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton(activity.getString(R.string.cancel)) { dialog, _ ->
                Toast.makeText(activity, activity.getString(R.string.permission_needed_to_proceed), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .show()
    }
}
