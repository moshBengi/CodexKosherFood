package com.example.codexkosherfood.data

import android.content.Context
import androidx.room.Room
import com.example.codexkosherfood.data.ai.AiIngredientReviewer
import com.example.codexkosherfood.data.ai.DisabledAiIngredientReviewer
import com.example.codexkosherfood.data.local.KosherFoodDatabase
import com.example.codexkosherfood.data.repository.ScanRepository
import com.example.codexkosherfood.domain.parser.IngredientParser
import com.example.codexkosherfood.domain.rules.KosherRulesEngine
import com.example.codexkosherfood.ocr.TextRecognizerManager
import com.google.gson.Gson

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: KosherFoodDatabase by lazy {
        Room.databaseBuilder(
            appContext,
            KosherFoodDatabase::class.java,
            "kosher_food.db",
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    val ingredientParser: IngredientParser by lazy { IngredientParser() }
    val rulesEngine: KosherRulesEngine by lazy { KosherRulesEngine() }
    val gson: Gson by lazy { Gson() }
    val textRecognizerManager: TextRecognizerManager by lazy { TextRecognizerManager() }
    val aiIngredientReviewer: AiIngredientReviewer by lazy { DisabledAiIngredientReviewer() }

    val scanRepository: ScanRepository by lazy {
        ScanRepository(
            dao = database.scanHistoryDao(),
            ingredientParser = ingredientParser,
            rulesEngine = rulesEngine,
            aiIngredientReviewer = aiIngredientReviewer,
            gson = gson,
        )
    }
}
