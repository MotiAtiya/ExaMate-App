package com.example.examate.data

import java.text.SimpleDateFormat
import java.util.*

data class ClassItem(
    val classId: String,
    val name: String,
    val openMaterial: Boolean,
    val testDate: String,
    val testStartTime: String,
    val testTimeHours: Int,
    val testTimeMinutes: Int
) {
    fun getParsedDateTime(): Date? {
        return try {
            val dateTimeString = "$testDate $testStartTime"
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(dateTimeString)
        } catch (e: Exception) {
            null
        }
    }

    fun getFormattedDate(): String {
        return try {
            val parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(testDate)
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(parsedDate)
        } catch (e: Exception) {
            testDate // return original if parsing fails
        }
    }
}
