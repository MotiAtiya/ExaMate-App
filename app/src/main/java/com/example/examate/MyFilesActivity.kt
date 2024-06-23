package com.example.examate

import FilesAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.examate.databinding.ActivityMyFilesBinding
import java.io.File
import java.io.FileOutputStream

class MyFilesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyFilesBinding
    private lateinit var filesAdapter: FilesAdapter
    private lateinit var sharedPreferences: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)

        // Initialize RecyclerView and set empty adapter initially
        binding.recyclerViewFiles.layoutManager = LinearLayoutManager(this)
        filesAdapter = FilesAdapter(emptyList(), this::openFile, this::removeFile)
        binding.recyclerViewFiles.adapter = filesAdapter

        binding.buttonAddFile.setOnClickListener {
            selectFile()
        }

        // Load existing files
        loadFiles()
    }

    private fun selectFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }
        startActivityForResult(intent, REQUEST_CODE_SELECT_FILE)
    }

    private fun loadFiles() {
        // Load files from SharedPreferences and update the adapter
        val fileNames = sharedPreferences.getStringSet("fileNames", mutableSetOf()) ?: mutableSetOf()
        filesAdapter.updateFiles(fileNames.toList())
        binding.emptyTextView.visibility = if (fileNames.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_FILE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                uploadFile(uri)
            }
        }
    }

    private fun uploadFile(uri: Uri) {
        val fileName = getFileName(uri)
        if (fileName.isNotEmpty()) {
            // Save file to internal storage
            val destinationFile = File(filesDir, fileName)
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            // Update shared preferences with the new file name
            val fileNames = sharedPreferences.getStringSet("fileNames", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
            fileNames.add(fileName)
            sharedPreferences.edit().putStringSet("fileNames", fileNames).apply()

            Toast.makeText(this, "File uploaded: $fileName", Toast.LENGTH_SHORT).show()
            loadFiles() // Refresh the file list after upload
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
            val uri = FileProvider.getUriForFile(this, "com.example.examate.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
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

        val fileNames = sharedPreferences.getStringSet("fileNames", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        fileNames.remove(fileName)
        sharedPreferences.edit().putStringSet("fileNames", fileNames).apply()

        Toast.makeText(this, "File deleted: $fileName", Toast.LENGTH_SHORT).show()
        loadFiles()
    }

    companion object {
        private const val REQUEST_CODE_SELECT_FILE = 1
    }
}
