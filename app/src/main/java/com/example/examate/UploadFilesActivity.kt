package com.example.examate

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.examate.databinding.ActivityUploadFilesBinding
import java.io.File
import java.io.FileOutputStream

class UploadFilesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadFilesBinding
    private lateinit var filesAdapter: FilesAdapter
    private lateinit var sharedPreferences: android.content.SharedPreferences
    private val localFiles = mutableListOf<File>()

    private lateinit var selectFileLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)

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

        // Load local files
        loadLocalFiles()
    }

    private fun selectFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }
        selectFileLauncher.launch(intent)
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

            // Update shared preferences with the new file name and URI
            val fileNames = localFiles.map { it.name }.toSet()
            val fileUris = sharedPreferences.getStringSet("fileUris", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
            fileUris.add("$fileName,$uri")
            sharedPreferences.edit().putStringSet("fileNames", fileNames).putStringSet("fileUris", fileUris).apply()

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
                putExtra("PDF_FILE_PATH", file.absolutePath)
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
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
        val fileUris = sharedPreferences.getStringSet("fileUris", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        fileUris.removeIf { it.startsWith("$fileName,") }
        sharedPreferences.edit().putStringSet("fileNames", fileNames).putStringSet("fileUris", fileUris).apply()

        // Refresh file list
        filesAdapter.updateFiles(fileNames.toList())
        binding.emptyTextView.visibility = if (fileNames.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun getSelectedFiles(): List<String> {
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
