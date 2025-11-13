package com.example.aifitness

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        

        val buttonProfile = findViewById<Button>(R.id.buttonProfile)
        buttonProfile.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }
        

        val buttonWorkout = findViewById<Button>(R.id.buttonWorkout)
        buttonWorkout.setOnClickListener {
            val intent = Intent(this, WorkoutActivity::class.java)
            startActivity(intent)
        }
        

        val buttonProgress = findViewById<Button>(R.id.buttonProgress)
        buttonProgress.setOnClickListener {
            val intent = Intent(this, ProgressActivity::class.java)
            startActivity(intent)
        }
        

        val buttonDiet = findViewById<Button>(R.id.buttonDiet)
        buttonDiet.setOnClickListener {
            val intent = Intent(this, AIDietActivity::class.java)
            startActivity(intent)
        }
        

        val buttonAIWorkout = findViewById<Button>(R.id.buttonAIWorkout)
        buttonAIWorkout.setOnClickListener {
            val intent = Intent(this, AIWorkoutActivity::class.java)
            startActivity(intent)
        }
        

        val buttonMusic = findViewById<Button>(R.id.buttonMusic)
        buttonMusic.setOnClickListener {
            val intent = Intent(this, MusicPlayerActivity::class.java)
            startActivity(intent)
        }

        val buttonAIChat = findViewById<ImageButton>(R.id.buttonAIChat)
        buttonAIChat.setOnClickListener {
            val intent = Intent(this, AIChatActivity::class.java)
            startActivity(intent)
        }
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}