package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import model.MeetingInfo
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeFormatterWithMinutes = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
private val timeFormatterHourOnly = DateTimeFormatter.ofPattern("h a").withZone(ZoneId.systemDefault())

private fun formatTime(instant: Instant): String {
    val minute = instant.atZone(ZoneId.systemDefault()).minute
    return if (minute == 0) timeFormatterHourOnly.format(instant) else timeFormatterWithMinutes.format(instant)
}
private val gifCorner = RoundedCornerShape(8.dp)
private val tinyPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)

@Composable
fun MeetingCard(
    meeting: MeetingInfo,
    followingMeeting: MeetingInfo?,
    tomorrowFirst: MeetingInfo?,
    tomorrowCount: Int,
    countdown: String,
    urgency: Int,
    skipLabel: String?,
    gifFile: File?,
    currentTheme: AppTheme,
    visibilityPrefs: VisibilityPrefs,
    onRefresh: () -> Unit,
    onPickGif: () -> Unit,
    onThemeSelected: (AppTheme) -> Unit,
    onVisibilityChange: (VisibilityPrefs) -> Unit,
    onSkipMeeting: () -> Unit,
    onSwitchToCompact: () -> Unit,
    onSwitchToFull: () -> Unit,
    onReAuth: () -> Unit,
) {
    val countdownColor = when (urgency) {
        2 -> Color(0xFFD32F2F)
        1 -> Color(0xFFF57C00)
        else -> MaterialTheme.colorScheme.primary
    }
    val startFormatted = formatTime(meeting.startTime)
    val endFormatted = formatTime(meeting.endTime)
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        SettingsDialog(
            prefs = visibilityPrefs,
            onPrefsChange = onVisibilityChange,
            onReAuth = onReAuth,
            onDismiss = { showSettings = false }
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        if (maxHeight < 200.dp) {
            CompactLayout(
                meeting, countdown, countdownColor,
                startFormatted, endFormatted,
                gifFile, visibilityPrefs, skipLabel,
                onPickGif, onSkipMeeting, onSwitchToFull,
            )
        } else {
            FullLayout(
                meeting, followingMeeting, tomorrowFirst, tomorrowCount,
                countdown, countdownColor,
                startFormatted, endFormatted,
                gifFile, currentTheme, visibilityPrefs, skipLabel,
                onRefresh, onPickGif, onThemeSelected, onSkipMeeting,
                onOpenSettings = { showSettings = true },
                onSwitchToCompact = onSwitchToCompact,
            )
        }
    }
}

