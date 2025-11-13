package com.example.aifitness

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageView
import com.airbnb.lottie.LottieAnimationView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ExerciseDetailActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var animationView: LottieAnimationView
    private lateinit var textViewExerciseName: TextView
    private lateinit var textViewDuration: TextView
    private lateinit var textViewDescription: TextView
    private lateinit var buttonStartTimer: Button
    private lateinit var textViewTimer: TextView

    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private var timeLeftInMillis: Long = 0L
    private var totalTimeInMillis: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_exercise_detail)

        initializeViews()
        setupToolbar()
        loadExerciseData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)

        textViewExerciseName = findViewById(R.id.textViewExerciseName)
        textViewDuration = findViewById(R.id.textViewDuration)
        textViewDescription = findViewById(R.id.textViewDescription)
        buttonStartTimer = findViewById(R.id.buttonStartTimer)
        textViewTimer = findViewById(R.id.textViewTimer)
        animationView = findViewById(R.id.exerciseAnimation)

        buttonStartTimer.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                if (timeLeftInMillis > 0) {
                    resumeTimer()
                } else {
                    startTimer()
                }
            }
        }
    }
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Exercise Details"
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun loadExerciseData() {
        val exerciseName = intent.getStringExtra("exercise_name") ?: "Unknown Exercise"
        val exerciseDuration = intent.getStringExtra("exercise_duration") ?: "Not specified"
        val animationRes = intent.getIntExtra("animation_res", R.raw.burpee)

        textViewExerciseName.text = exerciseName
        textViewDuration.text = "Duration: $exerciseDuration"
        animationView.setAnimation(animationRes)
        animationView.playAnimation()

        val description = getExerciseDescription(exerciseName)
        textViewDescription.text = description

        totalTimeInMillis = parseDurationToMs(exerciseDuration)
        timeLeftInMillis = totalTimeInMillis
        updateTimerText()
    }

    private fun getExerciseDescription(exerciseName: String): String {
        return when (exerciseName.lowercase()) {
            "push-ups" -> "A classic upper body exercise that targets chest, shoulders, and triceps. Keep your body straight and lower until your chest nearly touches the floor."
            "squats" -> "A fundamental lower body exercise that works your quadriceps, hamstrings, and glutes. Keep your back straight and lower until your thighs are parallel to the floor."
            "jumping jacks" -> "A full-body cardio exercise. Jump while spreading your legs and raising your arms overhead, then return to starting position."
            "plank" -> "An isometric core exercise. Hold a push-up position with your body in a straight line from head to heels."
            "lunges" -> "A single-leg exercise that targets your quadriceps, hamstrings, and glutes. Step forward and lower your body until both knees are bent at 90 degrees."
            "burpees" -> "A full-body explosive exercise combining a squat, push-up, and jump. Start standing, drop to a squat, do a push-up, jump back to squat, then jump up."
            "mountain climbers" -> "A dynamic cardio exercise. Start in plank position and rapidly alternate bringing your knees toward your chest."
            "high knees" -> "A cardio exercise where you run in place while lifting your knees as high as possible toward your chest."
            else -> "A great exercise to add to your workout routine. Focus on proper form and controlled movements."
        }
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(totalTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
                buttonStartTimer.text = "Pause Timer"
            }

            override fun onFinish() {
                isTimerRunning = false
                timeLeftInMillis = 0
                updateTimerText()
                buttonStartTimer.text = "Start Timer"
                textViewTimer.text = "Exercise Complete! 🎉"
            }
        }
        countDownTimer?.start()
        isTimerRunning = true
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        buttonStartTimer.text = "Resume Timer"
    }

    private fun resumeTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
                isTimerRunning = false
                timeLeftInMillis = 0
                updateTimerText()
                buttonStartTimer.text = "Start Timer"
                textViewTimer.text = "Exercise Complete! 🎉"
            }
        }
        countDownTimer?.start()
        isTimerRunning = true
        buttonStartTimer.text = "Pause Timer"
    }

    private fun updateTimerText() {
        val seconds = (timeLeftInMillis / 1000).toInt()
        textViewTimer.text = "Time remaining: ${seconds}s"
    }

    private fun parseDurationToMs(durationText: String): Long {
        return when {
            durationText.contains("seconds") -> {
                val seconds = durationText.filter { it.isDigit() }.toIntOrNull() ?: 30
                seconds * 1000L
            }
            durationText.contains("reps") -> 60 * 1000L
            else -> 30 * 1000L
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
