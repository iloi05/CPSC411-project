// This file controls the music of the game
package com.example.cpsc411_project

import android.content.Context
import android.media.MediaPlayer
import androidx.core.content.edit

object SoundManager {
    private var player: MediaPlayer? = null
    private var volume = 0.8f

    // loads default volume, once player adjusts, volume changes are saved and will load at next login
    fun loadVolume(context: Context) {
        val prefs = context.applicationContext
            .getSharedPreferences("settings", Context.MODE_PRIVATE)
        volume = prefs.getFloat("music_volume", 0.5f)
        player?.setVolume(volume, volume)
    }
    // starts music
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
    // pauses volume when app is closed or exited or put in background
    fun pauseMusic() {
        if (player?.isPlaying == true) {
            player?.pause()
        }
    }

    // retrieves saved volume
    fun getVolume(): Float = volume

    // saves player's volume settings
    fun setVolume(context: Context, newVolume: Float) {
        volume = newVolume.coerceIn(0f, 1f)
        player?.setVolume(volume, volume)
        context.applicationContext
            .getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit { putFloat("music_volume", volume) }
    }
}