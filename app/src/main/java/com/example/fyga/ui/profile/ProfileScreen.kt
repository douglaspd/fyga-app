package com.example.fyga.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.fyga.data.model.AccountType
import com.example.fyga.data.model.Post

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.logoutSuccess) {
        if (uiState.logoutSuccess) {
            onLogout()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                actions = {
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sair", tint = Color.White)
                    }
                }
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(color = Color(0xFFB00020))
                }

                uiState.errorMessage != null -> {
                    Text(uiState.errorMessage!!, color = Color.Red)
                }

                uiState.user != null -> {
                    val user = uiState.user!!
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header com informações do usuário
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(user.profileImageUrl ?: "https://picsum.photos/100/100"),
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(user.username, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(user.bio ?: "", color = Color.Gray, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(12.dp))

                                user.accountType?.let {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (it == AccountType.OPENED) Color.Green else Color.Red)
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            if (it == AccountType.OPENED) "Opened" else "Closed",
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                                Divider(color = Color.DarkGray)
                            }
                        }

                        // Grade de posts do usuário
                        items(uiState.posts, key = { it.id }) { post ->
                            val isLiked = uiState.currentUserId?.let { post.likedBy.contains(it) } ?: false
                            PostCardProfile(
                                post = post,
                                isLiked = isLiked,
                                onLikeClick = { viewModel.toggleLike(post.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card de Post para a tela de Perfil. Este é um componente "burro" (stateless).
 * Ele apenas exibe os dados que recebe e delega as ações para cima.
 */
@Composable
private fun PostCardProfile(
    post: Post,
    isLiked: Boolean, // Recebe se o post está curtido ou não
    onLikeClick: () -> Unit // Recebe a ação a ser executada no clique
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(post.imageUrl),
            contentDescription = "Post image",
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onLikeClick) { // Ação de clique é delegada
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Curtir",
                    tint = if (isLiked) Color.Red else Color.Gray // Cor baseada no estado recebido
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(post.description, color = Color.White, fontSize = 14.sp)
        }
    }
}
