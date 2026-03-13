package com.example.codexkosherfood.data

import android.content.Context
import androidx.room.Room
import com.example.codexkosherfood.BuildConfig
import com.example.codexkosherfood.data.ai.AiIngredientReviewer
import com.example.codexkosherfood.data.ai.AiReviewApi
import com.example.codexkosherfood.data.ai.HttpAiIngredientReviewer
import com.example.codexkosherfood.data.local.KosherFoodDatabase
import com.example.codexkosherfood.data.repository.ScanRepository
import com.example.codexkosherfood.domain.parser.IngredientParser
import com.example.codexkosherfood.domain.rules.KosherRulesEngine
import com.example.codexkosherfood.ocr.TextRecognizerManager
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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

    private val httpLoggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.AI_REVIEW_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private val aiReviewApi: AiReviewApi by lazy {
        retrofit.create(AiReviewApi::class.java)
    }

    val aiIngredientReviewer: AiIngredientReviewer by lazy {
        HttpAiIngredientReviewer(aiReviewApi)
    }

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
