package com.example.codexkosherfood.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.codexkosherfood.KosherFoodApplication
import com.example.codexkosherfood.ui.navigation.Screen
import com.example.codexkosherfood.ui.screen.CameraScanScreen
import com.example.codexkosherfood.ui.screen.GuideScreen
import com.example.codexkosherfood.ui.screen.HomeScreen
import com.example.codexkosherfood.ui.screen.ResultScreen
import com.example.codexkosherfood.ui.screen.ReviewScreen
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
                    onGuide = { navController.navigate(Screen.Guide.route) },
                )
            }

            composable(Screen.Guide.route) {
                GuideScreen(
                    onBack = { navController.popBackStack() },
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
                    onCheckAnotherIngredient = {
                        scanViewModel.startManualEntry()
                        navController.navigate(Screen.Review.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                )
            }
        }
    }
}
