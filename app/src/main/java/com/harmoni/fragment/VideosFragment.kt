// app/src/main/java/com/harmoni/fragment/VideosFragment.kt
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

class VideosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_videos, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        return view
    }

    override fun onResume() {
        super.onResume()
        setupList()
    }

    private fun setupList() {
        val videos = MainActivity.videoList
        if (videos.isEmpty()) {
            // Optional: show placeholder
        } else {
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = MediaAdapter(videos) { item ->
    val intent = Intent(context, VideoPlayerActivity::class.java)
    intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_PATH, item.path)
    context?.startActivity(intent)
}
                }
            }
        }
    }
}