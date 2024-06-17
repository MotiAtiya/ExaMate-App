package com.example.examate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.examate.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if user details are saved in SharedPreferences
        val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val firstName = sharedPreferences.getString("FirstName", null)
        val lastName = sharedPreferences.getString("LastName", null)
        val email = sharedPreferences.getString("Email", null)
        val status = sharedPreferences.getString("Status", null)

        // If details are present, navigate to the appropriate home activity
        if (!firstName.isNullOrEmpty() && !lastName.isNullOrEmpty() && !email.isNullOrEmpty() && !status.isNullOrEmpty()) {
            val intent = if (status == "Student") {
                Intent(this, StudentHomeActivity::class.java)
            } else {
                Intent(this, TeacherHomeActivity::class.java)
            }
            startActivity(intent)
            finish() // Close MainActivity to remove it from the back stack
        }

        // Set OnClickListener for the login button
        binding.login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
