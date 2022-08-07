package com.example.mediaplayer
import android.net.Uri

class MusicList(
    val title: String,
    val artist: String,
    val duration: String,
    var isPlaying: Boolean,
    val musicFile: Uri
)