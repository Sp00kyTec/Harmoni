// app/src/main/java/com/harmoni/fragment/PlaylistFragment.kt
package com.harmoni.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.harmoni.HarmoniDatabase
import com.harmoni.MainActivity
import com.harmoni.Playlist
import com.harmoni.R
import kotlinx.coroutines.*

class PlaylistFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnCreate: Button
    private lateinit var adapter: PlaylistAdapter
    private val playlists = mutableListOf<Playlist>()
    private val db by lazy { HarmoniDatabase.getDatabase(requireContext()) }
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_playlists, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        btnCreate = view.findViewById(R.id.btn_create_playlist)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PlaylistAdapter(playlists) { playlist ->
            // Click: open playlist tracks (TBD in next step)
            showPlaylistTracks(playlist)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        btnCreate.setOnClickListener {
            showCreatePlaylistDialog()
        }

        observePlaylists()
    }

    private fun observePlaylists() {
        scope.launch {
            db.playlistDao().getAllPlaylists().collect { list ->
                playlists.clear()
                playlists.addAll(list)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun showCreatePlaylistDialog() {
        val input = EditText(requireContext())
        input.hint = "Enter playlist name"

        AlertDialog.Builder(requireContext())
            .setTitle("Create Playlist")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    scope.launch {
                        try {
                            val playlist = Playlist(name = name)
                            db.playlistDao().insertPlaylist(playlist)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPlaylistTracks(playlist: Playlist) {
        // TODO: Show tracks in playlist (next feature)
        // For now, just toast
        Toast.makeText(context, "Playing ${playlist.name}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}