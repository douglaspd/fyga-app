package com.example.fyga.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyga.data.model.Comment
import com.example.fyga.data.model.Post
import com.example.fyga.data.model.User
import com.example.fyga.data.repository.AuthRepository
import com.example.fyga.data.repository.PostRepository
import com.example.fyga.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

enum class FeedTab {
    FOR_YOU, COVEN
}

data class FeedUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val currentUserId: String? = null,
    val currentUserProfile: User? = null,
    val selectedTab: FeedTab = FeedTab.FOR_YOU
)

class FeedViewModel(
    private val postRepository: PostRepository = PostRepository(),
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState = _uiState.asStateFlow()

    private var feedJob: Job? = null

    init {
        loadCurrentUserProfile()
        loadFeed(FeedTab.FOR_YOU)
    }

    fun onTabSelected(tab: FeedTab) {
        if (_uiState.value.selectedTab != tab) {
            _uiState.value = _uiState.value.copy(selectedTab = tab)
            loadFeed(tab)
        }
    }

    private fun loadFeed(tab: FeedTab) {
        feedJob?.cancel()
        feedJob = viewModelScope.launch {
            val userId = authRepository.currentUser?.uid
            _uiState.value = _uiState.value.copy(currentUserId = userId)

            val postsFlow = if (tab == FeedTab.FOR_YOU) {
                postRepository.getAllPosts()
            } else {
                val following = _uiState.value.currentUserProfile?.following ?: emptyList()
                postRepository.getFollowedPosts(following)
            }

            postsFlow
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
                
                if (_uiState.value.selectedTab == FeedTab.COVEN) {
                    loadFeed(FeedTab.COVEN)
                }
            }
        }
    }

    fun toggleFollow(targetUserId: String) {
        val myUserId = _uiState.value.currentUserId ?: return
        val currentProfile = _uiState.value.currentUserProfile ?: return
        
        val isFollowing = currentProfile.following.contains(targetUserId)
        
        // Atualização Otimista da UI
        val newFollowing = if (isFollowing) {
            currentProfile.following - targetUserId
        } else {
            currentProfile.following + targetUserId
        }
        
        _uiState.value = _uiState.value.copy(
            currentUserProfile = currentProfile.copy(following = newFollowing)
        )

        viewModelScope.launch {
            try {
                if (isFollowing) {
                    userRepository.unfollowUser(myUserId, targetUserId)
                } else {
                    userRepository.followUser(myUserId, targetUserId)
                }
                // Recarrega o perfil para garantir sincronia real
                loadCurrentUserProfile()
            } catch (e: Exception) {
                // Reverte em caso de erro
                _uiState.value = _uiState.value.copy(currentUserProfile = currentProfile)
            }
        }
    }

    fun toggleLike(postId: String) {
        val currentUserId = _uiState.value.currentUserId ?: return
        val currentPosts = _uiState.value.posts
        val updatedPosts = currentPosts.map { post ->
            if (post.id == postId) {
                val newLikedBy = if (post.likedBy.contains(currentUserId)) post.likedBy - currentUserId else post.likedBy + currentUserId
                post.copy(likedBy = newLikedBy)
            } else post
        }
        _uiState.value = _uiState.value.copy(posts = updatedPosts)

        viewModelScope.launch {
            try { postRepository.toggleLike(postId, currentUserId) }
            catch (e: Exception) { _uiState.value = _uiState.value.copy(posts = currentPosts) }
        }
    }

    fun addComment(postId: String, commentText: String) {
        val profile = _uiState.value.currentUserProfile ?: return
        val comment = Comment(username = profile.username, text = commentText)
        viewModelScope.launch {
            try { postRepository.addComment(postId, comment) }
            catch (e: Exception) { /* Erro */ }
        }
    }
}
