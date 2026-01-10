package com.example.fyga.data.model

import com.google.firebase.firestore.DocumentId

// Modelo para um comentário
data class Comment(
    val username: String = "",
    val text: String = ""
)

// Modelo para um Post
data class Post(
    // @DocumentId é uma anotação do Firestore que automaticamente preenche este campo
    // com o ID do documento. É crucial para sabermos qual post atualizar.
    @DocumentId
    val id: String = "",

    val userId: String = "", // ID do usuário que criou o post
    val username: String = "",
    val imageUrl: String = "",
    val description: String = "",
    val profilePic: String = "",
    val comments: List<Comment> = emptyList(),

    // Lista de IDs dos usuários que curtiram o post.
    val likedBy: List<String> = emptyList(),

    // Campo `liked` foi removido, pois agora o estado de "curtido"
    // será derivado da lista `likedBy` em relação ao usuário atual.
    val timestamp: com.google.firebase.Timestamp? = null
)
