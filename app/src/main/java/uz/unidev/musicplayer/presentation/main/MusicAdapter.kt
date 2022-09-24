package uz.unidev.musicplayer.presentation.main

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import uz.unidev.musicplayer.R
import uz.unidev.musicplayer.data.models.Music
import uz.unidev.musicplayer.databinding.ItemMusicBinding
import uz.unidev.musicplayer.utils.extensions.coloredString
import uz.unidev.musicplayer.utils.extensions.formatDuration

/**
 *  Created by Nurlibay Koshkinbaev on 23/09/2022 12:14
 */

class MusicAdapter : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    var musicList = mutableListOf<Music>()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var query: String? = null

    inner class MusicViewHolder(private val binding: ItemMusicBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(music: Music) {
            binding.tvSongName.text = musicList[absoluteAdapterPosition].title.coloredString(query)
            binding.tvSongAlbum.text = musicList[absoluteAdapterPosition].album
            binding.tvSongDuration.text = formatDuration(musicList[absoluteAdapterPosition].duration)
            Glide.with(binding.root.context)
                .load(musicList[absoluteAdapterPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.music_player_icon).centerCrop())
                .into(binding.ivMusicImage)

            binding.root.setOnClickListener {
                itemClick.invoke(absoluteAdapterPosition, ArrayList(musicList))
            }
        }
    }

    private var itemClick: (Int, ArrayList<Music>) -> Unit = { _, _ ->}
    fun onItemClickListener(block: (Int, ArrayList<Music>) -> Unit) {
        this.itemClick = block
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        return MusicViewHolder(ItemMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        return holder.bind(musicList[position])
    }

    override fun getItemCount() = musicList.size
}