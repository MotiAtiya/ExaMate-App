package com.example.examate

import FilesAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.examate.databinding.ActivityExamFilesBinding
import java.io.File

class ExamFilesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExamFilesBinding
    private lateinit var filesAdapter: FilesAdapter
    private lateinit var sharedPreferences: android.content.SharedPreferences
    private var isOpenMaterialAllowed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExamFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        isOpenMaterialAllowed = intent.getBooleanExtra("IS_OPEN_MATERIAL_ALLOWED", false)

        // Initialize RecyclerView and set empty adapter initially
        binding.recyclerViewFiles.layoutManager = LinearLayoutManager(this)
        filesAdapter = FilesAdapter(emptyList(), this::openFile, {})
        filesAdapter = FilesAdapter(emptyList(), this::openFile, this::removeFile)
        binding.recyclerViewFiles.adapter = filesAdapter

        // Load files for the exam
        loadFiles()
    }

    private fun loadFiles() {
        // TODO: Add teacher's files
        if (isOpenMaterialAllowed) {
            val fileNames = sharedPreferences.getStringSet("fileNames", mutableSetOf()) ?: mutableSetOf()
            filesAdapter.updateFiles(fileNames.toList())
        }
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
            Toast.makeText(this, R.string.file_not_found, Toast.LENGTH_SHORT).show()
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
        loadFiles()
    }
}
