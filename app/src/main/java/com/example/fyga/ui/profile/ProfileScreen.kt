package com.example.fyga.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import coil.compose.rememberAsyncImagePainter
import com.example.fyga.data.model.AccountType
import com.example.fyga.data.model.Post
import com.example.fyga.data.model.User

@Composable
fun ProfileScreen(
    user: User,
    posts: List<Post>,
    onMessageClick: () -> Unit = {},
    onViewContentClick: () -> Unit = {}
) {

    var likedPosts by remember {
        mutableStateOf(posts.map { it.liked }.toMutableStateList())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Header
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

                // AccountType
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (user.accountType == AccountType.OPENED) Color.Green else Color.Red)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        if (user.accountType == AccountType.OPENED) "Opened" else "Closed",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onMessageClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Mensagem", color = Color.White)
                    }
                    Button(
                        onClick = onViewContentClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Ver Conteúdo", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Posts
        items(posts) { post ->
            val index = posts.indexOf(post)
            val liked = likedPosts[index]

            PostCardProfile(
                post = post,
                liked = liked,
                onLikeClick = { likedPosts[index] = !liked },
                isClosed = user.accountType == AccountType.CLOSED
            )
        }
    }
}

@Composable
fun PostCardProfile(
    post: Post,
    liked: Boolean,
    onLikeClick: () -> Unit,
    isClosed: Boolean
) {
    var likedState by remember { mutableStateOf(liked) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Cabeçalho
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(post.imageUrl),
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = post.username,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Imagem do post
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = rememberAsyncImagePainter(post.imageUrl),
                contentDescription = "Post image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            if (isClosed) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0x99000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Conteúdo fechado",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Ações: curtir
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                onLikeClick()
            }) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Favorite,
                    contentDescription = "Curtir",
                    tint = if (liked) Color.Red else Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
