// app/src/main/java/com/harmoni/MediaAdapter.kt
package com.harmoni

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import java.io.File

class MediaAdapter(
    private val items: List<MediaItem>,
    private val onItemClick: (MediaItem) -> Unit,
    private val onOverflowClick: (MediaItem, View) -> Unit
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.iv_thumbnail)
        val title: TextView = itemView.findViewById(R.id.text_title)
        val subtitle: TextView = itemView.findViewById(R.id.text_subtitle)
        val path: TextView = itemView.findViewById(R.id.text_path)
        val overflow: ImageButton = itemView.findViewById(R.id.btn_overflow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val item = items[position]

        holder.title.text = item.title
        holder.subtitle.text = item.subtitle
        holder.path.text = item.path

        // Load album art using Coil
        holder.thumbnail.load(File(item.path)) {
            crossfade(true)
            placeholder(R.drawable.ic_music_note)
            error(R.drawable.ic_file)
            // Custom decoder can extract embedded art later
        }

        holder.itemView.setOnClickListener { onItemClick(item) }

        holder.overflow.setOnClickListener { v ->
            onOverflowClick(item, v)
        }
    }

    override fun getItemCount() = items.size
}