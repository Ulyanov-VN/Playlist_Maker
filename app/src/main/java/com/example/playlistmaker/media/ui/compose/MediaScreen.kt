package com.example.playlistmaker.media.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playlistmaker.R
import com.example.playlistmaker.media.ui.viewmodels.FavoriteTracksState
import com.example.playlistmaker.media.ui.viewmodels.FavoriteTracksViewModel
import com.example.playlistmaker.media.ui.viewmodels.PlaylistsState
import com.example.playlistmaker.media.ui.viewmodels.PlaylistsViewModel
import com.example.playlistmaker.playlist.domain.model.Playlist
import com.example.playlistmaker.search.domain.entity.Track
import com.example.playlistmaker.ui.compose.common.PlaylistGridItem
import com.example.playlistmaker.ui.compose.common.TrackListItem
import com.example.playlistmaker.ui.compose.common.playlistMakerPrimaryText
import com.example.playlistmaker.ui.compose.common.playlistMakerScreenBackground
import kotlinx.coroutines.launch

private val YsDisplayMedium = FontFamily(Font(R.font.ys_display_medium))

@Composable
fun MediaScreen(
    favoriteTracksViewModel: FavoriteTracksViewModel,
    playlistsViewModel: PlaylistsViewModel,
    onTrackClick: (Track) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
) {
    val favoriteState by favoriteTracksViewModel.state.observeAsState(FavoriteTracksState.Empty)
    val playlistsState by playlistsViewModel.state.observeAsState(PlaylistsState.Empty)

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val selectedTabIndex by remember { derivedStateOf { pagerState.currentPage } }

    val backgroundColor = playlistMakerScreenBackground()
    val titleColor = playlistMakerPrimaryText()

    val tabsBackground = if (isSystemInDarkTheme()) Color(0xFF1A1B22) else Color.White
    val tabTextColor = if (isSystemInDarkTheme()) Color.White else Color(0xFF1A1B22)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Text(
            text = stringResource(R.string.media),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 16.dp),
            textAlign = TextAlign.Start,
            color = titleColor,
            fontFamily = YsDisplayMedium,
            fontWeight = FontWeight.W500,
            fontSize = 22.sp
        )

        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth(),
            containerColor = tabsBackground,
            contentColor = tabTextColor,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = tabTextColor
                )
            },
            divider = {}
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = {
                    scope.launch { pagerState.animateScrollToPage(0) }
                },
                selectedContentColor = tabTextColor,
                unselectedContentColor = tabTextColor,
                text = {
                    Text(
                        text = stringResource(R.string.favorite_tracks),
                        fontFamily = YsDisplayMedium,
                        fontWeight = FontWeight.W500,
                        fontSize = 14.sp
                    )
                }
            )

            Tab(
                selected = selectedTabIndex == 1,
                onClick = {
                    scope.launch { pagerState.animateScrollToPage(1) }
                },
                selectedContentColor = tabTextColor,
                unselectedContentColor = tabTextColor,
                text = {
                    Text(
                        text = stringResource(R.string.Playlists),
                        fontFamily = YsDisplayMedium,
                        fontWeight = FontWeight.W500,
                        fontSize = 14.sp
                    )
                }
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> FavoriteTracksPage(
                    state = favoriteState,
                    onTrackClick = onTrackClick
                )

                1 -> PlaylistsPage(
                    state = playlistsState,
                    onCreatePlaylistClick = onCreatePlaylistClick,
                    onPlaylistClick = onPlaylistClick
                )
            }
        }
    }
}

@Composable
private fun FavoriteTracksPage(
    state: FavoriteTracksState,
    onTrackClick: (Track) -> Unit,
) {
    when (state) {
        is FavoriteTracksState.Empty -> {
            EmptyMediaBlock(
                text = stringResource(R.string.text_favorite_tracks)
            )
        }

        is FavoriteTracksState.Content -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
            ) {
                items(state.tracks, key = { it.trackId }) { track ->
                    TrackListItem(
                        track = track,
                        formatTrackTime = { millis ->
                            val totalSeconds = (millis ?: 0L) / 1000
                            val minutes = totalSeconds / 60
                            val seconds = totalSeconds % 60
                            String.format("%02d:%02d", minutes, seconds)
                        },
                        onClick = { onTrackClick(track) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistsPage(
    state: PlaylistsState,
    onCreatePlaylistClick: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = onCreatePlaylistClick,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSystemInDarkTheme()) Color.White else Color(0xFF1A1B22),
                    contentColor = if (isSystemInDarkTheme()) Color.Black else Color.White
                )
            ) {
                Text(
                    text = stringResource(R.string.new_playlist),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                    fontFamily = YsDisplayMedium,
                    fontWeight = FontWeight.W500,
                    fontSize = 14.sp
                )
            }
        }

        when (state) {
            is PlaylistsState.Empty -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyMediaBlock(
                        text = stringResource(R.string.text_playlist2)
                    )
                }
            }

            is PlaylistsState.Content -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 8.dp,
                        end = 8.dp,
                        top = 8.dp,
                        bottom = 16.dp
                    )
                ) {
                    items(state.playlists, key = { it.playlistId }) { playlist ->
                        PlaylistGridItem(
                            playlist = playlist,
                            onClick = { onPlaylistClick(playlist) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyMediaBlock(
    text: String,
) {
    val titleColor = playlistMakerPrimaryText()
    val smileRes = if (isSystemInDarkTheme()) R.drawable.smile_night else R.drawable.smile

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(smileRes),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.padding(top = 16.dp))

            Text(
                text = text,
                color = titleColor,
                textAlign = TextAlign.Center,
                fontFamily = YsDisplayMedium,
                fontWeight = FontWeight.W400,
                fontSize = 19.sp
            )
        }
    }
}