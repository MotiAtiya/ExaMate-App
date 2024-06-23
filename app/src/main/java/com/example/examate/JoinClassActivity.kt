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
import com.google.zxing.integration.android.IntentIntegrator
import java.text.SimpleDateFormat
import java.util.*

class JoinClassActivity : AppCompatActivity() {
    private lateinit var binding: ActivityJoinClassBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinClassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonSubmit.setOnClickListener {
            startQrCodeScanner()
        }

        binding.buttonEnterId.setOnClickListener {
            showEnterClassIdDialog()
        }
    }

    private fun startQrCodeScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setOrientationLocked(false)
        integrator.setPrompt(getString(R.string.scan_qr_code_prompt))
        integrator.initiateScan()
    }

    private fun showEnterClassIdDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.enter_class_id))

        val input = EditText(this)
        input.hint = getString(R.string.class_id_hint)
        builder.setView(input)

        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, getString(R.string.cancelled), Toast.LENGTH_LONG).show()
            } else {
                val classId = result.contents
                joinClass(classId)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
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
                        val classDetails = jsonObject.getAsJsonObject("classDetails")
                        val testDate = classDetails.get("testDate").asString
                        val testStartTime = classDetails.get("testStartTime").asString
                        val testTimeHours = classDetails.get("testTimeHours").asInt
                        val testTimeMinutes = classDetails.get("testTimeMinutes").asInt
                        val className = classDetails.get("name").asString
                        val isOpenMaterialAllowed = classDetails.get("openMaterial").asBoolean // Add this line

                        val currentDateTime = Calendar.getInstance()
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

                        val testDateTime = Calendar.getInstance().apply {
                            time = dateFormat.parse(testDate)
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
                                putExtra("IS_OPEN_MATERIAL_ALLOWED", isOpenMaterialAllowed) // Add this line
                            }
                            startActivity(intent)
                            finish()  // Close this activity
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
