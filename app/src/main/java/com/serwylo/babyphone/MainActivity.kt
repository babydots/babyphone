package com.serwylo.babyphone

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.serwylo.babyphone.databinding.ActivityMainBinding
import kotlinx.coroutines.delay

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var timer = 0

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        mediaPlayer = MediaPlayer.create(this, R.raw.sound_file_1)

        lifecycleScope.launchWhenResumed {
            mediaPlayer.start() // no need to call prepare(); create() does that for you
        }

        lifecycleScope.launchWhenResumed {
            while(true) {
                timer++
                binding.time.text = "%d:%02d".format(timer / 60, timer % 60)
                delay(1000)
            }
        }
    }
}