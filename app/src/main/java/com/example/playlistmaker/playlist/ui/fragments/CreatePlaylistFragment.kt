package com.example.playlistmaker.playlist.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentCreatePlaylistBinding
import com.example.playlistmaker.playlist.ui.viewmodels.CreatePlaylistState
import com.example.playlistmaker.playlist.ui.viewmodels.CreatePlaylistViewModel
import com.example.playlistmaker.utils.CustomToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream

class CreatePlaylistFragment : Fragment() {

    private var _binding: FragmentCreatePlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreatePlaylistViewModel by viewModel()

    private var selectedImageUri: Uri? = null
    private var imageFile: File? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(
                requireContext(),
                "Разрешение необходимо для выбора обложки",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                saveImageToAppStorage(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner
        ) {
            handleBackPress()
        }

        setupViews()
        setupListeners()
        observeViewModel()
    }

    private fun setupViews() {
        binding.createButton.isEnabled = false
        /*binding.createButton.alpha = 0.5f*/
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            handleBackPress()
        }

        binding.coverImage.setOnClickListener {
            checkPermissionsAndOpenImagePicker()
        }

        binding.nameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateName(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.descriptionEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateDescription(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.createButton.setOnClickListener {
            if (binding.createButton.isEnabled) {
                viewModel.createPlaylist()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.createButtonEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.createButton.isEnabled = enabled
            /*binding.createButton.alpha = if (enabled) 1.0f else 0.5f*/
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CreatePlaylistState.Initial -> {
                    // Ничего не делаем
                }
                is CreatePlaylistState.Creating -> {
                    binding.createButton.isEnabled = false
                    binding.createButton.text = getString(R.string.creating)
                }
                is CreatePlaylistState.Success -> {
                    binding.createButton.isEnabled = true
                    binding.createButton.text = getString(R.string.create)
                    showSuccessMessage(state.playlistName)

                    // Определяем, откуда пришел пользователь и куда возвращаться
                    val previousDestination = findNavController().previousBackStackEntry?.destination?.id

                    when (previousDestination) {
                        R.id.playerFragment -> {
                            // Пришли с аудиоплеера - возвращаемся на аудиоплеер
                            findNavController().popBackStack(R.id.playerFragment, false)
                        }
                        R.id.mediaFragment -> {
                            // Пришли с медиатеки - возвращаемся на медиатеку
                            findNavController().popBackStack(R.id.mediaFragment, false)
                        }
                        else -> {
                            // По умолчанию возвращаемся назад
                            findNavController().popBackStack()
                        }
                    }
                }
                is CreatePlaylistState.Error -> {
                    binding.createButton.isEnabled = true
                    binding.createButton.text = getString(R.string.create)
                    showErrorMessage(state.message)
                }
                else -> {
                    // Обработка других случаев
                }
            }
        }

        viewModel.hasUnsavedChanges.observe(viewLifecycleOwner) { hasChanges ->
            // Можно обновить UI, если нужно показать, что есть несохраненные изменения
        }
    }

    private fun checkPermissionsAndOpenImagePicker() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openImagePicker()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun openImagePicker() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Используем новый Photo Picker API для Android 13+
            Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                type = "image/*"
                putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, 1)
            }
        } else {
            // Для старых версий используем стандартный PICK
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
            }
        }

        pickImageLauncher.launch(intent)
    }

    private fun saveImageToAppStorage(uri: Uri) {
        lifecycleScope.launch {
            try {
                val context = requireContext()
                val contentResolver = context.contentResolver

                // Создаем файл во внутреннем хранилище приложения
                val fileName = "playlist_cover_${System.currentTimeMillis()}.jpg"
                val outputDir = context.filesDir
                val outputFile = File(outputDir, fileName)

                contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(outputFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }

                    // Сохраняем путь к файлу
                    imageFile = outputFile
                    viewModel.updateCoverImage(outputFile)

                    // Показываем изображение в UI
                    Glide.with(this@CreatePlaylistFragment)
                        .load(outputFile)
                        .placeholder(R.drawable.ic_add_photo)
                        .transform(RoundedCorners(16))
                        .into(binding.coverImage)
                } ?: run {
                    Toast.makeText(
                        context,
                        "Не удалось открыть изображение",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Ошибка при сохранении изображения",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun handleBackPress() {
        val hasUnsavedChanges = viewModel.hasUnsavedChanges.value ?: false

        if (hasUnsavedChanges) {
            showUnsavedChangesDialog()
        } else {
            findNavController().popBackStack()
        }
    }

    private fun showUnsavedChangesDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.finish_playlist_creation)
            .setMessage(R.string.unsaved_data_warning)
            .setPositiveButton(R.string.finish) { _, _ ->
                viewModel.resetUnsavedChanges()
                findNavController().popBackStack()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showSuccessMessage(playlistName: String) {
        CustomToast.showPlaylistCreated(
            context = requireContext(),
            playlistName = playlistName,
            duration = Toast.LENGTH_LONG
        )
    }

    private fun showErrorMessage(message: String) {
        CustomToast.show(
            context = requireContext(),
            message = message,
            type = CustomToast.ToastType.WARNING,
            duration = Toast.LENGTH_LONG
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}