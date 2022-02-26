package com.serwylo.babyphone.dialer

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.serwylo.babyphone.*
import com.serwylo.babyphone.contactlist.ContactListFragment
import com.serwylo.babyphone.databinding.ActivityMainBinding
import com.serwylo.babyphone.db.AppDatabase
import com.serwylo.babyphone.db.ContactRepository
import com.serwylo.babyphone.db.entities.Contact
import com.serwylo.immersivelock.ImmersiveLock
import com.squareup.picasso.Picasso
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: DialerViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: ContactRepository
    private var soundLibrary: RandomSoundLibrary? = null

    private var timer = 0
    private var nextSoundTime = -1

    private var tone1: MediaPlayer? = null
    private var tone2: MediaPlayer? = null

    private var currentTheme: String? = null

    private lateinit var immersiveLock: ImmersiveLock

    companion object {

        private const val TAG = "MainActivity"

        private fun queueNextSoundTime() = Random.nextInt(1, 4)

    }

    private fun playTone(tone: MediaPlayer?) {
        if (tone == null) {
            Log.w(TAG, "Tried to play tone, but it was unexpectedly null. Perhaps we touched a button before the tone was loaded.")
        } else {
            tone.seekTo(0)
            tone.start()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentTheme = ThemeManager.applyTheme(this)

        repository = ContactRepository(this, AppDatabase.getInstance(this).contactDao())

        viewModel = ViewModelProvider(
            this,
            DialerViewModelFactory(AppDatabase.getInstance(this).contactDao())
        ).get(DialerViewModel::class.java)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        binding = ActivityMainBinding.inflate(layoutInflater)

        immersiveLock = ImmersiveLock.Builder(binding.unlockWrapper)
            .onStopImmersiveMode { binding.toolbar.visibility = View.VISIBLE }
            .build()

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        viewModel.contact.observe(this) { contact ->
            if (contact != null) {
                bindContact(contact.contact)
                soundLibrary?.onPause()
                soundLibrary = RandomSoundLibrary(this, contact.sounds.map { Uri.parse(it.soundFilePath) })
            }
        }

        val context = this

        lifecycleScope.launch {

            listOf(
                launch { tone1 = MediaPlayer.create(context, R.raw.tone_550) },
                launch { tone2 = MediaPlayer.create(context, R.raw.tone_450) }
            ).joinAll()

            val tonePlayer = { tone: MediaPlayer? -> View.OnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    playTone(tone)
                    view.performClick()
                }
                true
            } }

            binding.imgDialpad.setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    playTone(tone1)
                    showDialPad()
                    view.performClick()
                }
                true
            }

            binding.imgMic.setOnTouchListener(tonePlayer(tone1))
            binding.imgSpeaker.setOnTouchListener(tonePlayer(tone1))
            binding.hangUp.setOnTouchListener(tonePlayer(tone2))

            binding.imgContacts.setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    tonePlayer(tone1).onTouch(view, event)
                    showContactList()
                    view.performClick()
                }
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

        Changelog.show(this)
    }

    private fun showDialPad() {
        val dialPad = DialPadFragment()
        dialPad.show(supportFragmentManager, "contact-list")
        dialPad.onButtonPressed { playTone(tone2) }
    }

    private fun showContactList() {
        val list = ContactListFragment()
        list.show(supportFragmentManager, "contact-list")
        list.onContactSelected { contact ->

            lifecycleScope.launch { repository.selectContact(contact) }

            // No need to update the view, because the LiveData<Contact> will do this in response
            // to changing the selected contact.

            list.dismiss()

        }
    }

    private fun bindContact(contact: Contact) {
        binding.name.text = contact.name
        if (contact.avatarPath.isNotEmpty()) {
            Picasso.get().load(contact.avatarPath).fit().centerCrop().into(binding.avatar)
        }
    }

    private fun tick() {
        timer++
        updateTimerLabel()

        soundLibrary?.let { library ->
            if (timer >= nextSoundTime && !library.isPlaying()) {
                Log.d(TAG, "Playing a new sound (and queuing up the next sound afterward).")
                val duration = library.playRandomSound()
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
        soundLibrary?.onPause()
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

        soundLibrary?.onResume()
    }

    private fun updateTimerLabel() {
        binding.time.text = "%d:%02d".format(timer / 60, timer % 60)
    }

}
