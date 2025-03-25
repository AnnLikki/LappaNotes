package com.example.android.lappanotes.ui.notes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.android.lappanotes.R
import com.example.android.lappanotes.data.database.entity.Note
import com.example.android.lappanotes.data.database.entity.NoteWithTags
import com.example.android.lappanotes.databinding.EditPageFragmentBinding
import com.example.android.lappanotes.viewmodel.NoteViewModel
import kotlinx.coroutines.launch

class EditPageFragment : Fragment() {

    private lateinit var viewModel: NoteViewModel
    private var backBinding: EditPageFragmentBinding? = null
    private val binding get() = backBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        backBinding = EditPageFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        // Adapt layout to keyboard
        ViewCompat.setOnApplyWindowInsetsListener(binding.notePanel) { notePanel, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val keyboardHeight = imeInsets.bottom - systemBars.bottom

            notePanel.setPadding(
                notePanel.paddingLeft,
                notePanel.paddingTop,
                notePanel.paddingRight,
                keyboardHeight.coerceAtLeast(0)
            )

            insets
        }

        // Expand etText inside scrollView
        binding.scrollView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.scrollView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val parentHeight = binding.notePanel.height - binding.notePanel.marginTop - binding.notePanel.marginBottom
                val tagContainerHeight = 0
                val availableHeight = parentHeight - tagContainerHeight

                binding.etText.minHeight = availableHeight
            }
        })

        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]

        arguments?.getInt("noteId")?.let { noteId ->
            // Log.d("AddNote.noteId", "Note ID: $noteId")
            lifecycleScope.launch {
                // Extract note
                val note =
                    when (val tmp = viewModel.getNoteWithTagsById(noteId)) {
                        null -> NoteWithTags(Note(text = ""), listOf())
                        else -> tmp
                    }
                Log.d("AAAAA", note.tags.joinToString(", ") { it.tag })

                // Setup page
                binding.etText.setText(note.note.text)
                binding.etTags.setText(note.tags.joinToString(", ") { it.tag })

            binding.button.setOnClickListener {
                val text = binding.etText.text.toString()

                val tags = binding.etTags.text.split(",").map { it.trim() }

                if (text.isNotBlank()) {
                    val editedNote = if (noteId == 0) {
                        Note(text = text)
                    } else {
                        Note(id = noteId, text = text)
                    }
                    Log.d("TAGS!!!!!!!!", tags.joinToString(", "))
                    lifecycleScope.launch {
                        viewModel.insertNoteWithTags(editedNote, tags)
                        findNavController().navigateUp()
                    }
                } else {
                    Toast.makeText(context, R.string.empty_text_error, Toast.LENGTH_SHORT)
                        .show()
                }
            }

            binding.button2.setOnClickListener {
                lifecycleScope.launch {
                    viewModel.deleteNoteById(noteId)
                    findNavController().navigateUp()
                }
            }
            }
        }
    }
}