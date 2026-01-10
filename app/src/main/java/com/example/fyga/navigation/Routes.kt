package com.example.fyga.navigation

sealed class Routes(val route: String) {
    object Login : Routes("login")
    object Register : Routes("register")
    object Feed : Routes("feed")
    object FinalRegister : Routes("final_register")
    object Profile : Routes("profile")
    object CreatePost : Routes("create_post") // Rota para a nova tela
}
