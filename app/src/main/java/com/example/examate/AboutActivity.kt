package com.example.examate

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.example.examate.databinding.ActivityAboutBinding
import android.content.Intent
import android.net.Uri

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val contactText = getString(R.string.contact_us) + "\n" +
                getString(R.string.meir_shuvax) +
                " meyirsh@edu.hac.ac.il\n" +
                getString(R.string.mordechai_atiya) +
                " mordechiat@edu.hac.ac.il"
        val spannableString = SpannableString(contactText)

        val meirEmail = "meyirsh@edu.hac.ac.il"
        val meirStart = contactText.indexOf(meirEmail)
        val meirEnd = meirStart + meirEmail.length

        val mordechaiEmail = "mordechiat@edu.hac.ac.il"
        val mordechaiStart = contactText.indexOf(mordechaiEmail)
        val mordechaiEnd = mordechaiStart + mordechaiEmail.length

        val meirClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", meirEmail, null))
                startActivity(Intent.createChooser(intent, "Send Email"))
            }
        }

        val mordechaiClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", mordechaiEmail, null))
                startActivity(Intent.createChooser(intent, "Send Email"))
            }
        }

        spannableString.setSpan(meirClickableSpan, meirStart, meirEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(mordechaiClickableSpan, mordechaiStart, mordechaiEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.aboutText2.text = spannableString
        binding.aboutText2.movementMethod = LinkMovementMethod.getInstance()
    }
}
