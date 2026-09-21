<div align="center">

  <h1>🎵 CONVX <sub>by Belze</sub></h1>
  <h3>Personal optimization fork of Convx — Liquid Glass music player for Android</h3>

  <p>
    <a href="https://github.com/021belze/Convx-belze-root/releases/latest">
      <img src="https://img.shields.io/github/v/release/021belze/Convx-belze-root?style=for-the-badge&logo=android&logoColor=white&color=4CAF50&label=DOWNLOAD" alt="Download Latest">
    </a>
    <a href="https://github.com/021belze/Convx-belze-root/releases">
      <img src="https://img.shields.io/github/downloads/021belze/Convx-belze-root/total?style=for-the-badge&color=blue" alt="Total Downloads">
    </a>
    <a href="LICENSE">
      <img src="https://img.shields.io/github/license/021belze/Convx-belze-root?style=for-the-badge" alt="License GPL-3.0">
    </a>
  </p>

  <p>
    <img src="https://img.shields.io/badge/Based%20on-Convx%20r84-8A2BE2?style=flat-square" alt="Based on Convx r84">
    <img src="https://img.shields.io/badge/Build-R8%20Optimized-FF6B35?style=flat-square" alt="R8 Optimized">
    <img src="https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android 8.0+">
    <img src="https://img.shields.io/badge/Side%20by%20Side-com.convx.music.debug-blueviolet?style=flat-square" alt="Side by Side Install">
  </p>

</div>

---

## 🔱 Tentang Fork Ini

**Convx by Belze** adalah fork pribadi dari [Convx](https://github.com/cosmictaserdev-creator/Convx) yang difokuskan pada optimasi performa, perbaikan bug, dan peningkatan animasi. Fork ini bukan pengganti resmi — melainkan versi eksperimental yang berjalan **berdampingan** (`com.convx.music.debug`) dengan Convx resmi.

> Semua kredit desain dan arsitektur tetap menjadi milik developer asli, [Aryan (CosmicTaser)](https://github.com/cosmictaserdev-creator) dan kontributor Convx.

---

## ✨ Apa yang Berbeda dari Convx Resmi?

### 🐛 Bug Fixes

| Bug | Status |
|---|---|
| **Navbar hilang / unclickable saat rotasi landscape** — HP modern dengan rasio 20:9 salah terdeteksi sebagai Tablet (`width >= 840dp` triggers `showRail = true`). Navbar menghilang, sidebar tablet muncul terjepit. | ✅ Fixed |
| **Sidebar tablet tidak bisa dipencet** — Gesture blocker di root Column mencegat semua touch event sebelum tab items bisa menerima klik | ✅ Fixed |
| **Recomposition thrashing saat scroll** — Mutasi `SnapshotState` di dalam `derivedStateOf` menyebabkan siklus invalidasi berulang di setiap frame scroll | ✅ Fixed |

### ⚡ Optimasi Performa

- **Full R8 Bytecode Optimization** — Minifikasi, dead-code stripping, dan resource shrinking aktif penuh di `debug` build type
- **AOT Compilation Mode** — `isDebuggable = false` mengaktifkan ART AOT compilation untuk Compose, setara dengan performa release build
- **Audio Pipeline Optimization** — Prefetch lebih cerdas, waktu tunggu audio playback lebih cepat
- **Lirik Deduplication** — Mencegah request lirik ganda dari beberapa provider sekaligus

### ✨ Animasi & UI

- **Fluid Navbar Spring Morphing** — Transisi kapsul navbar antara mode *collapsed / expanded / search* menggunakan `SizeTransform` dengan `spring(0.85f, 380f)`, kapsul membesar/mengecil secara elastis tanpa clipping kaku
- **Glass Backdrop Freeze** — Efek blur Liquid Glass dibekukan sinkron selama transisi morphing agar GPU tidak drop frame
- **Home FAB Spring Animations** — Tombol Shuffle & Mic di Home kini muncul/menghilang dengan kombinasi **spring slide + fade + micro scale pop**
- **Player V2 Apple Music Style** aktif secara default — desain modern dengan container morph halus dari mini ke fullscreen
- **Equalizer & Advanced Audio** tersedia langsung di menu titik 3 (&#8943;) player

---

## 📦 Download & Install

> **Berjalan berdampingan dengan Convx resmi!** Package ID fork ini adalah `com.convx.music.debug`, sehingga tidak akan menimpa aplikasi Convx asli yang sudah terpasang.

1. Buka halaman [**Releases**](https://github.com/021belze/Convx-belze-root/releases/latest)
2. Download file `.apk` sesuai perangkat
3. Izinkan instalasi dari sumber tidak dikenal di pengaturan Android
4. Install dan nikmati!

| File | Keterangan |
|---|---|
| `convx.apk` | Android, APK |

---

## 🔧 Build Sendiri

```bash
# Clone repositori ini
git clone https://github.com/021belze/Convx-belze-root.git
cd Convx-belze-root

# Build APK debug teroptimasi (R8)
.\gradlew assembleUniversalFossDebug --no-daemon -x lint

# APK tersimpan di:
# app/build/outputs/apk/universalFoss/debug/app-universal-foss-debug.apk
```

**Persyaratan:**
- Android Studio Meerkat / JDK 21
- Android SDK API 36
- NDK 27.0.12077973

---

## 📜 Upstream & Kredit

Fork ini berbasis pada **Convx r84** oleh [Aryan (CosmicTaser)](https://github.com/cosmictaserdev-creator).

| Proyek | Kontribusi |
|---|---|
| [**Convx**](https://github.com/cosmictaserdev-creator/Convx) | Basis utama fork ini |
| [**vivi-music**](https://github.com/vivizzz007/vivi-music) | Asal-usul Convx, Apple Music Player UI |
| [**Kyant0/backdrop**](https://github.com/Kyant0/backdrop) | Library real-time backdrop blur Liquid Glass |
| [**Better Lyrics**](https://github.com/better-lyrics/better-lyrics) | Synced karaoke-style lyrics |
| [**SimpMusic**](https://github.com/maxrave-dev/SimpMusic) | Lirik & audio streaming |

---

## 📄 Lisensi

Didistribusikan di bawah lisensi **GPL-3.0** — sama dengan Convx upstream. Lihat [LICENSE](LICENSE).

---

<div align="center">
  <sub>Fork ini dibuat untuk keperluan personal dan eksperimental oleh <strong>Belze</strong>.<br>
  Semua kebanggaan desain tetap menjadi milik tim Convx asli. ❤️</sub>
</div>
