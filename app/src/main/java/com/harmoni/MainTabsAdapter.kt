// app/src/main/java/com/harmoni/MainTabsAdapter.kt
package com.harmoni

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.harmoni.fragment.AudioFragment
import com.harmoni.fragment.VideosFragment

class MainTabsAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> VideosFragment()
            1 -> AudioFragment()
            else -> VideosFragment()
        }
    }

    override fun getItemCount(): Int = 2
}