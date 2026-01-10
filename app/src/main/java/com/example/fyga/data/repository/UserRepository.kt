package com.example.fyga.data.repository

import com.example.fyga.data.model.Post
import com.example.fyga.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val postsCollection = db.collection("posts")

    /**
     * Busca um único usuário no Firestore pelo seu ID.
     * @param userId O ID do usuário a ser buscado.
     * @return O objeto [User] se encontrado, ou null se não existir.
     */
    suspend fun getUserProfile(userId: String): User? {
        return try {
            usersCollection.document(userId).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Busca todos os posts de um usuário específico.
     * @param userId O ID do usuário cujos posts serão buscados.
     * @return Uma lista de objetos [Post].
     */
    suspend fun getPostsForUser(userId: String): List<Post> {
        return try {
            postsCollection
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING) // Ordena pelos mais recentes
                .get()
                .await()
                .toObjects(Post::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
