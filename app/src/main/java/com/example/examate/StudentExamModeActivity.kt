package com.example.examate

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import com.example.examate.databinding.ActivityStudentExamModeBinding
import java.util.Locale
import java.util.concurrent.TimeUnit

class StudentExamModeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentExamModeBinding

    private lateinit var countDownTimer: CountDownTimer
    private var timerRunning = false
    private var initialTimeMillis: Long = 600000 // Default timer duration in milliseconds (10 minutes)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ExamModeActivity", "onCreate")

        binding = ActivityStudentExamModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val className = intent.getStringExtra("EXAM_NAME")
        val remainingTimeMillis = intent.getLongExtra("REMAINING_TIME_MILLIS", 600000)

        binding.textView6.text = className
        initialTimeMillis = remainingTimeMillis

        val progressBar = binding.progressBarCircle
        if (Locale.getDefault().language == "en") {
            progressBar.rotation = -90f
        } else {
            progressBar.rotation = 90f
            progressBar.rotationY = 180f
        }
        startTimer()
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(initialTimeMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateTimerText(millisUntilFinished)
                updateProgressBar(millisUntilFinished)
            }

            override fun onFinish() {
                binding.textViewTime.text = "00:00:00"
                binding.progressBarCircle.progress = 0
                timerRunning = false
                Toast.makeText(this@StudentExamModeActivity, "Time's up!", Toast.LENGTH_LONG).show()

                // Start HomeActivity using this@ExamModeActivity as the context
                val intent = Intent(this@StudentExamModeActivity, HomeActivity::class.java)
                startActivity(intent)
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

    override fun onDestroy() {
        super.onDestroy()
        if (timerRunning) {
            countDownTimer.cancel()
        }
    }
}
