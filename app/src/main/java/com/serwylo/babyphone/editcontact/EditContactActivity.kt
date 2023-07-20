package com.serwylo.babyphone.editcontact

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.app.NavUtils
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.serwylo.babyphone.R
import com.serwylo.babyphone.ThemeManager
import com.serwylo.babyphone.databinding.ActivityEditContactBinding
import com.serwylo.babyphone.databinding.EditContactSoundItemBinding
import com.serwylo.babyphone.db.AppDatabase
import com.serwylo.babyphone.db.entities.Recording
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.util.*

private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
private const val REQUEST_IMAGE_CAPTURE = 1

@Suppress("unused")
private const val LOG_TAG = "EditContactActivity"

/**
 * Much of the audio recording code is adapted from https://developer.android.com/guide/topics/media/mediarecorder.
 */
class EditContactActivity : AppCompatActivity() {

    private lateinit var viewModel: EditContactViewModel
    private lateinit var binding: ActivityEditContactBinding

    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    companion object {
        const val CONTACT_ID = "contactId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        // Not sure why, but this has to come before super.onCreate(), or else the light theme
        // will have a dark background, making the text very hard to read. This seems to fix it,
        // though I have not investigated why. Source: https://stackoverflow.com/a/15657428.
        ThemeManager.applyTheme(this)

        super.onCreate(savedInstanceState)

        binding = ActivityEditContactBinding.inflate(layoutInflater)

        val contactId = intent.extras?.getLong(CONTACT_ID) ?: 0L
        viewModel = ViewModelProvider(
            this,
            EditContactViewModelFactory(this, AppDatabase.getInstance(this).contactDao(), contactId)
        ).get(EditContactViewModel::class.java)

        viewModel.isLoading.observe(this) { isLoading ->
            if (!isLoading) {
                setup()
            }
        }

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { NavUtils.navigateUpFromSameTask(this) }
    }

    private fun setup() {
        // Don't observe the name from the viewModel. We are the only ones who change it, and it can
        // cause loops with our text changed listener.
        binding.nameInput.setText(viewModel.name.value ?: "")
        binding.nameInput.addTextChangedListener { viewModel.updateName(it.toString()) }

        binding.sounds.layoutManager = LinearLayoutManager(this)
        binding.sounds.adapter = SoundAdapter(
            this,
            onDelete = { sound -> viewModel.deleteSound(sound) }
        ).also { adapter ->
            viewModel.sounds.observe(this) { adapter.setSounds(it) }
        }

        viewModel.avatarPath.observe(this) { maybeShowPhoto(it) }

        binding.startRecording.setOnClickListener {
            lifecycleScope.launch { viewModel.startRecording() }
        }

        binding.stopRecording.setOnClickListener {
            lifecycleScope.launch { viewModel.stopRecording() }
        }

        viewModel.isRecording.observe(this) { isRecording ->
            if (isRecording) {
                binding.startRecording.visibility = View.GONE
                binding.stopRecording.visibility = View.VISIBLE
            } else {
                binding.startRecording.visibility = View.VISIBLE
                binding.stopRecording.visibility = View.GONE
            }
        }

        binding.takePhoto.setOnClickListener { obtainImage() }
    }

    private fun obtainImage() {
        try {
            startActivityForResult(viewModel.createObtainImageIntent(), REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.edit_contact__unable_to_open_camera_app, Toast.LENGTH_LONG).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            lifecycleScope.launch {
                viewModel.onPhotoTaken()
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_contact_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete -> {
                onDeleteContact()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun maybeShowPhoto(path: String) {
        if (path.isNotEmpty()) {
            Picasso.get()
                .load(path)
                .stableKey(Date().toString())
                .fit()
                .centerCrop()
                .into(binding.photo)
        } else {
            binding.photo.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_unknown_contact))
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) finish()
    }

    private fun onDeleteContact() {
        AlertDialog.Builder(this)
            .setTitle(R.string.edit_contact__confirm_delete_title)
            .setMessage(R.string.edit_contact__confirm_delete_message)
            .setPositiveButton(R.string.btn__delete) { _, _ -> confirmDeleteContact() }
            .setNegativeButton(R.string.btn__cancel) { _, _ -> }
            .show()
    }

    private fun confirmDeleteContact() {
        lifecycleScope.launch {
            viewModel.deleteContact()
            finish()
        }
    }

}

private class SoundAdapter(
    private val context: Context,
    private val onDelete: (sound: Recording) -> Unit,
) : RecyclerView.Adapter<SoundAdapter.ViewHolder>() {

    private var sounds = emptyList<Recording>()

    class ViewHolder(val binding: EditContactSoundItemBinding) : RecyclerView.ViewHolder(binding.root)

    fun setSounds(sounds: List<Recording>) {
        this.sounds = sounds
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(EditContactSoundItemBinding.inflate(LayoutInflater.from(context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sound = sounds[position]

        holder.binding.play.setOnClickListener {
            MediaPlayer.create(context, Uri.parse(sound.soundFilePath)).start()
        }

        holder.binding.delete.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle(R.string.edit_contact__confirm_delete_title)
                .setMessage(R.string.edit_contact__confirm_delete_recording_message)
                .setPositiveButton(R.string.btn__delete) { _, _ -> onDelete(sound) }
                .setNegativeButton(R.string.btn__cancel) { _, _ -> }
                .show()
        }
    }

    override fun getItemCount() = sounds.size

}