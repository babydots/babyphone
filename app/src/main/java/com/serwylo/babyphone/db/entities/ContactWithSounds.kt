package com.serwylo.babyphone.db.entities

data class ContactWithSounds(
    val contact: Contact,
    val sounds: List<Recording>,
)