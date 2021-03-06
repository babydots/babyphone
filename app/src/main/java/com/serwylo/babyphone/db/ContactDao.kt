package com.serwylo.babyphone.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.serwylo.babyphone.db.entities.Contact
import com.serwylo.babyphone.db.entities.ContactWithSounds
import com.serwylo.babyphone.db.entities.Recording

@Dao
interface ContactDao {

    @Query("SELECT * FROM Contact WHERE isEnabled = 1")
    fun loadEnabledContacts(): LiveData<List<Contact>>

    @Query("SELECT * FROM Contact WHERE isEnabled = 1")
    suspend fun loadEnabledContactsSync(): List<Contact>

    @Query("SELECT * FROM Contact WHERE isDefault = 1")
    suspend fun loadDefaultContactsSync(): List<Contact>

    @Query("SELECT * FROM Contact")
    fun loadAllContacts(): LiveData<List<Contact>>

    @Transaction
    @Query("SELECT * FROM Contact WHERE id = (SELECT currentContactId FROM settings LIMIT 0, 1)")
    fun getCurrentContact(): LiveData<ContactWithSounds>

    @Query("SELECT currentContactId FROM settings LIMIT 0, 1")
    suspend fun getCurrentContactId(): Long

    @Query("SELECT * FROM Recording WHERE contactId = :contactId")
    fun getRecordingsForContact(contactId: Long): LiveData<List<Recording>>

    @Query("SELECT * FROM Recording WHERE contactId = :contactId")
    suspend fun getRecordingsForContactSync(contactId: Long): List<Recording>

    @Query("UPDATE Settings SET currentContactId = :id")
    fun changeCurrentContact(id: Long)

    @Query("UPDATE Settings SET currentContactId = (SELECT id FROM Contact WHERE isEnabled = 1 LIMIT 0, 1)")
    fun pickNewCurrentContact()

    @Query("SELECT * FROM Contact WHERE id = :id")
    fun getContact(id: Long): Contact

    @Insert
    fun insert(contact: Contact): Long

    @Insert
    fun insert(recording: Recording): Long

    @Insert
    fun insertAll(recordings: List<Recording>)

    @Delete
    fun delete(contact: Contact)

    @Delete
    fun delete(recording: Recording)

    @Update(entity = Contact::class)
    fun update(contact: Contact)

}