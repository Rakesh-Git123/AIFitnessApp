package com.example.aifitness

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.DecimalFormat

data class Badge(val name: String, val imageResId: Int, val minDays: Int)

class UserProfileActivity : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var editTextAge: EditText
    private lateinit var editTextHeight: EditText
    private lateinit var editTextWeight: EditText
    private lateinit var buttonSaveProfile: Button
    private lateinit var textViewBMI: TextView
    private lateinit var linearLayoutBadges: LinearLayout

    private lateinit var sharedPreferences: SharedPreferences
    private val prefsName = "UserProfilePrefs"

    companion object {
        private const val KEY_NAME = "name"
        private const val KEY_AGE = "age"
        private const val KEY_HEIGHT = "height"
        private const val KEY_WEIGHT = "weight"
        private const val KEY_BMI = "bmi"
        private const val KEY_BMI_CATEGORY = "bmi_category"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_profile)

        sharedPreferences = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        initializeViews()
        loadUserData()
        loadBadges()

        buttonSaveProfile.setOnClickListener {
            saveUserProfile()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
     private fun initializeViews() {
        editTextName = findViewById(R.id.editTextName)
        editTextAge = findViewById(R.id.editTextAge)
        editTextHeight = findViewById(R.id.editTextHeight)
        editTextWeight = findViewById(R.id.editTextWeight)
        buttonSaveProfile = findViewById(R.id.buttonSaveProfile)
        textViewBMI = findViewById(R.id.textViewBMI)
        linearLayoutBadges = findViewById(R.id.linearLayoutBadges)
    }

    private fun loadUserData() {
        val name = sharedPreferences.getString(KEY_NAME, "")
        val age = sharedPreferences.getString(KEY_AGE, "")
        val height = sharedPreferences.getString(KEY_HEIGHT, "")
        val weight = sharedPreferences.getString(KEY_WEIGHT, "")
        val bmi = sharedPreferences.getString(KEY_BMI, "")
        val bmiCategory = sharedPreferences.getString(KEY_BMI_CATEGORY, "")

        editTextName.setText(name)
        editTextAge.setText(age)
        editTextHeight.setText(height)
        editTextWeight.setText(weight)

        if (!bmi.isNullOrEmpty() && !bmiCategory.isNullOrEmpty()) {
            textViewBMI.text = "Your BMI: $bmi ($bmiCategory)"
        }
    }

    private fun saveUserProfile() {
        val name = editTextName.text.toString().trim()
        val ageStr = editTextAge.text.toString().trim()
        val heightStr = editTextHeight.text.toString().trim()
        val weightStr = editTextWeight.text.toString().trim()

        if (validateInput(name, ageStr, heightStr, weightStr)) return

        try {
            val age = ageStr.toInt()
            val height = heightStr.toDouble()
            val weight = weightStr.toDouble()

            val bmi = weight / (height * height)
            val bmiCategory = determineBMICategory(bmi)
            val formattedBMI = DecimalFormat("#.##").format(bmi)

            val editor = sharedPreferences.edit()
            editor.putString(KEY_NAME, name)
            editor.putString(KEY_AGE, ageStr)
            editor.putString(KEY_HEIGHT, heightStr)
            editor.putString(KEY_WEIGHT, weightStr)
            editor.putString(KEY_BMI, formattedBMI)
            editor.putString(KEY_BMI_CATEGORY, bmiCategory)
            editor.apply()

            textViewBMI.text = "Your BMI: $formattedBMI ($bmiCategory)"
            Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Enter valid numeric values for age, height, and weight.", Toast.LENGTH_LONG).show()
        }
    }

    private fun validateInput(name: String, age: String, height: String, weight: String): Boolean {
        var hasError = false
        if (TextUtils.isEmpty(name)) { editTextName.error = "Name is required"; hasError = true } else editTextName.error = null
        if (TextUtils.isEmpty(age)) { editTextAge.error = "Age is required"; hasError = true } else editTextAge.error = null
        if (TextUtils.isEmpty(height)) { editTextHeight.error = "Height is required"; hasError = true } else editTextHeight.error = null
        if (TextUtils.isEmpty(weight)) { editTextWeight.error = "Weight is required"; hasError = true } else editTextWeight.error = null
        if (hasError) Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        return hasError
    }

    private fun determineBMICategory(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Underweight"
            bmi in 18.5..24.9 -> "Normal"
            bmi >= 25.0 -> "Overweight"
            else -> "Unknown"
        }
    }

    // 🔥 BADGES FUNCTION
    private fun loadBadges() {
        val progressPrefs = getSharedPreferences("ProgressPrefs", Context.MODE_PRIVATE)
        val daysCompleted = progressPrefs.getInt("daysCompleted", 0)

        val allBadges = listOf(
            Badge("Beginner", R.drawable.badge_beginner, 1),
            Badge("Week Champ", R.drawable.badge_week, 7),
            Badge("Month Hero", R.drawable.badge_month, 30),
            Badge("60 Days Star", R.drawable.badge_60days, 60),
            Badge("90 Days Legend", R.drawable.badge_90days, 90)
        )

        linearLayoutBadges.removeAllViews()

        allBadges.forEach { badge ->
            if (daysCompleted >= badge.minDays) {
                val imageView = ImageView(this)
                imageView.setImageResource(badge.imageResId)
                val params = LinearLayout.LayoutParams(150, 150)
                params.setMargins(16, 0, 16, 0)
                imageView.layoutParams = params
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                linearLayoutBadges.addView(imageView)
            }
        }
    }
}




