package com.example.examate

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.examate.databinding.ActivityPdfViewerBinding
import com.github.barteksc.pdfviewer.PDFView
import java.io.File

class PdfViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfViewerBinding
    private lateinit var pdfView: PDFView
    private var isFinishingActivity = false
    private var blockNavigation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pdfView = binding.pdfView

        blockNavigation = intent.getBooleanExtra("BLOCK_NAVIGATION", false)

        val pdfFilePath = intent.getStringExtra("PDF_FILE_PATH")
        if (pdfFilePath != null) {
            displayPdf(pdfFilePath)
        } else {
            Toast.makeText(this, "No PDF file path provided", Toast.LENGTH_SHORT).show()
            Log.e("PdfViewerActivity", "No PDF file path provided in the intent")
        }

        disableSystemUI()
    }

    private fun displayPdf(filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            pdfView.fromFile(file)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .defaultPage(0)
                .load()
        } else {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus && blockNavigation && !isFinishingActivity) {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningTasks = activityManager.appTasks
            if (runningTasks.isNotEmpty()) {
                val topActivity = runningTasks[0].taskInfo.topActivity
                if (topActivity?.className != ExamFilesActivity::class.java.name) {
                    val intent = Intent(this, ExamFilesActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(intent)
                }
            }
        }
    }

    private fun disableSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun enableSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onBackPressed() {
        enableSystemUI()
        super.onBackPressed()
    }
}
