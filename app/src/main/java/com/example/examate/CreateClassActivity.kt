package com.example.examate

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.examate.data.CreateClassRequest
import com.example.examate.data.EditClassRequest
import com.example.examate.databinding.ActivityCreateClassBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File

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

        binding.buttonUploadFiles.setOnClickListener {
            val intent = Intent(this, if (isEditMode) UpdateFilesActivity::class.java else UploadFilesActivity::class.java).apply {
                putExtra("classId", classId)
                putExtra("isEditMode", isEditMode)
            }
            startActivityForResult(intent, REQUEST_CODE_VIEW_FILES)
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

                binding.buttonUploadFiles.setText(R.string.update_files)

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

                    // Show the loading overlay
                    binding.progressOverlay.visibility = View.VISIBLE

                    if (isEditMode) {
                        // Upload selected files for existing class
                        uploadSelectedFiles(classId!!) { fileUploadSuccess ->
                            if (fileUploadSuccess) {
                                // Make the POST request
                                NetworkUtils.postRequest(requestEndpoint, requestBody) { jsonElement ->
                                    runOnUiThread {
                                        // Hide the loading overlay
                                        binding.progressOverlay.visibility = View.GONE

                                        if (jsonElement != null) {
                                            val successMessage = if (isEditMode) R.string.class_updated_success else R.string.class_saved_success
                                            Toast.makeText(applicationContext, successMessage, Toast.LENGTH_SHORT).show()

                                            val editor = sharedPreferences.edit()
                                            editor.remove("fileNames").apply()

                                            val intent = Intent(this, TeacherHomeActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            val failureMessage = if (isEditMode) R.string.class_update_failed else R.string.class_save_failed
                                            Toast.makeText(applicationContext, failureMessage, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(this, "File upload failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        // Create new class and upload files
                        NetworkUtils.postRequest(requestEndpoint, requestBody) { jsonElement ->
                            runOnUiThread {
                                if (jsonElement != null) {
                                    val newClassId = jsonElement.asJsonObject["classId"].asString
                                    uploadSelectedFiles(newClassId) { fileUploadSuccess ->
                                        // Hide the loading overlay
                                        binding.progressOverlay.visibility = View.GONE

                                        if (fileUploadSuccess) {
                                            val successMessage = R.string.class_saved_success
                                            Toast.makeText(applicationContext, successMessage, Toast.LENGTH_SHORT).show()

                                            val editor = sharedPreferences.edit()
                                            editor.remove("fileNames").apply()

                                            val intent = Intent(this, TeacherHomeActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            Toast.makeText(this, "File upload failed", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    // Hide the loading overlay
                                    binding.progressOverlay.visibility = View.GONE
                                    val failureMessage = R.string.class_save_failed
                                    Toast.makeText(applicationContext, failureMessage, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                } else {
                    Toast.makeText(applicationContext, R.string.teacher_id_not_found, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uploadSelectedFiles(classId: String, callback: (Boolean) -> Unit) {
        val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val selectedFiles = sharedPreferences.getStringSet("fileNames", mutableSetOf()) ?: mutableSetOf()

        if (selectedFiles.isEmpty()) {
            callback(true)
            return
        }

        val storage = Firebase.storage
        val storageRef = storage.reference
        val firestore = Firebase.firestore
        val classDocRef = firestore.collection("classes").document(classId)
        var successCount = 0

        for (fileName in selectedFiles) {
            val file = File(filesDir, fileName)

            if (file.exists()) {
                val uri = Uri.fromFile(file)
                val pdfRef = storageRef.child("pdfs/$classId/$fileName")

                pdfRef.putFile(uri)
                    .addOnSuccessListener {
                        pdfRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                            // Save file metadata to Firestore
                            val fileData = hashMapOf(
                                "fileName" to fileName,
                                "downloadUrl" to downloadUrl.toString(),
                                "uploadTime" to System.currentTimeMillis()
                            )
                            classDocRef.collection("files").add(fileData)
                                .addOnSuccessListener {
                                    successCount++
                                    if (successCount == selectedFiles.size) {
                                        sharedPreferences.edit().remove("fileNames").apply()
                                        callback(true)
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Error adding document", e)
                                    callback(false)
                                }
                        }
                    }
                    .addOnFailureListener {
                        callback(false)
                    }
            } else {
                callback(false)
                return
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_VIEW_FILES && resultCode == Activity.RESULT_OK) {
            data?.getStringArrayListExtra("filePaths")?.let { filePaths ->
                val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putStringSet("fileNames", filePaths.toSet())
                editor.apply()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_VIEW_FILES = 1001
    }
}
