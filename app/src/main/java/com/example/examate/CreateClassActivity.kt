package com.example.examate

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.examate.databinding.ActivityCreateClassBinding
import java.util.Calendar
import java.util.Locale

class CreateClassActivity : AppCompatActivity(), TimePickerFragment.TimePickerListener, DatePickerFragment.DatePickerListener,
    HourSelectionDialog.HourSelectionListener,  MinuteSelectionDialog.MinuteSelectionListener  {
    private lateinit var binding: ActivityCreateClassBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("CreateClassActivity", "onCreate")

        binding = ActivityCreateClassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set current time to pickTime TextView
        val currentTime = Calendar.getInstance().time
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formattedTime = timeFormat.format(currentTime)
        binding.pickTime.text = formattedTime

        // Set current date to pickDate TextView
        val currentDate = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDate.time)
        binding.pickDate.text = formattedDate


        binding.selectHourButton.setOnClickListener {
            val dialog = HourSelectionDialog()
            dialog.show(supportFragmentManager, "com.example.examate.HourSelectionDialog")
        }

        binding.selectMinuteButton.setOnClickListener {
            val dialog = MinuteSelectionDialog()
            dialog.show(supportFragmentManager, "com.example.examate.MinuteSelectionDialog")
        }

        // Set OnClickListener for pickTime TextView to show com.example.examate.TimePickerFragment
        binding.pickTime.setOnClickListener {
            val timePickerFragment = TimePickerFragment()
            timePickerFragment.setListener(this)
            timePickerFragment.show(supportFragmentManager, "timePicker")
        }

        // Set OnClickListener for pickDate TextView to show com.example.examate.DatePickerFragment
        binding.pickDate.setOnClickListener {
            val datePickerFragment = DatePickerFragment()
            datePickerFragment.setListener(this)
            datePickerFragment.show(supportFragmentManager, "datePicker")
        }

        val saveButton = binding.buttonSave
        saveButton.setOnClickListener {
            // Retrieve values from EditText fields

            val courseName = binding.courseName.text.toString()

            if (courseName.isEmpty()) {
                Toast.makeText(applicationContext, R.string.fill_all_fields_message, Toast.LENGTH_SHORT).show()
            } else {
//                 Process the form data if all validations pass
                saveFormData(courseName)
            }
        }
    }


    // Method to save form data
    private fun saveFormData(courseName: String) {
        Toast.makeText(applicationContext, R.string.save_succeeded, Toast.LENGTH_SHORT).show()
        val intent = Intent(this,HomeActivity::class.java).apply {}
        startActivity(intent)
    }

    override fun onTimeSelected(hourOfDay: Int, minute: Int) {
        val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
        binding.pickTime.text = formattedTime
    }

    override fun onDateSelected(year: Int, month: Int, dayOfMonth: Int) {
        val formattedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
        binding.pickDate.text = formattedDate
    }

    override fun onHourSelected(hour: String) {
        val hours = arrayOf(hour)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, hours)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.selectHourButton.text = hour
    }
    override fun onMinuteSelected(minute: String) {
        val hours = arrayOf(minute)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, hours)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.selectMinuteButton.text = minute
    }
}
