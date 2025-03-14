package com.example.android.lappanotes.data.repository

import androidx.lifecycle.LiveData
import com.example.android.lappanotes.data.database.dao.NoteDao
import com.example.android.lappanotes.data.database.entity.Note

class NoteRepository(private val noteDao: NoteDao) {
    val allNotes: LiveData<List<Note>> = noteDao.getAllNotes()
    val allTags: LiveData<List<String>> = noteDao.getAllTags()

    suspend fun insert(note: Note) = noteDao.insert(note)
    suspend fun update(note: Note) = noteDao.update(note)
    suspend fun delete(note: Note) = noteDao.delete(note)
}