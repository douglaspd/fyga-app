package com.example.fyga.ui.login

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyga.data.repository.AuthRepository
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val phoneNumber: String = "",
    val verificationCode: String = "",
    val isPhoneValid: Boolean = false,
    val codeSent: Boolean = false,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    internal val verificationId: String? = null // Apenas para uso interno do ViewModel
)

class LoginViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onPhoneChanged(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        _uiState.value = _uiState.value.copy(
            phoneNumber = digitsOnly,
            isPhoneValid = digitsOnly.length > 10, // Validação simples
            errorMessage = null
        )
    }

    fun onCodeChanged(value: String) {
        _uiState.value = _uiState.value.copy(verificationCode = value, errorMessage = null)
    }

    fun sendVerificationCode(activity: ComponentActivity) {
        if (!_uiState.value.isPhoneValid) {
            _uiState.value = _uiState.value.copy(errorMessage = "Número de telefone inválido.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Verificação automática (ocorre em alguns dispositivos)
                _uiState.value = _uiState.value.copy(isLoading = false)
                signInWithCredential(credential)
            }

            override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                Log.w("LoginViewModel", "onVerificationFailed", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Falha ao enviar o código. Tente novamente."
                )
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    codeSent = true,
                    verificationId = verificationId
                )
            }
        }

        viewModelScope.launch {
            repository.sendVerificationCode(
                phoneNumber = "+55" + _uiState.value.phoneNumber, // Adiciona o código do país
                activity = activity,
                callbacks = callbacks
            )
        }
    }

    fun login() {
        val state = _uiState.value
        if (state.verificationId == null) {
            _uiState.value = state.copy(errorMessage = "ID de verificação não encontrado.")
            return
        }
        if (state.verificationCode.length < 6) {
            _uiState.value = state.copy(errorMessage = "Código inválido.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val credential = repository.verifyCode(state.verificationId, state.verificationCode)
                signInWithCredential(credential)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Código incorreto. Tente novamente."
                )
            }
        }
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            try {
                repository.signInWithCredential(credential)
                _uiState.value = _uiState.value.copy(isLoading = false, loginSuccess = true)
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Sign in failed", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Falha no login."
                )
            }
        }
    }
}
