package com.example.fyga.data.model

data class User(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val profileImageUrl: String? = null,
    val bio: String? = null,
    val accountType: AccountType = AccountType.OPENED, // OPENED ou CLOSED
    val balance: Double = 0.0,
    val totalEarned: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

enum class AccountType {
    OPENED,
    CLOSED
}
