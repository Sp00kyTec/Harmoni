// app/src/main/java/com/harmoni/MainTabsAdapter.kt
package com.harmoni

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.harmoni.fragment.AudioFragment
import com.harmoni.fragment.VideosFragment
import com.harmoni.fragment.PlaylistFragment

class MainTabsAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> VideosFragment()
            1 -> AudioFragment()
            2 -> PlaylistFragment()
            else -> VideosFragment()
        }
    }

    override fun getItemCount(): Int = 3
}