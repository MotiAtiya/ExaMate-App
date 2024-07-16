package com.example.examate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.examate.data.JoinClassRequest
import com.example.examate.databinding.ActivityJoinClassBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.text.SimpleDateFormat
import java.util.*

class JoinClassActivity : AppCompatActivity() {
    private lateinit var binding: ActivityJoinClassBinding

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val classId = result.contents
            joinClass(classId)
        } else {
            Toast.makeText(this, getString(R.string.cancelled), Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinClassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonScanQR.setOnClickListener {
            PermissionUtils.checkDoNotDisturbPermission(this) {
                startQrCodeScanner()
            }
        }

        binding.buttonEnterId.setOnClickListener {
            PermissionUtils.checkDoNotDisturbPermission(this) {
                showEnterClassIdDialog()
            }
        }
    }

    private fun startQrCodeScanner() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setCameraId(0)
            setBeepEnabled(false)
            setBarcodeImageEnabled(true)
            setOrientationLocked(false)
            setPrompt(getString(R.string.scan_qr_code_prompt))
        }
        barcodeLauncher.launch(options)
    }

    private fun showEnterClassIdDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.enter_class_id))

        val input = EditText(this)
        input.hint = getString(R.string.class_id_hint)
        builder.setView(input)

        builder.setPositiveButton(getString(R.string.ok)) { _, _ ->
            val classId = input.text.toString()
            if (classId.isEmpty()) {
                Toast.makeText(this, getString(R.string.please_enter_class_id), Toast.LENGTH_SHORT).show()
            } else {
                joinClass(classId)
            }
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun joinClass(classId: String) {
        val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val firstName = sharedPreferences.getString("FirstName", null)
        val lastName = sharedPreferences.getString("LastName", null)
        val email = sharedPreferences.getString("Email", null)

        if (firstName != null && lastName != null && email != null) {
            val requestBody = JoinClassRequest(
                classId = classId,
                firstName = firstName,
                lastName = lastName,
                email = email
            )

            NetworkUtils.postRequest("joinClass", requestBody) { jsonElement ->
                if (jsonElement != null && jsonElement.isJsonObject) {
                    val jsonObject = jsonElement.asJsonObject
                    if (jsonObject.has("studentId")) {
                        val studentId = jsonObject.get("studentId").asString
                        val editor = sharedPreferences.edit()
                        editor.putString("StudentId", studentId).apply()

                        val classDetails = jsonObject.getAsJsonObject("classDetails")
                        val testDate = classDetails.get("testDate").asString
                        val testStartTime = classDetails.get("testStartTime").asString
                        val testTimeHours = classDetails.get("testTimeHours").asInt
                        val testTimeMinutes = classDetails.get("testTimeMinutes").asInt
                        val className = classDetails.get("name").asString
                        val isOpenMaterialAllowed = classDetails.get("openMaterial").asBoolean

                        val currentDateTime = Calendar.getInstance()
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val testDateTime = Calendar.getInstance().apply {
                            time = dateFormat.parse(testDate)!!
                            val (hours, minutes) = testStartTime.split(":").map { it.toInt() }
                            set(Calendar.HOUR_OF_DAY, hours)
                            set(Calendar.MINUTE, minutes)
                        }

                        val testEndDateTime = Calendar.getInstance().apply {
                            time = testDateTime.time
                            add(Calendar.HOUR_OF_DAY, testTimeHours)
                            add(Calendar.MINUTE, testTimeMinutes)
                        }

                        if (currentDateTime.before(testDateTime) || currentDateTime.after(testEndDateTime)) {
                            Toast.makeText(this, getString(R.string.class_not_scheduled), Toast.LENGTH_LONG).show()
                            val intent = Intent(this, StudentHomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            val remainingTimeMillis = testEndDateTime.timeInMillis - currentDateTime.timeInMillis
                            val intent = Intent(this, StudentExamModeActivity::class.java).apply {
                                putExtra("EXAM_NAME", className)
                                putExtra("REMAINING_TIME_MILLIS", remainingTimeMillis)
                                putExtra("IS_OPEN_MATERIAL_ALLOWED", isOpenMaterialAllowed)
                                putExtra("STUDENT_ID", studentId)
                                putExtra("CLASS_ID", classId)
                            }
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.class_id_not_found), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, getString(R.string.failed_to_join_class), Toast.LENGTH_SHORT).show()
                    Log.e("JoinClassActivity", "Invalid response or not a JSON object: $jsonElement")
                }
            }
        } else {
            Toast.makeText(this, getString(R.string.user_data_not_found), Toast.LENGTH_SHORT).show()
        }
    }
}
