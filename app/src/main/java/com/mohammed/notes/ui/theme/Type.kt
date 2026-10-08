package com.mohammed.notes.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Two voices on purpose:
 *  - [Sans] for chrome (app bar, buttons, chips, metadata) so controls stay neutral.
 *  - [Serif] for anything the user reads or writes (note cards, editor) which is easier
 *    to read at length and stops the app looking like stock Android.
 *
 * Serif also resolves to a proper naskh face for Arabic without bundling a font file.
 *
 * One tier per role, nothing in between: 26 display (screen hero) → 18 title (headings
 * and note titles) → 16 bodyLarge (primary reading) → 14.5 bodyMedium (secondary) →
 * 13/12/11 small text and labels. Every size is sp so the system font scale is
 * respected end to end; layout adapts around the text instead of capping it.
 */
private val Sans = FontFamily.Default
private val Serif = FontFamily.Serif

val Typography = Typography(
    displaySmall = TextStyle(
        fontFamily = Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.2).sp
    ),
    titleLarge = TextStyle(
        fontFamily = Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.1).sp
    ),
    titleMedium = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.5.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

/**
 * The same ladder with metrics that suit naskh: letter-spacing is zeroed so joined
 * Arabic letterforms are never pulled apart, and line boxes grow so ascenders,
 * descenders and diacritics — and mixed Arabic/English runs — keep their air.
 * [NotesTheme] applies this when the app locale is Arabic.
 */
val Typography.forArabic: Typography
    get() = copy(
        displaySmall = displaySmall.withArabicMetrics(),
        titleLarge = titleLarge.withArabicMetrics(),
        titleMedium = titleMedium.withArabicMetrics(),
        titleSmall = titleSmall.withArabicMetrics(),
        bodyLarge = bodyLarge.withArabicMetrics(),
        bodyMedium = bodyMedium.withArabicMetrics(),
        bodySmall = bodySmall.withArabicMetrics(),
        labelLarge = labelLarge.withArabicMetrics(),
        labelMedium = labelMedium.withArabicMetrics(),
        labelSmall = labelSmall.withArabicMetrics(),
    )

private const val ArabicLineHeightFactor = 1.12f

private fun TextStyle.withArabicMetrics() = copy(
    letterSpacing = 0.sp,
    lineHeight = lineHeight * ArabicLineHeightFactor
)
