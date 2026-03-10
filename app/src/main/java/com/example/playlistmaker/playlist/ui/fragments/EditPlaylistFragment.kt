package com.example.playlistmaker.playlist.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentCreatePlaylistBinding
import com.example.playlistmaker.playlist.ui.viewmodels.EditPlaylistViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream

class EditPlaylistFragment : Fragment(R.layout.fragment_create_playlist) {

    private var _binding: FragmentCreatePlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditPlaylistViewModel by viewModel()

    private var playlistId: Long = 0L

    private val pickImage = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@registerForActivityResult
        val savedPath = saveImageToPrivateStorage(uri)
        viewModel.updateCoverPath(savedPath)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCreatePlaylistBinding.bind(view)

        playlistId = requireArguments().getLong("playlistId")

        // тексты под редактирование
        binding.title.text = getString(R.string.edit_playlist_title)
        binding.createButton.text = getString(R.string.save)

        setupBack()
        setupInputs()
        setupCoverClick()
        observe()

        viewModel.load(playlistId)
    }

    private fun setupBack() {
        binding.backButton.setOnClickListener { findNavController().navigateUp() }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // просто выйти без сохранения
            findNavController().navigateUp()
        }
    }

    private fun setupInputs() {
        binding.nameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateName(s?.toString().orEmpty())
            }
        })

        binding.descriptionEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateDescription(s?.toString().orEmpty())
            }
        })

        binding.createButton.setOnClickListener {
            //сохраняем только кнопкой "Сохранить"
            viewModel.save()
        }
    }

    private fun setupCoverClick() {
        binding.coverImage.setOnClickListener {
            pickImage.launch("image/*")
        }
    }

    private fun observe() {
        viewModel.state.observe(viewLifecycleOwner) { st ->
            if (st == null) return@observe

            if (binding.nameEditText.text?.toString() != st.name) {
                binding.nameEditText.setText(st.name)
                binding.nameEditText.setSelection(st.name.length)
            }

            val desc = st.description.orEmpty()
            if (binding.descriptionEditText.text?.toString() != desc) {
                binding.descriptionEditText.setText(desc)
                binding.descriptionEditText.setSelection(desc.length)
            }

            binding.createButton.isEnabled = st.isSaveEnabled

            if (!st.coverPath.isNullOrEmpty()) {
                Glide.with(binding.coverImage)
                    .load(File(st.coverPath))
                    .centerCrop()
                    .into(binding.coverImage)
            } else {
                binding.coverImage.setImageResource(R.drawable.ic_add_photo2)
            }
        }

        viewModel.saved.observe(viewLifecycleOwner) { saved ->
            if (saved == true) {
                // закрываем редактор, возвращаемся на плейлист
                findNavController().navigateUp()
            }
        }
    }

    private fun saveImageToPrivateStorage(uri: Uri): String? {
        return try {
            val dir = File(requireContext().filesDir, "playlist_covers")
            if (!dir.exists()) dir.mkdirs()

            val file = File(dir, "cover_${System.currentTimeMillis()}.jpg")

            requireContext().contentResolver.openInputStream(uri).use { input ->
                FileOutputStream(file).use { output ->
                    if (input != null) input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
