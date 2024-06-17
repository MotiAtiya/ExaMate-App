//package com.example.examate
//
//import android.content.pm.PackageManager
//import android.Manifest
//import android.content.Intent
//import android.os.Bundle
//import android.provider.MediaStore
//import android.util.Log
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import com.example.examate.databinding.ActivityHomeBinding
//
//class HomeActivityWithCamera : AppCompatActivity() {
//    lateinit var binding: ActivityHomeBinding
//
//    private val CAMERA_PERMISSION_REQUEST_CODE = 100
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        Log.d("HomeActivity", "on Create")
//
//        binding = ActivityHomeBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        val joinClassButton = binding.buttonJoinAClass
//        joinClassButton.setOnClickListener {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
//                PackageManager.PERMISSION_GRANTED
//            ) {
////                openCamera()
//                val intent = Intent(this, StudentExamModeActivity::class.java).apply {}
//                startActivity(intent)
//            } else {
//                // Permission not yet granted, request permission
//                ActivityCompat.requestPermissions(
//                    this,
//                    arrayOf(Manifest.permission.CAMERA),
//                    CAMERA_PERMISSION_REQUEST_CODE
//                )
//            }
//        }
//
//        val createClassButton = binding.buttonCreateNewClass
//        createClassButton.setOnClickListener {
//            val intent = Intent(this, CreateClassActivity::class.java).apply {}
//            startActivity(intent)
//        }
//    }
//
//
//    private fun openCamera() {
//        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        startActivity(cameraIntent)
//    }
//}