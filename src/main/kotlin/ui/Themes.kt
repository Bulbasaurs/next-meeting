package ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

data class AppTheme(val name: String, val colorScheme: ColorScheme, val fontFamily: FontFamily = FontFamily.Default)

val appThemes: List<AppTheme> by lazy {
    listOf(
        AppTheme(
            "Light",
            lightColorScheme(
                primary = Color(0xFF4F46E5),
                onPrimary = Color(0xFFFFFFFF),
                secondary = Color(0xFF6366F1),
                onSecondary = Color(0xFFFFFFFF),
                background = Color(0xFFF1F5F9),
                onBackground = Color(0xFF0F172A),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF0F172A),
                onSurfaceVariant = Color(0xFF475569),
                outline = Color(0xFF94A3B8),
                outlineVariant = Color(0xFFCBD5E1),
            ),
            FontFamily.Default
        ),
        AppTheme(
            "Dark",
            darkColorScheme(
                primary = Color(0xFFF97316),
                onPrimary = Color(0xFF1C1917),
                secondary = Color(0xFFFBBF24),
                onSecondary = Color(0xFF1C1917),
                background = Color(0xFF1C1917),
                onBackground = Color(0xFFF5F5F4),
                surface = Color(0xFF2C2825),
                onSurface = Color(0xFFF5F5F4),
                onSurfaceVariant = Color(0xFFA8A29E),
                outline = Color(0xFF78716C),
                outlineVariant = Color(0xFF44403C),
            ),
            FontFamily.SansSerif
        ),
        AppTheme(
            "Console",
            darkColorScheme(
                primary = Color(0xFF00FF41),
                onPrimary = Color(0xFF000000),
                secondary = Color(0xFF00CC33),
                onSecondary = Color(0xFF000000),
                background = Color(0xFF000000),
                onBackground = Color(0xFF00FF41),
                surface = Color(0xFF1C1C1C),
                onSurface = Color(0xFF00FF41),
                onSurfaceVariant = Color(0xFF00BB2D),
                outline = Color(0xFF006620),
                outlineVariant = Color(0xFF003310),
            ),
            FontFamily.Monospace
        ),
        AppTheme(
            "Outdoorsy",
            lightColorScheme(
                primary = Color(0xFF6B3A1F),
                onPrimary = Color(0xFFFFF8F0),
                secondary = Color(0xFF8B4513),
                onSecondary = Color(0xFFFFF8F0),
                background = Color(0xFF4A7C59),
                onBackground = Color(0xFF1A0D00),
                surface = Color(0xFFD4EDDA),
                onSurface = Color(0xFF2D1B00),
                onSurfaceVariant = Color(0xFF5C3317),
                outline = Color(0xFF8B4513),
                outlineVariant = Color(0xFFC4956A),
            ),
            TimesNewRomanFamily
        ),
        AppTheme(
            "Beach",
            lightColorScheme(
                primary = Color(0xFF0891B2),
                onPrimary = Color(0xFFFFFFFF),
                secondary = Color(0xFFF97316),
                onSecondary = Color(0xFFFFFFFF),
                background = Color(0xFFE0F2FE),
                onBackground = Color(0xFF0C4A6E),
                surface = Color(0xFFF0F9FF),
                onSurface = Color(0xFF0C4A6E),
                onSurfaceVariant = Color(0xFF0369A1),
                outline = Color(0xFF38BDF8),
                outlineVariant = Color(0xFFBAE6FD),
            ),
            ComicSansFamily
        ),
        AppTheme(
            "Midnight",
            darkColorScheme(
                primary = Color(0xFF9B8EC4),
                onPrimary = Color(0xFF1A1428),
                secondary = Color(0xFF7B6DAA),
                onSecondary = Color(0xFF1A1428),
                background = Color(0xFF0F0C1A),
                onBackground = Color(0xFFD4CCF0),
                surface = Color(0xFF27213D),
                onSurface = Color(0xFFD4CCF0),
                onSurfaceVariant = Color(0xFF9B8EC4),
                outline = Color(0xFF4A3F6E),
                outlineVariant = Color(0xFF2E2650),
            ),
            AlagardFamily
        ),
        AppTheme(
            "Lavender",
            lightColorScheme(
                primary = Color(0xFF8B5CF6),
                onPrimary = Color(0xFFFFFFFF),
                secondary = Color(0xFFEC4899),
                onSecondary = Color(0xFFFFFFFF),
                background = Color(0xFFF3F0FF),
                onBackground = Color(0xFF3B1D7E),
                surface = Color(0xFFFAFAFE),
                onSurface = Color(0xFF3B1D7E),
                onSurfaceVariant = Color(0xFF7C3AED),
                outline = Color(0xFFC4B5FD),
                outlineVariant = Color(0xFFEDE9FE),
            ),
            IansuiFamily
        ),
    )
}
