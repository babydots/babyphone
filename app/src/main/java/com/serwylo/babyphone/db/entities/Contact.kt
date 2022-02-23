package com.serwylo.babyphone.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Contact(

    val name: String,

    /**
     * A string that can be parsed to a [android.net.Uri] to be consumed by [com.squareup.picasso.Picasso].
     * Specifically, an absolute path to a file (file:///storage/.../blah.png), or an asset
     * path (file:///android_assets/blah.png).
     */
    val avatarPath: String,

    val isEnabled: Boolean = true,

    val isDefault: Boolean = false,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
)