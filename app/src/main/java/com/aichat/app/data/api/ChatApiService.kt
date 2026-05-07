package com.aichat.app.data.api

import com.aichat.app.data.model.ChatRequest
import com.aichat.app.data.model.ChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ChatApiService {
    @POST("chat/completions")
    suspend fun sendMessage(
        @Header("Authorization") apiKey: String,
        @Body request: ChatRequest
    ): Response<ChatResponse>
}
