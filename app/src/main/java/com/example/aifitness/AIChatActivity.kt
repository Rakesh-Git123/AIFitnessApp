package com.example.aifitness

import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AIChatActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerViewChat: RecyclerView
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: MaterialButton
    private lateinit var progressBar: ProgressBar

    private lateinit var aiApiService: AIApiService
    private lateinit var chatAdapter: ChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()

    private val API_KEY = "AIzaSyDKofoL1-tWi2GPHUPXgXVvzeluBk_fYB4"
    private val BASE_URL = "https://generativelanguage.googleapis.com/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_chat)

        initializeViews()
        setupToolbar()
        setupRecyclerView()
        setupApiService()

        buttonSend.setOnClickListener { sendMessage() }

        editTextMessage.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }

        chatMessages.add(ChatMessage("bot", "Hello! I'm your AI fitness assistant. Ask me anything about fitness, health, or workouts!"))
        chatAdapter.notifyItemInserted(0)
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        recyclerViewChat = findViewById(R.id.recyclerViewChat)
        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.buttonSend)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "AI Chat Assistant"
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(chatMessages)
        recyclerViewChat.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerViewChat.adapter = chatAdapter
    }

    private fun setupApiService() {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val newRequest = chain.request().newBuilder()
                .addHeader("x-goog-api-key", API_KEY)
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(newRequest)
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        aiApiService = retrofit.create(AIApiService::class.java)
    }

    private fun sendMessage() {
        val userMessage = editTextMessage.text.toString().trim()

        if (TextUtils.isEmpty(userMessage)) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            return
        }

        val chatMessage = ChatMessage("user", userMessage)
        chatMessages.add(chatMessage)
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        recyclerViewChat.smoothScrollToPosition(chatMessages.size - 1)

        editTextMessage.setText("")

        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {

                val prompt = "You are a fitness and health expert. Answer the following question about fitness, health, or workouts in a helpful and informative way: $userMessage"

                val request = GeminiRequest(
                    contents = listOf(GeminiContent(parts = listOf(GeminiPart(prompt))))
                )
                val response = aiApiService.getChatResponse(request)

                withContext(Dispatchers.Main) {
                    showLoading(false)
                    val botResponse = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    
                    if (!botResponse.isNullOrEmpty()) {

                        val botMessage = ChatMessage("bot", botResponse)
                        chatMessages.add(botMessage)
                        chatAdapter.notifyItemInserted(chatMessages.size - 1)
                        recyclerViewChat.smoothScrollToPosition(chatMessages.size - 1)
                    } else {
                        showError("No response received from AI")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showError("Error: ${e.message}")
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        buttonSend.isEnabled = !show
        editTextMessage.isEnabled = !show
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        val errorMessage = ChatMessage("bot", "Sorry, I encountered an error. Please try again.")
        chatMessages.add(errorMessage)
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        recyclerViewChat.smoothScrollToPosition(chatMessages.size - 1)
    }
}




