package com.example.playlistmaker.player.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlayerWithBottomSheetBinding
import com.example.playlistmaker.player.ui.viewmodels.PlayerState
import com.example.playlistmaker.player.ui.viewmodels.PlayerStatus
import com.example.playlistmaker.player.ui.viewmodels.PlayerViewModel
import com.example.playlistmaker.player.ui.views.PlaybackButtonView
import com.example.playlistmaker.playlist.ui.adapters.PlaylistBottomSheetAdapter
import com.example.playlistmaker.search.domain.entity.Track
import com.example.playlistmaker.utils.CustomToast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayerFragment : Fragment(R.layout.fragment_player_with_bottom_sheet) {

    private val viewModel: PlayerViewModel by viewModel()
    private var _binding: FragmentPlayerWithBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var playlistsAdapter: PlaylistBottomSheetAdapter
    private lateinit var playbackButton: PlaybackButtonView
    private var currentTrack: Track? = null

    // Флаг для отслеживания, нужно ли синхронизировать состояние кнопки
    private var shouldSyncButtonState = true

    // Views from bottom sheet
    private lateinit var bottomSheetContainer: View
    private lateinit var overlay: View
    private lateinit var playlistsRecyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var newPlaylistButton: android.widget.Button
    private lateinit var dragHandle: View
    private lateinit var bottomSheetTitle: android.widget.TextView
    private lateinit var bottomSheetDivider: View

    // Для обработки свайпа
    private var isDraggingBottomSheet = false
    private var startY = 0f
    private var bottomSheetInitialY = 0f
    private var currentAnimator: Animator? = null
    private var isClosingProgrammatically = false
    private var isOpeningProgrammatically = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentPlayerWithBottomSheetBinding.bind(view)

        // Инициализация PlaybackButtonView
        playbackButton = binding.playbackButton

        val track: Track? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("track", Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("track")
        }

        if (track == null) {
            findNavController().popBackStack()
            return
        }

        currentTrack = track
        viewModel.initialize(track)

        setupBackHandling()
        bindTrackData(track)
        setupPlaybackButton()
        setupFavoriteButton()
        setupAddToPlaylistButton()
        setupBottomSheet(view)
        observePlayerState()
        observeAddToPlaylistStatus()
        observeAddedToPlaylistIds()
        observePlaylists()
    }

    private fun setupBackHandling() {
        binding.backButton.setOnClickListener {
            viewModel.pausePlayback()
            findNavController().popBackStack()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Если Bottom Sheet открыт - закрываем его
                    if (::bottomSheetBehavior.isInitialized &&
                        bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
                        hideBottomSheet()
                    } else {
                        viewModel.pausePlayback()
                        findNavController().popBackStack()
                    }
                }
            }
        )
    }

    private fun observePlayerState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            updateFavoriteButton(state.isFavorite)

            when (state.status) {
                PlayerStatus.PLAYING -> {
                    // Синхронизируем кнопку ТОЛЬКО если изменение пришло не от нажатия
                    if (shouldSyncButtonState) {
                        playbackButton.setState(PlaybackButtonView.State.PAUSE)
                    }
                    updateCurrentTime(state.currentPosition)
                    playbackButton.isEnabled = true
                    shouldSyncButtonState = true
                }
                PlayerStatus.PAUSED -> {
                    if (shouldSyncButtonState) {
                        playbackButton.setState(PlaybackButtonView.State.PLAY)
                    }
                    updateCurrentTime(state.currentPosition)
                    playbackButton.isEnabled = true
                    shouldSyncButtonState = true
                }
                PlayerStatus.STOPPED -> {
                    playbackButton.setState(PlaybackButtonView.State.PLAY)
                    updateCurrentTime(0)
                    playbackButton.isEnabled = true
                }
                PlayerStatus.PREPARED -> {
                    binding.durationValue.text = state.trackDuration
                    updateCurrentTime(0)
                    playbackButton.setState(PlaybackButtonView.State.PLAY)
                    playbackButton.isEnabled = true
                }
                PlayerStatus.ERROR -> {
                    playbackButton.isEnabled = false
                }
            }
        }
    }

    private fun bindTrackData(track: Track) {
        val isDarkTheme = isDarkTheme()
        val placeholderResId =
            if (isDarkTheme) R.drawable.placeholder_dark else R.drawable.placeholder_light

        val coverUrl = viewModel.getCoverArtwork(track.artworkUrl100)

        Glide.with(this)
            .load(coverUrl)
            .placeholder(placeholderResId)
            .error(placeholderResId)
            .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.corner_radius)))
            .into(binding.albumArt)

        binding.trackName.text =
            track.trackName ?: getString(R.string.unknown_track)
        binding.artistName.text =
            track.artistName ?: getString(R.string.unknown_artist)
        binding.albumValue.text =
            track.collectionName ?: getString(R.string.unknown_album)

        val releaseYear = viewModel.getReleaseYear(track.releaseDate)
        binding.yearValue.text =
            releaseYear ?: getString(R.string.unknown_year)

        binding.genreValue.text =
            track.primaryGenreName ?: getString(R.string.unknown_genre)

        val countryName = viewModel.getCountryName(track.country)
        binding.countryValue.text = countryName
    }

    private fun setupPlaybackButton() {
        playbackButton.setOnPlaybackClickListener {
            // Здесь НЕ вызываем togglePlayPause, потому что кнопка уже сменила состояние
            // Просто передаем команду плееру с текущим состоянием кнопки
            shouldSyncButtonState = false // Не синхронизировать кнопку при следующем обновлении
            val isPlaying = playbackButton.getState() == PlaybackButtonView.State.PAUSE
            if (isPlaying) {
                viewModel.startPlayback()
            } else {
                viewModel.pausePlayback()
            }
        }
    }

    private fun setupFavoriteButton() {
        binding.favoriteButton.setOnClickListener {
            viewModel.onFavoriteClicked()
        }
    }

    private fun setupAddToPlaylistButton() {
        binding.addToPlaylistButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.loadPlaylists()
                val playlists = viewModel.playlists.value ?: emptyList()

                if (playlists.isEmpty()) {
                    findNavController().navigate(R.id.action_player_to_create_playlist)
                } else {
                    showBottomSheet()
                }
            }
        }
    }

    private fun setupBottomSheet(rootView: View) {
        // Находим все элементы
        bottomSheetContainer = rootView.findViewById(R.id.bottomSheetContainer)
        overlay = rootView.findViewById(R.id.overlay)

        // Находим элементы внутри Bottom Sheet
        playlistsRecyclerView = bottomSheetContainer.findViewById(R.id.playlistsRecyclerView)
        newPlaylistButton = bottomSheetContainer.findViewById(R.id.newPlaylistButton)
        dragHandle = bottomSheetContainer.findViewById(R.id.dragHandle)
        bottomSheetTitle = bottomSheetContainer.findViewById(R.id.title)
        bottomSheetDivider = bottomSheetContainer.findViewById(R.id.divider)

        // Настройка RecyclerView
        playlistsAdapter = PlaylistBottomSheetAdapter(emptyList()) { playlist ->
            currentTrack?.let { track ->
                viewModel.addTrackToPlaylist(playlist)
                hideBottomSheet()
            }
        }

        playlistsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        playlistsRecyclerView.adapter = playlistsAdapter

        // Получаем высоту экрана
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val halfScreenHeight = screenHeight / 2

        // Устанавливаем начальную позицию - за пределами экрана снизу
        bottomSheetContainer.translationY = screenHeight.toFloat()
        bottomSheetInitialY = (screenHeight - halfScreenHeight).toFloat()

        // Настройка Bottom Sheet Behavior
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer).apply {
            state = BottomSheetBehavior.STATE_HIDDEN

            isFitToContents = false
            isHideable = true
            skipCollapsed = true

            val layoutParams = bottomSheetContainer.layoutParams
            layoutParams.height = halfScreenHeight
            bottomSheetContainer.layoutParams = layoutParams

            peekHeight = 0

            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_HIDDEN -> {
                            if (!isClosingProgrammatically) {
                                overlay.visibility = View.GONE
                                overlay.isClickable = false
                                bottomSheet.translationY = screenHeight.toFloat()
                            }
                            isClosingProgrammatically = false
                        }
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            if (!isOpeningProgrammatically) {
                                overlay.visibility = View.VISIBLE
                                overlay.isClickable = true
                                overlay.alpha = 1f
                                bottomSheet.translationY = bottomSheetInitialY
                            }
                            isOpeningProgrammatically = false
                        }
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            state = BottomSheetBehavior.STATE_EXPANDED
                        }
                        else -> { }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    if (isClosingProgrammatically || isOpeningProgrammatically) return

                    if (slideOffset > 0.5f) {
                        bottomSheet.translationY = bottomSheetInitialY
                        return
                    }

                    val newY = screenHeight - (slideOffset + 1f) * halfScreenHeight
                    bottomSheet.translationY = newY.coerceAtMost(bottomSheetInitialY)

                    val alpha = (slideOffset + 1f) / 2f
                    overlay.alpha = alpha.coerceIn(0f, 1f)
                }
            })
        }

        dragHandle.setOnTouchListener { v, event ->
            handleBottomSheetSwipe(event)
        }

        bottomSheetTitle.setOnTouchListener { v, event ->
            handleBottomSheetSwipe(event)
        }

        newPlaylistButton.setOnClickListener {
            hideBottomSheet()
            findNavController().navigate(R.id.action_player_to_create_playlist)
        }

        overlay.setOnClickListener {
            hideBottomSheet()
        }

        bottomSheetContainer.setOnTouchListener { v, event ->
            if (event.y < 100.dpToPx()) {
                return@setOnTouchListener handleBottomSheetSwipe(event)
            }
            false
        }
    }

    private fun handleBottomSheetSwipe(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startY = event.rawY
                isDraggingBottomSheet = false
                currentAnimator?.cancel()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val currentY = event.rawY
                val deltaY = currentY - startY

                if (deltaY > 20 && !isDraggingBottomSheet &&
                    bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    isDraggingBottomSheet = true
                }

                if (isDraggingBottomSheet && deltaY > 0) {
                    val maxDrag = 400.dpToPx().toFloat()
                    val progress = deltaY.coerceIn(0f, maxDrag) / maxDrag

                    val newY = bottomSheetInitialY + deltaY
                    bottomSheetContainer.translationY = newY

                    overlay.alpha = 1 - progress
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val deltaY = event.rawY - startY

                if (deltaY > 150.dpToPx() || isDraggingBottomSheet) {
                    hideBottomSheetWithAnimation(deltaY)
                } else {
                    resetBottomSheetPosition()
                }

                isDraggingBottomSheet = false
            }
        }
        return true
    }

    private fun hideBottomSheetWithAnimation(deltaY: Float) {
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()

        currentAnimator?.cancel()

        isClosingProgrammatically = true

        val bottomSheetAnimator = ObjectAnimator.ofFloat(
            bottomSheetContainer,
            "translationY",
            bottomSheetContainer.translationY,
            screenHeight
        ).apply {
            duration = 250L
            interpolator = AccelerateInterpolator()
        }

        val overlayAnimator = ObjectAnimator.ofFloat(overlay, "alpha", overlay.alpha, 0f).apply {
            duration = 250L
            interpolator = AccelerateInterpolator()
        }

        currentAnimator = bottomSheetAnimator
        bottomSheetAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                currentAnimator = null
            }

            override fun onAnimationCancel(animation: Animator) {
                currentAnimator = null
            }
        })

        bottomSheetAnimator.start()
        overlayAnimator.start()
    }

    private fun resetBottomSheetPosition() {
        currentAnimator?.cancel()

        currentAnimator = ObjectAnimator.ofFloat(
            bottomSheetContainer,
            "translationY",
            bottomSheetContainer.translationY,
            bottomSheetInitialY
        ).apply {
            duration = 200L
            interpolator = AccelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    currentAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    currentAnimator = null
                }
            })
            start()
        }

        overlay.animate()
            .alpha(1f)
            .setDuration(200L)
            .start()
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun showBottomSheet() {
        viewModel.loadPlaylists()

        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
            val displayMetrics = resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            val halfScreenHeight = screenHeight / 2

            currentAnimator?.cancel()

            isOpeningProgrammatically = true

            val layoutParams = bottomSheetContainer.layoutParams
            layoutParams.height = halfScreenHeight
            bottomSheetContainer.layoutParams = layoutParams

            bottomSheetInitialY = (screenHeight - halfScreenHeight).toFloat()

            overlay.visibility = View.VISIBLE
            overlay.isClickable = true
            overlay.alpha = 0f

            currentAnimator = ObjectAnimator.ofFloat(
                bottomSheetContainer,
                "translationY",
                screenHeight.toFloat(),
                bottomSheetInitialY
            ).apply {
                duration = 300L
                interpolator = AccelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        currentAnimator = null
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        currentAnimator = null
                    }
                })
                start()
            }

            overlay.animate()
                .alpha(1f)
                .setDuration(300L)
                .start()
        }
    }

    private fun hideBottomSheet() {
        currentAnimator?.cancel()

        val screenHeight = resources.displayMetrics.heightPixels.toFloat()

        isClosingProgrammatically = true

        currentAnimator = ObjectAnimator.ofFloat(
            bottomSheetContainer,
            "translationY",
            bottomSheetContainer.translationY,
            screenHeight
        ).apply {
            duration = 250L
            interpolator = AccelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    overlay.visibility = View.GONE
                    currentAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    currentAnimator = null
                }
            })
            start()
        }

        overlay.animate()
            .alpha(0f)
            .setDuration(250L)
            .start()
    }

    private fun observePlaylists() {
        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            playlistsAdapter.updatePlaylists(playlists)

            if (playlists.isNotEmpty()) {
                bottomSheetDivider.visibility = View.VISIBLE
                playlistsRecyclerView.visibility = View.VISIBLE
                bottomSheetTitle.text = getString(R.string.add_to_playlist)
            } else {
                bottomSheetDivider.visibility = View.GONE
                playlistsRecyclerView.visibility = View.GONE
                bottomSheetTitle.text = getString(R.string.no_playlists_yet)
            }
        }
    }

    private fun observeAddToPlaylistStatus() {
        viewModel.addToPlaylistStatus.observe(viewLifecycleOwner) { status ->
            status?.let {
                when (it) {
                    is com.example.playlistmaker.player.ui.viewmodels.AddToPlaylistStatus.Success -> {
                        CustomToast.showTrackAddedToPlaylist(
                            context = requireContext(),
                            playlistName = it.playlistName,
                            duration = Toast.LENGTH_SHORT
                        )
                    }
                    is com.example.playlistmaker.player.ui.viewmodels.AddToPlaylistStatus.AlreadyExists -> {
                        CustomToast.showTrackAlreadyInPlaylist(
                            context = requireContext(),
                            playlistName = it.playlistName,
                            duration = Toast.LENGTH_SHORT
                        )
                    }
                    is com.example.playlistmaker.player.ui.viewmodels.AddToPlaylistStatus.Error -> {
                        CustomToast.show(
                            context = requireContext(),
                            message = "Ошибка: ${it.message}",
                            type = com.example.playlistmaker.utils.CustomToast.ToastType.WARNING,
                            duration = Toast.LENGTH_SHORT
                        )
                    }
                }
                viewModel.clearAddToPlaylistStatus()
            }
        }
    }

    private fun observeAddedToPlaylistIds() {
        viewModel.addedToPlaylistIds.observe(viewLifecycleOwner) { addedIds ->
            val isAdded = addedIds.contains(currentTrack?.trackId)
            updateAddToPlaylistButton(isAdded)
        }
    }

    private fun updateAddToPlaylistButton(isAdded: Boolean) {
        if (isAdded) {
            binding.addToPlaylistButton.setImageResource(R.drawable.ic_add_to_playlist)
        } else {
            val isDarkTheme = isDarkTheme()
            val playlistIcon = if (isDarkTheme) R.drawable.ic_playlist_night else R.drawable.ic_playlist
            binding.addToPlaylistButton.setImageResource(playlistIcon)
        }
    }

    private fun isDarkTheme(): Boolean {
        return resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        binding.favoriteButton.setImageResource(
            if (isFavorite) R.drawable.ic_favorite_border_add else R.drawable.ic_favorite_border
        )
    }

    private fun updateCurrentTime(currentPosition: Int) {
        binding.trackDuration.text =
            viewModel.formatTime(currentPosition.toLong())
    }

    override fun onPause() {
        super.onPause()
        viewModel.pausePlayback()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentAnimator?.cancel()
        playbackButton.release()
        viewModel.releasePlayer()
        _binding = null
    }
}