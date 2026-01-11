package com.example.fyga.data.repository

import com.example.fyga.data.model.Post
import com.example.fyga.data.model.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val postsCollection = db.collection("posts")

    suspend fun getUserProfile(userId: String): User? {
        return try {
            usersCollection.document(userId).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getPostsForUser(userId: String): List<Post> {
        return try {
            postsCollection
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Post::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Segue um usuário. Atualiza a lista 'following' de quem segue e 'followers' de quem é seguido.
     */
    suspend fun followUser(myUserId: String, targetUserId: String) {
        db.runTransaction { transaction ->
            val myRef = usersCollection.document(myUserId)
            val targetRef = usersCollection.document(targetUserId)

            transaction.update(myRef, "following", FieldValue.arrayUnion(targetUserId))
            transaction.update(targetRef, "followers", FieldValue.arrayUnion(myUserId))
            null
        }.await()
    }

    /**
     * Deixa de seguir um usuário.
     */
    suspend fun unfollowUser(myUserId: String, targetUserId: String) {
        db.runTransaction { transaction ->
            val myRef = usersCollection.document(myUserId)
            val targetRef = usersCollection.document(targetUserId)

            transaction.update(myRef, "following", FieldValue.arrayRemove(targetUserId))
            transaction.update(targetRef, "followers", FieldValue.arrayRemove(myUserId))
            null
        }.await()
    }
}
