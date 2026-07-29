# Pixer

<p align="center">
  <img src="assets/icon.png" alt="Pixer icon" width="128"/>
</p>

<p align="center">
  <strong>An unofficial Deezer client for Android</strong>
</p>

<p align="center">
  <a href="https://github.com/Minuga-RC/Pixer/releases/latest">
    <img src="https://img.shields.io/github/v/release/Minuga-RC/Pixer?include_prereleases&logo=github&style=for-the-badge&label=Latest%20Release" alt="Latest release">
  </a>
  <a href="https://github.com/Minuga-RC/Pixer/releases">
    <img src="https://img.shields.io/github/downloads/Minuga-RC/Pixer/total?logo=github&style=for-the-badge" alt="Total downloads">
  </a>
  <img src="https://img.shields.io/badge/Android-11%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android 11+">
  <img src="https://img.shields.io/badge/License-MIT-blue?style=for-the-badge" alt="MIT license">
</p>

<p align="center">
  <img src="assets/screenshot1.png" alt="Pixer home screen" width="205"/>
  <img src="assets/screenshot2.png" alt="Pixer now playing screen" width="205"/>
  <img src="assets/screenshot3.png" alt="Pixer library screen" width="205"/>
  <img src="assets/screenshot4.png" alt="Pixer lyrics screen" width="205"/>
</p>

## What It Is

Pixer is a dedicated, unofficial Deezer client for Android. It focuses on providing a clean, expressive Material 3 UI for browsing, searching, and streaming music directly from Deezer. 

## Acknowledgements

This project is a fork of [PixelPlayerOSS](https://github.com/lostf1sh/PixelPlayerOSS), originally created by [@lostf1sh](https://github.com/lostf1sh). 

While the original PixelPlayerOSS focused on local playback and self-hosted music libraries, **Pixer** removes all local capabilities to focus entirely on being a streamlined, cloud-first Deezer experience. Huge thanks to the original developer for providing such an incredible foundation and UI!

## Features

| Area | Highlights |
| --- | --- |
| Playback | Media3 playback engine, gapless playback, custom transitions, queue controls, shuffle |
| Deezer Integration | Stream directly from Deezer. Browse albums, artists, playlists, and search for your favorite tracks. |
| UI | Jetpack Compose, Material 3, dynamic color, light/dark themes, animated player surfaces |

## Requirements

| Requirement | Version |
| --- | --- |
| Android | 11 or newer, API 30+ |
| JDK | 21 |
| Android SDK | compile/target 37 |

## Build From Source

Clone the repository:

```sh
git clone https://github.com/Minuga-RC/Pixer.git
cd Pixer
```

Build the debug APK:

```sh
./gradlew :app:assembleDebug
```

Build a signed release APK:

```sh
./gradlew :app:assembleRelease
```

## Tech Stack

| Area | Technology |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose |
| Design | Material 3 |
| Playback | AndroidX Media3, ExoPlayer |
| Dependency Injection | Hilt |
| Networking | Retrofit, OkHttp |
| Images | Coil |

## License

Pixer is licensed under the [MIT License](LICENSE).

<p align="center">
  Maintained by <a href="https://github.com/Minuga-RC">Minuga</a>
</p>
