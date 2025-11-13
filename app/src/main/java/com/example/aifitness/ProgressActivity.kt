package com.example.aifitness

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView

class ProgressActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var textViewDaysCompleted: TextView
    private lateinit var textViewTarget: TextView
    private lateinit var buttonMarkComplete: Button
    private lateinit var buttonReset: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var cardViewProgress: MaterialCardView
    private lateinit var sharedPreferences: SharedPreferences
    private val prefsName = "ProgressPrefs"
    private val keyDaysCompleted = "daysCompleted"
    private val keyTarget = "targetDays"
    private val keyBadges = "badgesUnlocked"

    private var daysCompleted = 0
    private var currentTarget = 30
    private var badgesUnlocked = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_progress)
        sharedPreferences = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        initializeViews()
        setupToolbar()
        loadProgress()
        setupClickListeners()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        textViewDaysCompleted = findViewById(R.id.textViewDaysCompleted)
        textViewTarget = findViewById(R.id.textViewTarget)
        buttonMarkComplete = findViewById(R.id.buttonMarkComplete)
        buttonReset = findViewById(R.id.buttonReset)
        progressBar = findViewById(R.id.progressBar)
        cardViewProgress = findViewById(R.id.cardViewProgress)
    }
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Progress Tracker"
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }
    private fun setupClickListeners() {
        buttonMarkComplete.setOnClickListener { markDayComplete() }
        buttonReset.setOnClickListener { resetProgress() }
    }
    private fun loadProgress() {
        daysCompleted = sharedPreferences.getInt(keyDaysCompleted, 0)
        currentTarget = sharedPreferences.getInt(keyTarget, 30)
        badgesUnlocked = sharedPreferences.getStringSet(keyBadges, mutableSetOf())
            ?.map { it.toInt() }?.toMutableSet() ?: mutableSetOf()
        updateUI()
    }
    private fun markDayComplete() {
        daysCompleted++

        if (daysCompleted == currentTarget) {
            badgesUnlocked.add(currentTarget)
            currentTarget += 30
        }
        saveProgress()
        updateUI()
        val message = when {
            daysCompleted == 1 -> "Great! You've completed your first day! 🎉"
            badgesUnlocked.contains(daysCompleted) -> "Congrats! Target ${daysCompleted} reached! Badge unlocked 🏅"
            daysCompleted % 7 == 0 -> "Amazing! You've completed $daysCompleted days! Keep it up! 💪"
            else -> "Day $daysCompleted completed ✅"
        }
        textViewDaysCompleted.text = message
    }
    private fun resetProgress() {
        daysCompleted = 0
        currentTarget = 30
        badgesUnlocked.clear()
        saveProgress()
        updateUI()
        textViewDaysCompleted.text = "Progress reset to 0 days"
    }
    private fun saveProgress() {
        val editor = sharedPreferences.edit()
        editor.putInt(keyDaysCompleted, daysCompleted)
        editor.putInt(keyTarget, currentTarget)
        editor.putStringSet(keyBadges, badgesUnlocked.map { it.toString() }.toSet())
        editor.apply()
    }
    private fun updateUI() {
        val daysText = when {
            daysCompleted == 0 -> "You haven't completed any days yet"
            daysCompleted == 1 -> "You have completed 1 day"
            else -> "You have completed $daysCompleted days"
        }
        textViewDaysCompleted.text = daysText

        val progressPercentage = (daysCompleted.toFloat() / currentTarget * 100).toInt()
        progressBar.progress = progressPercentage.coerceAtMost(100)

        buttonMarkComplete.text = if (daysCompleted == 0) "Mark First Day Complete" else "Mark Day Complete"

        textViewTarget.text = "Target: $currentTarget days"

        updateMotivationalMessage()
    }

    private fun updateMotivationalMessage() {
        val motivationalText = when {
            daysCompleted == 0 -> "Start your fitness journey today! 💪"
            daysCompleted in 1..6 -> "Great start! Keep building that habit! 🔥"
            daysCompleted in 7..(currentTarget - 1) -> "Keep going! Almost to your target! 🏆"
            daysCompleted >= currentTarget -> "Target reached! Time for the next milestone! 👑"
            else -> "Keep going! Every day counts! 💯"
        }

    }
}


