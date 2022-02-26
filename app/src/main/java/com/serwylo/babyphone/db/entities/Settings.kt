package com.serwylo.babyphone.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Contact::class,
            parentColumns = ["id"],
            childColumns = ["currentContactId"],

            // This field is actually not able to be set to null, so this will trigger an exception.
            // Instead, we should manually change the contact first.
            onDelete = ForeignKey.SET_NULL,
        )
    ]
)
data class Settings(

    val currentContactId: Long,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0

)