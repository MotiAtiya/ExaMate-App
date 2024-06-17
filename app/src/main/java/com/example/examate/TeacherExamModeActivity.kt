package com.example.examate

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.examate.databinding.ActivityStudentExamModeBinding
import com.example.examate.databinding.ActivityTeacherExamModeBinding
import java.util.Locale
import java.util.concurrent.TimeUnit

class TeacherExamModeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherExamModeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherExamModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val className = intent.getStringExtra("className") ?: ""
        val classId = intent.getStringExtra("classId") ?: ""

        binding.className.text = className

        binding.buttonShowId.setOnClickListener {
            // Show dialog with class ID
            AlertDialog.Builder(this)
                .setTitle("Class ID")
                .setMessage(classId)
                .setPositiveButton("Close") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
}
