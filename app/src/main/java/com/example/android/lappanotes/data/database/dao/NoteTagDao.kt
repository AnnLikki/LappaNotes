package com.example.android.lappanotes.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.android.lappanotes.data.database.entity.NoteTagCrossRef

@Dao
interface NoteTagDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTagForNote(crossRef: NoteTagCrossRef)

    @Delete
    suspend fun deleteTagForNote(crossRef: NoteTagCrossRef)

    @Query("SELECT * FROM note_tags WHERE noteId = :noteId")
    suspend fun getTagsForNote(noteId: Int): List<NoteTagCrossRef>

    @Query("SELECT DISTINCT tag FROM note_tags")
    fun getAllUniqueTags(): LiveData<List<String>>

    @Query("DELETE FROM note_tags WHERE noteId = :noteId")
    suspend fun deleteAllTagsForNote(noteId: Int)
}