# Pixeezer

Personal fork of [Pixer](https://github.com/Minuga-RC/Pixer), an unofficial Deezer client for
Android — which is itself a fork of [PixelPlayerOSS](https://github.com/lostf1sh/PixelPlayerOSS).
Hobby project, kept in sync with upstream where it makes sense. Not affiliated with or endorsed
by Deezer.

## What it is

A native Android app for streaming from Deezer: browsing, searching, playlists, artist/album
pages, a full-screen player, and an optional alternate skin that mirrors the official Deezer
app's look. No local file playback or self-hosted servers — it's Deezer-only.

## Features

| Area | What's there |
| --- | --- |
| Playback | Media3/ExoPlayer engine, gapless playback, queue management, shuffle, custom track transitions, equalizer |
| Browsing | Home feed, search, library, daily mixes, recently played, genre pages, artist/album/playlist detail |
| Playlists | Create/edit playlists, smart playlists with rule-based auto-fill, duplicate-track detection |
| Account | Deezer login, plus an optional "Gateway" credential (`arl` token) that unlocks a second, unofficial Deezer API without the default API's pagination limits |
| Theming | Material 3 with dynamic color, light/dark, and a dedicated dark-only "Deezer" skin that reskins Home/Library/Player/Artist/Playlist to match the real app |
| Misc | Listening stats, lyrics, home-screen widgets |

## How it works

- **UI** — 100% Jetpack Compose, Material 3. Screens live under `presentation/screens`, shared
  building blocks under `presentation/components`.
- **State** — one `ViewModel` per screen/feature (`presentation/viewmodel`), exposing UI state as
  flows; screens collect and render, no business logic in composables.
- **DI** — Hilt wires repositories, API clients, and the database into ViewModels
  (`di/AppModule.kt`).
- **Deezer data** — Retrofit/OkHttp clients under `data/network/deezer` talk to Deezer's public
  API for browsing/search/streaming. When a Gateway `arl` is configured, a second client/repo
  (`data/repository/DeezerGatewayRepository.kt`) is used instead for endpoints that benefit from
  it — both paths feed the same ViewModels and UI.
- **Local storage** — Room (`data/database`) caches library data, playlists, and favorites;
  Jetpack DataStore (`data/preferences`) stores user settings (theme, playback prefs, gateway
  credentials).
- **Playback** — a foreground `MusicService` (AndroidX Media3) owns the player and queue, driving
  the notification, widgets, and the in-app player UI from the same source of truth.
- **Theming** — the selected app skin (Default vs. Deezer) is threaded through a
  `CompositionLocal` set once in `MainActivity`; screens branch on it either with small styling
  tweaks (corner radius, colors) or, for the main screens, a full alternate layout composable
  (`Deezer*Layout`/`Deezer*Content`) reusing the exact same ViewModel and data. See
  [`Pixer-master/docs/DEEZER_THEME_AND_GATEWAY_AUTH.md`](Pixer-master/docs/DEEZER_THEME_AND_GATEWAY_AUTH.md)
  for the full write-up.

## Repo layout

```
Pixeezer/
├── README.md          this file
└── Pixer-master/       the Android app (its own README, LICENSE, CONTRIBUTING, CHANGELOG)
```

## Build from source

```sh
cd Pixer-master
./gradlew :app:assembleDebug
```

Requires JDK 21, Android compile/target SDK 37, min SDK 30 (Android 11+). See
[`Pixer-master/CONTRIBUTING.md`](Pixer-master/CONTRIBUTING.md) for the full dev setup.

## Credits

- [@lostf1sh](https://github.com/lostf1sh) — original creator of PixelPlayerOSS
- [Minuga-RC](https://github.com/Minuga-RC) — creator of Pixer

## License

Licensed under the [MIT License](Pixer-master/LICENSE), same as upstream Pixer.
