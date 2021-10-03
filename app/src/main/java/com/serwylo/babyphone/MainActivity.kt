package com.serwylo.babyphone

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
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

    private lateinit var sounds: RandomSoundLibrary
    private lateinit var tones: ToneLibrary
    private var currentSound: MediaPlayer? = null

    private var currentTheme: String? = null

    companion object {

        private const val TAG = "MainActivity"

        private fun queueNextSoundTime() = Random.nextInt(1, 4)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentTheme = ThemeManager.applyTheme(this)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val context = this

        lifecycleScope.launch {
            launch {
                Log.d(TAG, "Loading sound files")
                sounds = RandomSoundLibrary.loadBabySounds(context)

                Log.d(TAG, "Queuing up the first sound effect.")
                nextSoundTime = timer + queueNextSoundTime()
            }

            launch {
                tones = ToneLibrary()
                tones.load(context)

                val tonePlayer = { tone: MediaPlayer -> View.OnTouchListener { view, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        tone.seekTo(0)
                        tone.start()
                        view.performClick()
                    }
                    true
                } }

                binding.imgDialpad.setOnTouchListener(tonePlayer(tones.getTone550()))
                binding.imgMic.setOnTouchListener(tonePlayer(tones.getTone550()))
                binding.imgSpeaker.setOnTouchListener(tonePlayer(tones.getTone550()))
                binding.hangUp.setOnTouchListener(tonePlayer(tones.getTone450()))
            }
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
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> startActivity(Intent(this, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
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

        // Upon returning from settings, the user may hit the "Up" button in the toolbar (in which
        // case the main splash screen will be recreated from scratch and themed appropriately in
        // the onCreate() method), or via the "back" button, in which case we will get here.
        //
        // If the user pressed back, then we will check if they changed the theme from what we set
        // it to originally during onCreate(), and if so, force the activity to be recreated (and
        // hence rethemed).
        if (!ThemeManager.getCurrentTheme().equals(currentTheme)) {
            ThemeManager.forceRestartActivityToRetheme(this)
        }

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