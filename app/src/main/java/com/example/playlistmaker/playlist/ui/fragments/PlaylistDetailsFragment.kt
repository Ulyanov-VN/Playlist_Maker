package com.example.playlistmaker.playlist.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistBinding
import com.example.playlistmaker.playlist.ui.adapters.PlaylistTracksAdapter
import com.example.playlistmaker.search.domain.entity.Track
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistDetailsFragment : Fragment(R.layout.fragment_playlist) {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: com.example.playlistmaker.playlist.ui.viewmodels.PlaylistDetailsViewModel by viewModel()

    private lateinit var tracksBottomSheet: BottomSheetBehavior<View>
    private lateinit var menuBottomSheet: BottomSheetBehavior<View>

    private lateinit var tracksAdapter: PlaylistTracksAdapter
    private var playlistId: Long = 0L

    // ✅ будем хранить название, чтобы показать его в диалоге удаления
    private var playlistName: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlaylistBinding.bind(view)

        playlistId = requireArguments().getLong("playlistId")

        setupBack()
        setupTracksBottomSheet()
        setupMenuBottomSheet()
        setupTracksList()
        setupButtons()
        observeState()
        observeDelete()

        viewModel.load(playlistId)
    }

    private fun setupBack() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (menuBottomSheet.state != BottomSheetBehavior.STATE_HIDDEN) {
                hideMenu()
            } else {
                findNavController().navigateUp()
            }
        }
    }

    private fun setupTracksBottomSheet() {
        tracksBottomSheet = BottomSheetBehavior.from(binding.tracksContainer)
        tracksBottomSheet.isHideable = false
        tracksBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun setupMenuBottomSheet() {
        menuBottomSheet = BottomSheetBehavior.from(binding.menuContainer)
        menuBottomSheet.isHideable = true
        menuBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN

        binding.menuOverlay.setOnClickListener { hideMenu() }

        menuBottomSheet.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                binding.menuOverlay.visibility =
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) View.GONE else View.VISIBLE
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }

    private fun setupTracksList() {
        tracksAdapter = PlaylistTracksAdapter(
            tracks = emptyList(),
            onClick = { track ->
                findNavController().navigate(
                    R.id.playerFragment,
                    bundleOf("track" to track)
                )
            },
            onLongClick = { track ->
                showDeleteTrackDialog(track)
            }
        )
        binding.rvTracks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTracks.adapter = tracksAdapter
    }

    private fun setupButtons() {
        binding.btnShare.setOnClickListener { sharePlaylist() }

        binding.btnMenu.setOnClickListener { showMenu() }

        binding.menuShare.setOnClickListener {
            hideMenu()
            sharePlaylist()
        }

        binding.menuEdit.setOnClickListener {
            hideMenu()
            findNavController().navigate(
                R.id.action_playlistDetails_to_editPlaylist,
                bundleOf("playlistId" to playlistId)
            )
        }

        binding.menuDelete.setOnClickListener {
            hideMenu()
            showDeletePlaylistDialog()
        }
    }

    private fun sharePlaylist() {
        val text = viewModel.buildShareText()
        if (text.isNullOrEmpty()) {
            Toast.makeText(
                requireContext(),
                "В этом плейлисте нет списка треков, которым можно поделиться",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share)))
    }

    private fun showMenu() {
        menuBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun hideMenu() {
        menuBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun showDeleteTrackDialog(track: Track) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage("Хотите удалить трек?")
            .setNegativeButton("НЕТ") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("ДА") { dialog, _ ->
                dialog.dismiss()
                viewModel.deleteTrackFromPlaylist(playlistId, track.trackId)
            }
            .show()
    }

    private fun showDeletePlaylistDialog() {
        val titleForDialog = playlistName.trim().ifEmpty { "плейлист" }

        MaterialAlertDialogBuilder(requireContext())
            .setMessage("Хотите удалить плейлист \"$titleForDialog\"?")
            .setNegativeButton("Нет") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Да") { dialog, _ ->
                dialog.dismiss()
                viewModel.deletePlaylist(playlistId)
            }
            .show()

    }

    private fun observeDelete() {
        viewModel.playlistDeleted.observe(viewLifecycleOwner) { deleted ->
            if (deleted == true) {
                findNavController().popBackStack(R.id.mediaFragment, false)
            }
        }
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { st ->
            if (st == null) return@observe

            // ✅ запоминаем актуальное название (нужно для диалога удаления)
            playlistName = st.name

            binding.tvTitle.text = st.name

            val desc = st.description?.trim().orEmpty()
            if (desc.isEmpty()) {
                binding.tvDescription.visibility = View.GONE
            } else {
                binding.tvDescription.visibility = View.VISIBLE
                binding.tvDescription.text = desc
            }

            binding.tvMeta.text = "${st.totalMinutesText} • ${st.trackCountText}"

            val placeholder = R.drawable.placeholder_dark
            if (!st.coverPath.isNullOrEmpty()) {
                Glide.with(binding.ivCover)
                    .load(st.coverPath)
                    .placeholder(placeholder)
                    .error(placeholder)
                    .centerCrop()
                    .into(binding.ivCover)

                Glide.with(binding.menuCover)
                    .load(st.coverPath)
                    .placeholder(placeholder)
                    .error(placeholder)
                    .centerCrop()
                    .into(binding.menuCover)
            } else {
                binding.ivCover.setImageResource(placeholder)
                binding.menuCover.setImageResource(placeholder)
            }

            binding.menuTitle.text = st.name
            binding.menuCount.text = st.trackCountText

            tracksAdapter.submitList(st.tracks)
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.VISIBLE
        _binding = null
        super.onDestroyView()
    }
}
