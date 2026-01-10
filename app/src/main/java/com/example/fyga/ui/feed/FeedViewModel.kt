package com.example.fyga.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyga.data.model.Comment
import com.example.fyga.data.model.Post
import com.example.fyga.data.model.User
import com.example.fyga.data.repository.AuthRepository
import com.example.fyga.data.repository.PostRepository
import com.example.fyga.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

data class FeedUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val currentUserId: String? = null,
    val currentUserProfile: User? = null // Armazena o perfil real do usuário
)

class FeedViewModel(
    private val postRepository: PostRepository = PostRepository(),
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadFeed()
        loadCurrentUserProfile()
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

    private fun loadCurrentUserProfile() {
        viewModelScope.launch {
            authRepository.currentUser?.uid?.let { uid ->
                val profile = userRepository.getUserProfile(uid)
                _uiState.value = _uiState.value.copy(currentUserProfile = profile)
            }
        }
    }

    fun toggleLike(postId: String) {
        val currentUserId = _uiState.value.currentUserId ?: return

        val currentPosts = _uiState.value.posts
        val updatedPosts = currentPosts.map { post ->
            if (post.id == postId) {
                val newLikedBy = if (post.likedBy.contains(currentUserId)) {
                    post.likedBy - currentUserId
                } else {
                    post.likedBy + currentUserId
                }
                post.copy(likedBy = newLikedBy)
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
        val currentUserProfile = _uiState.value.currentUserProfile ?: return
        
        // Agora usamos o username real do banco de dados
        val comment = Comment(
            username = currentUserProfile.username,
            text = commentText
        )

        viewModelScope.launch {
            try {
                postRepository.addComment(postId, comment)
            } catch (e: Exception) {
                // Tratar erro
            }
        }
    }
}
