package com.serwylo.babyphone

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.serwylo.babyphone.db.entities.Recording
import java.io.File

private const val LOG_TAG = "RandomSoundLibrary"

class RandomSoundLibrary(private val context: Context, private val sounds: List<Uri>) {

    private fun createMediaPlayer(sound: Uri): MediaPlayer {
        val path = sound.path ?: ""
        return if (path.startsWith("/android_asset/")) {

            // https://stackoverflow.com/questions/3289038/play-audio-file-from-the-assets-directory
            context.assets.openFd(path.substring("/android_asset/".length)).use { assetDescriptor ->
                MediaPlayer().apply {
                    setDataSource(
                        assetDescriptor.fileDescriptor,
                        assetDescriptor.startOffset,
                        assetDescriptor.length
                    )
                    prepare()
                }
            }
        } else {
            MediaPlayer.create(context, sound)
        }
    }

    private var currentSoundReference: Uri? = null
    private var currentSound: MediaPlayer? = null

    /**
     * Returns the duration of the sound which was picked.
     */
    fun playRandomSound(): Int {
        val toPickFrom = sounds.filter { it != currentSoundReference }

        if (toPickFrom.isEmpty()) {
            return 0
        }

        currentSoundReference = toPickFrom.random().also { soundRef ->
            currentSound = createMediaPlayer(soundRef).also { sound ->
                sound.start()
                sound.setOnCompletionListener {
                    Log.d(LOG_TAG, "Sound finished playing, setting to null.")
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
            Log.d(LOG_TAG, "Resuming existing sound.")
            sound.start()
        }
    }

}