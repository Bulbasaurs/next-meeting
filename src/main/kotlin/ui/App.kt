package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import calendar.CalendarCliService
import model.CalendarData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.EventQueue
import java.awt.FileDialog
import java.io.File
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CountDownLatch
import kotlin.math.abs

private val savedGifFile        = File(System.getProperty("user.home"), ".next-meeting-gif-path")
private val savedThemeFile      = File(System.getProperty("user.home"), ".next-meeting-theme")
private val savedVisibilityFile = File(System.getProperty("user.home"), ".next-meeting-visibility")

private fun loadVisibilityPrefs(): VisibilityPrefs {
    if (!savedVisibilityFile.exists()) return VisibilityPrefs()
    val map = savedVisibilityFile.readLines()
        .mapNotNull { line -> line.split("=").takeIf { it.size == 2 }?.let { it[0] to it[1] } }
        .toMap()
    return VisibilityPrefs(
        showCountdown = map["countdown"] != "false",
        showGif       = map["gif"] != "false",
        showTitle     = map["title"] != "false",
    )
}

private fun saveVisibilityPrefs(prefs: VisibilityPrefs) {
    savedVisibilityFile.writeText(
        "countdown=${prefs.showCountdown}\ngif=${prefs.showGif}\ntitle=${prefs.showTitle}"
    )
}

sealed interface AppState {
    object Loading : AppState
    data class Error(val message: String) : AppState
    data class NoUpcomingMeetings(val data: CalendarData) : AppState
    data class MeetingFound(val data: CalendarData) : AppState
}

@Composable
fun App(onSwitchToCompact: () -> Unit = {}, onSwitchToFull: () -> Unit = {}) {
    var appState by remember { mutableStateOf<AppState>(AppState.Loading) }
    var loadingMessage by remember { mutableStateOf("Loading calendar...") }
    var authUrl by remember { mutableStateOf<String?>(null) }
    var countdown by remember { mutableStateOf("") }
    var urgency by remember { mutableStateOf(0) }
    var skipLabel by remember { mutableStateOf<String?>(null) }
    var skippedMeetingStart by remember { mutableStateOf<Instant?>(null) }
    val cliService = remember { CalendarCliService() }
    var gifFile by remember { mutableStateOf<File?>(null) }
    var currentTheme by remember {
        val saved = if (savedThemeFile.exists()) savedThemeFile.readText().trim() else ""
        mutableStateOf(appThemes.firstOrNull { it.name == saved } ?: appThemes[0])
    }
    var visibilityPrefs by remember { mutableStateOf(loadVisibilityPrefs()) }
    val coroutineScope = rememberCoroutineScope()

    fun fetchMeeting() {
        coroutineScope.launch(Dispatchers.IO) {
            appState = AppState.Loading
            runCatching {
                val data = cliService.fetchCalendarData()
                // If the skipped meeting is still "next", advance past it; otherwise clear the skip.
                val effectiveData = if (skippedMeetingStart != null && data.nextMeeting?.startTime == skippedMeetingStart) {
                    data.copy(nextMeeting = data.followingMeeting, followingMeeting = null)
                } else {
                    skippedMeetingStart = null
                    data
                }
                appState = if (effectiveData.nextMeeting != null) AppState.MeetingFound(effectiveData)
                           else AppState.NoUpcomingMeetings(effectiveData)
            }.onFailure {
                appState = AppState.Error(it.message ?: "Failed to fetch calendar")
            }
        }
    }

    fun pickGif() {
        coroutineScope.launch(Dispatchers.IO) {
            val file = openFilePicker("Select GIF") { it.lowercase().endsWith(".gif") }
                ?: return@launch
            gifFile = file
            savedGifFile.writeText(file.absolutePath)
        }
    }

    fun selectTheme(theme: AppTheme) {
        currentTheme = theme
        savedThemeFile.writeText(theme.name)
    }

    // On startup, restore GIF and fetch live calendar
    LaunchedEffect(Unit) {
        val savedGif = if (savedGifFile.exists()) File(savedGifFile.readText().trim()) else null
        if (savedGif != null && savedGif.exists()) gifFile = savedGif
        fetchMeeting()
    }

    // Auto-refresh every 5 minutes
    LaunchedEffect(Unit) {
        while (true) {
            delay(5 * 60 * 1000L)
            fetchMeeting()
        }
    }

    // Refresh when the current meeting ends so the next one loads immediately
    LaunchedEffect(appState) {
        val state = appState
        if (state is AppState.MeetingFound) {
            val endTime = state.data.nextMeeting?.endTime ?: return@LaunchedEffect
            val untilEnd = Duration.between(Instant.now(), endTime).toMillis()
            if (untilEnd > 0) {
                delay(untilEnd)
                fetchMeeting()
            }
        }
    }

    // Countdown timer + skip-button label
    LaunchedEffect(appState) {
        val state = appState
        if (state is AppState.MeetingFound) {
            val meeting = state.data.nextMeeting ?: return@LaunchedEffect
            while (true) {
                val seconds = Duration.between(Instant.now(), meeting.startTime).seconds
                countdown = formatCountdown(seconds)
                urgency = when {
                    seconds < 0 -> 0
                    seconds < 300 -> 2
                    seconds < 900 -> 1
                    else -> 0
                }
                // seconds is negative while meeting is in progress
                skipLabel = when {
                    seconds <= -600 -> "Finished early?"
                    seconds <= -300 -> "Meeting cancelled?"
                    else -> null
                }
                delay(1000L)
            }
        } else {
            skipLabel = null
        }
    }

    fun reAuthenticate() {
        coroutineScope.launch(Dispatchers.IO) {
            loadingMessage = "Waiting for browser login..."
            authUrl = null
            appState = AppState.Loading
            runCatching { cliService.runAuthLogin { url -> authUrl = url } }
            authUrl = null
            loadingMessage = "Loading calendar..."
            fetchMeeting()
        }
    }

    val onRefresh: () -> Unit = { fetchMeeting() }
    val onReAuth: () -> Unit = { reAuthenticate() }
    val onSkipMeeting: () -> Unit = {
        val state = appState
        if (state is AppState.MeetingFound) {
            skippedMeetingStart = state.data.nextMeeting?.startTime
            val newData = state.data.copy(nextMeeting = state.data.followingMeeting, followingMeeting = null)
            appState = if (newData.nextMeeting != null) AppState.MeetingFound(newData)
                       else AppState.NoUpcomingMeetings(newData)
        }
    }

    MaterialTheme(colorScheme = currentTheme.colorScheme, typography = buildTypography(currentTheme.fontFamily)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when (val state = appState) {
                is AppState.Loading -> LoadingView(loadingMessage, authUrl)
                is AppState.Error -> ErrorView(state.message, onRetry = onRefresh, onReAuth = onReAuth)
                is AppState.NoUpcomingMeetings -> NoMeetingsCard(
                    data = state.data,
                    gifFile = gifFile,
                    currentTheme = currentTheme,
                    visibilityPrefs = visibilityPrefs,
                    onRefresh = onRefresh,
                    onPickGif = { pickGif() },
                    onThemeSelected = { selectTheme(it) },
                    onVisibilityChange = { prefs ->
                        visibilityPrefs = prefs
                        saveVisibilityPrefs(prefs)
                    },
                    onSwitchToCompact = onSwitchToCompact,
                    onSwitchToFull = onSwitchToFull,
                    onReAuth = onReAuth,
                )
                is AppState.MeetingFound -> MeetingCard(
                    meeting = state.data.nextMeeting!!,
                    followingMeeting = state.data.followingMeeting,
                    tomorrowFirst = state.data.tomorrowFirst,
                    tomorrowCount = state.data.tomorrowCount,
                    countdown = countdown,
                    urgency = urgency,
                    skipLabel = skipLabel,
                    gifFile = gifFile,
                    currentTheme = currentTheme,
                    visibilityPrefs = visibilityPrefs,
                    onRefresh = onRefresh,
                    onPickGif = { pickGif() },
                    onThemeSelected = { selectTheme(it) },
                    onVisibilityChange = { prefs ->
                        visibilityPrefs = prefs
                        saveVisibilityPrefs(prefs)
                    },
                    onSkipMeeting = onSkipMeeting,
                    onSwitchToCompact = onSwitchToCompact,
                    onSwitchToFull = onSwitchToFull,
                    onReAuth = onReAuth,
                )
            }
        }
    }
}

