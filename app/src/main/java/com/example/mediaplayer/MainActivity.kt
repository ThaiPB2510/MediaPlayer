package com.example.mediaplayer

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import com.example.mediaplayer.SongChangeListener
import com.example.mediaplayer.MusicList
import androidx.recyclerview.widget.RecyclerView
import android.media.MediaPlayer
import android.widget.TextView
import android.widget.SeekBar
import com.example.mediaplayer.MusicAdapter
import android.os.Bundle
import com.example.mediaplayer.R
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.os.Build
import android.widget.SeekBar.OnSeekBarChangeListener
import android.content.ContentResolver
import android.provider.MediaStore
import android.widget.Toast
import android.content.ContentUris
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer.OnPreparedListener
import android.media.MediaPlayer.OnCompletionListener
import android.widget.ImageView
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), SongChangeListener {
    private val musicLists: MutableList<MusicList> = ArrayList()
    private lateinit var musicList: RecyclerView
    private var mediaPlayer: MediaPlayer? = null
    private var startTime: TextView? = null
    private var endTime: TextView? = null
    private var isPlaying = false
    private lateinit var playerSeekBar: SeekBar
    private lateinit var playPauseImg: ImageView
    private var timer: Timer? = null
    private var currentSongPosition = 0
    private var musicAdapter: MusicAdapter? = null
    var serviceStarted = false
    lateinit var serviceIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        musicList = findViewById(R.id.musicList)
        val playPauseCard = findViewById<CardView>(R.id.playPauseCard)
        playPauseImg = findViewById(R.id.playPauseImg)
        val nextBtn = findViewById<ImageView>(R.id.nextBtn)
        val prevBtn = findViewById<ImageView>(R.id.prevBtn)
        playerSeekBar = findViewById(R.id.seekBar)
        startTime = findViewById(R.id.startTime)
        endTime = findViewById(R.id.endTime)
        musicList.setHasFixedSize(true)
        musicList.setLayoutManager(LinearLayoutManager(this))
        mediaPlayer = MediaPlayer()
        serviceIntent = Intent(applicationContext, MusicService::class.java)
        serviceIntent.setAction("NoAction")


        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            musicFiles
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 11)
            } else {
                musicFiles
            }
        }
        nextBtn.setOnClickListener {
            var nextSongListPosition = currentSongPosition + 1
            if (nextSongListPosition >= musicLists.size) {
                nextSongListPosition = 0
            }
            musicLists[currentSongPosition].isPlaying = false
            musicLists[nextSongListPosition].isPlaying = true
            musicAdapter!!.updateList(musicLists)
            musicList.scrollToPosition(nextSongListPosition)
            onChanged(nextSongListPosition)
        }
        prevBtn.setOnClickListener {
            var prevSongListPosition = currentSongPosition - 1
            if (prevSongListPosition < 0) {
                prevSongListPosition = musicLists.size - 1
            }
            musicLists[currentSongPosition].isPlaying = false
            musicLists[prevSongListPosition].isPlaying = true
            musicAdapter!!.updateList(musicLists)
            musicList.scrollToPosition(prevSongListPosition)
            onChanged(prevSongListPosition)
        }
        playPauseCard.setOnClickListener {
            if (isPlaying) {
                serviceStarted = false
                isPlaying = false
                mediaPlayer!!.pause()
                playPauseImg.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            } else {
                isPlaying = true
                serviceStarted = true
                startService(serviceIntent)
                mediaPlayer!!.start()
                playPauseImg.setImageResource(R.drawable.ic_baseline_pause_24)
            }
        }
        playerSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    if (isPlaying) {
                        mediaPlayer!!.seekTo(progress)
                    } else {
                        mediaPlayer!!.seekTo(0)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private val musicFiles: Unit
        private get() {
            val contentResolver = contentResolver
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val cursor = contentResolver.query(
                uri,
                null,
                MediaStore.Audio.Media.DATA + " LIKE?",
                arrayOf("%.mp3%"),
                null
            )
            if (cursor == null) {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            } else if (!cursor.moveToNext()) {
                Toast.makeText(this, "No Music Found", Toast.LENGTH_SHORT).show()
            } else {
                while (cursor.moveToNext()) {
                    val getMusicFileName =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                    val getArtistName =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                    val cursorId =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    val musicFileUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        cursorId
                    )
                    var getDuration: String = "00:00"
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        getDuration =
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION))
                    }
                    val musicList =
                        MusicList(getMusicFileName, getArtistName, getDuration, false, musicFileUri)
                    musicLists.add(musicList)
                }
                musicAdapter = MusicAdapter(musicLists, this@MainActivity)
                musicList!!.adapter = musicAdapter
            }
            cursor!!.close()
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            musicFiles
        } else {
            Toast.makeText(this, "Permission Declined by User", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onChanged(postion: Int) {
        currentSongPosition = postion
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
            mediaPlayer!!.reset()
        }
        mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        Thread {
            try {
                mediaPlayer!!.setDataSource(this@MainActivity, musicLists[postion].musicFile)
                mediaPlayer!!.prepare()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "Unable to Play song", Toast.LENGTH_SHORT).show()
            }
        }.start()
        mediaPlayer!!.setOnPreparedListener { mP ->
            val getTotalDuaration = mP.duration
            val generateDurations = String.format(
                Locale.getDefault(), "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(getTotalDuaration.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(getTotalDuaration.toLong()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getTotalDuaration.toLong()))
            )
            endTime!!.text = generateDurations
            isPlaying = true
            mP.start()
            playerSeekBar!!.max = getTotalDuaration
            playPauseImg!!.setImageResource(R.drawable.ic_baseline_pause_24)
        }
        timer = Timer()
        timer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    val getCurrentDuration = mediaPlayer!!.currentPosition
                    val generateDurations = String.format(
                        Locale.getDefault(), "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(getCurrentDuration.toLong()),
                        TimeUnit.MILLISECONDS.toSeconds(getCurrentDuration.toLong()) -
                                TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(
                                        getCurrentDuration.toLong()
                                    )
                                )
                    )
                    playerSeekBar!!.progress = getCurrentDuration
                    startTime!!.text = generateDurations
                }
            }
        }, 1000, 1000)
        mediaPlayer!!.setOnCompletionListener {
            mediaPlayer!!.reset()
            timer!!.purge()
            timer!!.cancel()
            isPlaying = false
            playPauseImg!!.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            playerSeekBar!!.progress = 0
        }
    }
}