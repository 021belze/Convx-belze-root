/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.ui.screens.wrapped

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.convx.music.R
import com.convx.music.ui.screens.wrapped.pages.ConclusionPage
import com.convx.music.ui.screens.wrapped.pages.PlaylistPage
import com.convx.music.ui.screens.wrapped.pages.WrappedIntro
import com.convx.music.ui.screens.wrapped.pages.WrappedMinutesScreen
import com.convx.music.ui.screens.wrapped.pages.WrappedMinutesTease
import com.convx.music.ui.screens.wrapped.pages.WrappedTop5AlbumsScreen
import com.convx.music.ui.screens.wrapped.pages.WrappedTop5ArtistsScreen
import com.convx.music.ui.screens.wrapped.pages.WrappedTop5SongsScreen
import com.convx.music.ui.screens.wrapped.pages.WrappedTopAlbumScreen
import com.convx.music.ui.screens.wrapped.pages.WrappedTopArtistScreen
import com.convx.music.ui.screens.wrapped.pages.WrappedTopSongScreen
import com.convx.music.ui.screens.wrapped.pages.WrappedTotalAlbumsScreen
import com.convx.music.ui.screens.wrapped.pages.WrappedTotalArtistsScreen
import com.convx.music.ui.screens.wrapped.pages.WrappedTotalSongsScreen
import com.convx.music.ui.screens.wrapped.components.WrappedBackground
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


sealed class WrappedScreenType {
    object Welcome : WrappedScreenType()
    object MinutesTease : WrappedScreenType()
    object MinutesReveal : WrappedScreenType()
    object TotalSongs : WrappedScreenType()
    object TopSongReveal : WrappedScreenType()
    object Top5Songs : WrappedScreenType()
    object TotalAlbums : WrappedScreenType()
    object TopAlbumReveal : WrappedScreenType()
    object Top5Albums : WrappedScreenType()
    object TotalArtists : WrappedScreenType()
    object TopArtistReveal : WrappedScreenType()
    object Top5Artists : WrappedScreenType()
    object Playlist : WrappedScreenType()
    object Conclusion : WrappedScreenType()
}

