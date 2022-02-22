package com.serwylo.babyphone

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import java.io.File

class ResourceRandomSoundLibrary(context: Context, private val soundResIds: List<Int>) : RandomSoundLibrary<Int>(context, soundResIds) {

    companion object {

        val mumTalk = listOf(
            R.raw.mum_mmm_hmm,
            R.raw.mum_oh_i_see,
            R.raw.mum_really,
            R.raw.mum_sounds_good,
            R.raw.mum_tell_me_more,
            R.raw.mum_uh_huh,
            R.raw.mum_wow,
            R.raw.mum_wow_2,
        )

        val dadTalk = listOf(
            R.raw.dad_mmm,
            R.raw.dad_oh_i_see,
            R.raw.dad_uh_huh,
            R.raw.dad_wow,
            R.raw.dad_yeah,
        )

        val babyTalk = listOf(
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

    }

    override fun createMediaPlayer(context: Context, sound: Int): MediaPlayer = MediaPlayer.create(context, sound)


}

class RecordedRandomSoundLibrary(context: Context, soundFiles: List<File>): RandomSoundLibrary<File>(context, soundFiles) {
    override fun createMediaPlayer(context: Context, sound: File): MediaPlayer = MediaPlayer.create(context, Uri.fromFile(sound))
}

abstract class RandomSoundLibrary<T>(private val context: Context, private val sounds: List<T>) {

    companion object {

        private const val TAG = "RandomSoundLibrary"

    }

    protected abstract fun createMediaPlayer(context: Context, sound: T): MediaPlayer

    private var currentSoundReference: T? = null
    private var currentSound: MediaPlayer? = null

    /**
     * Returns the duration of the sound which was picked.
     */
    fun playRandomSound(): Int {
        val toPickFrom = sounds.filter { it != currentSoundReference }

        currentSoundReference = toPickFrom.random().also { soundRef ->
            currentSound = createMediaPlayer(context, soundRef).also { sound ->
                sound.start()
                sound.setOnCompletionListener {
                    Log.d(TAG, "Sound finished playing, setting to null.")
                    currentSoundReference = null
                    currentSound = null
                }
            }
        }

        return currentSound?.duration ?: 0
    }

    fun isPlaying() = currentSound != null

    fun onPause() {
        currentSound?.let { sound ->
            if (sound.isPlaying) {
                sound.pause()
            } else {
                sound.stop()
                currentSoundReference = null
                currentSound = null
            }
        }
    }

    fun onResume() {
        currentSound?.let { sound ->
            Log.d(TAG, "Resuming existing sound.")
            sound.start()
        }
    }

}