package com.example.android.lappanotes.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.lappanotes.R
import com.example.android.lappanotes.data.database.dao.NoteDao
import com.example.android.lappanotes.data.database.entity.Note
import com.example.android.lappanotes.data.database.entity.NoteWithTags
import java.util.Date

class NoteAdapter(
    private val onClick: (NoteWithTags) -> Unit,
    private val onLongClick: (NoteWithTags) -> Unit
) : ListAdapter<NoteWithTags, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvText: TextView = itemView.findViewById(R.id.tvText)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val tvTags: TextView = itemView.findViewById(R.id.tvTags)

        fun bind(noteWithTags: NoteWithTags) {
            tvText.text = noteWithTags.note.text
            tvTimestamp.text = Date(noteWithTags.note.timestamp).toString()
            tvTags.text = noteWithTags.tags.joinToString(", ") { it.tag }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val noteWithTags = getItem(position)
        holder.bind(noteWithTags)
        holder.itemView.setOnClickListener { onClick(noteWithTags) }
        holder.itemView.setOnLongClickListener {
            onLongClick(noteWithTags)
            true
        }
    }

    private class NoteDiffCallback : DiffUtil.ItemCallback<NoteWithTags>() {
        override fun areItemsTheSame(oldItem: NoteWithTags, newItem: NoteWithTags): Boolean {
            return oldItem.note.id == newItem.note.id
        }

        override fun areContentsTheSame(oldItem: NoteWithTags, newItem: NoteWithTags): Boolean {
            return oldItem == newItem
        }
    }
}