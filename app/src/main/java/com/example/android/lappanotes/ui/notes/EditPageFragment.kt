package com.example.android.lappanotes.ui.notes

import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.core.content.ContextCompat
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
import kotlin.math.absoluteValue

class EditPageFragment : Fragment() {
    private lateinit var viewModel: NoteViewModel
    private var backBinding: EditPageFragmentBinding? = null
    private val binding get() = backBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        backBinding = EditPageFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        ViewCompat.setOnApplyWindowInsetsListener(binding.notePanel) { notePanel, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val keyboardHeight = imeInsets.bottom - systemBars.bottom

            notePanel.setPadding(
                notePanel.paddingLeft,
                notePanel.paddingTop,
                notePanel.paddingRight,
                keyboardHeight.coerceAtLeast(0),
            )

            insets
        }

        binding.scrollView.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.scrollView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    val parentHeight = binding.notePanel.height - binding.notePanel.marginTop - binding.notePanel.marginBottom
                    val tagContainerHeight = binding.etTags.height + binding.etTags.marginTop + binding.etTags.marginBottom
                    val availableHeight = parentHeight - tagContainerHeight

                    binding.etText.minHeight = availableHeight
                }
            },
        )

        binding.etTags.addTextChangedListener(
            object : TextWatcher {
                private var isFormatting = false
                private var prev = ""

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {}

                override fun afterTextChanged(s: Editable?) {
                    if (isFormatting) return
                    isFormatting = true

                    val select = binding.etTags.selectionEnd
                    val origLength = binding.etTags.length()

                    val input = s.toString()
                    val cleanInput = input.replace("#", "")
                    val splitList =
                        cleanInput.split(", ", ",")
                            .map { it.lowercase() }

                    val tags =
                        if (prev.split(", ", ",").count() < splitList.count()) {
                            splitList.filterIndexed { index, str ->
                                str.isNotEmpty() || index == splitList.lastIndex
                            }
                        } else {
                            splitList.filter { str ->
                                str.isNotEmpty()
                            }
                        }

                    val formatted = tags.joinToString(", ") { "#$it" }

                    prev = formatted
                    if (formatted != input) {
                        binding.etTags.setText(formatted)
                        val newPosition = select + formatted.length - origLength
                        binding.etTags.setSelection(newPosition.coerceIn(0, formatted.length))
                    }
                    applyTagColorSpans()
                    isFormatting = false
                }

                private fun applyTagColorSpans() {
                    val editable = binding.etTags.editableText ?: return
                    val colors =
                        listOf(
                            R.color.dark_pink,
                            R.color.light_pink,
                            R.color.dark_green,
                            R.color.light_green,
                        )

                    val existingSpans = editable.getSpans(0, editable.length, ForegroundColorSpan::class.java)
                    existingSpans.forEach { editable.removeSpan(it) }

                    var currentIndex = 0
                    while (currentIndex < editable.length) {
                        val commaIndex = editable.indexOf(',', currentIndex)
                        val endOfTag = if (commaIndex != -1) commaIndex else editable.length

                        val tag = editable.subSequence(currentIndex, endOfTag).toString().trim()
                        if (tag.isNotEmpty()) {
                            val tagName = tag.replace("#", "").trim()
                            val colorRes = colors[tagName.hashCode().absoluteValue % colors.size]
                            val color = ContextCompat.getColor(requireContext(), colorRes)

                            editable.setSpan(
                                ForegroundColorSpan(color),
                                currentIndex,
                                endOfTag,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
                            )
                        }

                        currentIndex = endOfTag + 1
                        while (currentIndex < editable.length && editable[currentIndex] == ' ') {
                            currentIndex++
                        }
                    }
                }
            },
        )

        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]

        arguments?.getInt("noteId")?.let { noteId ->
            lifecycleScope.launch {
                val note =
                    when (val tmp = viewModel.getNoteWithTagsById(noteId)) {
                        null -> NoteWithTags(Note(text = ""), listOf())
                        else -> tmp
                    }

                binding.etText.setText(note.note.text)
                binding.etTags.setText(note.tags.joinToString(", ") { it.tag })

                binding.button.setOnClickListener {
                    val text = binding.etText.text.toString()

                    val tags = binding.etTags.text.split(",").map { it.replace("#", "").trim() }

                    if (text.isNotBlank()) {
                        val editedNote =
                            if (noteId == 0) {
                                Note(text = text)
                            } else {
                                Note(id = noteId, text = text)
                            }
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
