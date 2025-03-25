package com.example.android.lappanotes.data.database.dao

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.lappanotes.data.database.NoteDatabase
import com.example.android.lappanotes.data.database.entity.Note
import com.example.android.lappanotes.data.database.entity.NoteTagCrossRef
import com.example.android.lappanotes.data.repository.NoteRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class NoteDaoTest {

    private lateinit var noteDao: NoteDao
    private lateinit var noteTagDao: NoteTagDao
    private lateinit var db: NoteDatabase
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: NoteRepository


    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, NoteDatabase::class.java)
            .setTransactionExecutor(Executors.newSingleThreadExecutor())
            .allowMainThreadQueries()
            .build()
        noteDao = db.noteDao()
        noteTagDao = db.noteTagDao()
        repository = NoteRepository(noteDao, noteTagDao)
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    @Test
    fun insertTagForNote() = runTest {
        
        val note = Note(text = "Test note")
        val noteId = noteDao.insert(note).toInt()
        
        noteTagDao.insertTagForNote(NoteTagCrossRef(noteId, "work"))
        val tags = noteTagDao.getTagsForNote(noteId)
        
        assertEquals(1, tags.size)
        assertEquals("work", tags[0].tag)
    }

    @Test
    fun insertNoteWithTagsTransaction() = runTest {
        val note = Note(text = "Test note")
        val tags = listOf("work", "urgent")
        
        repository.insertNoteWithTags(note, tags)
        
        val notes = noteDao.getAllNotes().getOrAwaitValue()
        val dbTags = noteTagDao.getAllUniqueTags().getOrAwaitValue()

        assertEquals(1, notes.size)
        assertEquals(2, dbTags.size)
        assertTrue(dbTags.containsAll(tags))
    }

//    @Test(expected = SQLiteConstraintException::class)
//    fun transactionRollbackOnFailure() = runTest {
//        
//        val note = Note(text = "Test note")
//        val tags = listOf("work", "invalid_tag")
//
//        repository.insertNoteWithTags(note, tags)
//
//        assertTrue(noteDao.getAllNotes().getOrAwaitValue().isEmpty())
//        assertTrue(noteTagDao.getAllUniqueTags().getOrAwaitValue().isEmpty())
//    }

    @Test
    fun insertAndGetNoteById() = runTest {
        val note = Note(text = "Test note")

        val insertedId = noteDao.insert(note)
        val result = noteDao.getNoteById(insertedId.toInt())

        assertNotNull(result)
        assertEquals("Test note", result?.text)
        assertEquals(insertedId.toInt(), result?.id)
    }

    @Test
    fun getAllNotesSortedByTimestamp() = runTest {
        val note1 = Note(text = "Note 1", timestamp = 1000L)
        val note2 = Note(text = "Note 2", timestamp = 2000L)
        
        noteDao.insert(note1)
        noteDao.insert(note2)
        val result = noteDao.getAllNotes().getOrAwaitValue()
        
        assertEquals(2, result.size)
        assertEquals(note2.text, result[0].text)
    }

    @Test
    fun deleteNote() = runTest {
        val note = Note(text = "Delete me")
        val insertedId = noteDao.insert(note)
        noteTagDao.insertTagForNote(NoteTagCrossRef(insertedId.toInt(), "test"))
        
        noteDao.delete(note.copy(id = insertedId.toInt()))
        val notesResult = noteDao.getAllNotes().getOrAwaitValue()
        val tagsResult = noteTagDao.getAllUniqueTags().getOrAwaitValue()

        
        assertTrue(notesResult.isEmpty())
        assertTrue(tagsResult.isEmpty())
    }

    @Test
    fun getAllTags() = runTest {
        val note1 = Note(text = "Note 1")
        val note2 = Note(text = "Note 2")
        val insertedId1 = noteDao.insert(note1)
        val insertedId2 = noteDao.insert(note2)

        noteTagDao.insertTagForNote(NoteTagCrossRef(insertedId1.toInt(), "work"))
        noteTagDao.insertTagForNote(NoteTagCrossRef(insertedId1.toInt(), "home"))
        noteTagDao.insertTagForNote(NoteTagCrossRef(insertedId2.toInt(), "work"))
        noteTagDao.insertTagForNote(NoteTagCrossRef(insertedId2.toInt(), "study"))
        
        val result = noteTagDao.getAllUniqueTags().getOrAwaitValue()
        
        assertEquals(setOf("work", "home", "study"), result.toSet())
    }

    private fun <T> LiveData<T>.getOrAwaitValue(): T {
        var result: T? = null
        val latch = CountDownLatch(1)
        val observer = object : Observer<T> {
            override fun onChanged(value: T) {
                result = value
                latch.countDown()
                this@getOrAwaitValue.removeObserver(this)
            }
        }
        observeForever(observer)
        latch.await()
        return result!!
    }
}