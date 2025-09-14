package com.harmoni

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.harmoni.fragment.*

class MainTabsAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> VideosFragment()
            1 -> AudioFragment()
            2 -> PlaylistFragment()
            3 -> EqualizerFragment()
            4 -> FileBrowserFragment()
            5 -> RecentlyPlayedFragment()
            6 -> SettingsFragment()
            else -> VideosFragment()
        }
    }

    override fun getItemCount(): Int = 7
}