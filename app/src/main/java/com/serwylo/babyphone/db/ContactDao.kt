package com.serwylo.babyphone.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.serwylo.babyphone.db.entities.Contact
import com.serwylo.babyphone.db.entities.Recording

@Dao
interface ContactDao {

    @Query("SELECT * FROM Contact WHERE isEnabled = 1")
    fun loadEnabledContacts(): LiveData<List<Contact>>

    @Query("SELECT * FROM Contact")
    fun loadAllContacts(): LiveData<List<Contact>>

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

}