package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class VisibilityPrefs(
    val showCountdown: Boolean = true,
    val showGif: Boolean = true,
    val showTitle: Boolean = true,
)

@Composable
fun SettingsDialog(
    prefs: VisibilityPrefs,
    onPrefsChange: (VisibilityPrefs) -> Unit,
    onReAuth: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SettingRow("Countdown",     prefs.showCountdown) { onPrefsChange(prefs.copy(showCountdown = it)) }
                SettingRow("Meeting title", prefs.showTitle)     { onPrefsChange(prefs.copy(showTitle = it)) }
                SettingRow("GIF",           prefs.showGif)       { onPrefsChange(prefs.copy(showGif = it)) }
                Spacer(Modifier.height(4.dp))
                OutlinedButton(onClick = { onDismiss(); onReAuth() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Re-authenticate with Google")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}

@Composable
private fun SettingRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
