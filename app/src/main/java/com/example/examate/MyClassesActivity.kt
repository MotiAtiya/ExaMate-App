package com.example.examate

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.examate.databinding.ActivityMyClassesBinding
//import kotlinx.android.synthetic.main.activity_my_classes.*

class MyClassesActivity : AppCompatActivity() {

    lateinit var binding : ActivityMyClassesBinding

    private val classesList = mutableListOf(
        MyClass("Class A", "2024-04-15", "10:00 AM"),
        MyClass("Class B", "2024-04-16", "2:00 PM"),
        MyClass("Class C", "2024-04-17", "4:30 PM")
        // Add more classes here...
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MyClassesActivity","on Create")

        binding = ActivityMyClassesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManager = LinearLayoutManager(this)
//        recyclerViewClasses.layoutManager = layoutManager

        val adapter = MyClassesAdapter(classesList)
//        recyclerViewClasses.adapter = adapter
    }
}
