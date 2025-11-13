package com.example.aifitness

import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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

class AIDietActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var editTextAge: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var editTextHeight: EditText
    private lateinit var editTextWeight: EditText
    private lateinit var spinnerGoal: Spinner
    private lateinit var buttonGenerateDiet: Button
    private lateinit var textViewDietPlan: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var cardViewResponse: MaterialCardView

    private lateinit var aiApiService: AIApiService

    private val API_KEY = "AIzaSyDKofoL1-tWi2GPHUPXgXVvzeluBk_fYB4"
    private val BASE_URL = "https://generativelanguage.googleapis.com/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_diet)

        initializeViews()
        setupToolbar()
        setupSpinners()
        setupApiService()

        buttonGenerateDiet.setOnClickListener { generateDietPlan() }
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        editTextAge = findViewById(R.id.editTextAge)
        spinnerGender = findViewById(R.id.spinnerGender)
        editTextHeight = findViewById(R.id.editTextHeight)
        editTextWeight = findViewById(R.id.editTextWeight)
        spinnerGoal = findViewById(R.id.spinnerGoal)
        buttonGenerateDiet = findViewById(R.id.buttonGenerateDiet)
        textViewDietPlan = findViewById(R.id.textViewDietPlan)
        progressBar = findViewById(R.id.progressBar)
        cardViewResponse = findViewById(R.id.cardViewResponse)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "AI Diet Planner"
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupSpinners() {

        val genderOptions = arrayOf("Male", "Female", "Other")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderOptions)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGender.adapter = genderAdapter

        val goalOptions = arrayOf("Lose Weight", "Gain Weight", "Maintain Weight", "Build Muscle", "Improve Health")
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

    private fun generateDietPlan() {
        if (!validateInputs()) return

        val prompt = """
            Suggest a healthy Indian diet plan for a ${editTextAge.text} year old ${spinnerGender.selectedItem} trying to ${spinnerGoal.selectedItem}. 
            Height: ${editTextHeight.text} cm, Weight: ${editTextWeight.text} kg.
            Format response with meal timings, portions, and water intake.
        """.trimIndent()

        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = GeminiRequest(
                    contents = listOf(GeminiContent(parts = listOf(GeminiPart(prompt))))
                )

                val response = aiApiService.getDietPlan(request)

                withContext(Dispatchers.Main) {
                    showLoading(false)
                    val dietPlan =
                        response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (!dietPlan.isNullOrEmpty()) displayDietPlan(dietPlan)
                    else showError("No diet plan received from AI")
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
        buttonGenerateDiet.isEnabled = !show
        cardViewResponse.visibility = if (show) android.view.View.GONE else android.view.View.VISIBLE
    }

    private fun displayDietPlan(dietPlan: String) {
        textViewDietPlan.text = dietPlan.replace("•", "\n•")
        cardViewResponse.visibility = android.view.View.VISIBLE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        textViewDietPlan.text = "⚠️ Error: $message\n\nPlease check your API key and internet connection."
        cardViewResponse.visibility = android.view.View.VISIBLE
    }
}