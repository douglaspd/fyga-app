package com.example.fyga.ui.createpost

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyga.data.model.Post
import com.example.fyga.data.repository.AuthRepository
import com.example.fyga.data.repository.PostRepository
import com.example.fyga.data.repository.StorageRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreatePostUiState(
    val selectedMediaUri: Uri? = null,
    val description: String = "",
    val isLoading: Boolean = false,
    val postCreatedSuccessfully: Boolean = false,
    val errorMessage: String? = null
)

class CreatePostViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val storageRepository: StorageRepository = StorageRepository(),
    private val postRepository: PostRepository = PostRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState = _uiState.asStateFlow()

    fun onDescriptionChange(newDescription: String) {
        _uiState.value = _uiState.value.copy(description = newDescription)
    }

    fun onMediaSelected(uri: Uri) {
        _uiState.value = _uiState.value.copy(selectedMediaUri = uri)
    }

    fun publishPost() {
        viewModelScope.launch {
            val uri = _uiState.value.selectedMediaUri
            val currentUser = authRepository.currentUser

            if (uri == null) {
                _uiState.value = _uiState.value.copy(errorMessage = "Por favor, selecione uma imagem ou vídeo.")
                return@launch
            }
            if (currentUser == null) {
                _uiState.value = _uiState.value.copy(errorMessage = "Usuário não autenticado.")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // 1. Upload da mídia para o Firebase Storage
                val downloadUrl = storageRepository.uploadFile(uri, "post_images")

                // 2. Criação do objeto Post
                val post = Post(
                    userId = currentUser.uid,
                    username = currentUser.displayName ?: "Usuário Anônimo",
                    profilePic = currentUser.photoUrl?.toString() ?: "",
                    imageUrl = downloadUrl,
                    description = _uiState.value.description,
                    timestamp = Timestamp.now()
                )

                // 3. Salva o post no Firestore
                postRepository.createPost(post)

                _uiState.value = _uiState.value.copy(isLoading = false, postCreatedSuccessfully = true)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Falha ao publicar o post: ${e.message}")
            }
        }
    }
}
