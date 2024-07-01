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
import com.example.examate.databinding.ActivityUpdateFilesBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class UpdateFilesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateFilesBinding
    private lateinit var filesAdapter: FilesAdapter
    private lateinit var sharedPreferences: android.content.SharedPreferences
    private lateinit var classId: String
    private val localFiles = mutableListOf<File>()
    private val serverFiles = mutableListOf<File>()

    private lateinit var selectFileLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        classId = intent.getStringExtra("classId") ?: ""

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

        // Load existing files for the class
        loadClassFiles(classId)
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
                val fileList = mutableListOf<File>()
                for (document in documents) {
                    val fileName = document.getString("fileName")
                    val downloadUrl = document.getString("downloadUrl")
                    if (fileName != null && downloadUrl != null) {
                        val localFile = File(filesDir, fileName)
                        if (!localFile.exists()) {
                            downloadFile(downloadUrl, localFile)
                        }
                        fileList.add(localFile)
                        serverFiles.add(localFile)
                    }
                }
                // Update your UI with the file list
                updateFilesList()
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting documents: ", exception)
            }
    }

    private fun downloadFile(downloadUrl: String, destinationFile: File) {
        try {
            val url = URL(downloadUrl)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.inputStream.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            Log.e("DownloadFile", "Error downloading file: ", e)
        }
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
            updateFilesList()
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
            Log.e("UpdateFilesActivity", "File not found: $fileName")
        }
    }

    private fun removeFile(fileName: String) {
        val file = File(filesDir, fileName)
        if (file.exists()) {
            file.delete()
        }

        // Remove from local files list if present
        localFiles.removeIf { it.name == fileName }
        // Remove from server files list if present
        serverFiles.removeIf { it.name == fileName }
        updateFilesList()
    }

    private fun updateFilesList() {
        val allFiles = (serverFiles + localFiles).map { it.name }
        filesAdapter.updateFiles(allFiles)
        binding.emptyTextView.visibility = if (allFiles.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
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
}
