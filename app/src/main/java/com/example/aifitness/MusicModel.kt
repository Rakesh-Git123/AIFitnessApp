package com.example.aifitness
data class Song(
    val id: Int,
    val name: String,
    val fileName: String,
    val duration: Long = 0L,
    var isPlaying: Boolean = false
)







