package com.example.cpsc411_project

import android.content.Context
import android.media.MediaPlayer

object SoundManager {
    private var player: MediaPlayer? = null
    private var volume = 0.8f

    fun startMusic(context: Context) {
        if (player == null) {
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
}