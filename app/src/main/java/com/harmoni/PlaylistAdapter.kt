// app/src/main/java/com/harmoni/PlaylistAdapter.kt
package com.harmoni

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.harmoni.R

class PlaylistAdapter(
    private val playlists: List<PlaylistWithCount>,
    private val onItemClick: (PlaylistWithCount) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.text_playlist_name)
        val textCount: TextView = itemView.findViewById(R.id.text_track_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist_with_count, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.textName.text = playlist.name
        holder.textCount.text = "${playlist.trackCount} track(s)"

        holder.itemView.setOnClickListener { onItemClick(playlist) }
    }

    override fun getItemCount() = playlists.size
}