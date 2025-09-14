// app/src/main/java/com/harmoni/fragment/AudioFragment.kt
package com.harmoni.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.harmoni.MainActivity
import com.harmoni.MediaAdapter
import com.harmoni.R

class AudioFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

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
        if (audios.isEmpty()) {
            // Optional: show "no music" message
        } else {
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = MediaAdapter(audios) { item ->
                context?.let {
                    android.widget.Toast.makeText(it, "Play audio:\n${item.path}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}