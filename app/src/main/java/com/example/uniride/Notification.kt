package com.example.uniride

data class Notification(
    val receiverEmail: String = "",
    val message: String = "",
    val timestamp: Long = 0L
)