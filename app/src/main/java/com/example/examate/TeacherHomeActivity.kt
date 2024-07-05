package com.example.examate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.examate.databinding.ActivityTeacherHomeBinding

class TeacherHomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTeacherHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val firstName = sharedPreferences.getString("FirstName", "")

        binding.textViewHello.text = getString(R.string.hello) + " " + firstName + "!"

        binding.buttonMyClasses.setOnClickListener {
            val intent = Intent(this, MyClassesActivity::class.java)
            startActivity(intent)
        }

        binding.buttonCreateNewClass.setOnClickListener {
            val intent = Intent(this, CreateClassActivity::class.java)
            startActivity(intent)
        }

        binding.buttonLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.logout_confirmation_title))
            setMessage(getString(R.string.logout_confirmation_message))
            setPositiveButton(getString(R.string.ok)) { _, _ ->
                deleteClassesAndLogout()
            }
            setNegativeButton(getString(R.string.cancel), null)
        }.show()
    }

    private fun deleteClassesAndLogout() {
        val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val teacherId = sharedPreferences.getString("TeacherId", null)

        if (teacherId != null) {
            val requestBody = mapOf(
                "teacherId" to teacherId
            )
            NetworkUtils.postRequest("deleteClassesByTeacher", requestBody) {
                runOnUiThread {
                    performLogout()
                }
            }
        } else {
            performLogout()
        }
    }

    private fun performLogout() {
        val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
