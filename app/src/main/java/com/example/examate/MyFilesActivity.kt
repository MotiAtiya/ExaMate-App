package com.example.examate

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.examate.databinding.ActivityMyFilesBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.File
import java.io.FileOutputStream

class MyFilesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyFilesBinding
    private lateinit var filesAdapter: FilesAdapter
    private lateinit var sharedPreferences: android.content.SharedPreferences
    private lateinit var classId: String
    private var isEditMode = false
    private val localFiles = mutableListOf<File>()

    private lateinit var selectFileLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        classId = intent.getStringExtra("classId") ?: ""
        isEditMode = intent.getBooleanExtra("isEditMode", false)

        // Initialize RecyclerView and set empty adapter initially
        binding.recyclerViewFiles.layoutManager = LinearLayoutManager(this)
        filesAdapter = FilesAdapter(emptyList(), this::openFile, this::removeFile)
        binding.recyclerViewFiles.adapter = filesAdapter

        binding.buttonAddFile.setOnClickListener {
            selectFile()
        }

        // Initialize the ActivityResultLauncher
        selectFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.also { uri ->
                    saveFileLocally(uri)
                }
            }
        }

        if (isEditMode) {
            // Load existing files for the class
            loadClassFiles(classId)
        } else {
            // Load local files
            loadLocalFiles()
        }
    }

    private fun selectFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }
        selectFileLauncher.launch(intent)
    }

    private fun loadClassFiles(classId: String) {
        val firestore = Firebase.firestore
        val classDocRef = firestore.collection("classes").document(classId)

        classDocRef.collection("files").get()
            .addOnSuccessListener { documents ->
                val fileList = mutableListOf<String>()
                for (document in documents) {
                    val fileName = document.getString("fileName")
                    fileName?.let { fileList.add(it) }
                }
                // Update your UI with the file list
                filesAdapter.updateFiles(fileList)
                binding.emptyTextView.visibility = if (fileList.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting documents: ", exception)
            }
    }

    private fun loadLocalFiles() {
        val fileNames = sharedPreferences.getStringSet("fileNames", mutableSetOf()) ?: mutableSetOf()
        filesAdapter.updateFiles(fileNames.toList())
        binding.emptyTextView.visibility = if (fileNames.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun saveFileLocally(uri: Uri) {
        val fileName = getFileName(uri)
        if (fileName.isNotEmpty()) {
            // Save file to internal storage
            val destinationFile = File(filesDir, fileName)
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            // Update local files list
            localFiles.add(destinationFile)
            val fileNames = localFiles.map { it.name }.toSet()
            sharedPreferences.edit().putStringSet("fileNames", fileNames).apply()

            // Refresh file list
            filesAdapter.updateFiles(fileNames.toList())
            binding.emptyTextView.visibility = if (fileNames.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        } else {
            Toast.makeText(this, "Failed to get file name", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileName(uri: Uri): String {
        var result = ""
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    result = cursor.getString(displayNameIndex)
                }
            }
        }
        return result
    }

    private fun openFile(fileName: String) {
        val file = File(filesDir, fileName)
        if (file.exists()) {
            val intent = Intent(this, PdfViewerActivity::class.java).apply {
                putExtra("PDF_FILE_NAME", fileName)
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
            Log.e("MyFilesActivity", "File not found: $fileName")
        }
    }

    private fun removeFile(fileName: String) {
        val file = File(filesDir, fileName)
        if (file.exists()) {
            file.delete()
        }

        // Update local files list
        localFiles.removeIf { it.name == fileName }
        val fileNames = localFiles.map { it.name }.toSet()
        sharedPreferences.edit().putStringSet("fileNames", fileNames).apply()

        // Refresh file list
        filesAdapter.updateFiles(fileNames.toList())
        binding.emptyTextView.visibility = if (fileNames.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    fun getSelectedFiles(): List<String> {
        return localFiles.map { it.absolutePath }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val data = Intent().apply {
            putStringArrayListExtra("filePaths", ArrayList(getSelectedFiles()))
        }
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    companion object {
        private const val REQUEST_CODE_SELECT_FILE = 1
    }
}