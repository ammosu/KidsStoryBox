# Repository Guidelines

## Project Structure & Module Organization
- `app/src/main/java/com/example/kidsstory/` holds Kotlin source organized by Clean Architecture: `data/`, `domain/`, `presentation/`, and `di/`.
- UI lives in `app/src/main/java/com/example/kidsstory/presentation/` with screens under `presentation/screens/` and navigation in `presentation/navigation/`.
- Resources are in `app/src/main/res/` and preset stories are in `app/src/main/assets/stories/` as JSON.
- Root build tooling is in `build.gradle.kts`, `settings.gradle.kts`, and `gradle/`.

## Build, Test, and Development Commands
- `./gradlew build` builds the app and runs unit tests.
- `./gradlew assembleDebug` produces a debug APK.
- `./gradlew installDebug` installs the debug build on a connected device/emulator.
- `./gradlew test` runs local JVM unit tests (none exist yet).
- `./gradlew clean` clears build outputs when you need a fresh build.

## Coding Style & Naming Conventions
- Kotlin follows the official style (4-space indentation, standard IntelliJ formatting).
- Naming: classes `PascalCase`, functions/properties `camelCase`, constants `UPPER_SNAKE_CASE`. File names should match the primary class.
- Keep layers separated: domain models in `domain/`, persistence/network models in `data/`, and UI state in `presentation/`.
- No formatter/linter is enforced in Gradle yet; run Android Studio “Reformat Code” before committing.

## Testing Guidelines
- Unit tests belong in `app/src/test/`, instrumentation tests in `app/src/androidTest/`.
- Use `*Test` suffixes (e.g., `StoryRepositoryTest`).
- If you add new use cases or data sources, add targeted unit tests and run `./gradlew test`.

## Commit & Pull Request Guidelines
- Current history uses short, sentence-case, imperative summaries (e.g., “Initial commit”, “Ignore .idea directory”). Keep commits concise and scoped.
- PRs should include a brief summary, testing steps, and screenshots for UI changes. Link related issues if applicable.

## Security & Configuration Tips
- API keys belong in `local.properties` (already git-ignored). Never commit real keys or credentials.
- Avoid committing generated files (`build/`, `.idea/`, or local SDK paths). Use placeholders like `path/to/KidsStoryBox` in docs.
