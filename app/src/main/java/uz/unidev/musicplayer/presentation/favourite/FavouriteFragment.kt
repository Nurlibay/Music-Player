package uz.unidev.musicplayer.presentation.favourite

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import uz.unidev.musicplayer.R
import uz.unidev.musicplayer.databinding.FragmentFavouriteBinding

/**
 *  Created by Nurlibay Koshkinbaev on 23/09/2022 00:05
 */

class FavouriteFragment: Fragment(R.layout.fragment_favourite) {

    private val binding: FragmentFavouriteBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}