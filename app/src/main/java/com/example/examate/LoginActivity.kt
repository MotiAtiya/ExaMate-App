package com.example.examate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.examate.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LoginActivity", "on Create")

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val saveButton = binding.buttonSave
        saveButton.setOnClickListener {
            // Retrieve values from EditText fields
            val firstName = binding.firstName.text.toString()
            val lastName = binding.lastName.text.toString()
            val email = binding.email.text.toString()
            val status = if (binding.radioButton.isChecked) "Student" else "Teacher"

            // Validate input
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || status.isEmpty()) {
                Toast.makeText(applicationContext, R.string.fill_all_fields_message, Toast.LENGTH_SHORT).show()
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(applicationContext, R.string.invalid_email_message, Toast.LENGTH_SHORT).show()
            } else {
                saveFormData(firstName, lastName, email, status)
            }
        }
    }

    // Method to save form data
    private fun saveFormData(firstName: String, lastName: String, email: String, status: String) {
        val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("FirstName", firstName)
        editor.putString("LastName", lastName)
        editor.putString("Email", email)
        editor.putString("Status", status)

        if (status == "Teacher") {
            val teacherId = generateTeacherId()
            editor.putString("TeacherId", teacherId)
        }

        editor.apply()

        Toast.makeText(applicationContext, R.string.save_succeeded, Toast.LENGTH_SHORT).show()
        val intent = if (status == "Student") {
            Intent(this, StudentHomeActivity::class.java)
        } else {
            Intent(this, TeacherHomeActivity::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun generateTeacherId(): String {
        val timestamp = System.currentTimeMillis().toString().take(10)
        val random = (10000..99999).random().toString()
        val checksum = calculateChecksum(timestamp, random)
        Log.d("timestamp: ", timestamp)
        Log.d("random: ", random)
        Log.d("checksum: ", checksum)
        Log.d("timestamp + random + checksum: ", timestamp + random + checksum)
        return timestamp + random + checksum
    }

    private fun calculateChecksum(timestamp: String, random: String): String {
        val sum = (timestamp + random).map { it.toString().toInt() }.sum()
        return (sum % 100000).toString().padStart(5, '0')
    }

}
