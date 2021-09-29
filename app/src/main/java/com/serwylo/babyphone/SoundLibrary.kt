package com.serwylo.babyphone

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

class SoundLibrary {

    companion object {
        private const val TAG = "SoundLibrary"
    }

    private var soundResources = listOf(
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

    private var sounds: List<MediaPlayer> = emptyList()

    suspend fun load(context: Context) {
        withContext(Dispatchers.IO) {
            val time = measureTimeMillis {
                sounds = soundResources
                    .map { async { MediaPlayer.create(context, it) } }
                    .awaitAll()
            }

            Log.d(TAG, "Loaded ${sounds.size} asynchronously in ${time}ms.")
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