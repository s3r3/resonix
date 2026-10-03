# Repository Guidelines

## Project Structure

Resonix is a Kotlin and Jetpack Compose multi-module Android application. `app/` owns the launcher activity and top-level composition. Reusable layers live under `core/`; user-facing features live under `feature/`.

Kotlin source files live in `src/main/kotlin/`. Place local tests in `src/test/` and device tests in `src/androidTest/`. Do not modify `PRIVACY` or release metadata unless explicitly requested.

## Commands

Use the checked-in Gradle wrapper with JDK 17:

- `./gradlew :app:assembleDebug`
- `./gradlew test`
- `./gradlew ktlintCheck`
- `./gradlew ktlintFormat`

## Conventions

- Package names are lowercase under `com.resonix.media`.
- Follow `.editorconfig`: four-space indentation and trailing commas where supported.
- Use `PascalCase` for types and composables, `camelCase` for methods and properties.
- Keep dependencies and module boundaries minimal. Do not add speculative abstractions.

## Security

Never commit `local.properties`, keystores, signing passwords, credentials, or generated APK files. Supply release signing values through environment variables or local Gradle properties only.
