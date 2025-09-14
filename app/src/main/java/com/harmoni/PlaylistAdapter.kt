// app/src/main/java/com/harmoni/PlaylistAdapter.kt
package com.harmoni

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.harmoni.fragment.PlaylistFragment
import com.harmoni.R

class PlaylistAdapter(
    private val playlists: List<Playlist>,
    private val onItemClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.text_playlist_name)
        val textCount: TextView = itemView.findViewById(R.id.text_track_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.textName.text = playlist.name

        // In future: count actual tracks
        holder.textCount.text = "Tracks: ?"

        holder.itemView.setOnClickListener { onItemClick(playlist) }
    }

    override fun getItemCount() = playlists.size
}