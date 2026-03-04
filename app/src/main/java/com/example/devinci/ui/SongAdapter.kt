package com.example.devinci.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.devinci.R
import com.example.devinci.model.DisplayItem
import com.example.devinci.model.Song
import java.util.concurrent.TimeUnit

class SongAdapter(
    private val onSongClick: (Song) -> Unit,
    private val onOptionsClick: (View, Song) -> Unit
) : ListAdapter<DisplayItem, RecyclerView.ViewHolder>(SongDiffCallback()) {

    private val TYPE_SONG = 0
    private val TYPE_FOOTER = 1

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DisplayItem.SongItem -> TYPE_SONG
            is DisplayItem.FooterItem -> TYPE_FOOTER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SONG) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
            SongViewHolder(view, onSongClick, onOptionsClick)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_footer, parent, false)
            FooterViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (holder is SongViewHolder && item is DisplayItem.SongItem) {
            holder.bind(item.song)
        }
    }

    class SongViewHolder(
        itemView: View,
        val onSongClick: (Song) -> Unit,
        val onOptionsClick: (View, Song) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tvSongTitle)
        private val artistTextView: TextView = itemView.findViewById(R.id.tvSongArtist)
        private val durationTextView: TextView = itemView.findViewById(R.id.tvSongDuration)
        private val albumArtImageView: ImageView = itemView.findViewById(R.id.ivAlbumArt)
        private val optionsButton: ImageButton = itemView.findViewById(R.id.btnOptions)

        fun bind(song: Song) {
            titleTextView.text = song.title
            artistTextView.text = song.artist

            val minutes = TimeUnit.MILLISECONDS.toMinutes(song.duration)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(song.duration) % 60
            durationTextView.text = String.format("%02d:%02d", minutes, seconds)

            Glide.with(itemView.context)
                .load(song.albumArtUri)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(albumArtImageView)

            itemView.setOnClickListener { onSongClick(song) }
            optionsButton.setOnClickListener { onOptionsClick(it, song) }
        }
    }

    class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class SongDiffCallback : DiffUtil.ItemCallback<DisplayItem>() {
        override fun areItemsTheSame(oldItem: DisplayItem, newItem: DisplayItem): Boolean {
            return if (oldItem is DisplayItem.SongItem && newItem is DisplayItem.SongItem) {
                oldItem.song.id == newItem.song.id
            } else {
                oldItem is DisplayItem.FooterItem && newItem is DisplayItem.FooterItem
            }
        }

        override fun areContentsTheSame(oldItem: DisplayItem, newItem: DisplayItem): Boolean {
            return oldItem == newItem
        }
    }
}
