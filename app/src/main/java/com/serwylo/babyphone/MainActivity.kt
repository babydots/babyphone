package com.serwylo.babyphone

import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.serwylo.babyphone.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var timer = 0
    private var nextSoundTime = timer + queueNextSoundTime()

    private lateinit var sounds: List<MediaPlayer>
    private var currentSound: MediaPlayer? = null

    private var isMuted = false

    companion object {
        private fun queueNextSoundTime() = Random.nextInt(1, 4)

        private fun pickNextSound(sounds: List<MediaPlayer>, current: MediaPlayer?) = sounds.filter { it !== current }.random()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        sounds = listOf(
            MediaPlayer.create(this, R.raw.babble_1),
            MediaPlayer.create(this, R.raw.babble_2),
            MediaPlayer.create(this, R.raw.babble_3),
            MediaPlayer.create(this, R.raw.babble_4),
            MediaPlayer.create(this, R.raw.babble_5),
            MediaPlayer.create(this, R.raw.ball_1),
            MediaPlayer.create(this, R.raw.bee_boo_1),
            MediaPlayer.create(this, R.raw.longer_babble_1),
            MediaPlayer.create(this, R.raw.longer_babble_2),
        )

        lifecycleScope.launchWhenResumed {
            currentSound?.start()
        }

        lifecycleScope.launchWhenResumed {
            while(true) {
                delay(1000)
                timer++
                updateTimerLabel()

                if (timer >= nextSoundTime) {
                    currentSound = pickNextSound(sounds, currentSound).apply {
                        start()
                        nextSoundTime = timer + (duration / 1000) + queueNextSoundTime()
                    }
                }
            }
        }

        updateTimerLabel()

        supportActionBar?.hide()
    }

    override fun onPause() {
        super.onPause()

        currentSound?.apply {
            if (currentPosition > 0) {
                pause()
            } else {
                stop()
                currentSound = null
            }
        }
    }

    override fun onResume() {
        super.onResume()

        currentSound?.apply {
            if (currentPosition > 0) {
                start()
            }
        }
    }

    private fun updateTimerLabel() {
        binding.time.text = "%d:%02d".format(timer / 60, timer % 60)
    }
}