package com.example.codexkosherfood.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codexkosherfood.data.repository.ScanRepository
import com.example.codexkosherfood.ui.navigation.Screen
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class HistoryDetailViewModel(
    savedStateHandle: SavedStateHandle,
    repository: ScanRepository,
) : ViewModel() {
    private val scanId: Long = checkNotNull(savedStateHandle[Screen.HistoryDetail.scanIdArg])

    val record = repository.observeScan(scanId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
