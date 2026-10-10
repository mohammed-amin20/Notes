<div align="center">

# Memo (ميمو)

The offline-first notes app for Android.

Everything you write — every note, account, and preference — lives on **your**
device. No accounts to sync, no backend to trust, no data to lose to the cloud.
Sign up, start typing, and your notes stay yours.

<img src="app/src/main/res/mipmap-nodpi/memo_launcher.png" width="120" alt="Memo logo"/>

</div>

<div align="center">

![Kotlin](https://img.shields.io/badge/Kotlin-2.1.21-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)
![Material 3](https://img.shields.io/badge/Material%203-7C4DFF?logo=materialdesign&logoColor=white)
![Room](https://img.shields.io/badge/Room-2E7D32?logo=sqlite&logoColor=white)
![Hilt](https://img.shields.io/badge/Hilt-026E9D)
![minSdk 26](https://img.shields.io/badge/minSdk-26-EB732D)
![targetSdk 35](https://img.shields.io/badge/targetSdk-35-1089D1)

</div>

## What you can do

- **Accounts** — sign up, log in, log out with a session-restore splash.
  Credentials never leave the device.
- **Notes** — create, edit, delete with live character/word counts and relative
  timestamps ("Today, 3:45 PM"). Deletion is undoable.
- **Search** — instant filtering as you type.
- **Pin & filter** — pin notes to the top and filter by All, Pinned, Work, or
  Personal; set a category per note.
- **Multi-select** — delete, pin/unpin, or hide in one step, including
  "Select all".
- **Hidden vault** — hide notes behind a 4-digit PIN: content encrypted with
  AES-256-GCM, key wrapped in the Android KeyStore, PIN never stored
  (PBKDF2-HMAC-SHA256, 600k iterations).
- **Appearance & language** — System / Light / Dark theme and English ↔ Arabic
  (full RTL, Arabic-Indic keypad) at runtime.

## Screens

The app flows from a branded splash into **authentication first**, then your
notes. All previews below are rendered from Jetpack Compose `@Preview`.

> UI previews rendered from Jetpack Compose `@Preview`.

#### Splash — light &middot; dark

<p align="center">
  <img src="docs/screenshots/splash-light.png" width="250" alt="Splash screen, light theme"/>
  <img src="docs/screenshots/splash-dark.png" width="250" alt="Splash screen, dark theme"/>
</p>

#### Sign up — light &middot; dark

<p align="center">
  <img src="docs/screenshots/signup-light.png" width="250" alt="Sign up screen, light theme"/>
  <img src="docs/screenshots/signup-dark.png" width="250" alt="Sign up screen, dark theme"/>
</p>

#### Login — light &middot; dark

<p align="center">
  <img src="docs/screenshots/login-light.png" width="250" alt="Login screen, light theme"/>
  <img src="docs/screenshots/login-dark.png" width="250" alt="Login screen, dark theme"/>
</p>

#### Notes list — light &middot; dark

<p align="center">
  <img src="docs/screenshots/notes-light.png" width="250" alt="Notes list, light theme"/>
  <img src="docs/screenshots/notes-dark.png" width="250" alt="Notes list, dark theme"/>
</p>

#### Search — results &middot; no results

<p align="center">
  <img src="docs/screenshots/search-light.png" width="250" alt="Searching notes, light theme"/>
  <img src="docs/screenshots/search-dark.png" width="250" alt="Search with no results, dark theme"/>
</p>

#### Empty state &middot; selection mode

<p align="center">
  <img src="docs/screenshots/no-notes-light.png" width="250" alt="Empty notes list, light theme"/>
  <img src="docs/screenshots/selected-dark.png" width="250" alt="Multi-select notes, dark theme"/>
</p>

#### Add / edit note — light &middot; dark

<p align="center">
  <img src="docs/screenshots/add-edit-note-light.png" width="250" alt="Add or edit note, light theme"/>
  <img src="docs/screenshots/add-edit-note-dark.png" width="250" alt="Add or edit note, dark theme"/>
</p>

#### Settings — light &middot; dark

<p align="center">
  <img src="docs/screenshots/settings-light.png" width="210" alt="Settings screen, light theme"/>
  <img src="docs/screenshots/settings-dark.png" width="210" alt="Settings screen, dark theme"/>
</p>

#### Settings states — log out dialog &middot; compact layout

<p align="center">
  <img src="docs/screenshots/settings-logout-dialog-light.png" width="210" alt="Log out confirmation dialog, light theme"/>
  <img src="docs/screenshots/settings-narrow-light.png" width="210" alt="Settings on a narrow screen, light theme"/>
</p>

#### PIN setup — create &middot; confirm &middot; mismatch

<p align="center">
  <img src="docs/screenshots/pin-setup-light.png" width="210" alt="PIN setup, light theme"/>
  <img src="docs/screenshots/pin-setup-dark.png" width="210" alt="PIN setup, dark theme"/>
  <img src="docs/screenshots/pin-setup-confirm.png" width="210" alt="PIN setup confirmation step"/>
  <img src="docs/screenshots/pin-setup-mismatch.png" width="210" alt="PIN mismatch warning"/>
</p>

#### PIN verify — unlocked &middot; forgot &middot; locked

<p align="center">
  <img src="docs/screenshots/pin-verify-light.png" width="210" alt="PIN verification, light theme"/>
  <img src="docs/screenshots/pin-verify-dark.png" width="210" alt="PIN verification, dark theme"/>
  <img src="docs/screenshots/pin-verify-forget-dialog.png" width="210" alt="Forgot PIN dialog"/>
  <img src="docs/screenshots/pin-verify-locked.png" width="210" alt="Vault locked state"/>
</p>

#### Hidden notes vault — list &middot; select &middot; empty

<p align="center">
  <img src="docs/screenshots/hidden-notes-list-light.png" width="210" alt="Hidden notes list, light theme"/>
  <img src="docs/screenshots/hidden-notes-list-dark.png" width="210" alt="Hidden notes list, dark theme"/>
  <img src="docs/screenshots/hidden-notes-select-mode.png" width="210" alt="Hidden notes selection mode"/>
  <img src="docs/screenshots/hidden-notes-empty.png" width="210" alt="Empty hidden notes list"/>
</p>

## Privacy, seriously

Hidden notes are **not** just tucked out of sight — they are encrypted.

- **Two-step PIN setup** with confirmation, plus a change-PIN flow.
- Note content encrypted with **AES-256-GCM**; the encryption key is wrapped by
  a key stored in the **Android KeyStore**. The PIN is **never stored** — only a
  PBKDF2-HMAC-SHA256 verifier (600,000 iterations).
- Wrong-PIN attempts lock the vault for **escalating periods** (30 seconds up to
  one hour); the vault locks automatically when the app is backgrounded.
- "Forgot PIN?" permanently deletes the hidden notes and removes the PIN — a
  deliberate consequence of the encryption design.

## Tech stack

| Technology | Usage |
|------------|-------|
| Kotlin 2.1.21 | Language |
| Jetpack Compose (Material 3, BOM 2025.05.00) | UI |
| Room 2.7.1 (KSP) | Local database (`notes_db`) |
| Hilt 2.56.2 | Dependency injection |
| Navigation Compose 2.9.0 | In-app navigation |
| kotlinx.serialization | Structured data handling |
| Android KeyStore + `javax.crypto` | Hidden-note encryption and PIN verification |

There is no backend: authentication, notes, and preferences are all stored in a
local Room database and shared preferences on the device.

## Project structure

Single-module Android app (`:app`), organized by feature:

```
app/src/main/java/com/mohammed/notes/
├── MainActivity.kt          # Single activity; theme, locale, and navigation root
├── NotesApp.kt              # Application (Hilt)
├── feature/auth/            # Sign up / login screens
├── feature/note/            # Notes list, search, selection, editor
├── feature/privacy/         # PIN setup/verify, hidden notes vault
├── feature/settings/        # Appearance, language, privacy, account
├── feature/core/
│   ├── data/                # Room database, DAOs, entities, shared preferences
│   ├── security/            # PIN hashing, encryption, throttling
│   ├── di/                  # Hilt module
│   └── presentation/        # Startup screen, shared UI
└── ui/                      # Theme (colors, type, shapes, motion) and locale
```

## Requirements

- **JDK 17 or newer** (used by Gradle/AGP; Android Studio's bundled JBR works).
  The app code targets Java 11 bytecode.
- **Android SDK**: `compileSdk` 35, `targetSdk` 35, `minSdk` 26 (Android 8.0+).
- **Android Studio** (or a CLI with `ANDROID_HOME` set / `local.properties`
  present). Gradle wrapper is pinned to **8.12.1**.

## Setup

1. Clone the repository.
2. Open the project in Android Studio and let it sync (this creates
   `local.properties` with `sdk.dir` automatically), **or** set
   `ANDROID_HOME` to your SDK location for command-line builds.
3. No API keys, secrets, or environment variables are required — the project
   needs no network services at build time.

## Building the debug APK

Building does **not** require a connected device.

**Windows (PowerShell / cmd):**

```powershell
.\gradlew.bat :app:assembleDebug
```

**macOS / Linux:**

```bash
./gradlew :app:assembleDebug
```

The debug APK (auto-signed with the debug keystore) is written to:

```
app/build/outputs/apk/debug/app-debug.apk
```

### Installing on a device (optional)

Installation is separate from building and requires an Android device or
emulator (with USB debugging enabled) to be connected:

```powershell
.\gradlew.bat :app:installDebug   # Windows
```

```bash
./gradlew :app:installDebug       # macOS / Linux
```

Alternatively, run the app from Android Studio (Run → your device).

## Tests and checks

- `PinCryptoTest` — host-based unit tests for PIN digit normalization, PBKDF2
  derivation, and AES-GCM round trips.
- `PinThrottleTest` — host-based unit tests for the PIN cooldown schedule.
- `ExampleUnitTest` — stock JVM smoke test.
- `ExampleInstrumentedTest` — stock instrumented smoke test (requires a device).

```powershell
.\gradlew.bat :app:testDebugUnitTest    # unit tests on the JVM
.\gradlew.bat :app:lintDebug            # Android lint
.\gradlew.bat :app:connectedDebugAndroidTest  # instrumented tests (device required)
```

(applying the equivalent `./gradlew` forms on macOS/Linux).

## Limitations

- **Local only, no sync or backup.** All notes live in the app's local database
  on a single device. There is no cloud sync; uninstalling the app or clearing
  its data erases everything.
- **Local credentials.** Account usernames, emails, and passwords are stored
  and validated locally in the database in plaintext. Do not reuse passwords
  you use elsewhere.
- **Hidden-note protection.** If the PIN is forgotten, resetting privacy
  permanently deletes hidden notes — enforced by the encryption design and
  cannot be undone. Hidden notes do not appear in the main list, search, or
  pinned section.
- **Device restore & migration.** The Android KeyStore key and the PIN
  preference are not backed up. After a restore or migration hidden notes may
  be present but undecryptable; the app surfaces such rows as "broken" so they
  can still be deleted.
- **Risky operations.** Hidden-note security relies on the Android KeyStore; a
  device that loses its Keystore keys cannot decrypt hidden content.

## Developer

Developed by **Mohammed Amen Ghazal** — [GitHub](https://github.com/mohammed-amin20)
