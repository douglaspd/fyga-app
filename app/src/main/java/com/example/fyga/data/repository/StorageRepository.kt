package com.example.fyga.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StorageRepository {

    private val storage = FirebaseStorage.getInstance()

    /**
     * Faz o upload de um arquivo para o Firebase Storage.
     * @param uri O Uri do arquivo local a ser enviado.
     * @param folder O nome da pasta no Storage (ex: "post_images").
     * @return A URL de download do arquivo após o upload.
     */
    suspend fun uploadFile(uri: Uri, folder: String): String {
        // Cria um nome de arquivo único para evitar colisões
        val fileName = "${folder}/${UUID.randomUUID()}"
        val storageRef = storage.reference.child(fileName)

        // Faz o upload do arquivo
        storageRef.putFile(uri).await()

        // Retorna a URL de download
        return storageRef.downloadUrl.await().toString()
    }
}
