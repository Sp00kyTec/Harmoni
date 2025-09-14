// app/src/main/java/com/harmoni/fragment/PlaylistDetailFragment.kt
package com.harmoni.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.harmoni.HarmoniDatabase
import com.harmoni.MainActivity
import com.harmoni.MediaAdapter
import com.harmoni.PlaylistEntry
import com.harmoni.R
import kotlinx.coroutines.*

class PlaylistDetailFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val db by lazy { HarmoniDatabase.getDatabase(requireContext()) }
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var playlistId: Long = -1
    private val entries = mutableListOf<MainActivity.Audio>()

    companion object {
        fun newInstance(playlistId: Long): PlaylistDetailFragment {
            val args = Bundle()
            args.putLong("playlistId", playlistId)
            val fragment = PlaylistDetailFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playlistId = arguments?.getLong("playlistId") ?: -1
    }

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
        loadTracks()
    }

    private fun loadTracks() {
        if (playlistId == -1) return

        scope.launch {
            val entryList = withContext(Dispatchers.IO) {
                db.playlistDao().getTracksInPlaylist(playlistId)
            }

            entries.clear()
            for (entry in entryList) {
                val audio = MainActivity.audioList.find { it.path == entry.audioPath }
                if (audio != null) entries.add(audio)
            }

            if (entries.isEmpty()) {
                Toast.makeText(context, "No tracks in this playlist", Toast.LENGTH_SHORT).show()
            }

            setupList()
        }
    }

    private fun setupList() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = MediaAdapter(entries) { item ->
            val intent = Intent(context, com.harmoni.AudioPlayerActivity::class.java).apply {
                putExtra(com.harmoni.AudioPlayerActivity.EXTRA_AUDIO_PATH, item.path)
                putExtra(com.harmoni.AudioPlayerActivity.EXTRA_TITLE, item.title)
                putExtra(com.harmoni.AudioPlayerActivity.EXTRA_ARTIST, (item as MainActivity.Audio).artist)
            }
            context?.startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}