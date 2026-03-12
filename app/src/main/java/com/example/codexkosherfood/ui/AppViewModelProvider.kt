package com.example.codexkosherfood.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.codexkosherfood.KosherFoodApplication
import com.example.codexkosherfood.ui.viewmodel.HistoryDetailViewModel
import com.example.codexkosherfood.ui.viewmodel.HistoryViewModel
import com.example.codexkosherfood.ui.viewmodel.ScanSessionViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            ScanSessionViewModel(kosherFoodApplication().appContainer.scanRepository)
        }
        initializer {
            HistoryViewModel(kosherFoodApplication().appContainer.scanRepository)
        }
        initializer {
            HistoryDetailViewModel(
                savedStateHandle = createSavedStateHandle(),
                repository = kosherFoodApplication().appContainer.scanRepository,
            )
        }
    }
}

private fun CreationExtras.kosherFoodApplication(): KosherFoodApplication {
    return this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as KosherFoodApplication
}
