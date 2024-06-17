package com.example.examate.data

data class CreateClassRequest(
    val name: String,
    val openMaterial: Boolean,
    val testDate: String,
    val testStartTime: String,
    val testTimeHours: Int,
    val testTimeMinutes: Int,
    val teacherId: String
)
