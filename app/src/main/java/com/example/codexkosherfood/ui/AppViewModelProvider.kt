package com.example.codexkosherfood.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.codexkosherfood.KosherFoodApplication
import com.example.codexkosherfood.ui.viewmodel.ScanSessionViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            ScanSessionViewModel(kosherFoodApplication().appContainer.scanRepository)
        }
    }
}

private fun CreationExtras.kosherFoodApplication(): KosherFoodApplication {
    return this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as KosherFoodApplication
}
