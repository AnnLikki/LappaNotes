package com.example.android.lappanotes.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.android.lappanotes.data.database.entity.Note
import com.example.android.lappanotes.data.database.entity.NoteTagCrossRef

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
    @Query("SELECT * FROM notes")
    fun getNotesWithTags(): LiveData<List<NoteWithTags>>

    data class NoteWithTags(
        @Embedded val note: Note,
        @Relation(
            parentColumn = "id",
            entityColumn = "noteId"
        )
        val tags: List<NoteTagCrossRef>
    )
}