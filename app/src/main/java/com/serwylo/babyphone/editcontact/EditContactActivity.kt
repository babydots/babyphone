package com.serwylo.babyphone.editcontact

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
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
import java.io.File
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
        binding.sounds.adapter = SoundAdapter(this).also { adapter ->
            viewModel.sounds.observe(this) { adapter.setSounds(it) }
        }

        viewModel.avatarPath.observe(this) { maybeShowPhoto(it) }

        binding.fab.setOnClickListener {
            lifecycleScope.launch {
                viewModel.toggleRecording()
            }
        }

        binding.takePhoto.setOnClickListener { obtainImage() }
    }

    private fun obtainImage() {
        try {
            startActivityForResult(viewModel.createObtainImageIntent(), REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Unable to open camera app", Toast.LENGTH_LONG).show()
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

}

private class SoundAdapter(private val context: Context) : RecyclerView.Adapter<SoundAdapter.ViewHolder>() {

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
                .setTitle("Confirm delete")
                .setMessage("Are you sure you want to remove this recording?")
                .setPositiveButton("Delete") { _, _ -> deleteSound(sound) }
                .setNegativeButton("Cancel") { _, _ -> }
                .show()
        }
    }

    private fun deleteSound(sound: Recording) {
        val dao = AppDatabase.getInstance(context).contactDao()

        // This should then trigger relevant LiveData events which will result in this adapter being
        // updated.
        dao.delete(sound)
    }

    override fun getItemCount() = sounds.size

}