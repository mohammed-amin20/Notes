package com.mohammed.notes.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * Two coordinated schemes on purpose:
 *  - Dark: mint #78E5D5 on slate #1B262E. Mint is a FILL that always carries near-black ink,
 *    and a separately tuned mint variant works as text or an icon on those surfaces.
 *  - Light: teal #167D86 on mist #F4F8F7 with white #FFFFFF cards, per the light design spec.
 *    Teal doubles as the light "primary" and as the accent (primary-as-icon), so it is tuned
 *    to reach 4.87:1 on white and 4.55:1 on the background in one value.
 * Neither scheme is ever handed to Material You dynamic extraction.
 *
 * Gold is decorative only — the brand mark and the launcher icon — because it fails AA as
 * text on the light surfaces (2.7:1 on white). Pins use `accent`, not gold: the spec asks
 * for a teal pin.
 */

// --- Brand fills ------------------------------------------------------------

val Mint = Color(0xFF78E5D5)
val MintInk = Color(0xFF14202A)

// Light-scheme primary fill. White on #167D86 measures 4.87:1, so it carries both the FAB
// and the selected chip's label.
val Teal = Color(0xFF167D86)
val OnTeal = Color(0xFFFFFFFF)

val MintContainerDark = Color(0xFF2E4F4C)
val OnMintContainerDark = Color(0xFFA8F0E6)
val MintContainerLight = Color(0xFFDDF5EF)
val OnMintContainerLight = Color(0xFF0B3D38)

// Mint tuned to stay legible as text or an icon (see ColorScheme.accent).
val MintTextDark = Color(0xFF78E5D5)
val MintTextLight = Teal

// Gold — decorative only: pins, launcher icon, brand mark. GoldTextLight is the one gold
// that is allowed near text, and only because it reaches 4.9:1 on white.
val Gold = Color(0xFFCBB981)
val GoldTextLight = Color(0xFF8A6D1F)
val GoldContainerDark = Color(0xFF4A4127)
val OnGoldContainerDark = Color(0xFFF2E7C4)
val GoldContainerLight = Color(0xFFF4E9C9)
val OnGoldContainerLight = Color(0xFF2B2200)

// --- Dark scheme: slate -----------------------------------------------------

val SlateBackground = Color(0xFF1B262E)
val SlateSurface = Color(0xFF253640)
val SlateSurfaceVariant = Color(0xFF314754)
val SlateOutline = Color(0xFF7A8D97)
val SlateOutlineVariant = Color(0xFF3E5563)
val SlateInk = Color(0xFFF3F6F7)
val SlateInkMuted = Color(0xFFB7C5CC)

val HarborContainerLowest = Color(0xFF141C22)
val HarborContainerLow = Color(0xFF1F2E37)
val HarborContainerHigh = Color(0xFF2B3F4A)
val HarborBright = Color(0xFF3A5162)
val HarborDim = Color(0xFF161F25)
val HarborInverseSurface = Color(0xFFE6ECEF)
val HarborInverseOnSurface = Color(0xFF1B262E)

// --- Light scheme: mist -----------------------------------------------------
//
// Spec tokens: bg #F4F8F7, card #FFFFFF, text #1B262E, secondary #627780, primary teal
// #167D86, search bg #E7F1EF, selected-note tint #DDF5EF, border #D8E7E4.
//
// `surfaceVariant` is the note-card role in both schemes, so it goes white here. That is
// *not* safe for the auth buttons' disabled container (they read this role directly) —
// those use `surfaceContainerHighest` instead, which is a no-op change in dark because
// both roles are SlateSurfaceVariant there.

val MistBackground = Color(0xFFF4F8F7)
val MistSurface = Color(0xFFFFFFFF)
val MistSurfaceVariant = Color(0xFFFFFFFF)
val MistOutline = Color(0xFF7E9691)
val MistOutlineVariant = Color(0xFFD8E7E4)
val MistInk = Color(0xFF1B262E)
val MistInkMuted = Color(0xFF627780)

val CloudContainerLowest = Color(0xFFFFFFFF)
val CloudContainerLow = Color(0xFFFAFCFC)
val CloudContainer = Color(0xFFF3F8F7)
val CloudContainerHigh = Color(0xFFE7F1EF)
val CloudContainerHighest = Color(0xFFDDEDEA)
val CloudDim = Color(0xFFE3EDEB)
val CloudInverseSurface = Color(0xFF253640)
val CloudInverseOnSurface = Color(0xFFF3F6F7)

// --- Errors -----------------------------------------------------------------

val ErrorLight = Color(0xFFB3261E)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFF9DEDC)
val OnErrorContainerLight = Color(0xFF410E0B)

val ErrorDark = Color(0xFFEF8F8F)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF4A2A2A)
val OnErrorContainerDark = Color(0xFFFFDAD6)
