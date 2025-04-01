package com.example.android.lappanotes.ui.adapter

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.lappanotes.R
import com.example.android.lappanotes.data.database.entity.NoteWithTags
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.absoluteValue

class NoteAdapter(
    private val onClick: (NoteWithTags) -> Unit,
    private val onLongClick: (NoteWithTags) -> Unit,
) : ListAdapter<NoteWithTags, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {
    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvText: TextView = itemView.findViewById(R.id.tvText)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val tvTags: TextView = itemView.findViewById(R.id.tvTags)

        fun bind(noteWithTags: NoteWithTags) {
            val dateFormat =
                when (Locale.getDefault().language) {
                    "ru" -> SimpleDateFormat("d MMM yyyy 'в' HH:mm", Locale.getDefault())
                    else -> SimpleDateFormat("MMM d, yyyy 'at' HH:mm", Locale.getDefault())
                }
            tvText.text = noteWithTags.note.text
            tvTimestamp.text = dateFormat.format(noteWithTags.note.timestamp)

            val tags = noteWithTags.tags.map { it.tag }
            val colors =
                listOf(
                    R.color.dark_pink,
                    R.color.light_pink,
                    R.color.dark_green,
                    R.color.light_green,
                )

            val spannable =
                SpannableStringBuilder().apply {
                    tags.forEachIndexed { index, tag ->
                        val colorRes = colors[tag.hashCode().absoluteValue % colors.size]
                        val color = ContextCompat.getColor(itemView.context, colorRes)

                        val start = length
                        append("#")
                        append(tag)
                        setSpan(ForegroundColorSpan(color), start, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                        if (index < tags.lastIndex) {
                            append(" ")
                        }
                    }
                }

            tvTags.text = spannable
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): NoteViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: NoteViewHolder,
        position: Int,
    ) {
        val noteWithTags = getItem(position)
        holder.bind(noteWithTags)
        holder.itemView.setOnClickListener { onClick(noteWithTags) }
        holder.itemView.setOnLongClickListener {
            onLongClick(noteWithTags)
            true
        }
    }

    private class NoteDiffCallback : DiffUtil.ItemCallback<NoteWithTags>() {
        override fun areItemsTheSame(
            oldItem: NoteWithTags,
            newItem: NoteWithTags,
        ): Boolean {
            return oldItem.note.id == newItem.note.id
        }

        override fun areContentsTheSame(
            oldItem: NoteWithTags,
            newItem: NoteWithTags,
        ): Boolean {
            return oldItem == newItem
        }
    }
}
