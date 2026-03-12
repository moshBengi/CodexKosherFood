package com.example.codexkosherfood.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Scan : Screen("scan")
    data object Review : Screen("review")
    data object Result : Screen("result")
    data object History : Screen("history")
    data object HistoryDetail : Screen("history/{scanId}") {
        const val scanIdArg = "scanId"
    }

    companion object {
        fun historyDetailRoute(id: Long): String = "history/$id"
    }
}
