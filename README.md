<div align="center">

<img src="resonix.png" alt="Resonix icon" width="128" height="128" />

# Resonix

**An open-source Android video player — fast, private, and modern.**

Built with Kotlin and Jetpack Compose. Play local videos, browse network libraries, and hide private videos in an encrypted Vault.

<br />

[![Version](https://img.shields.io/badge/version-0.1.0-6750A4?style=for-the-badge)](https://github.com/s3r3/resonix/releases)
[![License](https://img.shields.io/badge/license-GPL--3.0-blue?style=for-the-badge)](LICENSE)
[![Platform](https://img.shields.io/badge/Android-7.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](#-requirements)
[![Min SDK](https://img.shields.io/badge/minSdk-24-brightgreen?style=for-the-badge)](#-requirements)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.20-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Media3](https://img.shields.io/badge/Media3-ExoPlayer%201.11.1-FF6F00?style=flat-square&logo=google&logoColor=white)](https://developer.android.com/media/media3)
[![Gradle](https://img.shields.io/badge/Gradle-9.7.1-02303A?style=flat-square&logo=gradle&logoColor=white)](https://gradle.org)
[![ktlint](https://img.shields.io/badge/code%20style-ktlint-black?style=flat-square)](https://pinterest.github.io/ktlint/)
[![No Tracking](https://img.shields.io/badge/privacy-no%20tracking-success?style=flat-square&logo=shield&logoColor=white)](PRIVACY)

<br />

[**Download**](https://github.com/s3r3/resonix/releases) &nbsp;·&nbsp;
[**Features**](#-features) &nbsp;·&nbsp;
[**Getting Started**](#-getting-started) &nbsp;·&nbsp;
[**Contributing**](#-contributing) &nbsp;·&nbsp;
[**Report a Bug**](https://github.com/s3r3/resonix/issues)

</div>

---

## 📑 Table of Contents

- [Features](#-features)
- [Promo](#-promo)
- [Tech Stack](#-tech-stack)
- [Requirements](#-requirements)
- [Getting Started](#-getting-started)
- [Development Commands](#-development-commands)
- [Project Structure](#-project-structure)
- [Release Builds](#-release-builds)
- [Privacy](#-privacy)
- [Contributing](#-contributing)
- [License](#-license)

---

## ✨ Features

<table>
  <tr>
    <td width="50%" valign="top">

### 📚 Library
- Browse videos by folder or flat list (MediaStore)
- Search with history (stored locally via DataStore)
- Sort, multi-select, rename, and media-info dialogs
- Configurable thumbnails (first frame / frame-at-percentage / hybrid)
- Create and manage playlists

    </td>
    <td width="50%" valign="top">

### ▶️ Player
*(separate `PlayerActivity`)*
- Media3 ExoPlayer playback with MediaSession
- Picture-in-Picture, playback speed, audio/subtitle track selection
- Gesture controls
- Queue from a folder or multi-selection

    </td>
  </tr>
  <tr>
    <td width="50%" valign="top">

### 🔒 Vault
- Hide videos behind encryption (AES via `VaultCipher` / `VaultCrypto`)
- Unlock with PIN or biometric
- Hidden files leave MediaStore; restore decrypts back to shared storage

    </td>
    <td width="50%" valign="top">

### 🌐 Network
- Browse remote libraries: **SMB** (smbj), **FTP** (commons-net), **WebDAV** (sardine), **SSH/SFTP** (sshj)
- Add and validate connections

    </td>
  </tr>
  <tr>
    <td width="50%" valign="top">

### ⚙️ Settings
- Appearance: theme, dynamic color, accent, high-contrast dark
- Media library, player, gestures, audio, subtitle, general
- About + open-source licenses

    </td>
    <td width="50%" valign="top">

### 🧩 Other
- Video splash screen, global crash handler (`CrashActivity`)
- Leanback launcher support (Android TV–capable UI)
- **No accounts, no analytics, no tracking** — see [PRIVACY](PRIVACY)

    </td>
  </tr>
</table>

---

## 🎬 Promo

<div align="center">

<video src="https://raw.githubusercontent.com/s3r3/resonix/main/docs/promo-resonix.mp4" controls width="720"></video>

<br />

[Watch / download the promo video](docs/promo-resonix.mp4)

</div>

---

## 🛠 Tech Stack

| Area | Choice |
|------|--------|
| **Language** | Kotlin 2.4.20 |
| **UI** | Jetpack Compose (BOM 2026.09.00), Material 3, Material 3 Adaptive |
| **Navigation** | Navigation 3 (`androidx.navigation3`) |
| **Playback** | Media3 / ExoPlayer 1.11.1, nextlib decoders |
| **Persistence** | Room 2.8.5, DataStore (kotlinx.serialization) |
| **DI** | Koin 4.2.2 (annotations + KSP) |
| **Images** | Coil 3.6.3 (custom vault fetcher + thumbnail decoder) |
| **Build** | Gradle 9.7.1, AGP 9.4.1, version catalog (`gradle/libs.versions.toml`) |
| **Lint** | ktlint 12.3.0 |

> **SDK:** `minSdk` 24 · `compileSdk` / `targetSdk` 37 · JVM target 17

---

## 📋 Requirements

- Android Studio (AGP 9.x compatible) or JDK 17+ CLI
- Android SDK 37
- Device/emulator on Android 7.0+ (API 24)

---

## 🚀 Getting Started

```bash
git clone https://github.com/s3r3/resonix.git
cd resonix
./gradlew :app:assembleDebug
./gradlew :app:installDebug   # with a device/emulator connected
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`

---

## 💻 Development Commands

From [AGENTS.md](AGENTS.md):

| Command | Purpose |
|---------|---------|
| `./gradlew :app:assembleDebug` | Debug build |
| `./gradlew test` | Unit tests (JUnit4, Robolectric, coroutine-test) |
| `./gradlew ktlintCheck` | Style check |
| `./gradlew ktlintFormat` | Auto-format |
| `./gradlew :app:connectedAndroidTest` | Instrumented tests |
| `./gradlew lint` | Android lint |

**Conventions:** four-space indent, trailing commas, `PascalCase` types/composables, `camelCase` members. Package root: `com.resonix.media`.

---

## 🗂 Project Structure

```
app/                  # MainActivity, Nav3 graph, splash, crash handler, DI root
core/
  common/             # Extensions, dispatchers, system services
  data/               # Repositories (media, vault, playlist, search, prefs)
  database/           # Room (MediaDatabase: playlists, hidden videos, state, network)
  datastore/          # App/player/search-history preferences
  domain/             # Use cases
  media/              # MediaStore queries, vault crypto, network data sources
  model/              # Shared models
  ui/                 # Theme, MviViewModel base, shared composables
feature/
  media/              # Media picker, search, vault UI
  network/            # Connection CRUD + remote browse
  player/             # PlayerActivity, controls, gestures, PiP
  playlist/           # Playlist list/detail
  settings/           # All preference screens + AboutLibraries
fastlane/             # Play Store lanes (build, internal, beta, promote)
```

**Entry flow:** `ResonixApplication` (Koin + Coil) → `MainActivity` (theme from prefs → splash video → Nav3 starting at `MediaPickerRoute`) → playback opens `PlayerActivity` with URIs via `PlayerApi`.

---

## 📦 Release Builds

Signing is optional for debug. For release, supply via env vars or Gradle properties (**never commit**):

- `RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`

`./gradlew :app:bundleRelease` produces the AAB (R8 full mode, resource shrinking on).

<details>
<summary><b>🚢 Fastlane</b> — lanes in <code>fastlane/Fastfile</code> (Play package: <code>com.resonix.media</code>)</summary>

<br />

| Lane | Action |
|------|--------|
| `verify` | ktlint + unit tests + debug build |
| `build` | Signed release AAB |
| `lint` | `lintRelease` |
| `internal` / `beta` | Upload AAB to that track |
| `promote` | internal → beta → production (draft) |
| `metadata` | Store listing only |

Play upload needs `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` (see `fastlane/Appfile`).

</details>

---

## 🔐 Privacy

Resonix **collects no personal data** and **shares no data with third parties**.

- Media access uses `READ_MEDIA_VIDEO` / `READ_MEDIA_AUDIO` / `READ_MEDIA_IMAGES` (and legacy `READ_EXTERNAL_STORAGE` ≤ API 32).
- `INTERNET` is used only for optional network library browsing.

Full text: [PRIVACY](PRIVACY).

---

## 🤝 Contributing

Contributions are welcome!

1. Fork and branch: `git checkout -b feature/your-change`
2. Keep module boundaries tight; no speculative abstractions
3. Run `./gradlew ktlintCheck test` before opening a PR

Found a bug? [Open an issue](https://github.com/s3r3/resonix/issues).

---

## 📄 License

Distributed under the [GNU GPL v3](LICENSE).

<div align="center">

<br />

Made with ❤️ for Android video lovers

⭐ **If Resonix is useful to you, consider starring the repo!**

</div>
