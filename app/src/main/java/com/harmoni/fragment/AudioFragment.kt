// app/src/main/java/com/harmoni/fragment/AudioFragment.kt
package com.harmoni.fragment

import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.harmoni.HarmoniDatabase
import com.harmoni.MainActivity
import com.harmoni.MediaAdapter
import com.harmoni.Playlist
import com.harmoni.PlaylistEntry
import com.harmoni.R
import kotlinx.coroutines.*

class AudioFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val db by lazy { HarmoniDatabase.getDatabase(requireContext()) }
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_audio, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        return view
    }

    override fun onResume() {
        super.onResume()
        setupList()
    }

    private fun setupList() {
        val audios = MainActivity.audioList
        if (audios.isEmpty()) return

        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = MediaAdapter(audios) { item ->
            val intent = Intent(context, com.harmoni.AudioPlayerActivity::class.java)
            intent.putExtra(com.harmoni.AudioPlayerActivity.EXTRA_AUDIO_PATH, item.path)
            intent.putExtra(com.harmoni.AudioPlayerActivity.EXTRA_TITLE, item.title)
            intent.putExtra(com.harmoni.AudioPlayerActivity.EXTRA_ARTIST, (item as MainActivity.Audio).artist)
            context?.startActivity(intent)
        }

        // Set adapter
        recyclerView.adapter = adapter

        // Long press: show "Add to Playlist"
        recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                val child = rv.childViewUnder(e.x, e.y)
                if (child != null && e.action == MotionEvent.ACTION_DOWN) {
                    val position = rv.getChildAdapterPosition(child)
                    val audio = audios[position]

                    if (e.action == MotionEvent.ACTION_DOWN) {
                        child.setOnLongClickListener {
                            showAddToPlaylistDialog(audio)
                            true
                        }
                    }
                }
                return false
            }
        })
    }

    private fun showAddToPlaylistDialog(audio: MainActivity.Audio) {
        val popup = PopupMenu(requireContext(), it)
        popup.menu.add("Loading...")

        popup.setOnMenuItemClickListener { item ->
            val playlistId = item.itemId.toLong()
            scope.launch {
                try {
                    val entry = PlaylistEntry(playlistId = playlistId, audioPath = audio.path)
                    val result = db.playlistDao().addTrackToPlaylist(entry)
                    if (result != -1L) {
                        Toast.makeText(context, "Added to playlist", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Already in playlist", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to add", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }

        scope.launch {
            val playlists = withContext(Dispatchers.IO) {
                db.playlistDao().getAllPlaylists().firstOrNull() ?: emptyList()
            }

            popup.menu.clear()
            if (playlists.isEmpty()) {
                popup.menu.add(Menu.NONE, 0, 0, "No playlists found")
            } else {
                playlists.forEach { playlist ->
                    popup.menu.add(Menu.NONE, playlist.id.toInt(), Menu.NONE, playlist.name)
                }
            }

            popup.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}