<div align="center">

<img src="resonix.png" alt="Resonix icon" width="128" height="128" />

# Resonix

**Pemutar video Android open-source — cepat, privat, dan modern.**

Dibangun dengan Kotlin & Jetpack Compose. Putar video lokal, jelajahi library jaringan, dan sembunyikan video pribadi di Vault terenkripsi.

<br />

[![Version](https://img.shields.io/badge/version-0.1.0-6750A4?style=for-the-badge)](https://github.com/s3r3/resonix/releases)
[![License](https://img.shields.io/badge/license-GPL--3.0-blue?style=for-the-badge)](LICENSE)
[![Platform](https://img.shields.io/badge/Android-7.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](#-persyaratan)
[![Min SDK](https://img.shields.io/badge/minSdk-24-brightgreen?style=for-the-badge)](#-persyaratan)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.20-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Media3](https://img.shields.io/badge/Media3-ExoPlayer%201.11.1-FF6F00?style=flat-square&logo=google&logoColor=white)](https://developer.android.com/media/media3)
[![Gradle](https://img.shields.io/badge/Gradle-9.7.1-02303A?style=flat-square&logo=gradle&logoColor=white)](https://gradle.org)
[![ktlint](https://img.shields.io/badge/code%20style-ktlint-black?style=flat-square)](https://pinterest.github.io/ktlint/)
[![No Tracking](https://img.shields.io/badge/privacy-no%20tracking-success?style=flat-square&logo=shield&logoColor=white)](PRIVACY)

<br />

[**Download**](https://github.com/s3r3/resonix/releases) &nbsp;·&nbsp;
[**Fitur**](#-fitur) &nbsp;·&nbsp;
[**Memulai**](#-memulai) &nbsp;·&nbsp;
[**Kontribusi**](#-kontribusi) &nbsp;·&nbsp;
[**Laporkan Bug**](https://github.com/s3r3/resonix/issues)

</div>

---

## 📑 Daftar Isi

- [Fitur](#-fitur)
- [Tech Stack](#-tech-stack)
- [Persyaratan](#-persyaratan)
- [Memulai](#-memulai)
- [Perintah Pengembangan](#-perintah-pengembangan)
- [Struktur Proyek](#-struktur-proyek)
- [Build Rilis](#-build-rilis)
- [Privasi](#-privasi)
- [Kontribusi](#-kontribusi)
- [Lisensi](#-lisensi)

---

## ✨ Fitur

<table>
  <tr>
    <td width="50%" valign="top">

### 📚 Library
- Jelajahi video per folder atau daftar datar (MediaStore)
- Pencarian dengan riwayat (disimpan lokal via DataStore)
- Urutkan, multi-select, rename, dan dialog info media
- Thumbnail yang bisa diatur (frame pertama / frame pada persentase / hybrid)
- Buat dan kelola playlist

    </td>
    <td width="50%" valign="top">

### ▶️ Player
*(`PlayerActivity` terpisah)*
- Pemutaran Media3 ExoPlayer dengan MediaSession
- Picture-in-Picture, kecepatan putar, pilihan track audio/subtitle
- Kontrol gestur
- Antrean dari folder atau multi-selection

    </td>
  </tr>
  <tr>
    <td width="50%" valign="top">

### 🔒 Vault
- Sembunyikan video dengan enkripsi (AES via `VaultCipher` / `VaultCrypto`)
- Buka dengan PIN atau biometrik
- File tersembunyi keluar dari MediaStore; restore mendekripsi kembali ke penyimpanan bersama

    </td>
    <td width="50%" valign="top">

### 🌐 Network
- Jelajahi library jarak jauh: **SMB** (smbj), **FTP** (commons-net), **WebDAV** (sardine), **SSH/SFTP** (sshj)
- Tambah dan validasi koneksi

    </td>
  </tr>
  <tr>
    <td width="50%" valign="top">

### ⚙️ Pengaturan
- Tampilan: tema, dynamic color, aksen, dark mode kontras tinggi
- Library media, player, gestur, audio, subtitle, umum
- About + lisensi open-source

    </td>
    <td width="50%" valign="top">

### 🧩 Lainnya
- Splash screen video, global crash handler (`CrashActivity`)
- Dukungan Leanback launcher (UI siap Android TV)
- **Tanpa akun, tanpa analitik, tanpa tracking** — lihat [PRIVACY](PRIVACY)

    </td>
  </tr>
</table>

---

## 🛠 Tech Stack

| Area | Pilihan |
|------|---------|
| **Bahasa** | Kotlin 2.4.20 |
| **UI** | Jetpack Compose (BOM 2026.09.00), Material 3, Material 3 Adaptive |
| **Navigasi** | Navigation 3 (`androidx.navigation3`) |
| **Playback** | Media3 / ExoPlayer 1.11.1, nextlib decoders |
| **Persistensi** | Room 2.8.5, DataStore (kotlinx.serialization) |
| **DI** | Koin 4.2.2 (annotations + KSP) |
| **Gambar** | Coil 3.6.3 (custom vault fetcher + thumbnail decoder) |
| **Build** | Gradle 9.7.1, AGP 9.4.1, version catalog (`gradle/libs.versions.toml`) |
| **Lint** | ktlint 12.3.0 |

> **SDK:** `minSdk` 24 · `compileSdk` / `targetSdk` 37 · JVM target 17

---

## 📋 Persyaratan

- Android Studio (kompatibel AGP 9.x) atau JDK 17+ untuk CLI
- Android SDK 37
- Perangkat/emulator Android 7.0+ (API 24)

---

## 🚀 Memulai

```bash
git clone https://github.com/s3r3/resonix.git
cd resonix
./gradlew :app:assembleDebug
./gradlew :app:installDebug   # dengan perangkat/emulator terhubung
```

APK debug: `app/build/outputs/apk/debug/app-debug.apk`

---

## 💻 Perintah Pengembangan

Dari [AGENTS.md](AGENTS.md):

| Perintah | Fungsi |
|----------|--------|
| `./gradlew :app:assembleDebug` | Build debug |
| `./gradlew test` | Unit test (JUnit4, Robolectric, coroutine-test) |
| `./gradlew ktlintCheck` | Cek gaya kode |
| `./gradlew ktlintFormat` | Format otomatis |
| `./gradlew :app:connectedAndroidTest` | Instrumented test |
| `./gradlew lint` | Android lint |

**Konvensi:** indentasi empat spasi, trailing comma, `PascalCase` untuk tipe/composable, `camelCase` untuk member. Package root: `com.resonix.media`.

---

## 🗂 Struktur Proyek

```
app/                  # MainActivity, graf Nav3, splash, crash handler, DI root
core/
  common/             # Extensions, dispatchers, system services
  data/               # Repository (media, vault, playlist, search, prefs)
  database/           # Room (MediaDatabase: playlists, hidden videos, state, network)
  datastore/          # Preferensi app/player/riwayat pencarian
  domain/             # Use case
  media/              # Query MediaStore, vault crypto, network data sources
  model/              # Model bersama
  ui/                 # Tema, basis MviViewModel, composable bersama
feature/
  media/              # Media picker, search, UI vault
  network/            # CRUD koneksi + remote browse
  player/             # PlayerActivity, kontrol, gestur, PiP
  playlist/           # Daftar/detail playlist
  settings/           # Semua layar preferensi + AboutLibraries
fastlane/             # Lane Play Store (build, internal, beta, promote)
```

**Alur masuk:** `ResonixApplication` (Koin + Coil) → `MainActivity` (tema dari prefs → splash video → Nav3 mulai di `MediaPickerRoute`) → pemutaran membuka `PlayerActivity` dengan URI via `PlayerApi`.

---

## 📦 Build Rilis

Signing bersifat opsional untuk debug. Untuk rilis, berikan lewat environment variable atau Gradle properties (**jangan pernah di-commit**):

- `RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`

`./gradlew :app:bundleRelease` menghasilkan AAB (R8 full mode, resource shrinking aktif).

<details>
<summary><b>🚢 Fastlane</b> — lane di <code>fastlane/Fastfile</code> (package Play: <code>com.resonix.media</code>)</summary>

<br />

| Lane | Aksi |
|------|------|
| `verify` | ktlint + unit test + build debug |
| `build` | AAB rilis bertanda tangan |
| `lint` | `lintRelease` |
| `internal` / `beta` | Upload AAB ke track tersebut |
| `promote` | internal → beta → production (draft) |
| `metadata` | Hanya store listing |

Upload ke Play membutuhkan `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` (lihat `fastlane/Appfile`).

</details>

---

## 🔐 Privasi

Resonix **tidak mengumpulkan data pribadi** dan **tidak membagikan data ke pihak ketiga**.

- Akses media memakai `READ_MEDIA_VIDEO` / `READ_MEDIA_AUDIO` / `READ_MEDIA_IMAGES` (dan `READ_EXTERNAL_STORAGE` lama untuk ≤ API 32).
- `INTERNET` hanya dipakai untuk penjelajahan library jaringan yang opsional.

Teks lengkap: [PRIVACY](PRIVACY).

---

## 🤝 Kontribusi

Kontribusi sangat diterima!

1. Fork dan buat branch: `git checkout -b feature/perubahan-anda`
2. Jaga batas antar modul tetap rapat; tanpa abstraksi spekulatif
3. Jalankan `./gradlew ktlintCheck test` sebelum membuka PR

Menemukan bug? [Buka issue](https://github.com/s3r3/resonix/issues).

---

## 📄 Lisensi

Didistribusikan di bawah [GNU GPL v3](LICENSE).

<div align="center">

<br />

Dibuat dengan ❤️ untuk para pecinta video di Android

⭐ **Jika Resonix berguna, beri bintang di repo ini!**

</div>
