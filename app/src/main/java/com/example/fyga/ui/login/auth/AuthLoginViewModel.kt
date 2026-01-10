package com.example.fyga.ui.login.auth

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

class AuthLoginViewModel : ViewModel() {

    private val repository: AuthRepository = AuthRepository()

    // --------------------------------------------------------------------------------
    // 🔹 Estados da UI
    // --------------------------------------------------------------------------------

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber = _phoneNumber.asStateFlow()

    private val _verificationCode = MutableStateFlow("")
    val verificationCode = _verificationCode.asStateFlow()

    private val _verificationId = MutableStateFlow<String?>(null)

    // --------------------------------------------------------------------------------
    // 🔹 Funções de atualização da UI
    // --------------------------------------------------------------------------------

    fun onPhoneNumberChanged(phone: String) {
        _phoneNumber.value = phone
    }

    fun onVerificationCodeChanged(code: String) {
        _verificationCode.value = code
    }

    // --------------------------------------------------------------------------------
    // 🔹 Lógica de autenticação com Firebase
    // --------------------------------------------------------------------------------

    fun sendVerificationCode(activity: ComponentActivity) {
        viewModelScope.launch {
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Autenticação automática em alguns casos
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                    Log.w("AuthViewModel", "onVerificationFailed", e)
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    _verificationId.value = verificationId
                }
            }
            repository.sendVerificationCode(_phoneNumber.value, activity, callbacks)
        }
    }

    fun verifyCode() {
        viewModelScope.launch {
            _verificationId.value?.let {
                val credential = repository.verifyCode(it, _verificationCode.value)
                signInWithPhoneAuthCredential(credential)
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            try {
                repository.signInWithCredential(credential)
                // TODO: Navegar para a próxima tela ou atualizar estado de sucesso
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Sign in with credential failed", e)
            }
        }
    }
}