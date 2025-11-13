package com.example.aifitness

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AIApiService {

    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun getDietPlan(@Body request: GeminiRequest): Response<GeminiResponse>

    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun getWorkoutPlan(@Body request: GeminiRequest): Response<GeminiResponse>

    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun getChatResponse(@Body request: GeminiRequest): Response<GeminiResponse>
}

// Request & Response models
data class GeminiRequest(val contents: List<GeminiContent>)
data class GeminiContent(val parts: List<GeminiPart>)
data class GeminiPart(val text: String)

data class GeminiResponse(val candidates: List<GeminiCandidate>?)
data class GeminiCandidate(val content: GeminiContentResponse?)
data class GeminiContentResponse(val parts: List<GeminiPartResponse>?)
data class GeminiPartResponse(val text: String?)



