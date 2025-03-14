package com.example.android.lappanotes

import android.content.Context
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.lappanotes.data.database.NoteDatabase
import com.example.android.lappanotes.data.database.dao.NoteDao
import com.example.android.lappanotes.data.database.entity.Note
import com.example.android.lappanotes.data.database.utils.Converters
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class NoteDaoTest {

    private lateinit var database: NoteDatabase
    private lateinit var noteDao: NoteDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, NoteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        noteDao = database.noteDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetNote() = runBlocking {
        val note = Note(text = "Test note", tags = listOf("work", "test"))

        noteDao.insert(note)
        val retrievedNote = noteDao.getNoteById(note.id)

        assertNotNull(retrievedNote)
        assertEquals("Test note", retrievedNote?.text)
        assertEquals(listOf("work", "test"), retrievedNote?.tags)
    }

    @Test
    @Throws(Exception::class)
    fun updateNote() = runBlocking {
        val note = Note(text = "Old text")
        noteDao.insert(note)

        val updatedNote = note.copy(text = "New text")
        noteDao.update(updatedNote)
        val result = noteDao.getNoteById(note.id)

        assertEquals("New text", result?.text)
    }

    @Test
    @Throws(Exception::class)
    fun deleteNote() = runBlocking {
        val note = Note(text = "To delete")
        noteDao.insert(note)

        noteDao.delete(note)
        val result = noteDao.getNoteById(note.id)

        assertNull(result)
    }

    @Test
    @Throws(Exception::class)
    fun testLiveData() = runBlocking {
        val note1 = Note(text = "Note 1")
        val note2 = Note(text = "Note 2")
        noteDao.insert(note1)
        noteDao.insert(note2)

        val notesList = mutableListOf<List<Note>>()
        val observer = Observer<List<Note>> { notesList.add(it) }
        noteDao.getAllNotes().observeForever(observer)

        assertEquals(2, notesList.last().size)
        noteDao.getAllNotes().removeObserver(observer)
    }

    @Test
    fun testTagConverter() {
        val converter = Converters()
        val tags = listOf("work", "home", "urgent")

        // Конвертация в строку и обратно
        val stringTags = converter.fromList(tags)
        val restoredTags = converter.fromString(stringTags)

        assertEquals(tags, restoredTags)
    }

}