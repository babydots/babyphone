package com.serwylo.babyphone

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.app.NavUtils
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.serwylo.babyphone.databinding.ActivityEditContactBinding
import com.serwylo.babyphone.databinding.EditContactSoundItemBinding
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*

private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
private const val REQUEST_IMAGE_CAPTURE = 1


private const val LOG_TAG = "EditContactActivity"

/**
 * Much of the audio recording code is adapted from https://developer.android.com/guide/topics/media/mediarecorder.
 */
class EditContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditContactBinding

    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    private lateinit var contactDir: File

    private var recorder: MediaRecorder? = null
    private var isRecording = false

    private lateinit var adapter: SoundAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        // Not sure why, but this has to come before super.onCreate(), or else the light theme
        // will have a dark background, making the text very hard to read. This seems to fix it,
        // though I have not investigated why. Source: https://stackoverflow.com/a/15657428.
        ThemeManager.applyTheme(this)

        super.onCreate(savedInstanceState)

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

        contactDir = File(getExternalFilesDir("Contacts"), "1")
        if (!contactDir.exists()) {
            contactDir.mkdirs()
        }

        binding = ActivityEditContactBinding.inflate(layoutInflater)
        binding.fab.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }

            isRecording = !isRecording
        }

        binding.takePhoto.setOnClickListener { obtainImage() }

        adapter = SoundAdapter(this)
        binding.sounds.adapter = adapter
        binding.sounds.layoutManager = LinearLayoutManager(this)

        maybeShowPhoto()

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { NavUtils.navigateUpFromSameTask(this) }

        lifecycleScope.launchWhenCreated {
            withContext(Dispatchers.IO) {
                adapter.setSounds(getSounds())
            }
        }
    }

    private fun obtainImage() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val photoFile = File(contactDir, "photo.jpg")

        val photoURI: Uri = FileProvider.getUriForFile(
        this,
        "com.serwylo.babyphone.fileprovider",
            photoFile,
        )

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Unable to open camera app", Toast.LENGTH_LONG).show()
        }
    }

    private fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap? {
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            lifecycleScope.launch(Dispatchers.IO) {
                val file = File(contactDir, "photo.jpg")

                BitmapFactory.decodeFile(file.absolutePath).also { bitmap ->
                    val smallFile = File(contactDir, "photo.small.jpg")
                    if (smallFile.exists()) {
                        smallFile.delete()
                    }

                    val smallBitmap = getResizedBitmap(bitmap, 768);

                    smallFile.outputStream().use {
                        smallBitmap?.compress(Bitmap.CompressFormat.JPEG, 80, it)
                    }
                }

                file.delete()

                withContext(Dispatchers.Main) {
                    maybeShowPhoto()
                }
            }
        }
    }

    private fun maybeShowPhoto() {
        val file = File(contactDir, "photo.small.jpg")

        if (file.exists()) {
            Picasso.get()
                .load(file)
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

    private fun getSounds(): List<File> {
        return (contactDir.listFiles() ?: emptyArray<File>())
            .filter { it.extension == "3gp" }
            .filterNotNull()
    }

    private fun getSoundIds(): List<Int> {
        return getSounds()
            .map { it.nameWithoutExtension }
            .mapNotNull { it.toIntOrNull() }
    }

    private fun nextSoundFile(): File {
        val sounds = getSoundIds()

        val name = if (sounds.isEmpty()) {
            "1.3gp"
        } else {
            "${sounds.last() + 1}.3gp"
        }

        return File(contactDir, name)
    }

    private var currentlyRecordingSound: File? = null

    private fun startRecording() {
        binding.fab.icon = AppCompatResources.getDrawable(this, R.drawable.ic_stop)
        binding.fab.text = "Stop recording"

        nextSoundFile().also { soundFile ->
            currentlyRecordingSound = soundFile

            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(soundFile)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

                prepare()
                start()
            }
        }
    }

    private fun stopRecording() {
        binding.fab.icon = AppCompatResources.getDrawable(this, R.drawable.ic_record)
        binding.fab.text = "Record"

        recorder?.apply {
            stop()
            release()
            currentlyRecordingSound?.also {
                adapter.addSound(it)
            }
        }

        currentlyRecordingSound = null
        recorder = null
    }

}

private class SoundAdapter(private val context: Context) : RecyclerView.Adapter<SoundAdapter.ViewHolder>() {

    private var sounds = emptyList<File>()

    class ViewHolder(val binding: EditContactSoundItemBinding) : RecyclerView.ViewHolder(binding.root)

    fun setSounds(sounds: List<File>) {
        this.sounds = sounds
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(EditContactSoundItemBinding.inflate(LayoutInflater.from(context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sound = sounds[position]

        holder.binding.play.setOnClickListener {
            MediaPlayer.create(context, Uri.fromFile(sound)).start()
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

    private fun deleteSound(soundFile: File) {
        if (soundFile.exists()) {
            soundFile.delete()
        }

        notifyItemRemoved(sounds.indexOf(soundFile))

        sounds = sounds.filterNot { it === soundFile }
    }

    override fun getItemCount() = sounds.size

    fun addSound(file: File) {
        sounds = sounds.toMutableList().apply {
            add(file)
        }

        notifyItemInserted(sounds.size)
    }

}