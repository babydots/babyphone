package com.serwylo.babyphone.editcontact

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaRecorder
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.*
import com.serwylo.babyphone.db.AppDatabase
import com.serwylo.babyphone.db.ContactDao
import com.serwylo.babyphone.db.entities.Contact
import com.serwylo.babyphone.db.entities.Recording
import com.serwylo.babyphone.utils.debounce
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class EditContactViewModel(
    private val context: Context,
    private val dao: ContactDao,
    initialContactId: Long = 0,
) : ViewModel() {

    private lateinit var contact: Contact

    lateinit var sounds: LiveData<List<Recording>>

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(true)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _name: MutableLiveData<String> = MutableLiveData("")
    val name: LiveData<String> = _name

    private val _avatarPath: MutableLiveData<String> = MutableLiveData(null)
    val avatarPath: LiveData<String> = _avatarPath

    private val _isRecording: MutableLiveData<Boolean> = MutableLiveData(false)
    val isRecording: LiveData<Boolean> = _isRecording

    fun getContactId(): Long = if (this::contact.isInitialized) contact.id else 0L

    private lateinit var contactDir: File

    init {
        viewModelScope.launch(Dispatchers.IO) {
            contact = if (initialContactId > 0L) {
                dao.getContact(initialContactId)
            } else {
                val contact = Contact("", "")
                val id = dao.insert(contact)
                contact.copy(id = id)
            }

            contactDir = File(context.getExternalFilesDir("Contacts"), contact.id.toString())
            if (!contactDir.exists()) {
                contactDir.mkdirs()
            }

            sounds = dao.getRecordingsForContact(contact.id)

            withContext(Dispatchers.Main) {
                _name.value = contact.name
                _avatarPath.value = contact.avatarPath
                _isLoading.value = false
            }
        }
    }

    fun updateName(name: String) {
        _name.value = name
        debouncedSaveName(name)
    }

    suspend fun deleteContact() = withContext(Dispatchers.IO) {

        if (contact.avatarPath.isNotEmpty()) {
            val file = Uri.parse(contact.avatarPath).toFile()
            if (file.exists()) {
                file.delete()
            }
        }

        dao.getRecordingsForContactSync(contact.id).forEach { recording ->
            val file = Uri.parse(recording.soundFilePath).toFile()
            if (file.exists()) {
                file.delete()
            }
        }

        contactDir.delete()

        val enabledContacts = dao.loadEnabledContactsSync().filter { it.id != contact.id }
        if (enabledContacts.isEmpty()) {
            val default = dao.loadDefaultContactsSync().first()
            dao.update(default.copy(isEnabled = true))
            dao.changeCurrentContact(default.id)
            dao.delete(contact)
        } else {
            dao.changeCurrentContact(enabledContacts.first().id)
            dao.delete(contact)
        }

    }

    private var recorder: MediaRecorder? = null
    private var currentlyRecordingSound: File? = null

    suspend fun startRecording() = withContext(Dispatchers.IO) {

        withContext(Dispatchers.Main) { _isRecording.value = true }

        File(contactDir, "${Date().time}.${Math.random() * 10000000}.aac").also { soundFile ->
            currentlyRecordingSound = soundFile

            recorder = MediaRecorder().apply {
                // Tried using AudioSource.VOICE_RECOGNITION - but the sound cancellation provided
                // ends up sounding tinny and robotic.
                setAudioSource(MediaRecorder.AudioSource.MIC)

                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

                // Nice high quality settings for voice recording.
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(soundFile)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

                prepare()
                start()
            }
        }
    }

    suspend fun stopRecording() = withContext(Dispatchers.IO) {

        withContext(Dispatchers.Main) { _isRecording.value = false }

        recorder?.apply {
            stop()
            release()
            currentlyRecordingSound?.also { file ->
                dao.insert(
                    Recording(
                        contact.id,
                        file.toUri().toString(),
                    )
                )
            }
        }

        currentlyRecordingSound = null
        recorder = null
    }

    fun createObtainImageIntent() =
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(
                MediaStore.EXTRA_OUTPUT,
                FileProvider.getUriForFile(
                    context,
            "com.serwylo.babyphone.fileprovider",
                    File(contactDir, "photo.jpg"),
                )
            )
        }

    suspend fun onPhotoTaken() = withContext(Dispatchers.IO) {
            val file = File(contactDir, "photo.jpg")

            BitmapFactory.decodeFile(file.absolutePath).also { bitmap ->
                val smallFile = File(contactDir, "photo.small.jpg")
                if (smallFile.exists()) {
                    smallFile.delete()
                }

                val smallBitmap = shrinkPhoto(bitmap);

                smallFile.outputStream().use {
                    smallBitmap?.compress(Bitmap.CompressFormat.JPEG, 80, it)
                }

                withContext(Dispatchers.Main) {
                    _avatarPath.value = smallFile.toUri().toString()
                }

                contact = contact.copy(avatarPath = smallFile.toUri().toString()).also {
                    dao.update(it)
                }
            }

            file.delete()
        }

    private val debouncedSaveName = debounce<String>(300, viewModelScope) { name ->
        viewModelScope.launch(Dispatchers.IO) {
            contact = contact.copy(name = name).also {
                dao.update(it)
            }
        }
    }

    private fun shrinkPhoto(image: Bitmap): Bitmap? {
        val maxSize = 512
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

    fun deleteSound(sound: Recording) {
        viewModelScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(context).contactDao()

            // This should then trigger relevant LiveData events which will result in this adapter being
            // updated.
            dao.delete(sound)
        }
    }

}

class EditContactViewModelFactory(private val context: Context, private val dao: ContactDao, private val contactId: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditContactViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditContactViewModel(context.applicationContext, dao, contactId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
