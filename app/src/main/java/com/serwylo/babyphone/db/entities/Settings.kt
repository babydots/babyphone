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
            onDelete = ForeignKey.SET_NULL,
        )
    ]
)
data class Settings(

    val currentContactId: Long,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0

)