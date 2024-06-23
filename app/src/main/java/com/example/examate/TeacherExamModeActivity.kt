package com.example.examate

import Student
import StudentsAdapter
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.examate.databinding.ActivityTeacherExamModeBinding
import com.example.examate.databinding.ItemStudentBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.JsonArray

class TeacherExamModeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherExamModeBinding
    private lateinit var studentsAdapter: StudentsAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var classId: String? = null
    private var teacherId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherExamModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val className = intent.getStringExtra("className") ?: ""
        classId = intent.getStringExtra("classId") ?: ""

        binding.className.text = className

        binding.buttonShowId.setOnClickListener {
            classId?.let {
                // Generate QR code
                val qrCodeBitmap = generateQRCode(it)

                // Show dialog with QR code
                val imageView = ImageView(this)
                imageView.setImageBitmap(qrCodeBitmap)

                val customTitle = LayoutInflater.from(this).inflate(R.layout.dialog_title_centered, null) as TextView

                AlertDialog.Builder(this)
                    .setCustomTitle(customTitle)
                    .setView(imageView)
                    .setPositiveButton("Close") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }

        // Initialize SwipeRefreshLayout and RecyclerView
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            loadConnectedStudents()
        }

        binding.recyclerViewStudents.layoutManager = LinearLayoutManager(this)
        studentsAdapter = StudentsAdapter(emptyList())
        binding.recyclerViewStudents.adapter = studentsAdapter

        // Retrieve teacherId from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        teacherId = sharedPreferences.getString("TeacherId", null)

        loadConnectedStudents()
    }

    private fun generateQRCode(text: String): Bitmap {
        val size = 512 // pixels
        val qrCodeWriter = QRCodeWriter()
        return try {
            val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
                }
            }
            bitmap
        } catch (e: WriterException) {
            Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).apply {
                eraseColor(0xFFFFFFFF.toInt()) // Set to white in case of error
            }
        }
    }

    private fun loadConnectedStudents() {
        val requestBody = mapOf(
            "classId" to classId/*,
            "teacherId" to teacherId*/
        )
        NetworkUtils.postRequest("getAllStudentsByClass" /*"getConnectedStudent"*/, requestBody) { jsonElement ->
//            runOnUiThread {
                swipeRefreshLayout.isRefreshing = false
                if (jsonElement != null && jsonElement.isJsonArray) {
                    val studentsJsonArray = jsonElement.asJsonArray
                    val students = mutableListOf<Student>()
                    studentsJsonArray.forEach { jsonElement ->
                        val studentObject = jsonElement.asJsonObject
                        students.add(
                            Student(
                                firstName = studentObject.get("firstName").asString,
                                lastName = studentObject.get("lastName").asString,
                                email = studentObject.get("email").asString
                            )
                        )
                    }
                    studentsAdapter.updateStudents(students)
                } else {
                    Toast.makeText(this, "Failed to load students", Toast.LENGTH_SHORT).show()
                }
            }
//        }
    }
}
