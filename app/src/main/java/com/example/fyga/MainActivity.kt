package com.example.fyga

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.fyga.navigation.AppNavHost
import com.example.fyga.ui.theme.FygaTheme
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            FygaTheme {
                AppNavHost()
            }
        }
    }
}
