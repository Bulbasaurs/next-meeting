import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ui.App

fun main() = application {
    val state = rememberWindowState(width = 560.dp, height = 400.dp)
    Window(
        onCloseRequest = ::exitApplication,
        title = "Next Meeting",
        state = state
    ) {
        App(
            onSwitchToCompact = { state.size = DpSize(500.dp, 76.dp) },
            onSwitchToFull = { state.size = DpSize(560.dp, 420.dp) },
        )
    }
}
