package com.example.fyga.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fyga.data.repository.AuthRepository
import com.example.fyga.data.repository.UserRepository
import com.example.fyga.ui.createpost.CreatePostScreen
import com.example.fyga.ui.feed.FeedScreen
import com.example.fyga.ui.login.LoginScreen
import com.example.fyga.ui.profile.ProfileScreen
import com.example.fyga.ui.register.FinalRegisterScreen
import com.example.fyga.ui.register.RegisterScreen
import com.example.fyga.ui.register.RegisterViewModel
import kotlinx.coroutines.tasks.await

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    val authRepository = AuthRepository()
    val userRepository = UserRepository()
    val registerViewModel: RegisterViewModel = viewModel()
    
    // Estado para controlar qual a tela inicial (evita o flash de tela branca)
    var startDestination by remember { mutableStateOf<String?>(null) }

    // Lógica de Redirecionamento Automático
    LaunchedEffect(Unit) {
        val currentUser = authRepository.currentUser
        if (currentUser != null) {
            // Usuário está logado, vamos ver se ele tem perfil completo
            val profile = userRepository.getUserProfile(currentUser.uid)
            if (profile != null) {
                startDestination = Routes.Feed.route
            } else {
                // Logado mas sem perfil (ex: fechou o app no meio do registro)
                startDestination = Routes.Register.route
            }
        } else {
            // Não logado
            startDestination = Routes.Login.route
        }
    }

    // Enquanto decide a rota, não mostra nada (ou poderia ser uma Splash Screen)
    if (startDestination == null) return

    NavHost(
        navController = navController,
        startDestination = startDestination!!
    ) {
        composable(Routes.Login.route) {
            LoginScreen(
                onRegisterClick = { navController.navigate(Routes.Register.route) },
                onLoginSuccess = {
                    // Após o login SMS, verificamos se o usuário já existe no banco
                    val uid = authRepository.currentUser?.uid
                    if (uid != null) {
                        // Fazemos uma verificação rápida
                        navController.navigate(Routes.Feed.route) {
                            popUpTo(Routes.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }
        
        composable(Routes.Register.route) {
            RegisterScreen(
                viewModel = registerViewModel,
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate(Routes.FinalRegister.route) }
            )
        }
        
        composable(Routes.FinalRegister.route) {
            FinalRegisterScreen(
                viewModel = registerViewModel,
                onRegisterComplete = {
                    navController.navigate(Routes.Feed.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Feed.route) {
            FeedScreen(navController = navController)
        }
        
        composable(Routes.Profile.route) {
            ProfileScreen(onLogout = {
                navController.navigate(Routes.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            })
        }
        
        composable(Routes.CreatePost.route) {
            CreatePostScreen(navController = navController)
        }
    }
}
