package com.example.examate

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class HourSelectionDialog : DialogFragment() {

    private lateinit var listener: HourSelectionListener

    interface HourSelectionListener {
        fun onHourSelected(hour: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as HourSelectionListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement HourSelectionListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val hours = ArrayList<String>()
        val hourValues = ArrayList<Int>()

        for (i in 0..10) {
            val hourString = if (i == 1) {
                "$i ${getString(R.string.hour)}"
            } else {
                "$i ${getString(R.string.hours)}"
            }
            hours.add(hourString)
            hourValues.add(i)
        }

        val items = hours.toTypedArray()

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.select_hours)
        builder.setItems(items) { _, which ->
            val selectedHour = hourValues[which]
            listener.onHourSelected(selectedHour)
        }

        return builder.create()
    }
}
