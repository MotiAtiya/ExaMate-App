package com.example.examate

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.examate.databinding.ActivityTeacherExamModeBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TeacherExamModeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherExamModeBinding
    private lateinit var studentsAdapter: StudentsAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var classId: String? = null
    private var disconnectId: String? = null
    private var teacherId: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private val updatesList = mutableListOf<String>()
    private var isButtonFlashing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherExamModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val className = intent.getStringExtra("className") ?: ""
        classId = intent.getStringExtra("classId") ?: ""
        disconnectId = intent.getStringExtra("disconnectId") ?: ""

        binding.className.text = className

        binding.buttonShowId.setOnClickListener {
            classId?.let { id ->
                showQRCodeDialog(id)
            }
        }

        binding.buttonShowDisconnectId.setOnClickListener {
            disconnectId?.let { id ->
                showQRCodeDialog(id)
            }
        }

        binding.buttonUpdates.setOnClickListener {
            showUpdatesDialog()
            stopButtonFlashing()
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
        startPeriodicUpdateCheck() // Start periodic server checks
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

    private fun showQRCodeDialog(id: String) {
        val qrCodeBitmap = generateQRCode(id)
        val imageView = ImageView(this)
        imageView.setImageBitmap(qrCodeBitmap)

        val customTitle = LayoutInflater.from(this).inflate(R.layout.dialog_title_centered, null) as TextView

        AlertDialog.Builder(this)
            .setCustomTitle(customTitle)
            .setView(imageView)
            .setPositiveButton(getString(R.string.close)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun loadConnectedStudents() {
        val requestBody = mapOf(
            "classId" to classId,
            "teacherId" to teacherId
        )
        NetworkUtils.postRequest("getConnectedStudents", requestBody) { jsonElement ->
            runOnUiThread {
                swipeRefreshLayout.isRefreshing = false
                if (jsonElement != null) {
                    val studentsJsonArray = jsonElement.asJsonObject.getAsJsonArray("students")
                    val studentsList = mutableListOf<Student>()
                    studentsJsonArray.forEach { jsonElement ->
                        val studentObject = jsonElement.asJsonObject
                        studentsList.add(
                            Student(
                                firstName = studentObject.get("firstName").asString,
                                lastName = studentObject.get("lastName").asString,
                                email = studentObject.get("email").asString
                            )
                        )
                    }
                    // Update adapter with fetched data
                    studentsAdapter.updateStudents(studentsList)
                    binding.emptyTextView.visibility = if (studentsList.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                } else {
                    binding.emptyTextView.visibility = android.view.View.VISIBLE
                    Toast.makeText(this, "Failed to load students", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startPeriodicUpdateCheck() {
        handler.post(object : Runnable {
            override fun run() {
                checkForUpdates()
                handler.postDelayed(this, 5000) // Schedule next check in 5 seconds
            }
        })
    }

    private var newUpdatesCount = 0

    private fun checkForUpdates() {
        val requestBody = mapOf(
            "classId" to classId,
            "teacherId" to teacherId
        )
        NetworkUtils.postRequest("getNewUpdates", requestBody) { jsonElement ->
            runOnUiThread {
                if (jsonElement != null && jsonElement.isJsonObject) {
                    val jsonObject = jsonElement.asJsonObject
                    if (jsonObject.has("updates")) {
                        val updatesArray = jsonObject.getAsJsonArray("updates")
                        if (updatesArray.size() > 0) {
                            updatesArray.forEach { updateElement ->
                                val updateObject = updateElement.asJsonObject
                                val note = updateObject.getAsJsonObject("note").get("note").asString
                                val timestamp = updateObject.getAsJsonObject("note").get("timestamp").asJsonObject
                                val timeInMillis = timestamp.get("_seconds").asLong * 1000 + timestamp.get("_nanoseconds").asLong / 1000000
                                val formattedTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timeInMillis))
                                updatesList.add(0, "$formattedTime: $note")
                                newUpdatesCount++
                                Toast.makeText(this, note, Toast.LENGTH_LONG).show()
                            }
                            startButtonFlashing()
                        }
                    }
                }
            }
        }
    }

    private fun showUpdatesDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.updates))

        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_updates, null)
        val updatesContainer = dialogLayout.findViewById<LinearLayout>(R.id.updatesContainer)

        updatesList.forEachIndexed { index, update ->
            val updateTextView = TextView(this).apply {
                text = update
                textSize = 16f
                setPadding(8, 8, 8, 8)
                setBackgroundColor(if (index < newUpdatesCount) getColor(R.color.light_gray) else getColor(R.color.white))
            }
            updatesContainer.addView(updateTextView)

            // Add a divider line between updates
            if (index < updatesList.size - 1) {
                val dividerView = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2 // Height of the divider line
                    ).apply {
                        setMargins(0, 8, 0, 8) // Add some margin to the divider
                    }
                    setBackgroundColor(getColor(R.color.collectionBlack)) // Color of the divider line
                }
                updatesContainer.addView(dividerView)
            }
        }

        builder.setView(dialogLayout)
        builder.setPositiveButton(getString(R.string.close)) { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.setOnDismissListener {
            newUpdatesCount = 0 // Reset the count of new updates
            updatesList.replaceAll { it.replaceFirst(Regex("^\\d{2}:\\d{2}:\\d{2}: "), "") } // Remove the time prefix
        }
        dialog.show()
    }



    private fun startButtonFlashing() {
        if (!isButtonFlashing) {
            isButtonFlashing = true
            handler.post(flashButtonRunnable)
        }
    }

    private fun stopButtonFlashing() {
        isButtonFlashing = false
        handler.removeCallbacks(flashButtonRunnable)
        binding.buttonUpdates.alpha = 1f // Reset alpha to default
    }

    private val flashButtonRunnable = object : Runnable {
        private var isVisible = true

        override fun run() {
            if (isButtonFlashing) {
                binding.buttonUpdates.alpha = if (isVisible) 1f else 0.5f
                isVisible = !isVisible
                handler.postDelayed(this, 500) // Flash every 500ms
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Stop the periodic checks when the activity is destroyed
    }
}
