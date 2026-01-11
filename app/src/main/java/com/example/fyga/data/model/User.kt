package com.example.fyga.data.model

import com.google.firebase.firestore.DocumentId

enum class AccountType {
    OPENED, CLOSED
}

data class User(
    @DocumentId
    val id: String = "",
    val username: String = "",
    val phoneNumber: String = "", // Adicionado telefone
    val bio: String = "",
    val profileImageUrl: String = "",
    val accountType: AccountType = AccountType.OPENED,
    val role: String = "", // Gothic ou Hunter
    val following: List<String> = emptyList(),
    val followers: List<String> = emptyList()
)
