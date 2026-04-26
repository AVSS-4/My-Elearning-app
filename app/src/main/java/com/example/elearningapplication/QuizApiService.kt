package com.example.elearningapplication

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// --- PART 1: THE DATA SPECS (The "Box" the questions come in) ---
data class QuizResponse(val results: List<ApiQuestionModel>)

data class ApiQuestionModel(
    val question: String,
    val correct_answer: String,
    val incorrect_answers: List<String>,
    val difficulty: String
)

// --- PART 2: THE INTERFACE (The "Blueprint" for the request) ---
interface QuizApiService {
    // UPDATED: Changed amount to 25
    @GET("api.php?amount=25&type=multiple")
    fun getQuestions(
        @Query("category") categoryId: Int
    ): Call<QuizResponse>
}

// --- PART 3: THE CLIENT (The "Engine" that actually goes to the internet) ---
object RetrofitClient {
    private const val BASE_URL = "https://opentdb.com/"

    val instance: QuizApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(QuizApiService::class.java)
    }
}