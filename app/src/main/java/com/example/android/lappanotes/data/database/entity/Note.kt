package com.example.android.lappanotes.data.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
)

data class NoteWithTags(
    @Embedded val note: Note,
    @Relation(
        parentColumn = "id",
        entityColumn = "noteId",
    )
    val tags: List<NoteTagCrossRef>,
)
