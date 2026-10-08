package com.mohammed.notes.feature.settings.presentation
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.material.icons.filled.Lock
import com.mohammed.notes.feature.privacy.presentation.PrivacyLockController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohammed.notes.ui.theme.LocalThemeModeController
import com.mohammed.notes.ui.locale.LocalAppLocaleController
import com.mohammed.notes.ui.locale.currentAppLocale
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mohammed.notes.R
import com.mohammed.notes.ui.locale.AppLocale
import com.mohammed.notes.ui.theme.FormMeasure
import com.mohammed.notes.ui.theme.Motion
import com.mohammed.notes.ui.theme.NotesTheme
import com.mohammed.notes.ui.theme.Size
import com.mohammed.notes.ui.theme.ThemeMode
import com.mohammed.notes.ui.theme.Space
import com.mohammed.notes.ui.theme.accent
import com.mohammed.notes.ui.theme.rememberAnimationsEnabled



@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
    onGoToHiddenNotes: () -> Unit,
    onGoToChangePin: () -> Unit,
    viewModal: SettingsScreenViewModel = hiltViewModel()
) {
    val state by viewModal.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModal.loggedOut.collect { onLoggedOut() }
    }

    // Appearance and language are app-level concerns owned above the nav graph; the screen
    // only reads the controllers and reports taps. Account + logout stay in the ViewModel,
    // whose events drive the host-level navigation hand-off.
    val themeController = LocalThemeModeController.current
    val localeController = LocalAppLocaleController.current

    SettingsContent(
        state = state,
        onAction = viewModal::onAction,
        onBack = onBack,
        onGoToHiddenNotes = onGoToHiddenNotes,
        onGoToChangePin = onGoToChangePin,
        themeMode = themeController.mode,
        onThemeModeChange = themeController::set,
        locale = currentAppLocale(),
        onLocaleChange = localeController::set
    )
}

/**
 * Stateless so it previews without Hilt. Four sections on one scroll surface, the last
 * three in rounded section cards that reuse the note list's `surface` role (white on the
 * pale-mint background, slate on navy) against a `surfaceContainerLow` option tile.
 */
@Composable
fun SettingsContent(
    state: SettingsScreenState,
    onAction: (SettingsScreenAction) -> Unit,
    onBack: () -> Unit,
    onGoToHiddenNotes: () -> Unit,
    onGoToChangePin: () -> Unit,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    locale: String,
    onLocaleChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val versionName = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { SettingsTopBar(onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Below ~340dp wide (or at large accessibility text) the theme and language
            // choices stop sitting side by side and become full-width rows so labels and
            // badges stay readable.
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val fontScale = LocalDensity.current.fontScale
                val stacked = maxWidth < 340.dp || fontScale >= 1.3f
                Column(
                    modifier = Modifier
                        .widthIn(max = FormMeasure)
                        .fillMaxWidth()
                        .padding(horizontal = Space.lg, vertical = Space.md)
                        .verticalScroll(rememberScrollState())
                ) {
                    SettingsSection(stringResource(R.string.settings_appearance)) {
                        ThemeOptionGroup(
                            themeMode = themeMode,
                            onThemeModeChange = onThemeModeChange,
                            stacked = stacked
                        )
                    }

                    Spacer(Modifier.size(Space.xl))
                    SettingsSection(stringResource(R.string.settings_language)) {
                        LanguageOptionGroup(
                            locale = locale,
                            onLocaleChange = onLocaleChange,
                            stacked = stacked
                        )
                    }

                    Spacer(Modifier.size(Space.xl))
                    SettingsSection(stringResource(R.string.settings_privacy)) {
                        Column {
                            SettingsRow(
                                icon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) },
                                title = stringResource(R.string.privacy_hidden_notes),
                                onClick = onGoToHiddenNotes
                            )
                            SettingsRow(
                                icon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) },
                                title = stringResource(
                                    if (state.hasPin) R.string.privacy_change_pin else R.string.privacy_set_pin
                                ),
                                onClick = if (state.hasPin) onGoToChangePin else onGoToHiddenNotes
                            )
                        }
                    }

                    Spacer(Modifier.size(Space.xl))
                    SettingsSection(stringResource(R.string.settings_account)) {
                        AccountSectionContent(state = state, onAction = onAction)
                    }

                    Spacer(Modifier.size(Space.xl))
                    SettingsSection(stringResource(R.string.settings_about)) {
                        AboutSectionContent(
                            appName = stringResource(R.string.app_name),
                            versionName = versionName
                        )
                    }

                    Spacer(Modifier.size(Space.huge))
                    }
                }
            }
        }

    if (state.logoutDialogVisible) {
        AlertDialog(
            onDismissRequest = {
                onAction(SettingsScreenAction.OnLogoutDialogVisibleChange(false))
            },
            title = { Text(stringResource(R.string.logout_dialog_title)) },
            text = { Text(stringResource(R.string.logout_dialog_body)) },
            confirmButton = {
                TextButton(onClick = { onAction(SettingsScreenAction.OnLogoutConfirmed) }) {
                    Text(
                        text = stringResource(R.string.action_log_out),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onAction(SettingsScreenAction.OnLogoutDialogVisibleChange(false))
                }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

// --- Layout scaffolding ----------------------------------------------------

@Composable
private fun ColumnScope.SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = Space.xs)
    )
    Spacer(Modifier.size(Space.sm))
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(Size.hairline, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(Space.md)) { content() }
    }
}

