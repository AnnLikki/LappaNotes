package com.example.android.lappanotes.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.android.lappanotes.data.database.entity.Note
import com.example.android.lappanotes.data.database.entity.NoteTagCrossRef
import com.example.android.lappanotes.data.database.entity.NoteWithTags

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Int): Note?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(crossRef: NoteTagCrossRef)

    @Query("SELECT DISTINCT tag FROM note_tags")
    fun getAllTags(): LiveData<List<String>>

    @Transaction
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotesWithTags(): LiveData<List<NoteWithTags>>

    @Transaction
    @Query("SELECT * FROM notes WHERE id IN (SELECT noteId FROM note_tags WHERE tag = :tag) ORDER BY timestamp DESC")
    fun getNotesWithTagsByTag(tag: String): LiveData<List<NoteWithTags>>
}
