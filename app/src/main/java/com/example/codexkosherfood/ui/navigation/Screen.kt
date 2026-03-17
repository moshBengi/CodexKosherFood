package com.example.codexkosherfood.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Guide : Screen("guide")
    data object Scan : Screen("scan")
    data object Review : Screen("review")
    data object Result : Screen("result")
}
