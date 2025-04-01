package com.example.android.lappanotes.ui.notes

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ViewSwitcher
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.lappanotes.R
import com.example.android.lappanotes.ui.adapter.NoteAdapter
import com.example.android.lappanotes.viewmodel.NoteViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

class ListPageFragment : Fragment() {
    private lateinit var viewModel: NoteViewModel
    private lateinit var adapter: NoteAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewSwitcher: ViewSwitcher
    private lateinit var expandButton: ImageButton
    private var isExpanded = false
    private var currentTags = emptyList<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        isExpanded = false

        val view = inflater.inflate(R.layout.list_page_fragment, container, false)
        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]

        // Настройка RecyclerView
        adapter =
            NoteAdapter(
                onClick = { noteWithTags -> navigateToEdit(noteWithTags.note.id) },
                onLongClick = { noteWithTags -> showDeleteDialog(noteWithTags.note.id) },
            )
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Подписка на данные
        viewModel.setSearchQuery(null)

        viewModel.filteredNotes.observe(viewLifecycleOwner) { notes ->
            adapter.submitList(notes)
        }

        viewModel.searchQuery.observe(viewLifecycleOwner) { query ->
            val clearButton = view?.findViewById<ImageButton>(R.id.btnClearFilter)
            clearButton?.visibility = if (query.isNullOrBlank()) View.GONE else View.VISIBLE
            setupChipGroup(currentTags, query)
        }

        viewModel.allTags.observe(viewLifecycleOwner) { tags ->
            currentTags = tags
            setupChipGroup(tags, viewModel.searchQuery.value)
        }

        val clearButton = view.findViewById<ImageButton>(R.id.btnClearFilter)
        clearButton.setOnClickListener {
            viewModel.setSearchQuery(null)
            setupChipGroup(currentTags, viewModel.searchQuery.value)
        }

        val fabAdd: FloatingActionButton = view.findViewById(R.id.fabAdd)
        fabAdd.setOnClickListener { navigateToEdit(0) }

        // Настройка панели тегов
        viewSwitcher = view.findViewById(R.id.tagContainer)
        expandButton = view.findViewById(R.id.btnExpand)

        expandButton.setOnClickListener {
            isExpanded = !isExpanded
            viewSwitcher.showNext()
            expandButton.setImageResource(
                if (isExpanded) R.drawable.ic_star else R.drawable.ic_star,
            )
            setupChipGroup(currentTags, viewModel.searchQuery.value)
        }

        return view
    }

    private fun navigateToEdit(noteId: Int) {
        findNavController().navigate(
            R.id.action_notes_to_addNote,
            bundleOf("noteId" to noteId),
        )
    }

    private fun showDeleteDialog(noteId: Int) {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.delete_confirmation)
            .setPositiveButton("DELETE!!!") { _, _ ->
                lifecycleScope.launch {
                    viewModel.deleteNoteById(noteId)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    @SuppressLint("SetTextI18n")
    private fun setupChipGroup(
        tags: List<String>,
        activeTag: String?,
    ) {
        val activeChipGroup =
            if (isExpanded) {
                viewSwitcher.findViewById<ChipGroup>(R.id.chipGroupVertical)
            } else {
                viewSwitcher.findViewById<ChipGroup>(R.id.chipGroupHorizontal)
            }
        val inactiveChipGroup =
            if (!isExpanded) {
                viewSwitcher.findViewById<ChipGroup>(R.id.chipGroupVertical)
            } else {
                viewSwitcher.findViewById<ChipGroup>(R.id.chipGroupHorizontal)
            }

        activeChipGroup.removeAllViews()
        inactiveChipGroup.removeAllViews()

        tags.forEach { tag ->
            Chip(requireContext()).apply {
                text = "#$tag"
                isClickable = true
                setOnClickListener { viewModel.setSearchQuery(tag) }

                val colors =
                    listOf(
                        R.color.dark_pink,
                        R.color.light_pink,
                        R.color.dark_green,
                        R.color.light_green,
                    )
                val colorRes = colors[tag.hashCode().absoluteValue % colors.size]
                val color = ContextCompat.getColor(context, colorRes)
                chipBackgroundColor = ColorStateList.valueOf(color)

                val typedValue = TypedValue()
                context.theme.resolveAttribute(
                    com.google.android.material.R.attr.colorOnSurface,
                    typedValue,
                    true,
                )
                val defaultTextColor = typedValue.data

                val isActive = tag == activeTag
                val textColor =
                    if (isActive) {
                        ContextCompat.getColor(context, R.color.orange)
                    } else {
                        defaultTextColor
                    }
                setTextColor(textColor)

                activeChipGroup.addView(this)
            }
        }
    }
}