private fun openFilePicker(title: String, filter: (String) -> Boolean): File? {
    var result: File? = null
    val latch = CountDownLatch(1)
    EventQueue.invokeLater {
        val dialog = FileDialog(null as java.awt.Frame?, title, FileDialog.LOAD)
        dialog.setFilenameFilter { _, name -> filter(name) }
        dialog.isVisible = true
        val dir = dialog.directory
        val file = dialog.file
        if (dir != null && file != null) result = File(dir, file)
        dialog.dispose()
        latch.countDown()
    }
    latch.await()
    return result
}

@Composable
private fun LoadingView(message: String = "Loading calendar...", authUrl: String? = null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            CircularProgressIndicator()
            Text(message)
            if (authUrl != null) {
                Text(
                    "If the browser didn't open, copy this URL:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                androidx.compose.foundation.text.selection.SelectionContainer {
                    Text(
                        authUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit, onReAuth: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text("Something went wrong", style = MaterialTheme.typography.titleLarge)
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRetry) { Text("Retry") }
                OutlinedButton(onClick = onReAuth) { Text("Re-authenticate") }
            }
        }
    }
}


private fun formatCountdown(totalSeconds: Long): String = when {
    totalSeconds < -60 -> "Started ${abs(totalSeconds) / 60}m ago"
    totalSeconds < 0 -> "Started just now"
    totalSeconds < 60 -> "Starting now!"
    totalSeconds < 3600 -> "in ${totalSeconds / 60}m ${totalSeconds % 60}s"
    else -> "in ${totalSeconds / 3600}h ${(totalSeconds % 3600) / 60}m"
}
