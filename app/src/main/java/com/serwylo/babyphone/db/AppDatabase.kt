package com.serwylo.babyphone.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.serwylo.babyphone.db.entities.Contact
import com.serwylo.babyphone.db.entities.Recording
import com.serwylo.babyphone.db.migrations.makeDatabaseSeeder

@Database(
    entities = [
        Contact::class,
        Recording::class,
    ],
    version = 1,
)
abstract class AppDatabase: RoomDatabase() {

    abstract fun contactDao(): ContactDao

    companion object {

        private var db: AppDatabase? = null

        fun getInstance(context: Context) =
            db ?: synchronized(this) {
                db ?: buildDatabase(context).also { db = it }
            }

        private fun buildDatabase(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "database.db")
                .addCallback(makeDatabaseSeeder(context))
                .build()
    }
}