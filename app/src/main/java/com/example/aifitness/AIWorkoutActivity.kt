package com.example.aifitness

import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AIWorkoutActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var editTextAge: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var editTextHeight: EditText
    private lateinit var editTextWeight: EditText
    private lateinit var spinnerFitnessLevel: Spinner
    private lateinit var spinnerGoal: Spinner
    private lateinit var buttonGenerateWorkout: Button
    private lateinit var textViewWorkoutPlan: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var cardViewResponse: MaterialCardView

    private lateinit var aiApiService: AIApiService

    // 🧠 Using the same API key as diet planner
    private val API_KEY = "AIzaSyDKofoL1-tWi2GPHUPXgXVvzeluBk_fYB4"
    private val BASE_URL = "https://generativelanguage.googleapis.com/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_workout)

        initializeViews()
        setupToolbar()
        setupSpinners()
        setupApiService()

        buttonGenerateWorkout.setOnClickListener { generateWorkoutPlan() }
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        editTextAge = findViewById(R.id.editTextAge)
        spinnerGender = findViewById(R.id.spinnerGender)
        editTextHeight = findViewById(R.id.editTextHeight)
        editTextWeight = findViewById(R.id.editTextWeight)
        spinnerFitnessLevel = findViewById(R.id.spinnerFitnessLevel)
        spinnerGoal = findViewById(R.id.spinnerGoal)
        buttonGenerateWorkout = findViewById(R.id.buttonGenerateWorkout)
        textViewWorkoutPlan = findViewById(R.id.textViewWorkoutPlan)
        progressBar = findViewById(R.id.progressBar)
        cardViewResponse = findViewById(R.id.cardViewResponse)
    }
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "AI Workout Planner"
        toolbar.setNavigationOnClickListener { finish() }
    }
    private fun setupSpinners() {
        val genderOptions = arrayOf("Male", "Female", "Other")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderOptions)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGender.adapter = genderAdapter

        val fitnessLevelOptions = arrayOf("Beginner", "Intermediate", "Advanced")
        val fitnessLevelAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, fitnessLevelOptions)
        fitnessLevelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFitnessLevel.adapter = fitnessLevelAdapter

        val goalOptions = arrayOf("Lose Weight", "Gain Muscle", "Build Strength", "Improve Endurance", "General Fitness")
        val goalAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, goalOptions)
        goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGoal.adapter = goalAdapter
    }
    private fun setupApiService() {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val newRequest = chain.request().newBuilder()
                .addHeader("x-goog-api-key", API_KEY) // ✅ Correct header for Gemini API
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(newRequest)
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS) // ✅ connect timeout
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)    // ✅ read timeout
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)   // ✅ write timeout
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        aiApiService = retrofit.create(AIApiService::class.java)
    }
    private fun generateWorkoutPlan() {
        if (!validateInputs()) return
        val height = editTextHeight.text.toString().toDoubleOrNull() ?: 0.0
        val weight = editTextWeight.text.toString().toDoubleOrNull() ?: 0.0
        val bmi = if (height > 0) weight / ((height / 100) * (height / 100)) else 0.0
        val prompt = """
            Create a personalized workout plan for a ${editTextAge.text} year old ${spinnerGender.selectedItem} with ${spinnerFitnessLevel.selectedItem} fitness level.
            Height: ${editTextHeight.text} cm, Weight: ${editTextWeight.text} kg, BMI: ${String.format("%.1f", bmi)}
            Goal: ${spinnerGoal.selectedItem}
            
            Please provide:
            1. Weekly workout schedule (days and focus areas)
            2. Specific exercises for each day with sets and reps
            3. Rest periods and recovery tips
            4. Progression recommendations
            5. Safety considerations based on fitness level
            
            Format the response clearly with day-wise breakdown and exercise details.
        """.trimIndent()
        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = GeminiRequest(
                    contents = listOf(GeminiContent(parts = listOf(GeminiPart(prompt))))
                )
                val response = aiApiService.getWorkoutPlan(request)
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    val workoutPlan =
                        response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (!workoutPlan.isNullOrEmpty()) displayWorkoutPlan(workoutPlan)
                    else showError("No workout plan received from AI")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showError("Error: ${e.message}")
                }
            }
        }
    }
    private fun validateInputs(): Boolean {
        var isValid = true
        if (TextUtils.isEmpty(editTextAge.text)) {
            editTextAge.error = "Age is required"; isValid = false
        }
        if (TextUtils.isEmpty(editTextHeight.text)) {
            editTextHeight.error = "Height is required"; isValid = false
        }
        if (TextUtils.isEmpty(editTextWeight.text)) {
            editTextWeight.error = "Weight is required"; isValid = false
        }

        if (!isValid) Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
        return isValid
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        buttonGenerateWorkout.isEnabled = !show
        cardViewResponse.visibility = if (show) android.view.View.GONE else android.view.View.VISIBLE
    }

    private fun displayWorkoutPlan(workoutPlan: String) {
        textViewWorkoutPlan.text = workoutPlan.replace("•", "\n•")
        cardViewResponse.visibility = android.view.View.VISIBLE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        textViewWorkoutPlan.text = "⚠️ Error: $message\n\nPlease check your API key and internet connection."
        cardViewResponse.visibility = android.view.View.VISIBLE
    }
}
