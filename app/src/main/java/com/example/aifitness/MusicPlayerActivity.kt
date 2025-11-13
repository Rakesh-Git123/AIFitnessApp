package com.example.aifitness

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import android.widget.Toast

class MusicPlayerActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var recyclerViewSongs: RecyclerView
    private lateinit var cardViewNowPlaying: MaterialCardView
    private lateinit var textViewNowPlayingSong: TextView
    private lateinit var seekBarProgress: SeekBar
    private lateinit var buttonNowPlayingPlayPause: MaterialButton
    private lateinit var buttonNowPlayingStop: MaterialButton
    private lateinit var songAdapter: SongAdapter
    private var mediaPlayer: MediaPlayer? = null
    private var currentPosition = -1
    private var isUserSeeking = false
    private val handler = Handler(Looper.getMainLooper())
    private val songs = listOf(
        Song(1, "Workout Song 1", "song1"),
        Song(2, "Workout Song 2", "song2"),
        Song(3, "Workout Song 3", "song3"),
        Song(4, "Motivational Beat", "song4"),
        Song(5, "Cardio Mix", "song5")
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_music_player)
        initializeViews()
        setupToolbar()
        setupRecyclerView()
        setupNowPlayingSection()
        setupSeekBar()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        recyclerViewSongs = findViewById(R.id.recyclerViewSongs)
        cardViewNowPlaying = findViewById(R.id.cardViewNowPlaying)
        textViewNowPlayingSong = findViewById(R.id.textViewNowPlayingSong)
        seekBarProgress = findViewById(R.id.seekBarProgress)
        buttonNowPlayingPlayPause = findViewById(R.id.buttonNowPlayingPlayPause)
        buttonNowPlayingStop = findViewById(R.id.buttonNowPlayingStop)
    }
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Workout Music"
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }
    private fun setupRecyclerView() {
        songAdapter = SongAdapter(songs) { song, position ->
            playSong(song, position)
        }
        recyclerViewSongs.layoutManager = LinearLayoutManager(this)
        recyclerViewSongs.adapter = songAdapter
    }
    private fun setupNowPlayingSection() {
        cardViewNowPlaying.visibility = android.view.View.GONE
        buttonNowPlayingPlayPause.setOnClickListener {
            togglePlayPause()
        }
        buttonNowPlayingStop.setOnClickListener {
            stopSong()
        }
    }
    private fun setupSeekBar() {
        seekBarProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) { isUserSeeking = true }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = false
                mediaPlayer?.let {
                    val position = (seekBar?.progress ?: 0) * it.duration / 100
                    it.seekTo(position)
                }
            }
        })
    }
    private fun playSong(song: Song, position: Int) {
        stopSong()

        val resId = resources.getIdentifier(song.fileName, "raw", packageName)
        if (resId == 0) {
            Toast.makeText(this, "Song not found: ${song.fileName}", Toast.LENGTH_SHORT).show()
            return
        }
        mediaPlayer = MediaPlayer.create(this, resId)
        mediaPlayer?.start()
        cardViewNowPlaying.visibility = android.view.View.VISIBLE
        textViewNowPlayingSong.text = song.name
        buttonNowPlayingPlayPause.text = "Pause"
        currentPosition = position
        handler.post(updateSeekBarRunnable)
        mediaPlayer?.setOnCompletionListener {
            stopSong()
        }
    }
    private fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                buttonNowPlayingPlayPause.text = "Play"
            } else {
                it.start()
                buttonNowPlayingPlayPause.text = "Pause"
            }
        }
    }
    private fun stopSong() {
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacks(updateSeekBarRunnable)
        cardViewNowPlaying.visibility = android.view.View.GONE
        buttonNowPlayingPlayPause.text = "Play"
    }
    private val updateSeekBarRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let {
                if (it.isPlaying && !isUserSeeking) {
                    val progress = (it.currentPosition * 100) / it.duration
                    seekBarProgress.progress = progress
                }
                handler.postDelayed(this, 1000)
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        stopSong()
    }
}







