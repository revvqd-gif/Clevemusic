# Clevemusic

A native Android music player that streams audio from YouTube. Built with Kotlin and
Jetpack Compose, tuned for a dark interface with a lime accent.

![min SDK](https://img.shields.io/badge/minSdk-26-C6F94E)
![target SDK](https://img.shields.io/badge/targetSdk-34-C6F94E)

## Features

| | |
|---|---|
| **Search** | Full YouTube search with debounced input, skeleton loading, and result caching |
| **Playback** | Audio-only streaming through Media3 / ExoPlayer with background playback and lock-screen controls |
| **Queue** | Shuffle, repeat off / all / one, seek, skip |
| **Library** | Local playlists backed by Room, plus an automatically maintained "recently played" list |
| **Offline metadata** | Stream URLs cached for 6 hours so replays start instantly |
| **Design** | Dark + lime theme, Sora for brand type, Outfit for UI, Phosphor icons, elastic press animations |

## Tech stack

- **Language:** Kotlin 2.0.21
- **UI:** Jetpack Compose + Material3
- **Database:** Room
- **Playback:** Media3 1.6.1 (ExoPlayer + MediaSessionService)
- **Extraction:** NewPipe Extractor v0.27.1
- **Networking:** OkHttp 5.1.0
- **Images:** Coil 2.7.0
- **Fonts:** [Sora](https://fonts.google.com/specimen/Sora), [Outfit](https://fonts.google.com/specimen/Outfit)
- **Icons:** [Phosphor Icons](https://phosphoricons.com) (icon font, regular + fill)

## Building

GitHub Actions builds every push to `main` (see `.github/workflows/build.yml`). The APK
is attached to each workflow run as `clevemusic-debug`.

To publish a downloadable release, run the workflow manually with **release** enabled.

### Local build

```bash
./gradlew :app:assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

Requirements: JDK 17+, Android SDK platform 35 + build-tools 35.

## Project layout

```
app/src/main/java/com/cleve/music/
├── data/
│   ├── local/      Room entities, DAOs, database
│   ├── model/      Track, ResolvedTrack, RepeatMode
│   ├── remote/     NewPipe downloader adapter + YouTube extractor
│   └── repo/       MusicRepository (cache-aware)
├── player/         PlaybackService (MediaSessionService), PlayerHolder
└── ui/
    ├── common/     Theme, Phosphor icon set, press feedback, shared components
    ├── home/       Brand header, hero card, recent list
    ├── search/     Search field, suggestions, results
    ├── library/    Playlists and their contents
    └── player/     Mini player, expanded now-playing screen
```

## Design system

| Token | Value |
|---|---|
| Background | `#0B0D0A` |
| Surface | `#151A12` |
| Elevated surface | `#1C2318` |
| Accent (lime) | `#C6F94E` |
| Text primary | `#F2F6EC` |
| Text secondary | `#9AA793` |

Press feedback uses an asymmetric spring: fast, non-bouncy scale-down to `0.94` on touch,
then a slight overshoot on release, paired with a light haptic tick.

## Notes

- Audio is streamed directly from YouTube; nothing is downloaded or redistributed.
- Stream URLs expire, so the cache re-resolves after 6 hours.
- The app targets a personal, sideloaded build. It is not on any app store.

## License

Personal project. Third-party components keep their own licenses (NewPipe Extractor is
GPLv3; review it before distributing binaries publicly).
