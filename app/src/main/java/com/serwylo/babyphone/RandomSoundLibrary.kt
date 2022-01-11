package com.serwylo.babyphone

import android.content.Context
import android.media.MediaPlayer
import android.util.Log

class RandomSoundLibrary(private val context: Context, private val soundResIds: List<Int>) {

    companion object {

        private const val TAG = "RandomSoundLibrary"

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

    private var currentSoundResId: Int? = null
    private var currentSound: MediaPlayer? = null

    /**
     * Returns the duration of the sound which was picked.
     */
    fun playRandomSound(): Int {
        val toPickFrom = soundResIds.filter { it != currentSoundResId }

        currentSoundResId = toPickFrom.random().also { soundId ->
            currentSound = MediaPlayer.create(context, soundId).also { sound ->
                sound.start()
                sound.setOnCompletionListener {
                    Log.d(TAG, "Sound finished playing, setting to null.")
                    currentSoundResId = null
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
                currentSoundResId = null
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