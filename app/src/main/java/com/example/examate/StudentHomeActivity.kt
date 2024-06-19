package com.example.examate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.examate.databinding.ActivityStudentHomeBinding

class StudentHomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val firstName = sharedPreferences.getString("FirstName", "")

        binding.textViewHello.text = getString(R.string.hello) + " " + firstName + "!"
        binding.buttonJoinAClass.setOnClickListener {
            val intent = Intent(this, JoinClassActivity::class.java)
            startActivity(intent)
        }

        binding.buttonMyFiles.setOnClickListener {
            val intent = Intent(this, MyFilesActivity::class.java)
            startActivity(intent)
        }

        binding.buttonLogout.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
