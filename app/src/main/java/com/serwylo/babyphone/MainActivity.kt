package com.serwylo.babyphone

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.serwylo.babyphone.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var timer = 0
    private var nextSoundTime = -1

    private lateinit var sounds: SoundLibrary
    private var currentSound: MediaPlayer? = null

    companion object {

        private const val TAG = "MainActivity"

        private fun queueNextSoundTime() = Random.nextInt(1, 4)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val context = this

        lifecycleScope.launch {
            Log.d(TAG, "Loading sound files")
            sounds = SoundLibrary()
            sounds.load(context)

            Log.d(TAG, "Queuing up the first sound effect.")
            nextSoundTime = timer + queueNextSoundTime()
        }

        lifecycleScope.launchWhenResumed {
            Log.d(TAG, "Starting timer to run in background (but will not start playing sounds until they are loaded).")
            while(true) {
                delay(1000)
                timer++
                updateTimerLabel()

                if (nextSoundTime > 0 && timer >= nextSoundTime) {
                    currentSound = sounds.getRandomSound(currentSound).apply {
                        Log.d(TAG, "Playing a new sound (and queueing up the next sound afterwards).")
                        nextSoundTime = timer + (duration / 1000) + queueNextSoundTime()
                        start()
                        this.setOnCompletionListener {
                            Log.d(TAG, "Sound finished playing, setting to null.")
                            currentSound = null
                        }
                    }
                }
            }
        }

        lifecycleScope.launchWhenResumed {
            currentSound?.let {
                Log.d(TAG, "Resuming app, will resume existing sound.")
                it.start()
            }
        }

        updateTimerLabel()

        supportActionBar?.hide()
    }

    override fun onPause() {
        super.onPause()

        currentSound?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.stop()
                currentSound = null
            }
        }
    }

    override fun onResume() {
        super.onResume()

        currentSound?.let {
            if (it.currentPosition > 0) {
                it.start()
            }
        }
    }

    private fun updateTimerLabel() {
        binding.time.text = "%d:%02d".format(timer / 60, timer % 60)
    }
}