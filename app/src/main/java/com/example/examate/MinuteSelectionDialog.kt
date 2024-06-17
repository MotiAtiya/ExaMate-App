package com.example.examate

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class MinuteSelectionDialog : DialogFragment() {

    private lateinit var listener: MinuteSelectionListener

    interface MinuteSelectionListener {
        fun onMinuteSelected(minute: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as MinuteSelectionListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement MinuteSelectionListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val minutes = ArrayList<String>()
        val minuteValues = ArrayList<Int>()

        for (i in 0..59) {
            val minuteString = if (i == 1) {
                "$i ${getString(R.string.minute)}"
            } else {
                "$i ${getString(R.string.minutes)}"
            }
            minutes.add(minuteString)
            minuteValues.add(i)
        }

        val items = minutes.toTypedArray()

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.select_minutes)
        builder.setItems(items) { _, which ->
            val selectedMinute = minuteValues[which]
            listener.onMinuteSelected(selectedMinute)
        }

        return builder.create()
    }
}
