package com.example.android.lappanotes.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.android.lappanotes.data.database.entity.Note

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Int): Note?

    @Query("SELECT DISTINCT tags FROM notes")
    fun getAllTags(): LiveData<List<String>>
}