package com.example.aifitness

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.card.MaterialCardView
import android.widget.TextView

class WorkoutActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_workout)

        recyclerView = findViewById(R.id.recyclerViewWorkouts)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Daily Workout Plan"
        toolbar.setNavigationOnClickListener { onBackPressed() }

        setupRecyclerView()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        val exerciseList = createExerciseList()
        val adapter = WorkoutAdapter(exerciseList) { exercise ->

            val intent = Intent(this, ExerciseDetailActivity::class.java)
            intent.putExtra("exercise_name", exercise.name)
            intent.putExtra("exercise_duration", exercise.duration)
            intent.putExtra("animation_res", exercise.animationRes)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun createExerciseList(): List<Exercise> {
        return listOf(
            Exercise("Push-ups", R.raw.pushups, "3 sets of 15 reps"),
            Exercise("Squats", R.raw.squat, "3 sets of 20 reps"),
            Exercise("Jumping Jacks", R.raw.jump, "20 seconds"),
            Exercise("Plank", R.raw.plank, "30 seconds"),
            Exercise("Lunges", R.raw.lunges, "3 sets of 12 reps each leg"),
            Exercise("Burpees", R.raw.burpee, "10 reps"),
            Exercise("Mountain Climbers", R.raw.burpee, "30 seconds"),
            Exercise("High Knees", R.raw.high, "20 seconds")
        )
    }
}

data class Exercise(
    val name: String,
    val animationRes: Int,
    val duration: String
)

class WorkoutAdapter(
    private val exerciseList: List<Exercise>,
    private val onItemClick: (Exercise) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout, parent, false)
        return WorkoutViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        holder.bind(exerciseList[position])
    }

    override fun getItemCount(): Int = exerciseList.size

    class WorkoutViewHolder(itemView: android.view.View, val onItemClick: (Exercise) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val cardView: MaterialCardView = itemView.findViewById(R.id.cardViewExercise)
        private val lottieAnimationView: LottieAnimationView = itemView.findViewById(R.id.exerciseAnimation)
        private val textViewExerciseName: TextView = itemView.findViewById(R.id.textViewExerciseName)
        private val textViewDuration: TextView = itemView.findViewById(R.id.textViewDuration)

        fun bind(exercise: Exercise) {
            lottieAnimationView.setAnimation(exercise.animationRes)
            lottieAnimationView.playAnimation()
            textViewExerciseName.text = exercise.name
            textViewDuration.text = exercise.duration

            cardView.setOnClickListener { onItemClick(exercise) }
        }
    }
}
