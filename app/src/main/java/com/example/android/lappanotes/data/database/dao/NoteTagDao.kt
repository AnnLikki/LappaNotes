package com.example.android.lappanotes.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.android.lappanotes.data.database.entity.Note
import com.example.android.lappanotes.data.database.entity.NoteTagCrossRef

@Dao
interface NoteTagDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTagForNote(noteTag: NoteTagCrossRef)

    @Delete
    suspend fun deleteTagForNote(crossRef: NoteTagCrossRef)

    @Query("SELECT * FROM note_tags WHERE noteId = :noteId")
    fun getTagsForNote(noteId: Int): LiveData<List<NoteTagCrossRef>>

    @Query("SELECT * FROM note_tags WHERE noteId = :noteId")
    suspend fun getTagsForNoteSync(noteId: Int): List<NoteTagCrossRef>

    @Query("SELECT DISTINCT tag FROM note_tags")
    fun getAllUniqueTags(): LiveData<List<String>>

    @Query("DELETE FROM note_tags WHERE noteId = :noteId")
    suspend fun deleteAllTagsForNote(noteId: Int)

    @Query("SELECT notes.* FROM notes INNER JOIN note_tags ON notes.id = note_tags.noteId WHERE note_tags.tag = :tag")
    fun getNotesByTag(tag: String): LiveData<List<Note>>
}