package com.example.examate

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.Calendar

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    interface DatePickerListener {
        fun onDateSelected(year: Int, month: Int, dayOfMonth: Int)
    }

    private var listener: DatePickerListener? = null

    fun setListener(listener: DatePickerListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker.
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog with minimum date set to today's date.
        val datePickerDialog = DatePickerDialog(requireContext(),this, year, month, day)

        // Set the minimum date to today's date to allow only future dates to be selected.
        datePickerDialog.datePicker.minDate = c.timeInMillis

        // Return the DatePickerDialog.
        return datePickerDialog
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        // Notify the listener with the selected date
        listener?.onDateSelected(year, month, dayOfMonth)
    }
}
