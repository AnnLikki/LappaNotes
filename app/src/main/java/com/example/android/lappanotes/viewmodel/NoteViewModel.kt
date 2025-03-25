package com.example.android.lappanotes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.example.android.lappanotes.data.database.NoteDatabase
import com.example.android.lappanotes.data.database.entity.Note
import com.example.android.lappanotes.data.database.entity.NoteWithTags
import com.example.android.lappanotes.data.repository.NoteRepository
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository
    val allTags: LiveData<List<String>>
    val allNotes: LiveData<List<NoteWithTags>>

    private val _searchQuery = MutableLiveData<String?>()
    val searchQuery: LiveData<String?> get() = _searchQuery
    private val _filteredNotes = MediatorLiveData<List<NoteWithTags>>()
    val filteredNotes: LiveData<List<NoteWithTags>> get() = _filteredNotes
    private var currentNotesSource: LiveData<List<NoteWithTags>>? = null

    init {
        val noteDao = NoteDatabase.getInstance(application).noteDao()
        val noteTagDao = NoteDatabase.getInstance(application).noteTagDao()
        repository = NoteRepository(noteDao, noteTagDao)
        allTags = repository.getAllUniqueTags()
        allNotes = repository.allNotesWithTags

        _filteredNotes.addSource(_searchQuery) { query ->
            currentNotesSource?.let { _filteredNotes.removeSource(it) }
            currentNotesSource = if (query.isNullOrBlank()) {
                repository.allNotesWithTags
            } else {
                repository.getNotesWithTagsByTag(query)
            }
            currentNotesSource?.let { source ->
                _filteredNotes.addSource(source) { notes ->
                    _filteredNotes.value = notes
                }
            }
        }
    }

    fun setSearchQuery(query: String?) {
        _searchQuery.value = query
    }

    suspend fun insertNoteWithTags(note: Note, tags: List<String>) {
        val noteId: Long
        if(note.id == 0) {
            noteId = repository.insert(note)
        }else{
            repository.update(note)
            noteId = note.id.toLong()
        }
        repository.insertTagsForNote(noteId.toInt(), tags)
    }

    suspend fun getNoteById(noteId: Int):Note? = repository.getNoteById(noteId)

    suspend fun getNoteWithTagsById(noteId: Int):NoteWithTags? = repository.getNoteWithTagsById(noteId)

    suspend fun deleteNoteById(noteId: Int) {
        repository.deleteTagsForNote(noteId)
        repository.getNoteById(noteId)?.let { repository.delete(it) }
    }

}