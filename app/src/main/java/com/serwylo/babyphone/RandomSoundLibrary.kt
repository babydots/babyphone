package com.serwylo.babyphone

import android.content.Context
import android.media.MediaPlayer

class RandomSoundLibrary(private val sounds: List<MediaPlayer>) {

    companion object {

        private var babyTalk = listOf(
            R.raw.babble_1,
            R.raw.babble_2,
            R.raw.babble_3,
            R.raw.babble_baby_1,
            R.raw.babble_baby_2,
            R.raw.babble_misc,
            R.raw.ball,
            R.raw.ball_bee_boo,
            R.raw.bee_boo,
            R.raw.hey_babble,
            R.raw.hey_babble_2,
            R.raw.hey_babble_3,
            R.raw.hey_babble_4,
            R.raw.hey_babble_5,
            R.raw.hey_babble_6,
            R.raw.poo_poo_poo,
            R.raw.poo_poo_sss,
            R.raw.quiet_babble,
            R.raw.squeal,
            R.raw.uh_oh_1,
        )

        suspend fun loadBabySounds(context: Context): RandomSoundLibrary {
            return RandomSoundLibrary(loadSoundResources(context, babyTalk))
        }

    }

    fun getRandomSound(exclude: MediaPlayer? = null): MediaPlayer {
        val toPickFrom = sounds.filter {
            it !== exclude
        }

        if (toPickFrom.isEmpty()) {
            error("Sound library not yet loaded. Must call loadAsync() or loadSync() before getRandomSound().")
        }

        return toPickFrom.random()
    }

}