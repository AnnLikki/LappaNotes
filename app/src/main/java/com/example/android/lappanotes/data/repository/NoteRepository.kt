package com.example.android.lappanotes.data.repository

import androidx.lifecycle.LiveData
import androidx.room.Transaction
import com.example.android.lappanotes.data.database.dao.NoteDao
import com.example.android.lappanotes.data.database.dao.NoteTagDao
import com.example.android.lappanotes.data.database.entity.Note
import com.example.android.lappanotes.data.database.entity.NoteTagCrossRef
import com.example.android.lappanotes.data.database.entity.NoteWithTags

class NoteRepository(
    private val noteDao: NoteDao,
    private val noteTagDao: NoteTagDao,
) {
    val allNotes: LiveData<List<Note>> = noteDao.getAllNotes()
    val allNotesWithTags: LiveData<List<NoteWithTags>> = noteDao.getAllNotesWithTags()
    val allTags: LiveData<List<String>> = noteDao.getAllTags()

    suspend fun getNoteById(noteId: Int) = noteDao.getNoteById(noteId)

    suspend fun insert(note: Note) = noteDao.insert(note)

    suspend fun update(note: Note) = noteDao.update(note)

    suspend fun delete(note: Note) = noteDao.delete(note)

    suspend fun insertTagsForNote(
        noteId: Int,
        tags: List<String>,
    ) {
        deleteTagsForNote(noteId)
        tags.filter { it.isNotBlank() }.forEach {
            noteTagDao.insertTagForNote(NoteTagCrossRef(noteId, it.trim().lowercase()))
        }
    }

    suspend fun deleteTagsForNote(noteId: Int) {
        noteTagDao.deleteAllTagsForNote(noteId)
    }

    fun getAllUniqueTags(): LiveData<List<String>> = noteTagDao.getAllUniqueTags()

    @Transaction
    suspend fun insertNoteWithTags(
        note: Note,
        tags: List<String>,
    ) {
        val noteId = noteDao.insert(note)
        insertTagsForNote(noteId.toInt(), tags)
    }

    suspend fun getNoteWithTagsById(noteId: Int): NoteWithTags? {
        val note = getNoteById(noteId) ?: return null
        val tags = noteTagDao.getTagsForNoteSync(noteId)
        return NoteWithTags(note, tags)
    }

    fun getNotesWithTagsByTag(tag: String): LiveData<List<NoteWithTags>> {
        return noteDao.getNotesWithTagsByTag(tag)
    }
}
