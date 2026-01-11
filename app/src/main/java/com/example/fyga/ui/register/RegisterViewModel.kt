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

    fun onUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(username = value, errorMessage = null)
    }

    fun onBioChange(value: String) {
        _uiState.value = _uiState.value.copy(bio = value, errorMessage = null)
    }

    fun onAccountTypeChange(value: AccountType) {
        _uiState.value = _uiState.value.copy(accountType = value)
    }

    fun onImageSelected(uri: Uri) {
        _uiState.value = _uiState.value.copy(selectedImageUri = uri)
    }

    fun onRoleSelected(role: String) {
        _uiState.value = _uiState.value.copy(role = role)
    }

    /**
     * Finaliza o registro salvando o perfil no Firestore.
     * Vincula o UID do Firebase Auth ao documento do usuário.
     */
    fun completeRegistration() {
        val state = _uiState.value
        val currentUser = authRepository.currentUser

        // Validação básica de segurança
        if (state.username.trim().length < 3) {
            _uiState.value = state.copy(errorMessage = "O nome de usuário deve ter pelo menos 3 caracteres.")
            return
        }

        if (currentUser == null) {
            _uiState.value = state.copy(errorMessage = "Erro crítico: Usuário não autenticado no Firebase.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                var imageUrl = ""
                
                // 1. Upload da foto de perfil para o Firebase Storage (se houver)
                state.selectedImageUri?.let { uri ->
                    imageUrl = storageRepository.uploadFile(uri, "profile_pics")
                }

                // 2. Criação do objeto User vinculado ao UID e Telefone reais
                val user = User(
                    id = currentUser.uid, // O ID do documento será o UID do Firebase
                    username = state.username.trim(),
                    phoneNumber = currentUser.phoneNumber ?: "", // Vincula o telefone verificado via SMS
                    bio = state.bio.trim(),
                    accountType = state.accountType,
                    profileImageUrl = imageUrl,
                    role = state.role
                )

                // 3. Persistência no Firestore através do Repository
                authRepository.saveUserProfile(user)
                
                // 4. Sucesso! O AppNavHost detectará o isSuccess e mandará para o Feed
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false, 
                    errorMessage = "Falha ao salvar perfil: ${e.localizedMessage}"
                )
            }
        }
    }
}
