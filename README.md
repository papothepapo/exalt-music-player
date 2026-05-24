# Exalt Music

A compact Android music player designed for the LG Exalt LTE / Android 6 flip-phone form factor.

The app favors hardware-key navigation over touch:

- Album Artists, Albums, and Folders tabs are switched with D-pad left/right.
- Center D-pad opens items and toggles play/pause on the player screen.
- While playing, D-pad left/right seeks backward/forward by the configured number of seconds.
- Holding D-pad left/right accelerates seeking over time.
- While paused, D-pad left/right moves to previous/next track.
- The player menu is available from the menu/right soft key and includes playback speeds up to 2.5x.
- Playback uses a foreground notification with a Stop action.
- Hebrew and other RTL tags are displayed with Android RTL-aware text views.
- Track tags can be edited individually, and album/artist/folder groups can be edited in bulk as app-local metadata overrides.
- Settings include forward/back seek duration, font family, and font size.

## Compatibility target

- `minSdk 23` for Android 6.0 Marshmallow.
- Plain Java Android APIs, no touchscreen-oriented app framework dependency.
- CI builds the debug APK in GitHub Actions with the Android SDK and Gradle installed on the runner.

## CI

The workflow at `.github/workflows/android.yml` runs:

```sh
gradle --no-daemon assembleDebug
```

This repository intentionally does not require a local Gradle install or local Gradle build to verify the app.