@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = Space.xs, vertical = Space.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// --- Appearance ------------------------------------------------------------

private val ThemeModes = listOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM)

@Composable
private fun ColumnScope.ThemeOptionGroup(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    stacked: Boolean,
) {
    val labelRes = { mode: ThemeMode ->
        when (mode) {
            ThemeMode.LIGHT -> R.string.settings_theme_light
            ThemeMode.DARK -> R.string.settings_theme_dark
            ThemeMode.SYSTEM -> R.string.settings_theme_system
        }
    }

    if (stacked) {
        Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
            ThemeModes.forEach { mode ->
                ThemeOptionCard(
                    label = stringResource(labelRes(mode)),
                    selected = themeMode == mode,
                    onClick = { onThemeModeChange(mode) },
                    illustration = themeIllustration(mode),
                    stacked = true
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(Space.sm)
        ) {
            ThemeModes.forEach { mode ->
                ThemeOptionCard(
                    label = stringResource(labelRes(mode)),
                    selected = themeMode == mode,
                    onClick = { onThemeModeChange(mode) },
                    illustration = themeIllustration(mode),
                    stacked = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun themeIllustration(mode: ThemeMode): @Composable (Modifier) -> Unit = when (mode) {
    ThemeMode.LIGHT -> { modifier -> LightThemeIllustration(modifier) }
    ThemeMode.DARK -> { modifier -> DarkThemeIllustration(modifier) }
    ThemeMode.SYSTEM -> { modifier -> SystemThemeIllustration(modifier) }
}

@Composable
private fun ThemeOptionCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    illustration: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
    stacked: Boolean,
) {
    val animationsOn = rememberAnimationsEnabled()
    val shape = MaterialTheme.shapes.small
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = if (animationsOn) tween(Motion.enter) else snap(),
        label = "optionBorder"
    )
    val optionModifier = modifier
        .clip(shape)
        .background(MaterialTheme.colorScheme.surfaceContainerLow)
        .border(width = 2.dp, color = borderColor, shape = shape)
        .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)

    if (stacked) {
        Row(
            modifier = optionModifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = Size.touchTarget)
                .padding(horizontal = Space.md, vertical = Space.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(Size.logoSize)) {
                illustration(Modifier.fillMaxSize())
            }
            Spacer(Modifier.width(Space.md))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            CheckBadge(selected = selected, modifier = Modifier.size(Size.iconLg))
        }
    } else {
        Column(
            modifier = optionModifier.padding(Space.xs),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.fillMaxWidth().aspectRatio(1.15f)) {
                illustration(Modifier.fillMaxSize())
                CheckBadge(
                    selected = selected,
                    modifier = Modifier.align(Alignment.TopEnd).padding(Space.xs)
                )
            }
            Spacer(Modifier.height(Space.xs))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp),
                minLines = 2
            )
        }
    }
}

// --- Language --------------------------------------------------------------

private val LanguageTags = listOf(AppLocale.Arabic, AppLocale.English)

