package com.example.aifitness

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

/**
 * RecyclerView Adapter for displaying songs list
 */
class SongAdapter(
    private val songs: List<Song>,
    private val onPlayPauseClick: (Song, Int) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.cardViewSong)
        val textViewSongName: TextView = itemView.findViewById(R.id.textViewSongName)
        val buttonPlayPause: MaterialButton = itemView.findViewById(R.id.buttonPlayPause) // <-- MaterialButton
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]

        holder.textViewSongName.text = song.name

        // Update play/pause text or icon based on playing state
        holder.buttonPlayPause.text = if (song.isPlaying) "Pause" else "Play"

        // Set click listener for play/pause button
        holder.buttonPlayPause.setOnClickListener {
            onPlayPauseClick(song, position)
        }

        // Optional: Set click listener for entire card
        holder.cardView.setOnClickListener {
            onPlayPauseClick(song, position)
        }
    }

    override fun getItemCount(): Int = songs.size

    /**
     * Update the playing state of a song and refresh the view
     */
    fun updatePlayingState(playingPosition: Int, isPlaying: Boolean) {
        songs[playingPosition].isPlaying = isPlaying
        notifyItemChanged(playingPosition)
    }
}







