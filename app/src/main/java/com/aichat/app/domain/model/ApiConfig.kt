package com.aichat.app.domain.model

data class ApiConfig(
    val apiKey: String = "",
    val baseUrl: String = "https://api.openai.com/v1/",
    val model: String = "gpt-3.5-turbo"
)
