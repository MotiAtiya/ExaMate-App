package com.example.examate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.examate.data.JoinClassRequest
import com.example.examate.databinding.ActivityJoinClassBinding
import java.text.SimpleDateFormat
import java.util.*

class JoinClassActivity : AppCompatActivity() {
    private lateinit var binding: ActivityJoinClassBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinClassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonSubmit.setOnClickListener {
            val classId = binding.classIdInput.text.toString()
            if (classId.isEmpty()) {
                Toast.makeText(this, "Please enter a class ID", Toast.LENGTH_SHORT).show()
            } else {
                joinClass(classId)
            }
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
                            Toast.makeText(this, "This class is not scheduled for the current time.", Toast.LENGTH_LONG).show()
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            val remainingTimeMillis = testEndDateTime.timeInMillis - currentDateTime.timeInMillis
                            val intent = Intent(this, StudentExamModeActivity::class.java).apply {
                                putExtra("EXAM_NAME", className)
                                putExtra("REMAINING_TIME_MILLIS", remainingTimeMillis)
                            }
                            startActivity(intent)
                            finish()  // Close this activity
                        }
                    } else {
                        Toast.makeText(this, "Class ID not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Failed to join class", Toast.LENGTH_SHORT).show()
                    Log.e("JoinClassActivity", "Invalid response or not a JSON object: $jsonElement")
                }
            }
        } else {
            Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
        }
    }
}
