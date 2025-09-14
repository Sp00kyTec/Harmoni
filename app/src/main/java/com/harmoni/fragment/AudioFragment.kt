// app/src/main/java/com/harmoni/fragment/AudioFragment.kt
package com.harmoni.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.harmoni.R

class AudioFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_audio, container, false)
        val textView = view.findViewById<TextView>(R.id.text_message)
        textView.text = "🎵 Your music library"
        return view
    }
}