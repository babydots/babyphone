package com.serwylo.babyphone

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

suspend fun loadSoundResources(context: Context, soundResourceIds: List<Int>): List<MediaPlayer> {

    val sounds: List<MediaPlayer>

    withContext(Dispatchers.IO) {
        val time = measureTimeMillis {
            sounds = soundResourceIds
                .map { async { MediaPlayer.create(context, it) } }
                .awaitAll()
        }

        Log.d("SoundLibrary", "Loaded ${sounds.size} asynchronously in ${time}ms.")
    }

    return sounds

}
