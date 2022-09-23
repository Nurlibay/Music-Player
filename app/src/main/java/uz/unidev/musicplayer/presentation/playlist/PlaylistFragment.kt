package uz.unidev.musicplayer.presentation.playlist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import uz.unidev.musicplayer.R
import uz.unidev.musicplayer.databinding.FragmentPlaylistBinding

/**
 *  Created by Nurlibay Koshkinbaev on 23/09/2022 00:06
 */

class PlaylistFragment: Fragment(R.layout.fragment_playlist) {

    private val binding: FragmentPlaylistBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            ivBack.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

}