package com.serwylo.babyphone

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.app.NavUtils
import com.serwylo.babyphone.databinding.ActivityEditContactBinding
import java.io.IOException

private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

private const val LOG_TAG = "EditContactActivity"

/**
 * Much of the audio recording code is adapted from https://developer.android.com/guide/topics/media/mediarecorder.
 */
class EditContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditContactBinding

    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    private lateinit var fileName: String

    private var recorder: MediaRecorder? = null
    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {

        // Not sure why, but this has to come before super.onCreate(), or else the light theme
        // will have a dark background, making the text very hard to read. This seems to fix it,
        // though I have not investigated why. Source: https://stackoverflow.com/a/15657428.
        ThemeManager.applyTheme(this)

        super.onCreate(savedInstanceState)

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

        fileName = "${filesDir.absolutePath}/audiorecordtest.3gp"

        binding = ActivityEditContactBinding.inflate(layoutInflater)
        binding.fab.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }

            isRecording = !isRecording
        }

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { NavUtils.navigateUpFromSameTask(this) }
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

    private fun startRecording() {
        binding.fab.icon = AppCompatResources.getDrawable(this, R.drawable.ic_stop)
        binding.fab.text = "Stop recording"

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }

            start()
        }
    }


    private fun stopRecording() {
        binding.fab.icon = AppCompatResources.getDrawable(this, R.drawable.ic_record)
        binding.fab.text = "Record"

        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }

}