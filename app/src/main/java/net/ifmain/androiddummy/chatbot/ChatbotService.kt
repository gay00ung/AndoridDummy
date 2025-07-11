package net.ifmain.androiddummy.chatbot

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET

/**
 * AndroidDummy
 * Class : ChatbotService.
 * Created by gayoung.
 * Created On 2025-07-11.
 * Description:
 */
data class ChatRequest(
    val message: String,
    val user_data: Map<String, Any>? = null
)

data class ChatResponse(
    val success: Boolean,
    val response: String?,
    val error: String?
)

data class HealthResponse(
    val status: String
)

interface ChatbotApi {
    @POST("/chat")
    suspend fun sendMessage(@Body request: ChatRequest): ChatResponse

    @GET("/health")
    suspend fun checkHealth(): HealthResponse
}

object ChatbotService {
     private const val BASE_URL = "http://192.168.0.2:5000/"

    val api: ChatbotApi by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChatbotApi::class.java)
    }
}