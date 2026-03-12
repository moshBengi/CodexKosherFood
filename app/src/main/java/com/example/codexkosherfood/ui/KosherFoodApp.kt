package com.example.codexkosherfood.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.codexkosherfood.KosherFoodApplication
import com.example.codexkosherfood.ui.navigation.Screen
import com.example.codexkosherfood.ui.screen.CameraScanScreen
import com.example.codexkosherfood.ui.screen.HistoryDetailScreen
import com.example.codexkosherfood.ui.screen.HistoryScreen
import com.example.codexkosherfood.ui.screen.HomeScreen
import com.example.codexkosherfood.ui.screen.ResultScreen
import com.example.codexkosherfood.ui.screen.ReviewScreen
import com.example.codexkosherfood.ui.viewmodel.HistoryDetailViewModel
import com.example.codexkosherfood.ui.viewmodel.HistoryViewModel
import com.example.codexkosherfood.ui.viewmodel.ScanSessionViewModel

@Composable
fun KosherFoodApp() {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as KosherFoodApplication

    val scanViewModel: ScanSessionViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val scanState = scanViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNewScan = {
                        scanViewModel.reset()
                        navController.navigate(Screen.Scan.route)
                    },
                    onManualCheck = {
                        scanViewModel.startManualEntry()
                        navController.navigate(Screen.Review.route)
                    },
                    onHistory = { navController.navigate(Screen.History.route) },
                )
            }

            composable(Screen.Scan.route) {
                CameraScanScreen(
                    textRecognizerManager = application.appContainer.textRecognizerManager,
                    onBack = { navController.popBackStack() },
                    onScanResult = { result ->
                        scanViewModel.onOcrResult(result)
                        navController.navigate(Screen.Review.route)
                    },
                )
            }

            composable(Screen.Review.route) {
                ReviewScreen(
                    uiState = scanState.value,
                    onBack = { navController.popBackStack() },
                    onTextChanged = scanViewModel::updateEditedText,
                    onAnalyze = {
                        scanViewModel.analyze()
                        navController.navigate(Screen.Result.route)
                    },
                )
            }

            composable(Screen.Result.route) {
                ResultScreen(
                    uiState = scanState.value,
                    onBack = { navController.popBackStack(Screen.Home.route, false) },
                    onAiReview = scanViewModel::reviewUncertainIngredients,
                    onSave = scanViewModel::save,
                    onScanAgain = {
                        scanViewModel.reset()
                        navController.navigate(Screen.Scan.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                )
            }

            composable(Screen.History.route) {
                val historyViewModel: HistoryViewModel = viewModel(factory = AppViewModelProvider.Factory)
                val historyState = historyViewModel.history.collectAsStateWithLifecycle()
                HistoryScreen(
                    items = historyState.value,
                    onBack = { navController.popBackStack() },
                    onItemClick = { id -> navController.navigate(Screen.historyDetailRoute(id)) },
                )
            }

            composable(
                route = Screen.HistoryDetail.route,
                arguments = listOf(navArgument(Screen.HistoryDetail.scanIdArg) { type = NavType.LongType }),
            ) {
                val detailViewModel: HistoryDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
                val record = detailViewModel.record.collectAsStateWithLifecycle()
                HistoryDetailScreen(
                    record = record.value,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
