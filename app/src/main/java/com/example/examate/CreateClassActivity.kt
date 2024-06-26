package com.example.examate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.examate.data.CreateClassRequest
import com.example.examate.data.EditClassRequest
import com.example.examate.databinding.ActivityCreateClassBinding

@Suppress("IMPLICIT_CAST_TO_ANY")
class CreateClassActivity : AppCompatActivity(), TimePickerFragment.TimePickerListener, DatePickerFragment.DatePickerListener,
    HourSelectionDialog.HourSelectionListener, MinuteSelectionDialog.MinuteSelectionListener {

    private lateinit var binding: ActivityCreateClassBinding
    private var selectedHour: Int? = null
    private var selectedMinute: Int? = null
    private var isEditMode = false
    private var classId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("CreateClassActivity", "onCreate")

        binding = ActivityCreateClassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set OnClickListener for pickTime TextView to show TimePickerFragment
        binding.pickTime.setOnClickListener {
            val timePickerFragment = TimePickerFragment()
            timePickerFragment.setListener(this)
            timePickerFragment.show(supportFragmentManager, "timePicker")
        }

        // Set OnClickListener for pickDate TextView to show DatePickerFragment
        binding.pickDate.setOnClickListener {
            val datePickerFragment = DatePickerFragment()
            datePickerFragment.setListener(this)
            datePickerFragment.show(supportFragmentManager, "datePicker")
        }

        binding.selectHourButton.setOnClickListener {
            val dialog = HourSelectionDialog()
            dialog.show(supportFragmentManager, "HourSelectionDialog")
        }

        binding.selectMinuteButton.setOnClickListener {
            val dialog = MinuteSelectionDialog()
            dialog.show(supportFragmentManager, "MinuteSelectionDialog")
        }

        binding.buttonViewFiles.setOnClickListener {
            val intent = Intent(this, MyFilesActivity::class.java)
            startActivity(intent)
        }

        // Check if intent has extra data for editing a class
        intent?.let {
            classId = it.getStringExtra("classId")
            if (classId != null) {
                isEditMode = true
                binding.courseName.setText(it.getStringExtra("name"))
                binding.switch1.isChecked = it.getBooleanExtra("openMaterial", false)
                binding.pickDate.text = it.getStringExtra("testDate")
                binding.pickTime.text = it.getStringExtra("testStartTime")
                selectedHour = it.getIntExtra("testTimeHours", 0)
                selectedMinute = it.getIntExtra("testTimeMinutes", 0)
                binding.selectHourButton.text = selectedHour.toString()
                binding.selectMinuteButton.text = selectedMinute.toString()
            }
        }

        val saveButton = binding.buttonSave
        saveButton.setOnClickListener {
            // Retrieve values from EditText fields
            val courseName = binding.courseName.text.toString()
            val openMaterial = binding.switch1.isChecked
            val testDate = binding.pickDate.text.toString()
            val testStartTime = binding.pickTime.text.toString()
            val testTimeHours = selectedHour
            val testTimeMinutes = selectedMinute

            // Default values from resources
            val defaultDate = getString(R.string.select_date)
            val defaultTime = getString(R.string.select_time)

            // Validate input
            if (courseName.isEmpty() || testDate == defaultDate || testStartTime == defaultTime || testTimeHours == null || testTimeMinutes == null) {
                Toast.makeText(applicationContext, R.string.fill_all_fields_message, Toast.LENGTH_LONG).show()
            } else {
                // Retrieve teacherId from SharedPreferences
                val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
                val teacherId = sharedPreferences.getString("TeacherId", null)

                if (teacherId != null) {
                    val requestBody = if (isEditMode) {
                        EditClassRequest(
                            name = courseName,
                            openMaterial = openMaterial,
                            testDate = testDate,
                            testStartTime = testStartTime,
                            testTimeHours = testTimeHours,
                            testTimeMinutes = testTimeMinutes,
                            teacherId = teacherId,
                            classId = classId!!
                        )
                    } else {
                        CreateClassRequest(
                            name = courseName,
                            openMaterial = openMaterial,
                            testDate = testDate,
                            testStartTime = testStartTime,
                            testTimeHours = testTimeHours,
                            testTimeMinutes = testTimeMinutes,
                            teacherId = teacherId
                        )
                    }

                    val requestEndpoint = if (isEditMode) "editClass" else "createNewClass"
                    Log.d("POST request", requestBody.toString())

                    // Show the loading overlay
                    binding.progressOverlay.visibility = View.VISIBLE

                    // Make the POST request
                    NetworkUtils.postRequest(requestEndpoint, requestBody) { jsonElement ->
                        runOnUiThread {
                            // Hide the loading overlay
                            binding.progressOverlay.visibility = View.GONE

                            if (jsonElement != null) {
                                Log.d("POST response", jsonElement.toString())
                                val successMessage = if (isEditMode) R.string.class_updated_success else R.string.class_saved_success
                                Toast.makeText(applicationContext, successMessage, Toast.LENGTH_SHORT).show()

                                val editor = sharedPreferences.edit()
                                editor.remove("fileNames").apply()

                                val intent = Intent(this, TeacherHomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Log.d("POST response", "Failed to get response")
                                val failureMessage = if (isEditMode) R.string.class_update_failed else R.string.class_save_failed
                                Toast.makeText(applicationContext, failureMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(applicationContext, R.string.teacher_id_not_found, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onTimeSelected(hourOfDay: Int, minute: Int) {
        val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
        binding.pickTime.text = formattedTime
    }

    override fun onDateSelected(year: Int, month: Int, dayOfMonth: Int) {
        val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
        binding.pickDate.text = formattedDate
    }

    override fun onHourSelected(hour: Int) {
        selectedHour = hour
        binding.selectHourButton.text = if (hour == 1) getString(R.string.hour_formatted, hour) else getString(R.string.hours_formatted, hour)
    }

    override fun onMinuteSelected(minute: Int) {
        selectedMinute = minute
        binding.selectMinuteButton.text = if (minute == 1) getString(R.string.minute_formatted, minute) else getString(R.string.minutes_formatted, minute)
    }
}
