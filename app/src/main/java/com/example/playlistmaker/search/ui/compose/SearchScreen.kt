package com.example.playlistmaker.search.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playlistmaker.R
import com.example.playlistmaker.search.domain.entity.Track
import com.example.playlistmaker.search.ui.viewmodels.SearchUiState
import com.example.playlistmaker.ui.compose.common.TrackListItem
import com.example.playlistmaker.ui.compose.common.playlistMakerPrimaryText
import com.example.playlistmaker.ui.compose.common.playlistMakerScreenBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

private val YsDisplayMedium = FontFamily(Font(R.font.ys_display_medium))
private val YsDisplayRegular = FontFamily(Font(R.font.ys_display_regular))

@Composable
fun SearchScreen(
    stateFlow: StateFlow<SearchUiState>,
    history: List<Track>,
    formatTrackTime: (Long?) -> String,
    onSearch: (String) -> Unit,
    onRetry: () -> Unit,
    onClearSearchState: () -> Unit,
    onClearHistory: () -> Unit,
    onTrackClick: (Track) -> Unit,
) {
    val uiState by stateFlow.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }

    val currentOnSearch by rememberUpdatedState(onSearch)
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val backgroundColor = playlistMakerScreenBackground()
    val titleColor = playlistMakerPrimaryText()

    LaunchedEffect(query) {
        if (query.isNotBlank()) {
            delay(2000L)
            currentOnSearch(query)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Text(
            text = stringResource(R.string.search),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 24.dp),
            textAlign = TextAlign.Start,
            color = titleColor,
            fontFamily = YsDisplayMedium,
            fontWeight = FontWeight.W500,
            fontSize = 22.sp
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(36.dp)
                .background(
                    color = Color(0xFFE6E8EB),
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.search_2),
                    contentDescription = null,
                    tint = Color.Black
                )

                BasicTextField(
                    value = query,
                    onValueChange = { newValue ->
                        query = newValue
                        if (newValue.isBlank()) {
                            onClearSearchState()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontFamily = YsDisplayRegular,
                        fontWeight = FontWeight.W400,
                        fontSize = 16.sp
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (query.isNotBlank()) {
                                onSearch(query)
                            }
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    ),
                    cursorBrush = SolidColor(Color(0xFF3772E7)),
                    decorationBox = { innerTextField ->
                        if (query.isEmpty()) {
                            Text(
                                text = stringResource(R.string.search),
                                color = if (isSystemInDarkTheme()) Color(0xFF1A1B22) else Color(0xFFAEAFB4),
                                fontFamily = YsDisplayRegular,
                                fontWeight = FontWeight.W400,
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                )

                if (query.isNotBlank()) {
                    IconButton(
                        onClick = {
                            query = ""
                            onClearSearchState()
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_clear),
                            contentDescription = stringResource(R.string.clear),
                            tint = Color.Black
                        )
                    }
                }
            }
        }

        when {
            query.isBlank() && history.isNotEmpty() -> {
                HistoryBlock(
                    history = history,
                    formatTrackTime = formatTrackTime,
                    onTrackClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onTrackClick(it)
                    },
                    onClearHistory = onClearHistory
                )
            }

            query.isBlank() -> {
                Box(modifier = Modifier.fillMaxSize())
            }

            uiState is SearchUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF3772E7))
                }
            }

            uiState is SearchUiState.Success -> {
                val tracks = (uiState as SearchUiState.Success).tracks
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(tracks, key = { it.trackId }) { track ->
                        TrackListItem(
                            track = track,
                            formatTrackTime = formatTrackTime,
                            onClick = { onTrackClick(track) }
                        )
                    }
                }
            }

            uiState is SearchUiState.NoResults -> {
                SearchPlaceholder(
                    imageRes = if (isSystemInDarkTheme()) R.drawable.smile_night else R.drawable.smile,
                    text = stringResource(R.string.search_nothing)
                )
            }

            uiState is SearchUiState.Error -> {
                ErrorPlaceholder(onRetry = onRetry)
            }

            else -> {
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun HistoryBlock(
    history: List<Track>,
    formatTrackTime: (Long?) -> String,
    onTrackClick: (Track) -> Unit,
    onClearHistory: () -> Unit,
) {
    val titleColor = playlistMakerPrimaryText()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.history_title),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                textAlign = TextAlign.Center,
                color = titleColor,
                fontFamily = YsDisplayMedium,
                fontWeight = FontWeight.W500,
                fontSize = 19.sp
            )
        }

        items(history, key = { it.trackId }) { track ->
            TrackListItem(
                track = track,
                formatTrackTime = formatTrackTime,
                onClick = { onTrackClick(track) }
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onClearHistory,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSystemInDarkTheme()) Color.White else Color(0xFF1A1B22),
                        contentColor = if (isSystemInDarkTheme()) Color.Black else Color.White
                    )
                ) {
                    Text(
                        text = stringResource(R.string.clear_history),
                        fontFamily = YsDisplayMedium,
                        fontWeight = FontWeight.W500,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchPlaceholder(
    imageRes: Int,
    text: String,
) {
    val textColor = playlistMakerPrimaryText()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = null
            )

            Text(
                text = text,
                modifier = Modifier.padding(top = 16.dp),
                color = textColor,
                textAlign = TextAlign.Center,
                fontFamily = YsDisplayMedium,
                fontWeight = FontWeight.W400,
                fontSize = 19.sp
            )
        }
    }
}

@Composable
private fun ErrorPlaceholder(
    onRetry: () -> Unit,
) {
    val textColor = playlistMakerPrimaryText()
    val imageRes = if (isSystemInDarkTheme()) {
        R.drawable.errorsinternet_dark
    } else {
        R.drawable.errorsinternet
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = null
            )

            Text(
                text = stringResource(R.string.error_text),
                modifier = Modifier.padding(top = 12.dp),
                color = textColor,
                textAlign = TextAlign.Center,
                fontFamily = YsDisplayMedium,
                fontWeight = FontWeight.W400,
                fontSize = 19.sp
            )

            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text(
                    text = stringResource(R.string.refresh_button),
                    fontFamily = YsDisplayMedium,
                    fontWeight = FontWeight.W500,
                    fontSize = 14.sp
                )
            }
        }
    }
}