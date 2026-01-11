package com.example.fyga.ui.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.fyga.data.model.Post
import com.example.fyga.navigation.Routes
import com.example.fyga.ui.components.VideoPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    navController: NavController,
    viewModel: FeedViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("Fyga", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
                )
                TabRow(
                    selectedTabIndex = if (uiState.selectedTab == FeedTab.FOR_YOU) 0 else 1,
                    containerColor = Color.Black,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        if (tabPositions.isNotEmpty()) {
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(tabPositions[if (uiState.selectedTab == FeedTab.FOR_YOU) 0 else 1]),
                                color = Color.Red
                            )
                        }
                    },
                    divider = {}
                ) {
                    Tab(
                        selected = uiState.selectedTab == FeedTab.FOR_YOU,
                        onClick = { viewModel.onTabSelected(FeedTab.FOR_YOU) },
                        text = { Text("4you", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = uiState.selectedTab == FeedTab.COVEN,
                        onClick = { viewModel.onTabSelected(FeedTab.COVEN) },
                        text = { Text("Coven", fontWeight = FontWeight.Bold) }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.CreatePost.route) },
                containerColor = Color(0xFFB00020),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Post")
            }
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
                uiState.posts.isEmpty() && uiState.selectedTab == FeedTab.COVEN -> {
                    Text("Seu Coven está vazio. Siga novos usuários!", color = Color.Gray)
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.posts, key = { it.id }) { post ->
                            val isLiked = post.likedBy.contains(uiState.currentUserId)
                            val isFollowing = uiState.currentUserProfile?.following?.contains(post.userId) ?: false
                            val isMe = post.userId == uiState.currentUserId

                            PostCard(
                                post = post,
                                isLiked = isLiked,
                                isFollowing = isFollowing,
                                isMe = isMe,
                                onLikeClick = { viewModel.toggleLike(post.id) },
                                onCommentClick = { postId, text -> viewModel.addComment(postId, text) },
                                onFollowClick = { viewModel.toggleFollow(post.userId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PostCard(
    post: Post,
    isLiked: Boolean,
    isFollowing: Boolean,
    isMe: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: (String, String) -> Unit,
    onFollowClick: () -> Unit
) {
    var commentsVisible by remember { mutableStateOf(false) }
    var newCommentText by remember { mutableStateOf("") }

    val isVideo = post.imageUrl.contains(".mp4", ignoreCase = true) || 
                  post.imageUrl.contains(".mov", ignoreCase = true) ||
                  post.imageUrl.contains("post_videos", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(post.profilePic),
                contentDescription = "Foto de perfil",
                modifier = Modifier.size(42.dp).clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(post.username, color = Color.White, fontWeight = FontWeight.Bold)
            
            if (!isMe) {
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onFollowClick) {
                    Text(
                        text = if (isFollowing) "Kindred" else "Follow",
                        color = if (isFollowing) Color.Gray else Color.Red,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isVideo) {
            VideoPlayer(videoUrl = post.imageUrl)
        } else {
            Image(
                painter = rememberAsyncImagePainter(post.imageUrl),
                contentDescription = "Post image",
                modifier = Modifier.fillMaxWidth().height(380.dp).clip(RoundedCornerShape(8.dp)).background(Color.DarkGray),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onLikeClick) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Curtir",
                    tint = if (isLiked) Color.Red else Color.Gray
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(onClick = { commentsVisible = !commentsVisible }) {
                Icon(Icons.Default.ModeComment, contentDescription = "Comentar", tint = Color.Gray)
            }
        }

        Text(post.description, color = Color.White, fontSize = 15.sp, modifier = Modifier.padding(horizontal = 16.dp))

        if (commentsVisible) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                post.comments.forEach { comment ->
                    Row {
                        Text(comment.username, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(comment.text, color = Color.White, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { Text("Adicione um comentário...") },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = Color.White,
                            focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Gray, unfocusedIndicatorColor = Color.DarkGray
                        )
                    )
                    IconButton(onClick = {
                        if (newCommentText.isNotBlank()) {
                            onCommentClick(post.id, newCommentText)
                            newCommentText = ""
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Publicar", tint = Color.White)
                    }
                }
            }
        }
    }
}
