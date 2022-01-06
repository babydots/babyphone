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
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import com.serwylo.babyphone.databinding.ActivityMainBinding
import com.serwylo.immersivelock.ImmersiveLock
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var timer = 0
    private var nextSoundTime = -1

    private var contact: Contact? = null
    private var tone1: MediaPlayer? = null
    private var tone2: MediaPlayer? = null

    private var currentTheme: String? = null

    private lateinit var immersiveLock: ImmersiveLock

    companion object {

        private const val TAG = "MainActivity"

        private fun queueNextSoundTime() = Random.nextInt(1, 4)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentTheme = ThemeManager.applyTheme(this)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        binding = ActivityMainBinding.inflate(layoutInflater)

        immersiveLock = ImmersiveLock.Builder(binding.unlockWrapper)
            .onStopImmersiveMode { binding.toolbar.visibility = View.VISIBLE }
            .build()

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val context = this

        lifecycleScope.launch {

            listOf(
                launch { tone1 = MediaPlayer.create(context, R.raw.tone_550) },
                launch { tone2 = MediaPlayer.create(context, R.raw.tone_450) }
            ).joinAll()

            val tonePlayer = { tone: MediaPlayer? -> View.OnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    if (tone == null) {
                        Log.e(TAG, "Tone should not be null. Pressed ${context.resources.getResourceName(view.id)}")
                    } else {
                        tone.seekTo(0)
                        tone.start()
                    }
                    view.performClick()
                }
                true
            } }

            binding.imgDialpad.setOnTouchListener(tonePlayer(tone1))
            binding.imgMic.setOnTouchListener(tonePlayer(tone1))
            binding.imgSpeaker.setOnTouchListener(tonePlayer(tone1))
            binding.hangUp.setOnTouchListener(tonePlayer(tone2))

            binding.imgContacts.setOnTouchListener { view, event ->
                tonePlayer(tone1).onTouch(view, event)
                true
            }
        }

        lifecycleScope.launchWhenResumed {
            Log.d(TAG, "Starting timer to run in background (but will not start playing sounds until they are loaded).")
            while(true) {
                delay(1000)
                tick()
            }
        }

        updateTimerLabel()

        Log.d(TAG, "Queuing up the first sound effect.")
        nextSoundTime = timer + queueNextSoundTime()
    }

    /**
     * If the currently loaded contact is different than what the preferences dictate, ensure
     * we update the [contact].
      */
    private fun reloadContact(): Boolean {
        val contactNameFromPrefs = ContactManager.getSelectedContactName(this)
        if (contact?.name == contactNameFromPrefs) {
            return false
        }

        contact = ContactManager.getContact(this, contactNameFromPrefs)
        return true
    }

    private fun tick() {
        timer++
        updateTimerLabel()

        contact?.let { contact ->
            if (timer >= nextSoundTime && !contact.soundLibrary.isPlaying()) {
                Log.d(TAG, "Playing a new sound (and queuing up the next sound afterward).")
                val duration = contact.soundLibrary.playRandomSound()
                nextSoundTime = timer + (duration / 1000) + queueNextSoundTime()
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_lock -> {
                immersiveLock.startImmersiveMode(this)
                binding.toolbar.visibility = View.GONE
            }
            R.id.menu_settings -> startActivity(Intent(this, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        contact?.soundLibrary?.onPause()
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

        // If the contact was changed from the settings menu, then we will need to reload the
        // contact and all of its associated data.
        if (reloadContact()) {
            contact?.let { contact ->
                binding.avatar.setImageDrawable(AppCompatResources.getDrawable(this, contact.avatarDrawableId))
                binding.name.text = contact.label
            }
        }

        contact?.soundLibrary?.onResume()
    }

    private fun updateTimerLabel() {
        binding.time.text = "%d:%02d".format(timer / 60, timer % 60)
    }

}