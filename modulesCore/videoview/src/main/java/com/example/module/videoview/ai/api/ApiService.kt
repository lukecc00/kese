package com.example.aitrae.api

import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/v3/chat/completions")
    suspend fun analyzeImage(
        @Body request: ImageAnalysisRequest
    ): Response<ImageAnalysisResponse>

    @POST("api/v3/chat/completions")
    fun chat(
        @Body request: ImageAnalysisRequest
    ): Single<ImageAnalysisResponse>
}

data class ImageAnalysisRequest(
    val messages: List<Message>,
    val model: String,
    val temperature: Double = 0.7,
    val top_p: Double = 0.9,
    val stream: Boolean = false
)

data class Message(
    val role: String = "user",
    val content: List<Content>
)

data class Content(
    val type: String,
    val text: String? = null,
    val image_url: ImageUrl? = null
)

data class ImageUrl(
    val url: String,
    val detail: String = "high"
)

data class ImageAnalysisResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>
)

data class Choice(
    val index: Int,
    val message: ResponseMessage,
    val finish_reason: String
)

data class ResponseMessage(
    val role: String,
    val content: String
)
