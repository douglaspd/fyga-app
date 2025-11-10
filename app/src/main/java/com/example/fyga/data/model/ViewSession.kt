package com.example.fyga.data.model

data class ViewSession(
    val id: String = "",
    val viewerId: String = "",
    val postId: String = "",
    val startTime: Long = 0L,
    val endTime: Long? = null,
    val durationMillis: Long? = null,
    val amountPaid: Double = 0.0
)
