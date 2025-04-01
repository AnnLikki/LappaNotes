package com.example.android.lappanotes.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.android.lappanotes.data.database.dao.NoteDao
import com.example.android.lappanotes.data.database.dao.NoteTagDao
import com.example.android.lappanotes.data.database.entity.Note
import com.example.android.lappanotes.data.database.entity.NoteTagCrossRef

@Database(entities = [Note::class, NoteTagCrossRef::class], version = 1, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    abstract fun noteTagDao(): NoteTagDao

    companion object {
        @Volatile
        private var dbInstance: NoteDatabase? = null

        fun getInstance(context: Context): NoteDatabase {
            return dbInstance ?: synchronized(this) {
                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        NoteDatabase::class.java,
                        "note_database",
                    ).build()
                dbInstance = instance
                instance
            }
        }
    }
}
