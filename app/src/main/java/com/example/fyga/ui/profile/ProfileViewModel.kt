package com.example.fyga.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyga.data.model.Post
import com.example.fyga.data.model.User
import com.example.fyga.data.repository.AuthRepository
import com.example.fyga.data.repository.PostRepository
import com.example.fyga.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val posts: List<Post> = emptyList(),
    val currentUserId: String? = null, // Adicionado para saber quem está logado
    val errorMessage: String? = null,
    val logoutSuccess: Boolean = false
)

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository(),
    private val postRepository: PostRepository = PostRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserProfileAndPosts()
    }

    private fun loadUserProfileAndPosts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val firebaseUser = authRepository.currentUser ?: run {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Usuário não autenticado.")
                return@launch
            }

            // Define o ID do usuário logado no estado
            _uiState.value = _uiState.value.copy(currentUserId = firebaseUser.uid)

            try {
                val userProfile = userRepository.getUserProfile(firebaseUser.uid)
                val userPosts = userRepository.getPostsForUser(firebaseUser.uid)
                _uiState.value = _uiState.value.copy(isLoading = false, user = userProfile, posts = userPosts)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Falha ao carregar o perfil e os posts.")
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

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = _uiState.value.copy(logoutSuccess = true)
        }
    }
}
