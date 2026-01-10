package com.example.fyga.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fyga.ui.createpost.CreatePostScreen
import com.example.fyga.ui.feed.FeedScreen
import com.example.fyga.ui.login.LoginScreen
import com.example.fyga.ui.profile.ProfileScreen
import com.example.fyga.ui.register.FinalRegisterScreen
import com.example.fyga.ui.register.RegisterScreen

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Routes.Login.route
    ) {
        // Login e Registro
        composable(Routes.Login.route) {
            LoginScreen(
                onRegisterClick = { navController.navigate(Routes.Register.route) },
                onLoginSuccess = {
                    navController.navigate(Routes.Feed.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.Register.route) {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onRegisterClick = { navController.navigate(Routes.FinalRegister.route) }
            )
        }
        composable(Routes.FinalRegister.route) {
            FinalRegisterScreen(
                onRegisterComplete = {
                    navController.navigate(Routes.Feed.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Telas principais
        composable(Routes.Feed.route) {
            FeedScreen(navController = navController)
        }
        composable(Routes.Profile.route) {
            ProfileScreen(onLogout = {
                navController.navigate(Routes.Login.route) {
                    popUpTo(Routes.Feed.route) { inclusive = true }
                }
            })
        }
        composable(Routes.CreatePost.route) {
            CreatePostScreen(navController = navController) // Correção: Passando o NavController
        }
    }
}
