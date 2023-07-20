package com.serwylo.babyphone.db.migrations

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.annotation.StringRes
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.serwylo.babyphone.R
import com.serwylo.babyphone.db.AppDatabase
import com.serwylo.babyphone.db.ContactDao
import com.serwylo.babyphone.db.entities.Contact
import com.serwylo.babyphone.db.entities.Recording
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun makeDatabaseSeeder(context: Context) = object : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {

        val dao = AppDatabase.getInstance(context).contactDao()

        GlobalScope.launch(Dispatchers.IO) {

            val babyId = createDefaultContact(context, dao, defaultContactBaby)
            createDefaultContact(context, dao, defaultContactDad)
            createDefaultContact(context, dao, defaultContactMum)

            // Intentionally don't make available via DAO, we don't want anyone else to be able
            // to insert more than the default single row of settings into our database.
            val settings = ContentValues()
            settings.put("currentContactId", babyId)
            db.insert("Settings", SQLiteDatabase.CONFLICT_FAIL, settings)

        }

    }
}

private fun createDefaultContact(context: Context, dao: ContactDao, contact: InitContact): Long {

    val id = dao.insert(
        Contact(
            context.getString(contact.nameRes),
            "file:///android_asset/${contact.avatar}",
            isEnabled = true,
            isDefault = true,
        )
    )

    dao.insertAll(
        contact.sounds.map {
            Recording(
                contactId = id,
                soundFilePath = "file:///android_asset/$it",
            )
        }
    )

    return id

}

private data class InitContact(@StringRes val nameRes: Int, val avatar: String, val sounds: List<String>)

private val defaultContactBaby = InitContact(
    R.string.default_contact__baby,
    "baby.jpg",
    listOf(
        "babble_1.ogg",
        "babble_2.ogg",
        "babble_3.ogg",
        "babble_baby_1.ogg",
        "babble_baby_2.ogg",
        "babble_misc.ogg",
        "ball.ogg",
        "ball_bee_boo.ogg",
        "bee_boo.ogg",
        "hey_babble.ogg",
        "hey_babble_2.ogg",
        "hey_babble_3.ogg",
        "hey_babble_4.ogg",
        "hey_babble_5.ogg",
        "hey_babble_6.ogg",
        "poo_poo_poo.ogg",
        "poo_poo_sss.ogg",
        "quiet_babble.ogg",
        "squeal.ogg",
        "uh_oh_1.ogg",
    ),
)

private val defaultContactDad = InitContact(
    R.string.default_contact__dad,
    "dad.jpg",
    listOf(
        "dad_mmm.ogg",
        "dad_oh_i_see.ogg",
        "dad_uh_huh.ogg",
        "dad_wow.ogg",
        "dad_yeah.ogg",
    )
)

private val defaultContactMum = InitContact(
    R.string.default_contact__mum,
    "mum.jpg",
    listOf(
        "mum_mmm_hmm.ogg",
        "mum_oh_i_see.ogg",
        "mum_really.ogg",
        "mum_sounds_good.ogg",
        "mum_tell_me_more.ogg",
        "mum_uh_huh.ogg",
        "mum_wow.ogg",
        "mum_wow_2.ogg",
    )
)