@Composable
private fun ColumnScope.LanguageOptionGroup(
    locale: String,
    onLocaleChange: (String) -> Unit,
    stacked: Boolean,
) {
    val labelRes = { tag: String ->
        if (tag == AppLocale.Arabic) R.string.settings_lang_arabic else R.string.settings_lang_english
    }

    if (stacked) {
        Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
            LanguageTags.forEach { tag ->
                LanguageOptionCard(
                    label = stringResource(labelRes(tag)),
                    selected = locale == tag,
                    onClick = { onLocaleChange(tag) }
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(Space.sm)
        ) {
            LanguageTags.forEach { tag ->
                LanguageOptionCard(
                    label = stringResource(labelRes(tag)),
                    selected = locale == tag,
                    onClick = { onLocaleChange(tag) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LanguageOptionCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val animationsOn = rememberAnimationsEnabled()
    val shape = MaterialTheme.shapes.small
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = if (animationsOn) tween(Motion.enter) else snap(),
        label = "langBorder"
    )

    Row(
        modifier = modifier
            .defaultMinSize(minHeight = Size.touchTarget)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(width = 2.dp, color = borderColor, shape = shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = Space.md, vertical = Space.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        CheckBadge(selected = selected, modifier = Modifier.size(Size.iconLg))
    }
}

// --- Shared selection affordance -------------------------------------------

@Composable
private fun CheckBadge(selected: Boolean, modifier: Modifier = Modifier) {
    val animationsOn = rememberAnimationsEnabled()
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.5f,
        animationSpec = if (animationsOn) tween(Motion.enter) else snap(),
        label = "badgeScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = if (animationsOn) tween(Motion.enter) else snap(),
        label = "badgeAlpha"
    )
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.primary)
            .graphicsLayer {
                this.scaleX = scale
                this.scaleY = scale
                this.alpha = alpha
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(Size.iconSm + 2.dp)
        )
    }
}

// --- Account and About -----------------------------------------------------

@Composable
private fun ColumnScope.AccountSectionContent(
    state: SettingsScreenState,
    onAction: (SettingsScreenAction) -> Unit,
) {
    if (state.username.isNotBlank()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Space.xs)
        ) {
            Text(
                text = state.username,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (state.email.isNotBlank()) {
                Spacer(Modifier.size(Space.xxs))
                Text(
                    text = state.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = Size.hairline
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = Size.touchTarget)
            .selectable(
                selected = false,
                role = Role.Button,
                onClick = { onAction(SettingsScreenAction.OnLogoutClick) }
            )
            .padding(vertical = Space.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(Size.iconLg)
        )
        Spacer(Modifier.width(Space.md))
        Text(
            text = stringResource(R.string.action_log_out),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun ColumnScope.AboutSectionContent(
    appName: String,
    versionName: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Space.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.memo_logo),
            contentDescription = null,
            modifier = Modifier.size(Size.logoSize),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.accent)
        )
        Spacer(Modifier.width(Space.md))
        Text(
            text = appName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (versionName.isNotBlank()) {
            Text(
                text = stringResource(R.string.settings_version, versionName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// --- Theme preview illustrations -------------------------------------------

private val PreviewLightBg = Color(0xFFF4F8F7)
private val PreviewLightCard = Color(0xFFFFFFFF)
private val PreviewLightBar = Color(0xFFD8E7E4)
private val PreviewAccentTeal = Color(0xFF167D86)
private val PreviewDarkBg = Color(0xFF1B262E)
private val PreviewDarkCard = Color(0xFF253640)
private val PreviewDarkBar = Color(0xFF3E5563)
private val PreviewAccentMint = Color(0xFF78E5D5)

private val PreviewBoxShape = RoundedCornerShape(10.dp)
private val PreviewCardShape = RoundedCornerShape(4.dp)
private val PreviewBarHeight = 3.dp
private val PreviewCardPadding = PaddingValues(horizontal = Space.sm, vertical = Space.xs)

/**
 * Fixed palette on purpose: each preview depicts one scheme regardless of the theme the
 * screen is currently rendered in, and both schemes use the same `Card`-on-`background`
 * composition as the app.
 */
@Composable
private fun SingleThemeIllustration(
    background: Color,
    card: Color,
    bar: Color,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.clip(PreviewBoxShape).background(background)) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.62f)
                .aspectRatio(1.5f)
                .clip(PreviewCardShape)
                .background(card)
                .padding(PreviewCardPadding),
            verticalArrangement = Arrangement.spacedBy(Space.xxs)
        ) {
            PreviewBar(bar, 0.66f)
            PreviewBar(bar, 0.44f)
            PreviewBar(accent, 0.5f)
        }
    }
}

@Composable
private fun LightThemeIllustration(modifier: Modifier = Modifier) {
    SingleThemeIllustration(
        background = PreviewLightBg,
        card = PreviewLightCard,
        bar = PreviewLightBar,
        accent = PreviewAccentTeal,
        modifier = modifier
    )
}

@Composable
private fun DarkThemeIllustration(modifier: Modifier = Modifier) {
    SingleThemeIllustration(
        background = PreviewDarkBg,
        card = PreviewDarkCard,
        bar = PreviewDarkBar,
        accent = PreviewAccentMint,
        modifier = modifier
    )
}

/**
 * One note card straddling the light/dark split: the same note reads in either scheme,
 * which is what "system" means here.
 */
@Composable
private fun SystemThemeIllustration(modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(PreviewBoxShape)) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth().weight(1f).background(PreviewLightBg))
            Box(Modifier.fillMaxWidth().weight(1f).background(PreviewDarkBg))
        }
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.62f)
                .aspectRatio(1.5f)
                .clip(PreviewCardShape)
        ) {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxWidth().weight(1f).background(PreviewLightCard))
                Box(Modifier.fillMaxWidth().weight(1f).background(PreviewDarkCard))
            }
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = Space.sm, vertical = Space.xs),
                verticalArrangement = Arrangement.spacedBy(Space.xxs)
            ) {
                PreviewBar(PreviewLightBar, 0.62f)
                PreviewBar(PreviewLightBar, 0.4f)
                Spacer(Modifier.weight(1f))
                PreviewBar(PreviewDarkBar, 0.45f)
            }
        }
    }
}

