package com.example.examate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.examate.data.ClassItem
import com.example.examate.data.GetClassesByIdRequest
import com.example.examate.databinding.ActivityMyClassesBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyClassesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyClassesBinding
    private lateinit var classesAdapter: ClassesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MyClassesActivity", "on Create")

        binding = ActivityMyClassesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize RecyclerView and set empty adapter initially
        binding.recyclerViewClasses.layoutManager = LinearLayoutManager(this)
        classesAdapter = ClassesAdapter(emptyList(),
            onDeleteClick = { classItem -> deleteClass(classItem) },
            onEditClick = { classItem -> editClass(classItem) },
            onStartExamClick = { classItem -> startExam(classItem) }
        )
        binding.recyclerViewClasses.adapter = classesAdapter

        // Retrieve teacherId from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val teacherId = sharedPreferences.getString("TeacherId", null)

        if (teacherId != null) {
            // Prepare the POST request body
            val requestBody = GetClassesByIdRequest(
                teacherId = teacherId
            )

            // Fetch data and update adapter
            NetworkUtils.postRequest("getClassesById", requestBody) { jsonElement ->
                if (jsonElement != null) {
                    val classesJsonArray = jsonElement.asJsonObject.getAsJsonArray("classes")
                    val classesList = mutableListOf<ClassItem>()
                    classesJsonArray.forEach { jsonElement ->
                        val classItem = jsonElement.asJsonObject
                        classesList.add(
                            ClassItem(
                                classId = classItem.get("classId").asString,
                                name = classItem.get("name").asString,
                                openMaterial = classItem.get("openMaterial").asBoolean,
                                testDate = classItem.get("testDate").asString,
                                testStartTime = classItem.get("testStartTime").asString,
                                testTimeHours = classItem.get("testTimeHours").asInt,
                                testTimeMinutes = classItem.get("testTimeMinutes").asInt
                            )
                        )
                    }

                    // Sort the list by date and time
                    classesList.sortBy { it.getParsedDateTime() }

                    // Update adapter with fetched data
                    classesAdapter.updateClasses(classesList)
                } else {
                    Log.d("POST response", "Failed to get response")
                }
            }
        } else {
            Toast.makeText(this, "Teacher ID not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteClass(classItem: ClassItem) {
        // Retrieve teacherId from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val teacherId = sharedPreferences.getString("TeacherId", null)

        if (teacherId != null) {
            val requestBody = mapOf(
                "teacherId" to teacherId,
                "classId" to classItem.classId
            )
            Log.d("teacherId: ", teacherId)
            Log.d("classId: ", classItem.classId)

            NetworkUtils.postRequest("deleteClass", requestBody) { jsonElement ->
                if (jsonElement != null && jsonElement.asJsonObject.get("success").asBoolean) {
                    Toast.makeText(this, "Class deleted successfully", Toast.LENGTH_SHORT).show()
                    // Refresh the list after deletion
                    classesAdapter.updateClasses(classesAdapter.classesList.filter { it.classId != classItem.classId })
                } else {
                    Toast.makeText(this, "Failed to delete class", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun editClass(classItem: ClassItem) {
        // Navigate to CreateClassActivity with class details for editing
        val intent = Intent(this, CreateClassActivity::class.java).apply {
            putExtra("classId", classItem.classId)
            putExtra("name", classItem.name)
            putExtra("openMaterial", classItem.openMaterial)
            putExtra("testDate", classItem.testDate)
            putExtra("testStartTime", classItem.testStartTime)
            putExtra("testTimeHours", classItem.testTimeHours)
            putExtra("testTimeMinutes", classItem.testTimeMinutes)
        }
        startActivity(intent)
    }

    private fun startExam(classItem: ClassItem) {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (currentDate != classItem.testDate) {
            Toast.makeText(this, "Exam cannot be started today", Toast.LENGTH_SHORT).show()
        } else {
            // Navigate to TeacherExamModeActivity with class details
            val intent = Intent(this, TeacherExamModeActivity::class.java)
            intent.putExtra("className", classItem.name)
            intent.putExtra("classId", classItem.classId)
            startActivity(intent)
        }
    }
}
