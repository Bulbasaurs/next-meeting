package ui

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font

private fun fontFamilyFromResource(resourcePath: String): FontFamily {
    val bytes = Thread.currentThread().contextClassLoader
        ?.getResourceAsStream(resourcePath)?.readBytes()
        ?: return FontFamily.Default
    return runCatching { FontFamily(Font(resourcePath, bytes)) }.getOrElse { FontFamily.Default }
}

val ComicSansFamily:     FontFamily by lazy { fontFamilyFromResource("fonts/ComicSansMS.ttf") }
val TimesNewRomanFamily: FontFamily by lazy { fontFamilyFromResource("fonts/TimesNewRoman.ttf") }
val NoteworthyFamily:       FontFamily by lazy { fontFamilyFromResource("fonts/Noteworthy.ttc") }
val SnellRoundhandFamily:   FontFamily by lazy { fontFamilyFromResource("fonts/SnellRoundhand.ttc") }
val IansuiFamily:           FontFamily by lazy { fontFamilyFromResource("fonts/Iansui-Regular.ttf") }
val AlagardFamily:       FontFamily by lazy { fontFamilyFromResource("fonts/alagard.ttf") }

fun buildTypography(fontFamily: FontFamily): Typography {
    val base = Typography()
    return Typography(
        displayLarge   = base.displayLarge.copy(fontFamily = fontFamily),
        displayMedium  = base.displayMedium.copy(fontFamily = fontFamily),
        displaySmall   = base.displaySmall.copy(fontFamily = fontFamily),
        headlineLarge  = base.headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = base.headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall  = base.headlineSmall.copy(fontFamily = fontFamily),
        titleLarge     = base.titleLarge.copy(fontFamily = fontFamily),
        titleMedium    = base.titleMedium.copy(fontFamily = fontFamily),
        titleSmall     = base.titleSmall.copy(fontFamily = fontFamily),
        bodyLarge      = base.bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium     = base.bodyMedium.copy(fontFamily = fontFamily),
        bodySmall      = base.bodySmall.copy(fontFamily = fontFamily),
        labelLarge     = base.labelLarge.copy(fontFamily = fontFamily),
        labelMedium    = base.labelMedium.copy(fontFamily = fontFamily),
        labelSmall     = base.labelSmall.copy(fontFamily = fontFamily),
    )
}
