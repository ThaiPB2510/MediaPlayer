package com.example.mediaplayer

import com.example.mediaplayer.MusicList
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaplayer.MusicAdapter.MyViewHolder
import com.example.mediaplayer.SongChangeListener
import android.view.ViewGroup
import android.view.LayoutInflater
import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.mediaplayer.R
import java.util.*
import java.util.concurrent.TimeUnit

class MusicAdapter(private var list: List<MusicList>, private val context: Context) :
    RecyclerView.Adapter<MusicAdapter.MyViewHolder>() {
    private var playingPosition = 0
    private val songChangeListener: SongChangeListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.music_adapter_layout, null)
        )
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val list1 = list[position]
        if (list1.isPlaying) {
            playingPosition = position
            holder.rootLayout.setBackgroundResource(R.drawable.round_back_blue_10)
        } else {
            holder.rootLayout.setBackgroundResource(R.drawable.round_back_10)
        }
        val generateDurations = String.format(
            Locale.getDefault(), "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(list1.duration.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(list1.duration.toLong()) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(list1.duration.toLong()))
        )
        holder.title.text = list1.title
        holder.artist.text = list1.artist
        holder.musicDuration.text = generateDurations
        holder.rootLayout.setOnClickListener {
            list[playingPosition].isPlaying = false
            list1.isPlaying = true
            songChangeListener.onChanged(position)
            notifyDataSetChanged()
        }
    }

    fun updateList(list: List<MusicList>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rootLayout: RelativeLayout
        val title: TextView
        val artist: TextView
        val musicDuration: TextView

        init {
            rootLayout = itemView.findViewById(R.id.rootLayout)
            title = itemView.findViewById(R.id.musicTitle)
            artist = itemView.findViewById(R.id.musicArtist)
            musicDuration = itemView.findViewById(R.id.musicDuration)
        }
    }

    init {
        songChangeListener = context as SongChangeListener
    }
}