@Composable
fun NoMeetingsCard(
    data: model.CalendarData,
    gifFile: File?,
    currentTheme: AppTheme,
    visibilityPrefs: VisibilityPrefs,
    onRefresh: () -> Unit,
    onPickGif: () -> Unit,
    onThemeSelected: (AppTheme) -> Unit,
    onVisibilityChange: (VisibilityPrefs) -> Unit,
    onSwitchToCompact: () -> Unit,
    onSwitchToFull: () -> Unit,
    onReAuth: () -> Unit,
) {
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        SettingsDialog(
            prefs = visibilityPrefs,
            onPrefsChange = onVisibilityChange,
            onReAuth = onReAuth,
            onDismiss = { showSettings = false }
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        if (maxHeight < 200.dp) {
            // Compact: message + GIF + expand button
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (visibilityPrefs.showGif) {
                        GifWidget(gifFile = gifFile, onPickGif = onPickGif, modifier = Modifier.size(48.dp))
                    }
                    Text(
                        "No more meetings today :-)",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                    )
                    TextButton(onClick = onSwitchToFull, contentPadding = tinyPadding) {
                        Text("⊞", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        } else {
            // Full: card with GIF, message, tomorrow info, and buttons
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                TextButton(
                    onClick = onSwitchToCompact,
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                    contentPadding = tinyPadding,
                ) { Text("▭", style = MaterialTheme.typography.labelSmall) }
                Card(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("No more meetings today :-)", style = MaterialTheme.typography.headlineSmall)
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = onRefresh, modifier = Modifier.weight(1f), contentPadding = tinyPadding) {
                                    Text("Refresh", textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
                                }
                                ThemeMenuButton(currentTheme = currentTheme, onThemeSelected = onThemeSelected, compact = false, modifier = Modifier.weight(1f))
                                TextButton(onClick = { showSettings = true }, modifier = Modifier.weight(1f), contentPadding = tinyPadding) {
                                    Text("Settings", textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                InfoPopupButton(label = "Tomorrow?", modifier = Modifier.weight(1f)) {
                                    if (data.tomorrowFirst != null) {
                                        Text(data.tomorrowFirst.title, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            "${formatTime(data.tomorrowFirst.startTime)} – ${formatTime(data.tomorrowFirst.endTime)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        if (data.tomorrowCount > 1) {
                                            Text("+${data.tomorrowCount - 1} more", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    } else {
                                        Text("Nothing scheduled tomorrow", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                        if (visibilityPrefs.showGif) {
                            GifWidget(gifFile = gifFile, onPickGif = onPickGif, modifier = Modifier.size(120.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactLayout(
    meeting: MeetingInfo,
    countdown: String,
    countdownColor: Color,
    startFormatted: String,
    endFormatted: String,
    gifFile: File?,
    prefs: VisibilityPrefs,
    skipLabel: String?,
    onPickGif: () -> Unit,
    onSkipMeeting: () -> Unit,
    onSwitchToFull: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (prefs.showGif) {
                GifWidget(gifFile = gifFile, onPickGif = onPickGif, modifier = Modifier.size(36.dp))
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                if (prefs.showTitle) {
                    Text(text = meeting.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (prefs.showCountdown) {
                        Text(text = countdown, style = MaterialTheme.typography.bodySmall, color = countdownColor, maxLines = 1, fontWeight = FontWeight.Bold)
                    }
                    Text(text = "$startFormatted – $endFormatted", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                val actionLink = meeting.streamLink ?: meeting.videoLink
                val actionLabel = if (meeting.streamLink != null) "Watch" else "Join"
                if (actionLink != null) {
                    Button(
                        onClick = { runCatching { Desktop.getDesktop().browse(URI(actionLink)) } },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) { Text(actionLabel, style = MaterialTheme.typography.labelSmall) }
                }
                if (skipLabel != null) {
                    TextButton(onClick = onSkipMeeting, contentPadding = tinyPadding) {
                        Text(skipLabel, style = MaterialTheme.typography.labelSmall)
                    }
                }
                TextButton(onClick = onSwitchToFull, contentPadding = tinyPadding) { Text("⊞", style = MaterialTheme.typography.labelSmall) }
            }
        }
    }
}

@Composable
private fun FullLayout(
    meeting: MeetingInfo,
    followingMeeting: MeetingInfo?,
    tomorrowFirst: MeetingInfo?,
    tomorrowCount: Int,
    countdown: String,
    countdownColor: Color,
    startFormatted: String,
    endFormatted: String,
    gifFile: File?,
    currentTheme: AppTheme,
    prefs: VisibilityPrefs,
    skipLabel: String?,
    onRefresh: () -> Unit,
    onPickGif: () -> Unit,
    onThemeSelected: (AppTheme) -> Unit,
    onSkipMeeting: () -> Unit,
    onOpenSettings: () -> Unit,
    onSwitchToCompact: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        TextButton(
            onClick = onSwitchToCompact,
            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
            contentPadding = tinyPadding
        ) { Text("▭", style = MaterialTheme.typography.labelSmall) }
        Card(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (prefs.showCountdown) {
                        Text(text = countdown, style = MaterialTheme.typography.headlineLarge, color = countdownColor, fontWeight = FontWeight.Bold)
                    }
                    if (prefs.showTitle) {
                        Text(text = meeting.title, style = MaterialTheme.typography.headlineSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    Text(text = "$startFormatted – $endFormatted", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        val actionLink = meeting.streamLink ?: meeting.videoLink
                        val actionLabel = if (meeting.streamLink != null) "Watch\nLive" else "Join\nMeeting"
                        if (actionLink != null) {
                            TextButton(
                                onClick = { runCatching { Desktop.getDesktop().browse(URI(actionLink)) } },
                                modifier = Modifier.weight(1f),
                                contentPadding = tinyPadding
                            ) { Text(actionLabel, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall) }
                        }
                        if (skipLabel != null) {
                            TextButton(
                                onClick = onSkipMeeting,
                                modifier = Modifier.weight(1f),
                                contentPadding = tinyPadding
                            ) { Text(skipLabel, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall) }
                        }
                        TextButton(onClick = onRefresh, modifier = Modifier.weight(1f), contentPadding = tinyPadding) {
                            Text("Refresh", textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
                        }
                        ThemeMenuButton(currentTheme = currentTheme, onThemeSelected = onThemeSelected, compact = false, modifier = Modifier.weight(1f))
                        TextButton(onClick = onOpenSettings, modifier = Modifier.weight(1f), contentPadding = tinyPadding) {
                            Text("Settings", textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        InfoPopupButton(
                            label = "What's\nnext?",
                            modifier = Modifier.weight(1f)
                        ) {
                            if (followingMeeting != null) {
                                Text(followingMeeting.title, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${formatTime(followingMeeting.startTime)} – ${formatTime(followingMeeting.endTime)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text("No more meetings today", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        InfoPopupButton(
                            label = "Tomorrow?",
                            modifier = Modifier.weight(1f)
                        ) {
                            if (tomorrowFirst != null) {
                                Text(tomorrowFirst.title, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${formatTime(tomorrowFirst.startTime)} – ${formatTime(tomorrowFirst.endTime)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (tomorrowCount > 1) {
                                    Text(
                                        "+${tomorrowCount - 1} more",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Text("Nothing scheduled tomorrow", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                if (prefs.showGif) {
                    GifWidget(gifFile = gifFile, onPickGif = onPickGif, modifier = Modifier.size(120.dp))
                }
            }
        }
    }
}

/** A button that opens a dismissible popup showing arbitrary content. */
@Composable
private fun InfoPopupButton(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        TextButton(onClick = { expanded = true }, contentPadding = tinyPadding, modifier = Modifier.fillMaxWidth()) {
            Text(label, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                content()
            }
        }
    }
}

/** A button that opens a dropdown of all available themes. */
@Composable
private fun ThemeMenuButton(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        TextButton(
            onClick = { expanded = true },
            contentPadding = tinyPadding,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (compact) "◑" else "Theme", textAlign = TextAlign.Center, style = if (compact) LocalTextStyle.current else MaterialTheme.typography.labelSmall)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            appThemes.forEach { theme ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (theme.name == currentTheme.name) "✓ ${theme.name}" else "   ${theme.name}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        onThemeSelected(theme)
                        expanded = false
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun GifWidget(gifFile: File?, onPickGif: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(gifCorner)
            .clickable { onPickGif() },
        contentAlignment = Alignment.Center
    ) {
        if (gifFile != null && gifFile.exists()) {
            AnimatedGif(file = gifFile, modifier = Modifier.fillMaxSize())
        } else {
            Text(text = "+", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}
