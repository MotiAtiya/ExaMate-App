package com.example.examate

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import com.example.examate.databinding.ActivityStudentExamModeBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.util.Locale
import java.util.concurrent.TimeUnit

class StudentExamModeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentExamModeBinding

    private lateinit var countDownTimer: CountDownTimer
    private var timerRunning = false
    private var initialTimeMillis: Long = 600000 // Default timer duration in milliseconds (10 minutes)
    private var isOpenMaterialAllowed = false
    private var studentId: String? = null
    private var classId: String? = null
    private var isScanningQrCode = false
    private var isFinishingActivity = false
    private var firstLoadFiles = true

    private lateinit var qrCodeScannerLauncher: ActivityResultLauncher<ScanOptions>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ExamModeActivity", "onCreate")

        binding = ActivityStudentExamModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        qrCodeScannerLauncher = registerForActivityResult(ScanContract()) { result ->
            isScanningQrCode = false
            if (result.contents == null) {
                Toast.makeText(this, getString(R.string.cancelled), Toast.LENGTH_LONG).show()
                disableSystemUI()
            } else {
                val disconnectId = result.contents
                logoutClass(disconnectId)
            }
        }

        handleIntent(intent)

        val progressBar = binding.progressBarCircle
        if (Locale.getDefault().language == "en") {
            progressBar.rotation = -90f
        } else {
            progressBar.rotation = 90f
            progressBar.rotationY = 180f
        }

        binding.viewFilesButton.setOnClickListener {
            val intent = Intent(this, ExamFilesActivity::class.java).apply {
                putExtra("IS_OPEN_MATERIAL_ALLOWED", isOpenMaterialAllowed)
                putExtra("CLASS_ID", classId)
                putExtra("FIRST_LOAD_FILES", firstLoadFiles)
            }
            startActivity(intent)
            firstLoadFiles = false
        }

        binding.finishButton.setOnClickListener {
            startQrCodeScanner()
        }

        startTimer()

        // Disable user interactions with the system UI
        disableSystemUI()
    }

    private fun handleIntent(intent: Intent) {
        val className = intent.getStringExtra("EXAM_NAME")
        initialTimeMillis = intent.getLongExtra("REMAINING_TIME_MILLIS", 600000)
        isOpenMaterialAllowed = intent.getBooleanExtra("IS_OPEN_MATERIAL_ALLOWED", false)
        studentId = intent.getStringExtra("STUDENT_ID")
        classId = intent.getStringExtra("CLASS_ID")

        binding.textView6.text = className
    }

    private fun startQrCodeScanner() {
        isScanningQrCode = true
        val options = ScanOptions().apply {
            setOrientationLocked(false)
            setPrompt(getString(R.string.scan_qr_code_prompt))
        }
        qrCodeScannerLauncher.launch(options)
    }

    private fun logoutClass(disconnectId: String) {
        val requestBody = mapOf(
            "classId" to classId,
            "disconnectId" to disconnectId,
            "studentId" to studentId
        )

        NetworkUtils.postRequest("disconnectClass", requestBody) { jsonElement ->
            if (jsonElement != null && jsonElement.isJsonObject) {
                val jsonObject = jsonElement.asJsonObject
                if (jsonObject.get("success").asBoolean) {
                    runOnUiThread {
                        enableSystemUI() // Ensure the system UI is enabled before navigating away
                        isFinishingActivity = true
                        val intent = Intent(this, StudentHomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, getString(R.string.not_allowed_to_logout), Toast.LENGTH_SHORT).show()
                        disableSystemUI() // Re-disable the system UI if logout is not allowed
                    }
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this, getString(R.string.failed_to_logout), Toast.LENGTH_SHORT).show()
                    disableSystemUI() // Re-disable the system UI if logout fails
                }
            }
        }
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(initialTimeMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateTimerText(millisUntilFinished)
                updateProgressBar(millisUntilFinished)
                initialTimeMillis -= 1000
            }

            override fun onFinish() {
                binding.textViewTime.text = "00:00:00"
                binding.progressBarCircle.progress = 0
                timerRunning = false
                Toast.makeText(this@StudentExamModeActivity, "Time's up!", Toast.LENGTH_LONG).show()

                enableSystemUI() // Enable system UI before navigating away
                isFinishingActivity = true
                val intent = Intent(this@StudentExamModeActivity, StudentHomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        countDownTimer.start()
        timerRunning = true
    }

    private fun updateTimerText(millisUntilFinished: Long) {
        val hms = String.format(
            "%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % TimeUnit.MINUTES.toSeconds(1)
        )
        binding.textViewTime.text = hms
    }

    private fun updateProgressBar(millisUntilFinished: Long) {
        val progress = ((millisUntilFinished.toFloat() / initialTimeMillis) * 100).toInt()
        binding.progressBarCircle.progress = progress
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        // Prevent back navigation
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus && !isScanningQrCode && !isFinishingActivity) {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningTasks = activityManager.appTasks
            if (runningTasks.isNotEmpty()) {
                val topActivity = runningTasks[0].taskInfo.topActivity
                if (topActivity?.className != ExamFilesActivity::class.java.name &&
                    topActivity?.className != ScanOptions::class.java.name) {
                    // Bring the activity back to the front if the user tries to navigate away
                    val intent = Intent(this, StudentExamModeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    intent.putExtra("EXAM_NAME", binding.textView6.text.toString())
                    intent.putExtra("REMAINING_TIME_MILLIS", initialTimeMillis)
                    intent.putExtra("IS_OPEN_MATERIAL_ALLOWED", isOpenMaterialAllowed)
                    intent.putExtra("STUDENT_ID", studentId)
                    intent.putExtra("CLASS_ID", classId)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("ExamModeActivity", "onNewIntent called")
        intent?.let {
            handleIntent(it)
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

    override fun onDestroy() {
        super.onDestroy()
        if (timerRunning) {
            countDownTimer.cancel()
        }
    }
}
