package com.example.fyga.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyga.data.model.Comment
import com.example.fyga.data.model.Post
import com.example.fyga.data.repository.AuthRepository
import com.example.fyga.data.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

data class FeedUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val currentUserId: String? = null
)

class FeedViewModel(
    private val postRepository: PostRepository = PostRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadFeed()
    }

    private fun loadFeed() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(currentUserId = authRepository.currentUser?.uid)

            postRepository.getAllPosts()
                .onStart { _uiState.value = _uiState.value.copy(isLoading = true) }
                .catch { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Falha ao carregar o feed.")
                }
                .collect { posts ->
                    _uiState.value = _uiState.value.copy(isLoading = false, posts = posts)
                }
        }
    }

    fun toggleLike(postId: String) {
        val currentUserId = _uiState.value.currentUserId ?: return

        val currentPosts = _uiState.value.posts
        val updatedPosts = currentPosts.map { post ->
            if (post.id == postId) {
                post.copy(likedBy = if (post.likedBy.contains(currentUserId)) post.likedBy - currentUserId else post.likedBy + currentUserId)
            } else {
                post
            }
        }
        _uiState.value = _uiState.value.copy(posts = updatedPosts)

        viewModelScope.launch {
            try {
                postRepository.toggleLike(postId, currentUserId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(posts = currentPosts)
            }
        }
    }

    fun addComment(postId: String, commentText: String) {
        val currentUser = authRepository.currentUser ?: return
        // O ideal é ter o nome do usuário salvo no perfil, mas usaremos o ID por enquanto
        val username = currentUser.displayName ?: "Anônimo"
        val comment = Comment(username = username, text = commentText)

        viewModelScope.launch {
            try {
                postRepository.addComment(postId, comment)
            } catch (e: Exception) {
                // Tratar erro, talvez com um novo estado na UI
            }
        }
    }
}
