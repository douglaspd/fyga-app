package com.example.fyga.data.repository

import com.example.fyga.data.model.Comment
import com.example.fyga.data.model.Post
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PostRepository {

    private val db = FirebaseFirestore.getInstance()
    private val postsCollection = db.collection("posts")

    fun getAllPosts(): Flow<List<Post>> = callbackFlow {
        val listener = postsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val posts = snapshot.toObjects(Post::class.java)
                    trySend(posts)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun createPost(post: Post) {
        postsCollection.add(post).await()
    }

    suspend fun addComment(postId: String, comment: Comment) {
        postsCollection.document(postId).update("comments", FieldValue.arrayUnion(comment)).await()
    }

    suspend fun toggleLike(postId: String, userId: String) {
        val postRef = postsCollection.document(postId)
        db.runTransaction {
            transaction ->
            val post = transaction.get(postRef)
            val likedBy = post.get("likedBy") as? List<String> ?: emptyList()

            if (likedBy.contains(userId)) {
                transaction.update(postRef, "likedBy", FieldValue.arrayRemove(userId))
            } else {
                transaction.update(postRef, "likedBy", FieldValue.arrayUnion(userId))
            }
            null
        }.await()
    }
}
