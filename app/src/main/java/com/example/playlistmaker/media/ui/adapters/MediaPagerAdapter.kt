package com.example.playlistmaker.media.ui.adapters



import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.playlistmaker.media.ui.fragments.FavoriteTracksFragment
import com.example.playlistmaker.media.ui.fragments.PlaylistsFragment

class MediaPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment =
        when (position) {
            0 -> FavoriteTracksFragment()
            1 -> PlaylistsFragment()
            else -> FavoriteTracksFragment()
        }
}