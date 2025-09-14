// app/src/main/java/com/harmoni/MediaAdapter.kt
package com.harmoni

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.harmoni.R
import com.harmoni.fragment.VideosFragment

class MediaAdapter(
    private val items: List<MediaItem>,
    private val onItemClick: (MediaItem) -> Unit
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconType: ImageView = itemView.findViewById(R.id.icon_type)
        val textTitle: TextView = itemView.findViewById(R.id.text_title)
        val textSubtitle: TextView = itemView.findViewById(R.id.text_subtitle)
        val textPath: TextView = itemView.findViewById(R.id.text_path)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val item = items[position]

        holder.textTitle.text = item.title
        holder.textSubtitle.text = item.subtitle
        holder.textPath.text = item.path

        // Set icon based on type
        when (item) {
            is MainActivity.Video -> {
                holder.iconType.setImageResource(R.drawable.ic_file)
                // Later: set video-specific icon
            }
            is MainActivity.Audio -> {
                holder.iconType.setImageResource(R.drawable.ic_file)
                // Later: set music note icon
            }
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size
}