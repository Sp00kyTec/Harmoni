// app/src/main/java/com/harmoni/fragment/VideosFragment.kt
package com.harmoni.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.harmoni.R

class VideosFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_videos, container, false)
        val textView = view.findViewById<TextView>(R.id.text_message)
        textView.text = "🎥 Videos will appear here"
        return view
    }
}