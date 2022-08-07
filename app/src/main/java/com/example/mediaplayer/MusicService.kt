package com.example.mediaplayer

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import java.util.*

class MusicService : Service() {

    private val timer = Timer()

    override fun onCreate() {
        super.onCreate()
        player = MediaPlayer.create(this, songToPlay)
        player.setLooping(true)
        sendMusicDuration()
    }

    override fun onStartCommand(intent: Intent, flag: Int, startId: Int): Int{

        timer.scheduleAtFixedRate(SendCurrentPosition(),0,1000)

        if (!player.isPlaying){
            player.start()
        }

        return START_STICKY
    }

    private fun sendMusicDuration() {
        val intent = Intent("sendMusicDuration")
        intent.putExtra("musicDuration", player.duration)
        sendBroadcast(intent)
    }

    private inner class SendCurrentPosition(): TimerTask(){
        override fun run() {
            val intent = Intent("sendCurrentPosition")
            intent.putExtra("currentPosition", player.currentPosition)
            sendBroadcast(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.stop()
        player.reset()
        player.release()
        timer.cancel()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    companion object{
        var songToPlay = 0
        lateinit var player: MediaPlayer
    }
}