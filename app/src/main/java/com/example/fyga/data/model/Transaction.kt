package com.example.fyga.data.model

data class Transaction(
    val id: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.DEPOSIT, // ou PAYMENT, EARNING
    val timestamp: Long = System.currentTimeMillis()
)

enum class TransactionType {
    DEPOSIT,
    PAYMENT,
    EARNING
}