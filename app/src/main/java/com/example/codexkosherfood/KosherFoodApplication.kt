package com.example.codexkosherfood

import android.app.Application
import com.example.codexkosherfood.data.AppContainer

class KosherFoodApplication : Application() {
    val appContainer: AppContainer by lazy { AppContainer() }
}
