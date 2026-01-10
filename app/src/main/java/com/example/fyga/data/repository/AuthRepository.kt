package com.example.fyga.data.repository

import androidx.activity.ComponentActivity
import com.example.fyga.data.model.User
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun sendVerificationCode(
        phoneNumber: String,
        activity: ComponentActivity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // Número de telefone para o qual enviar o código
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout e unidade
            .setActivity(activity) // Activity (necessária para a verificação)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun verifyCode(verificationId: String, code: String): PhoneAuthCredential {
        return PhoneAuthProvider.getCredential(verificationId, code)
    }

    suspend fun signInWithCredential(credential: PhoneAuthCredential) = auth.signInWithCredential(credential).await()

    suspend fun saveUserProfile(user: User) {
        // Usa o ID do usuário do Firebase como ID do documento
        val firebaseUser = auth.currentUser ?: throw IllegalStateException("Usuário não autenticado.")
        val userWithId = user.copy(id = firebaseUser.uid)

        db.collection("users").document(userWithId.id).set(userWithId).await()
    }

    fun logout() {
        auth.signOut()
    }

    val currentUser get() = auth.currentUser
}
