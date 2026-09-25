# Location Logger

Android application for logging device location.

> Detailed requirements and feature scope will be added as the project progresses.

## Tech stack

| Area          | Choice                          |
|---------------|---------------------------------|
| Language      | Kotlin                          |
| UI            | Jetpack Compose, Material 3     |
| Min SDK       | 26 (Android 8.0)                |
| Target SDK    | 37                              |
| Build         | Gradle (Kotlin DSL), version catalog |

## Getting started

### Requirements

- Android Studio (latest stable)
- JDK 11 or newer (Gradle toolchain is resolved automatically)
- Android SDK Platform 37

### Build and run

```bash
# Debug build
./gradlew assembleDebug

# Install on a connected device / emulator
./gradlew installDebug
```

On Windows use `gradlew.bat` instead of `./gradlew`.

### Tests

```bash
./gradlew testDebugUnitTest          # unit tests
./gradlew connectedDebugAndroidTest  # instrumented tests (device required)
```

## Project structure

```
app/
  src/main/java/com/example/locationlogger/   application code
  src/test/                                   unit tests
  src/androidTest/                            instrumented tests
docs/
  CONVENTIONS.md                              coding and git conventions
```

## Documentation

- [Conventions](docs/CONVENTIONS.md)
