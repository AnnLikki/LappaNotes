package com.example.android.lappanotes.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "note_tags",
    primaryKeys = ["noteId", "tag"],
    foreignKeys = [
        ForeignKey(
            entity = Note::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NoteTagCrossRef(
    val noteId: Int,
    val tag: String
)