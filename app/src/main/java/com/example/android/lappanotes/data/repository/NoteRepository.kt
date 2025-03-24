package com.example.android.lappanotes.data.repository

import androidx.lifecycle.LiveData
import androidx.room.Transaction
import com.example.android.lappanotes.data.database.dao.NoteDao
import com.example.android.lappanotes.data.database.dao.NoteTagDao
import com.example.android.lappanotes.data.database.entity.Note
import com.example.android.lappanotes.data.database.entity.NoteTagCrossRef

class NoteRepository(
    private val noteDao: NoteDao,
    private val noteTagDao: NoteTagDao
) {
    val allNotes: LiveData<List<Note>> = noteDao.getAllNotes()
    val allTags: LiveData<List<String>> = noteDao.getAllTags()

    suspend fun insert(note: Note) = noteDao.insert(note)
    suspend fun update(note: Note) = noteDao.update(note)
    suspend fun delete(note: Note) = noteDao.delete(note)

    suspend fun insertTagsForNote(noteId: Int, tags: List<String>) {
        tags.forEach { tag ->
            noteTagDao.insertTagForNote(NoteTagCrossRef(noteId, tag))
        }
    }

    suspend fun deleteTagsForNote(noteId: Int) {
        noteTagDao.deleteAllTagsForNote(noteId)
    }

    fun getAllUniqueTags(): LiveData<List<String>> = noteTagDao.getAllUniqueTags()

    @Transaction
    suspend fun insertNoteWithTags(note: Note, tags: List<String>) {
        val noteId = noteDao.insert(note).toInt()
        tags.forEach { tag ->
            noteTagDao.insertTagForNote(NoteTagCrossRef(noteId, tag))
        }
    }
}