@Composable
fun WrappedScreen(navController: NavController) {
    val context = LocalContext.current
    val manager = remember { provideWrappedManager(context) }

    CompositionLocalProvider(LocalWrappedManager provides manager) {
        WrappedScreenContent(navController = navController)
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WrappedScreenContent(navController: NavController) {
    val onClose: () -> Unit = {
        navController.previousBackStackEntry?.savedStateHandle?.set("wrapped_seen", true)
        navController.popBackStack()
    }
    BackHandler(onBack = onClose)

    val messagePairSaver = Saver<MessagePair, List<Any>>(
        save = { listOf(it.range.first, it.range.last, it.tease, it.reveal) },
        restore = {
            MessagePair(
                range = (it[0] as Long)..(it[1] as Long),
                tease = it[2] as String,
                reveal = it[3] as String
            )
        }
    )
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val manager = LocalWrappedManager.current
    val audioService = remember { WrappedAudioService(view.context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(Unit) {
        val window = (view.context as android.app.Activity).window
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> audioService.pause()
                Lifecycle.Event.ON_RESUME -> audioService.resume()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
            lifecycleOwner.lifecycle.removeObserver(observer)
            audioService.release()
        }
    }

    val screens = remember {
        listOf(
            WrappedScreenType.Welcome,
            WrappedScreenType.MinutesTease,
            WrappedScreenType.MinutesReveal,
            WrappedScreenType.TotalSongs,
            WrappedScreenType.TopSongReveal,
            WrappedScreenType.Top5Songs,
            WrappedScreenType.TotalAlbums,
            WrappedScreenType.TopAlbumReveal,
            WrappedScreenType.Top5Albums,
            WrappedScreenType.TotalArtists,
            WrappedScreenType.TopArtistReveal,
            WrappedScreenType.Top5Artists,
            WrappedScreenType.Playlist,
            WrappedScreenType.Conclusion
        )
    }
    val pagerState = rememberPagerState(pageCount = { screens.size })
    val state by manager.state.collectAsState()
    val isMuted by audioService.isMuted.collectAsState()
    val messagePair = rememberSaveable(state.totalMinutes, saver = messagePairSaver) {
        WrappedRepository.getMessage(state.totalMinutes)
    }

    LaunchedEffect(Unit) {
        manager.prepare()
    }

    LaunchedEffect(pagerState, state.trackMap) {
        if (state.trackMap.isEmpty()) return@LaunchedEffect

        snapshotFlow { pagerState.currentPage }.distinctUntilChanged().collect { page ->
            val screen = screens.getOrNull(page)
            audioService.playTrack(state.trackMap[screen])
        }
    }


    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                // Segmented story progress indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    screens.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    when {
                                        index <= pagerState.currentPage -> Color.White
                                        else -> Color.White.copy(alpha = 0.28f)
                                    }
                                )
                        )
                    }
                }

                // Header controls row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.35f))
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.close),
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    val account = state.accountInfo
                    if (account != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            if (account.thumbnailUrl != null) {
                                AsyncImage(
                                    model = account.thumbnailUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(6.dp))
                            }
                            Text(
                                text = account.name,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else if (state.isAllTimeFallback) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.Black.copy(alpha = 0.35f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "All-Time",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Spacer(Modifier.width(36.dp))
                    }

                    IconButton(
                        onClick = { audioService.toggleMute() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.35f))
                    ) {
                        val icon = if (isMuted) R.drawable.volume_off else R.drawable.volume_up
                        Icon(
                            painter = painterResource(icon),
                            contentDescription = "Mute",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        WrappedBackground {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (screens[page]) {
                    is WrappedScreenType.Welcome -> WrappedIntro { scope.launch { pagerState.animateScrollToPage(page = 1) } }
                    is WrappedScreenType.MinutesTease -> WrappedMinutesTease(
                        messagePair = messagePair,
                        onNavigateForward = { scope.launch { pagerState.animateScrollToPage(page = 2) } },
                        isDataReady = state.isDataReady
                    )
                    is WrappedScreenType.MinutesReveal -> WrappedMinutesScreen(
                        messagePair = messagePair, totalMinutes = state.totalMinutes,
                        isVisible = pagerState.currentPage == screens.indexOf(WrappedScreenType.MinutesReveal)
                    )
                    is WrappedScreenType.TotalSongs -> WrappedTotalSongsScreen(
                        uniqueSongCount = state.uniqueSongCount,
                        isVisible = pagerState.currentPage == screens.indexOf(WrappedScreenType.TotalSongs)
                    )
                    is WrappedScreenType.TopSongReveal -> WrappedTopSongScreen(
                        topSong = state.topSongs.firstOrNull(),
                        isVisible = pagerState.currentPage == screens.indexOf(WrappedScreenType.TopSongReveal)
                    )
                    is WrappedScreenType.Top5Songs -> WrappedTop5SongsScreen(
                        topSongs = state.topSongs.take(5),
                        isVisible = pagerState.currentPage == screens.indexOf(WrappedScreenType.Top5Songs)
                    )
                    is WrappedScreenType.TotalAlbums -> WrappedTotalAlbumsScreen(
                        uniqueAlbumCount = state.totalAlbums,
                        isVisible = pagerState.currentPage == screens.indexOf(WrappedScreenType.TotalAlbums)
                    )
                    is WrappedScreenType.TopAlbumReveal -> WrappedTopAlbumScreen(
                        topAlbum = state.topAlbum,
                        isVisible = pagerState.currentPage == screens.indexOf(WrappedScreenType.TopAlbumReveal)
                    )
                    is WrappedScreenType.Top5Albums -> WrappedTop5AlbumsScreen(
                        topAlbums = state.top5Albums,
                        isVisible = pagerState.currentPage == screens.indexOf(WrappedScreenType.Top5Albums)
                    )
                    is WrappedScreenType.TotalArtists -> WrappedTotalArtistsScreen(
                        uniqueArtistCount = state.uniqueArtistCount,
                        isVisible = pagerState.currentPage == screens.indexOf(WrappedScreenType.TotalArtists)
                    )
                    is WrappedScreenType.TopArtistReveal -> WrappedTopArtistScreen(
                        topArtist = state.topArtists.firstOrNull(),
                        isVisible = pagerState.currentPage == screens.indexOf(WrappedScreenType.TopArtistReveal)
                    )
                    is WrappedScreenType.Top5Artists -> WrappedTop5ArtistsScreen(
                        topArtists = state.topArtists,
                        isVisible = pagerState.currentPage == screens.indexOf(WrappedScreenType.Top5Artists)
                    )
                    is WrappedScreenType.Playlist -> PlaylistPage()
                    is WrappedScreenType.Conclusion -> ConclusionPage(onClose = onClose)
                }
            }
        }
    }
}
