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
    private var currentTrack: Track? = null

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
        setupPlayPauseButton()
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
                    updatePlayPauseButton(true)
                    updateCurrentTime(state.currentPosition)
                    binding.playPauseButton.isEnabled = true
                }
                PlayerStatus.PAUSED -> {
                    updatePlayPauseButton(false)
                    updateCurrentTime(state.currentPosition)
                    binding.playPauseButton.isEnabled = true
                }
                PlayerStatus.STOPPED -> {
                    updatePlayPauseButton(false)
                    updateCurrentTime(0)
                    binding.playPauseButton.isEnabled = true
                }
                PlayerStatus.PREPARED -> {
                    binding.durationValue.text = state.trackDuration
                    updateCurrentTime(0)
                    binding.playPauseButton.isEnabled = true
                }
                PlayerStatus.ERROR -> {
                    binding.playPauseButton.isEnabled = false
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

    private fun setupPlayPauseButton() {
        binding.playPauseButton.setOnClickListener {
            viewModel.togglePlayPause()
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
                    // Если плейлистов нет, сразу переходим к созданию
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

            // Минимальная конфигурация
            isFitToContents = false
            isHideable = true
            skipCollapsed = true

            // Устанавливаем фиксированную высоту
            val layoutParams = bottomSheetContainer.layoutParams
            layoutParams.height = halfScreenHeight
            bottomSheetContainer.layoutParams = layoutParams

            // Peek height = 0, чтобы не показывалось свернутым
            peekHeight = 0

            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_HIDDEN -> {
                            // Если скрываем программно, не делаем ничего (уже сделали в hideBottomSheet)
                            if (!isClosingProgrammatically) {
                                overlay.visibility = View.GONE
                                overlay.isClickable = false
                                // Возвращаем на начальную позицию (за экраном)
                                bottomSheet.translationY = screenHeight.toFloat()
                            }
                            isClosingProgrammatically = false
                        }
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            // Если открываем программно, не делаем ничего (уже сделали в showBottomSheet)
                            if (!isOpeningProgrammatically) {
                                overlay.visibility = View.VISIBLE
                                overlay.isClickable = true
                                overlay.alpha = 1f
                                // Устанавливаем позицию - половина экрана снизу
                                bottomSheet.translationY = bottomSheetInitialY
                            }
                            isOpeningProgrammatically = false
                        }
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            // Не должно происходить
                            state = BottomSheetBehavior.STATE_EXPANDED
                        }
                        else -> {
                            // Для других состояний
                        }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    // Игнорируем стандартные slide события при программном открытии/закрытии
                    if (isClosingProgrammatically || isOpeningProgrammatically) return

                    // Блокируем движение вверх (slideOffset > 0.5)
                    if (slideOffset > 0.5f) {
                        // Фиксируем на позиции halfScreenHeight
                        bottomSheet.translationY = bottomSheetInitialY
                        return
                    }

                    // Рассчитываем положение для анимации
                    val newY = screenHeight - (slideOffset + 1f) * halfScreenHeight
                    bottomSheet.translationY = newY.coerceAtMost(bottomSheetInitialY)

                    // Плавное изменение прозрачности overlay
                    val alpha = (slideOffset + 1f) / 2f // От 0.0 до 1.0
                    overlay.alpha = alpha.coerceIn(0f, 1f)
                }
            })
        }

        // Настройка drag handle для плавного закрытия
        dragHandle.setOnTouchListener { v, event ->
            handleBottomSheetSwipe(event)
        }

        // Настройка свайпа по заголовку
        bottomSheetTitle.setOnTouchListener { v, event ->
            handleBottomSheetSwipe(event)
        }

        // Кнопка "Новый плейлист"
        newPlaylistButton.setOnClickListener {
            hideBottomSheet()
            findNavController().navigate(R.id.action_player_to_create_playlist)
        }

        // Клик по overlay для закрытия
        overlay.setOnClickListener {
            hideBottomSheet()
        }

        // Настройка касания по самому bottom sheet для свайпа
        bottomSheetContainer.setOnTouchListener { v, event ->
            // Если касание в верхней части bottom sheet, обрабатываем свайп
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
                // Отменяем текущую анимацию если есть
                currentAnimator?.cancel()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val currentY = event.rawY
                val deltaY = currentY - startY

                // Если начали тянуть вниз и bottom sheet открыт
                if (deltaY > 20 && !isDraggingBottomSheet &&
                    bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    isDraggingBottomSheet = true
                }

                // Если тянем вниз
                if (isDraggingBottomSheet && deltaY > 0) {
                    // Плавно двигаем bottom sheet вниз пропорционально свайпу
                    val maxDrag = 400.dpToPx().toFloat()
                    val progress = deltaY.coerceIn(0f, maxDrag) / maxDrag

                    // Двигаем вниз от начальной позиции
                    val newY = bottomSheetInitialY + deltaY
                    bottomSheetContainer.translationY = newY

                    // Уменьшаем прозрачность overlay
                    overlay.alpha = 1 - progress
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val deltaY = event.rawY - startY

                // Если протащили достаточно далеко вниз
                if (deltaY > 150.dpToPx() || isDraggingBottomSheet) {
                    // Анимация закрытия
                    hideBottomSheetWithAnimation(deltaY)
                } else {
                    // Возвращаем на начальную позицию
                    resetBottomSheetPosition()
                }

                isDraggingBottomSheet = false
            }
        }
        return true
    }

    private fun hideBottomSheetWithAnimation(deltaY: Float) {
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()

        // Отменяем предыдущую анимацию
        currentAnimator?.cancel()

        // Устанавливаем флаг программного закрытия
        isClosingProgrammatically = true

        // Создаем анимацию для bottom sheet
        val bottomSheetAnimator = ObjectAnimator.ofFloat(
            bottomSheetContainer,
            "translationY",
            bottomSheetContainer.translationY,
            screenHeight
        ).apply {
            duration = 250L
            interpolator = AccelerateInterpolator()
        }

        // Создаем анимацию для overlay
        val overlayAnimator = ObjectAnimator.ofFloat(overlay, "alpha", overlay.alpha, 0f).apply {
            duration = 250L
            interpolator = AccelerateInterpolator()
        }

        // Комбинируем анимации
        currentAnimator = bottomSheetAnimator
        bottomSheetAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // После анимации меняем состояние BottomSheetBehavior
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                currentAnimator = null
            }

            override fun onAnimationCancel(animation: Animator) {
                currentAnimator = null
            }
        })

        // Запускаем анимации
        bottomSheetAnimator.start()
        overlayAnimator.start()
    }

    private fun resetBottomSheetPosition() {
        // Отменяем текущую анимацию
        currentAnimator?.cancel()

        // Анимация возврата на место
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

        // Проверяем, не открыт ли уже
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
            val displayMetrics = resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            val halfScreenHeight = screenHeight / 2

            // Отменяем текущую анимацию
            currentAnimator?.cancel()

            // Устанавливаем флаг программного открытия
            isOpeningProgrammatically = true

            // Устанавливаем высоту
            val layoutParams = bottomSheetContainer.layoutParams
            layoutParams.height = halfScreenHeight
            bottomSheetContainer.layoutParams = layoutParams

            // Обновляем начальную позицию
            bottomSheetInitialY = (screenHeight - halfScreenHeight).toFloat()

            // Показываем overlay сразу с прозрачностью 0
            overlay.visibility = View.VISIBLE
            overlay.isClickable = true
            overlay.alpha = 0f

            // Создаем анимацию появления снизу
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
                        // Перед началом анимации устанавливаем состояние EXPANDED
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

            // Анимация для overlay
            overlay.animate()
                .alpha(1f)
                .setDuration(300L)
                .start()
        }
    }

    private fun hideBottomSheet() {
        // Отменяем текущую анимацию
        currentAnimator?.cancel()

        val screenHeight = resources.displayMetrics.heightPixels.toFloat()

        // Устанавливаем флаг программного закрытия
        isClosingProgrammatically = true

        // Создаем плавную анимацию закрытия
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
                    // Только после завершения анимации меняем состояние
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

        // Анимация для overlay
        overlay.animate()
            .alpha(0f)
            .setDuration(250L)
            .start()
    }

    private fun observePlaylists() {
        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            playlistsAdapter.updatePlaylists(playlists)

            // Обновляем UI в зависимости от наличия плейлистов
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
            // Используем иконку с галочкой (добавлено)
            binding.addToPlaylistButton.setImageResource(R.drawable.ic_add_to_playlist)
        } else {
            // Используем обычную иконку плейлиста
            val isDarkTheme = isDarkTheme()
            val playlistIcon = if (isDarkTheme) R.drawable.ic_playlist_night else R.drawable.ic_playlist
            binding.addToPlaylistButton.setImageResource(playlistIcon)
        }
    }

    private fun updatePlayPauseButton(isPlaying: Boolean) {
        val isDarkTheme = isDarkTheme()
        val playIcon = if (isDarkTheme) R.drawable.play_night else R.drawable.play_day
        val pauseIcon = if (isDarkTheme) R.drawable.pause_night else R.drawable.pause_day

        binding.playPauseButton.setImageResource(
            if (isPlaying) pauseIcon else playIcon
        )
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
        // Отменяем все анимации
        currentAnimator?.cancel()
        viewModel.releasePlayer()
        _binding = null
    }
}