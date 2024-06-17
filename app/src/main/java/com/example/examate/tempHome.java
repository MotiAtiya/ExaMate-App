//package com.example.examate
//
//import android.content.Intent
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.provider.Settings
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.example.examate.databinding.ActivityMainBinding
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.functions.FirebaseFunctions
//import com.google.firebase.functions.FirebaseFunctionsException
//
//class MainActivity : AppCompatActivity() {
//
//private lateinit var functions: FirebaseFunctions
//private lateinit var binding: ActivityMainBinding
//
//        override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // Initialize Firebase Functions
//        functions = FirebaseFunctions.getInstance()
//
////        if (OverlayPermission.checkPermission(this)) {
////            startOverlayService()
////        } else {
////            OverlayPermission.requestPermission(this)
////        }
//
//        binding.login.setOnClickListener {
//        val intent = Intent(this, LoginActivity::class.java)
//        startActivity(intent)
//        }
//
////        val firebaseAuth = FirebaseAuth.getInstance()
////        val currentUser = firebaseAuth.currentUser
//
////        if (currentUser != null) {
////            // User is signed in
////            val userId = currentUser.uid
////            Toast.makeText(this, "true", Toast.LENGTH_LONG).show()
////
////            // Use the user ID for authenticated function calls
////            // ...
////        } else {
////            Toast.makeText(this, "false", Toast.LENGTH_LONG).show()
////
////            // User is not signed in
////            // Handle the case where the user needs to sign in before proceeding
////        }
//
//        // Example function call
////        callAddHelloWorldFunction()
//        }
//
////    private fun callAddHelloWorldFunction() {
////        functions
////            .getHttpsCallable("addHelloWorld")
////            .call()
////            .addOnCompleteListener { task ->
////                if (task.isSuccessful) {
////                    // Handle successful result
////                    val result = task.result?.data as? Map<*, *>
////                    val message = result?.get("text") as? String
////                    Toast.makeText(this, "Message: $message", Toast.LENGTH_LONG).show()
////                } else {
////                    // Handle error
////                    val e = task.exception
////                    if (e is FirebaseFunctionsException) {
////                        val code = e.code
////                        val details = e.details
////                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
////                    } else {
////                        Toast.makeText(this, "Error: ${e?.message}", Toast.LENGTH_LONG).show()
////                    }
////                }
////            }
////    }
//
//private fun startOverlayService() {
//        val intent = Intent(this, OverlayService::class.java)
//        startService(intent)
//        }
//
//        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
//        if (OverlayPermission.checkPermission(this)) {
//        startOverlayService()
//        } else {
//        // Permission not granted
//        Toast.makeText(this, "Overlay permission is required to display the app over other apps.", Toast.LENGTH_LONG).show()
//        }
//        }
//        }
//
//        companion object {
//        const val REQUEST_OVERLAY_PERMISSION = 1001
//        }
//        }
