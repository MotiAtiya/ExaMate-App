package com.example.examate

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.examate.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val createClassButton = binding.buttonCreateNewClass
        createClassButton.setOnClickListener {
            val intent = Intent(this, CreateClassActivity::class.java)
            startActivity(intent)
        }

        val joinClassButton = binding.buttonJoinAClass
        joinClassButton.setOnClickListener {
            val intent = Intent(this, JoinClassActivity::class.java)
            startActivity(intent)
        }
    }
}