@Composable
private fun PreviewBar(color: Color, widthFraction: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(PreviewBarHeight)
            .clip(RoundedCornerShape(1.5.dp))
            .background(color)
    )
}

// --- Previews --------------------------------------------------------------

@Preview(showBackground = true, showSystemUi = true, widthDp = 420)
@Composable
private fun SettingsLightPreview() {
    NotesTheme(darkTheme = false) {
        SettingsContent(
            state = SettingsScreenState(username = "reader", email = "reader@example.com"),
            onAction = {},
            onBack = {},
            onGoToHiddenNotes = {},
            onGoToChangePin = {},
            themeMode = ThemeMode.LIGHT,
            onThemeModeChange = {},
            locale = AppLocale.English,
            onLocaleChange = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, widthDp = 420)
@Composable
private fun SettingsDarkPreview() {
    NotesTheme(darkTheme = true) {
        SettingsContent(
            state = SettingsScreenState(),
            onAction = {},
            onBack = {},
            onGoToHiddenNotes = {},
            onGoToChangePin = {},
            themeMode = ThemeMode.DARK,
            onThemeModeChange = {},
            locale = AppLocale.Arabic,
            onLocaleChange = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, widthDp = 420)
@Composable
private fun SettingsLogoutDialogPreview() {
    NotesTheme(darkTheme = false) {
        SettingsContent(
            state = SettingsScreenState(logoutDialogVisible = true),
            onAction = {},
            onBack = {},
            onGoToHiddenNotes = {},
            onGoToChangePin = {},
            themeMode = ThemeMode.SYSTEM,
            onThemeModeChange = {},
            locale = AppLocale.English,
            onLocaleChange = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, widthDp = 320)
@Composable
private fun SettingsNarrowPreview() {
    NotesTheme(darkTheme = false) {
        SettingsContent(
            state = SettingsScreenState(),
            onAction = {},
            onBack = {},
            onGoToHiddenNotes = {},
            onGoToChangePin = {},
            themeMode = ThemeMode.SYSTEM,
            onThemeModeChange = {},
            locale = AppLocale.English,
            onLocaleChange = {}
        )
    }
}
