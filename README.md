# Resonix

<img src="resonix.png" alt="Resonix icon" width="128" />

Open-source Android video player built with Kotlin and Jetpack Compose. Browse, organize, and play local videos — plus optional network libraries (SMB, FTP, WebDAV) and an encrypted Vault for private videos.

**App ID**: `com.resonix.media` · **Version**: 0.1.0 · **License**: GPL-3.0

## Features

**Library**
- Browse videos by folder or flat list (MediaStore)
- Search with history (stored locally via DataStore)
- Sort, multi-select, rename, and media-info dialogs
- Configurable thumbnails (first frame / frame-at-percentage / hybrid)
- Playlist create and manage

**Player** (separate `PlayerActivity`)
- Media3 ExoPlayer playback with MediaSession
- Picture-in-Picture, playback speed, audio/subtitle track selection
- Gesture controls
- Queue from folder or multi-selection

**Vault**
- Hide videos behind encryption (AES via `VaultCipher` / `VaultCrypto`)
- Unlock with PIN or biometric
- Hidden files leave MediaStore; restore decrypts back to shared storage

**Network**
- Browse remote libraries: SMB (smbj), FTP (commons-net), WebDAV (sardine), SSH/SFTP (sshj)
- Add and validate connections

**Settings**
- Appearance (theme, dynamic color, accent, high-contrast dark)
- Media library, player, gestures, audio, subtitle, general, About + open-source licenses

**Other**
- Video splash screen, global crash handler (`CrashActivity`)
- Leanback launcher support (Android TV–capable UI)
- No accounts, no analytics, no tracking (see [PRIVACY](PRIVACY))

## Tech stack

| Area | Choice |
|------|--------|
| Language | Kotlin 2.4.20 |
| UI | Jetpack Compose (BOM 2026.09.00), Material 3, Material 3 Adaptive |
| Navigation | Navigation 3 (`androidx.navigation3`) |
| Playback | Media3 / ExoPlayer 1.11.1, nextlib decoders |
| Persistence | Room 2.8.5, DataStore (kotlinx.serialization) |
| DI | Koin 4.2.2 (annotations + KSP) |
| Images | Coil 3.6.3 (custom vault fetcher + thumbnail decoder) |
| Build | Gradle 9.7.1, AGP 9.4.1, version catalog (`gradle/libs.versions.toml`) |
| Lint | ktlint 12.3.0 |

SDK: `minSdk` 24 · `compileSdk`/`targetSdk` 37 · JVM target 17

## Requirements

- Android Studio (AGP 9.x compatible) or JDK 17+ CLI
- Android SDK 37
- Device/emulator on Android 7.0+ (API 24)

## Getting started

```bash
git clone https://github.com/s3r3/resonix.git
cd resonix
./gradlew :app:assembleDebug
./gradlew :app:installDebug   # with a device/emulator connected
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`

## Development commands

From [AGENTS.md](AGENTS.md):

| Command | Purpose |
|---------|---------|
| `./gradlew :app:assembleDebug` | Debug build |
| `./gradlew test` | Unit tests (JUnit4, Robolectric, coroutine-test) |
| `./gradlew ktlintCheck` | Style check |
| `./gradlew ktlintFormat` | Auto-format |
| `./gradlew :app:connectedAndroidTest` | Instrumented tests |
| `./gradlew lint` | Android lint |

Conventions: four-space indent, trailing commas, `PascalCase` types/composables, `camelCase` members. Package root: `com.resonix.media`.

## Project structure

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

Entry flow: `ResonixApplication` (Koin + Coil) → `MainActivity` (theme from prefs → splash video → Nav3 starting at `MediaPickerRoute`) → playback opens `PlayerActivity` with URIs via `PlayerApi`.

## Release builds

Signing is optional for debug. For release, supply via env vars or Gradle properties (never commit):

- `RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`

`./gradlew :app:bundleRelease` produces the AAB (R8 full mode, resource shrinking on).

### Fastlane

Lanes in `fastlane/Fastfile` (Play package `com.resonix.media`):

| Lane | Action |
|------|--------|
| `verify` | ktlint + unit tests + debug build |
| `build` | Signed release AAB |
| `lint` | `lintRelease` |
| `internal` / `beta` | Upload AAB to that track |
| `promote` | internal → beta → production (draft) |
| `metadata` | Store listing only |

Play upload needs `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` (see `fastlane/Appfile`).

## Privacy

No personal data collection; no third-party data sharing. Media access uses `READ_MEDIA_VIDEO` / `READ_MEDIA_AUDIO` / `READ_MEDIA_IMAGES` (and legacy `READ_EXTERNAL_STORAGE` ≤ API 32). `INTERNET` is used only for optional network library browsing. Full text: [PRIVACY](PRIVACY).

## Contributing

1. Fork and branch: `git checkout -b feature/your-change`
2. Keep module boundaries tight; no speculative abstractions
3. `./gradlew ktlintCheck test` before opening a PR

## License

[GNU GPL v3](LICENSE).
