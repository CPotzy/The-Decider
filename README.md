# The-Decider

ADHD-friendly Android app that surfaces one household or sadhana task at a time. Swipe right to do it, swipe left to defer. Backend schedules silently; pressure builds on overdue tasks.

## Install

Latest debug APK: https://github.com/CPotzy/The-Decider/releases/latest

On Android: enable "Install unknown apps" for your browser, download the APK, tap to install.

## Build locally

Requires JDK 17, Android SDK 34.

```
./gradlew :app:assembleDebug
```

## Tests

Unit tests (selection engine, parsing, domain logic):

```
./gradlew :app:testDebugUnitTest
```

Instrumented tests (DAOs — requires connected device or emulator):

```
./gradlew :app:connectedAndroidTest
```

## Docs

- Design spec: `docs/superpowers/specs/2026-05-21-the-decider-design.md`
- MVP implementation plan: `docs/superpowers/plans/2026-05-21-the-decider-mvp.md`
