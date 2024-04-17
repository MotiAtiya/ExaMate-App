package com.example.examate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.examate.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    lateinit var binding : ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LoginActivity","on Create")

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)



        val saveButton = binding.buttonSave
        saveButton.setOnClickListener {
            // Retrieve values from EditText fields

            val firstName = binding.firstName.text.toString()
            val lastName = binding.lastName.text.toString()
            val email = binding.email.text.toString()

// Validate input if needed
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
//                 Display error message if any field is empty
                Toast.makeText(applicationContext, R.string.fill_all_fields_message, Toast.LENGTH_SHORT).show()
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//                 Display error message if email format is invalid
                Toast.makeText(applicationContext, R.string.invalid_email_message, Toast.LENGTH_SHORT).show()
            } else {
//                 Process the form data if all validations pass
                saveFormData(firstName, lastName, email)
            }
        }
    }


    // Method to save form data
    private fun saveFormData(firstName: String, lastName: String, email: String) {
        Toast.makeText(applicationContext, R.string.save_succeeded, Toast.LENGTH_SHORT).show()
        val intent = Intent(this,HomeActivity::class.java).apply {}
        startActivity(intent)
    }
}