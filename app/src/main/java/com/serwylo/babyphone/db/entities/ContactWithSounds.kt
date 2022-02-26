package com.serwylo.babyphone.db.entities

import androidx.room.Embedded
import androidx.room.Relation

data class ContactWithSounds(
    @Embedded
    val contact: Contact,

    @Relation(
        parentColumn = "id",
        entityColumn = "contactId",
    )
    val sounds: List<Recording>,
)