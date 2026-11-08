package com.example.cpsc411_project

import android.content.Context
import android.media.MediaPlayer

object SoundManager {
    private var player: MediaPlayer? = null
    private var volume = 0.8f

    fun loadVolume(context: Context) {
        val prefs = context.applicationContext
            .getSharedPreferences("settings", Context.MODE_PRIVATE)
        volume = prefs.getFloat("music_volume", 0.5f)
        player?.setVolume(volume, volume)
    }

    fun startMusic(context: Context) {
        if (player == null) {
            loadVolume(context)
            player = MediaPlayer.create(context.applicationContext, R.raw.beach)?.apply {
                isLooping = true
                setVolume(volume, volume)
            }
        }
        if (player?.isPlaying == false) {
            player?.start()
        }
    }
    fun pauseMusic() {
        if (player?.isPlaying == true) {
            player?.pause()
        }
    }

    fun getVolume(): Float = volume

    fun setVolume(context: Context, newVolume: Float) {
        volume = newVolume.coerceIn(0f, 1f)
        player?.setVolume(volume, volume)
        context.applicationContext
            .getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit()
            .putFloat("music_volume", volume)
            .apply()
    }
}