package com.example.examate

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.examate.databinding.ActivityExamFilesBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream

class ExamFilesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExamFilesBinding
    private lateinit var filesAdapter: FilesAdapter
    private lateinit var sharedPreferences: android.content.SharedPreferences
    private var isOpenMaterialAllowed = false
    private var classId: String? = null
    private var isFinishingActivity = false
    private val localFiles = mutableListOf<File>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExamFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        isOpenMaterialAllowed = intent.getBooleanExtra("IS_OPEN_MATERIAL_ALLOWED", false)
        classId = intent.getStringExtra("CLASS_ID")

        binding.recyclerViewFiles.layoutManager = LinearLayoutManager(this)
        filesAdapter =
            FilesAdapter(emptyList(), this::openFile, null) // No delete icon in ExamFilesActivity
        binding.recyclerViewFiles.adapter = filesAdapter

        // Show loading GIF
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyTextView.visibility = View.GONE

        // Check if this is the first time viewing files in this session
        if (intent.getBooleanExtra("FIRST_LOAD_FILES", false)) {
            clearLocalFiles() // Clear local files before loading new ones
            if (isOpenMaterialAllowed) {
                copySharedFilesToLocalFolder()
            }
            loadFiles() // Download the teacher's files to the device
        } else {
            updateFilesList()
        }
        disableSystemUI()
    }

    private fun clearLocalFiles() {
        val examFilesDir = File(getExternalFilesDir(null), "ExamFiles")
        if (examFilesDir.exists() && examFilesDir.isDirectory) {
            examFilesDir.listFiles()?.forEach { it.delete() }
        }
    }

    private fun copySharedFilesToLocalFolder() {
        val fileUris = sharedPreferences.getStringSet("fileUris", mutableSetOf()) ?: mutableSetOf()
        val examFilesDir = File(getExternalFilesDir(null), "ExamFiles")

        fileUris.forEach { fileUriString ->
            val (fileName, uriString) = fileUriString.split(',')
            val sourceUri = Uri.parse(uriString)
            val destFile = File(examFilesDir, fileName)

            try {
                // Ensure we have the persisted URI permission
                contentResolver.takePersistableUriPermission(
                    sourceUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    FileOutputStream(destFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                localFiles.add(destFile)
            } catch (_: Exception) {
            }
        }
    }

    private fun loadFiles() {
        if (classId != null) {
            val firestore = Firebase.firestore
            val classDocRef = firestore.collection("classes").document(classId!!)

            classDocRef.collection("files").get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val fileName = document.getString("fileName")
                        val downloadUrl = document.getString("downloadUrl")
                        if (fileName != null && downloadUrl != null) {
                            val localFile = File(getExternalFilesDir("ExamFiles"), fileName)
                            if (!localFile.exists()) {
                                downloadFileFromFirebase(downloadUrl, localFile)
                            } else {
                                localFiles.add(localFile)
                            }
                        }
                    }
                    runOnUiThread {
                        updateFilesList()
                        if (localFiles.isEmpty()) {
                            binding.emptyTextView.visibility = View.VISIBLE
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Error getting documents: ", exception)
                    runOnUiThread {
                        binding.progressBar.visibility = View.GONE
                        binding.emptyTextView.visibility = View.VISIBLE
                    }
                }
        }
    }

    private fun downloadFileFromFirebase(downloadUrl: String, destinationFile: File) {
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(downloadUrl)
        storageRef.getFile(destinationFile)
            .addOnSuccessListener {
                localFiles.add(destinationFile)
                runOnUiThread { updateFilesList() }
            }
            .addOnFailureListener { }
    }

    private fun openFile(fileName: String) {
        val file = File(getExternalFilesDir("ExamFiles"), fileName)
        if (file.exists()) {
            enableSystemUI()
            val intent = Intent(this, PdfViewerActivity::class.java).apply {
                putExtra("PDF_FILE_PATH", file.absolutePath)
                putExtra("BLOCK_NAVIGATION", true) // Pass the flag
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
            Log.e("ExamFilesActivity", "File not found: $fileName")
        }
    }

    private fun updateFilesList() {
        val examFilesDir = File(getExternalFilesDir(null), "ExamFiles")
        if (examFilesDir.exists() && examFilesDir.isDirectory) {
            localFiles.clear()
            localFiles.addAll(examFilesDir.listFiles()?.toList() ?: emptyList())
        }
        val allFiles = localFiles.map { it.name }
        filesAdapter.updateFiles(allFiles)
        binding.progressBar.visibility = View.GONE
        binding.emptyTextView.visibility = if (allFiles.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus && !isFinishingActivity) {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningTasks = activityManager.appTasks
            val topActivity = runningTasks[0].taskInfo.topActivity
            if (topActivity?.className != StudentExamModeActivity::class.java.name &&
                topActivity?.className != PdfViewerActivity::class.java.name
            ) {
                val intent = Intent(this, StudentExamModeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(intent)
            }

        }
    }

    override fun onPause() {
        super.onPause()
        onWindowFocusChanged(false)
    }

    override fun onStop() {
        super.onStop()
        onWindowFocusChanged(false)
    }

    private fun disableSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.systemBars())
                it.systemBarsBehavior = WindowInsetsController
                    .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun enableSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.show(WindowInsets.Type.systemBars())
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onBackPressed() {
        enableSystemUI()
        super.onBackPressed()
    }
}
