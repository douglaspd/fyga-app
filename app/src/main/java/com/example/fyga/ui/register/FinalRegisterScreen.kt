package com.example.fyga.ui.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.fyga.data.model.AccountType
import com.example.fyga.data.model.User
import com.example.fyga.ui.login.auth.AuthLoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinalRegisterScreen(
    authViewModel: AuthLoginViewModel,
    onRegisterComplete: () -> Unit,
    onBack: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var accountType by remember { mutableStateOf(AccountType.OPENED) }
    var isLoading by remember { mutableStateOf(false) }

    val currentUser = authViewModel.currentUser

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Fyga",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Personalize seu perfil",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Image(
                painter = rememberAsyncImagePainter(currentUser?.photoUrl),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Red, CircleShape)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nome de usuário", color = Color.Gray) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Red,
                    unfocusedContainerColor = Color.DarkGray,
                    focusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio (opcional)", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Red,
                    unfocusedContainerColor = Color.DarkGray,
                    focusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Toggle OPENED / CLOSED
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AccountTypeToggle(
                    selected = accountType == AccountType.OPENED,
                    label = "Opened",
                    onClick = { accountType = AccountType.OPENED }
                )
                AccountTypeToggle(
                    selected = accountType == AccountType.CLOSED,
                    label = "Closed",
                    onClick = { accountType = AccountType.CLOSED }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    if (username.isNotBlank() && currentUser != null) {
                        isLoading = true
                        val user = User(
                            id = currentUser.uid,
                            username = username.trim(),
                            email = currentUser.email ?: "",
                            bio = bio.trim(),
                            accountType = accountType
                        )
                        authViewModel.saveUserProfile(user) {
                            isLoading = false
                            authViewModel.saveUserProfile(user) {
                                isLoading = false
                                onRegisterComplete()
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isLoading)
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                else
                    Text("Entrar no GothWolrd", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun AccountTypeToggle(selected: Boolean, label: String, onClick: () -> Unit) {
    val bgColor = if (selected) Color.Red else Color.DarkGray
    val textColor = if (selected) Color.White else Color.LightGray

    Box(
        modifier = Modifier
            .width(130.dp)
            .height(45.dp)
            .background(bgColor, RoundedCornerShape(12.dp))
            .wrapContentSize(Alignment.Center)
            .clickable { onClick() }
    ) {
        Text(label, color = textColor, fontWeight = FontWeight.SemiBold)
    }
}

