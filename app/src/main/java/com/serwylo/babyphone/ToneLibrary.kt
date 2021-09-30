package com.serwylo.babyphone

import android.content.Context
import android.media.MediaPlayer

class ToneLibrary {

    companion object {

        private var tones = listOf(
            R.raw.tone_450,
            R.raw.tone_475,
            R.raw.tone_500,
            R.raw.tone_525,
            R.raw.tone_550,
            R.raw.tone_575,
            R.raw.tone_600,
        )

    }

    private lateinit var sounds: List<MediaPlayer>

    suspend fun load(context: Context) {
        sounds = loadSoundResources(context, tones)
    }

    fun getTone450() = sounds[0]
    fun getTone475() = sounds[1]
    fun getTone500() = sounds[2]
    fun getTone525() = sounds[3]
    fun getTone550() = sounds[4]
    fun getTone575() = sounds[5]
    fun getTone600() = sounds[6]

}