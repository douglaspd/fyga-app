package com.example.fyga.ui.register

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyga.data.model.AccountType
import com.example.fyga.data.model.User
import com.example.fyga.data.repository.AuthRepository
import com.example.fyga.data.repository.StorageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val username: String = "",
    val bio: String = "",
    val accountType: AccountType = AccountType.OPENED,
    val selectedImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val role: String = "" // Gothic ou Hunter
)

class RegisterViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val storageRepository: StorageRepository = StorageRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    fun onUsernameChange(value: String) { _uiState.value = _uiState.value.copy(username = value) }
    fun onBioChange(value: String) { _uiState.value = _uiState.value.copy(bio = value) }
    fun onAccountTypeChange(value: AccountType) { _uiState.value = _uiState.value.copy(accountType = value) }
    fun onImageSelected(uri: Uri) { _uiState.value = _uiState.value.copy(selectedImageUri = uri) }
    fun onRoleSelected(role: String) { _uiState.value = _uiState.value.copy(role = role) }

    fun completeRegistration() {
        val state = _uiState.value
        val currentUser = authRepository.currentUser

        if (state.username.isBlank() || currentUser == null) {
            _uiState.value = state.copy(errorMessage = "Nome de usuário é obrigatório.")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            try {
                var imageUrl = ""
                // 1. Upload da foto de perfil se selecionada
                state.selectedImageUri?.let { uri ->
                    imageUrl = storageRepository.uploadFile(uri, "profile_pics")
                }

                // 2. Criar objeto User
                val user = User(
                    id = currentUser.uid,
                    username = state.username.trim(),
                    bio = state.bio.trim(),
                    accountType = state.accountType,
                    profileImageUrl = imageUrl,
                    role = state.role // Salva a escolha Gothic/Hunter
                )

                // 3. Salvar no Firestore
                authRepository.saveUserProfile(user)
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }
}
