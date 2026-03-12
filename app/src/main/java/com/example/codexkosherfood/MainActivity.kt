package com.example.codexkosherfood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.codexkosherfood.ui.KosherFoodApp
import com.example.codexkosherfood.ui.theme.CodexKosherFoodTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CodexKosherFoodTheme {
                KosherFoodApp()
            }
        }
    }
}
