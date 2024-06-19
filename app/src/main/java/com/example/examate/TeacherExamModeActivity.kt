package com.example.examate

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.examate.databinding.ActivityTeacherExamModeBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import android.widget.ImageView
import android.view.LayoutInflater
import android.widget.TextView

class TeacherExamModeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherExamModeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherExamModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val className = intent.getStringExtra("className") ?: ""
        val classId = intent.getStringExtra("classId") ?: ""

        binding.className.text = className

        binding.buttonShowId.setOnClickListener {
            // Generate QR code
            val qrCodeBitmap = generateQRCode(classId)

            // Show dialog with QR code
            val imageView = ImageView(this)
            imageView.setImageBitmap(qrCodeBitmap)

            val customTitle = LayoutInflater.from(this).inflate(R.layout.dialog_title_centered, null) as TextView

            AlertDialog.Builder(this)
                .setCustomTitle(customTitle)
                .setView(imageView)
                .setPositiveButton("Close") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun generateQRCode(text: String): Bitmap {
        val size = 512 // pixels
        val qrCodeWriter = QRCodeWriter()
        return try {
            val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
                }
            }
            bitmap
        } catch (e: WriterException) {
            Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).apply {
                eraseColor(0xFFFFFFFF.toInt()) // Set to white in case of error
            }
        }
    }
}
