package com.serwylo.babyphone.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Contact::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class Recording(

    val contactId: Long,

    /**
     * A string that can be parsed to a [android.net.Uri]. Either an absolute path to a sound
     * recording (e.g. file:///storage/.../blah.3gp), or a custom path indicating we should use
     * the [android.content.res.AssetManager] to load it (e.g. file:///android_asset/blah.3gp).
     */
    val soundFilePath: String,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
)