# KidsStory Android App - Agent Guidelines

## Project Overview
KidsStory is an Android application for children that provides interactive storytelling with AI-generated content, text-to-speech, and multimedia features. Built with Clean Architecture using Kotlin, Jetpack Compose, Hilt, Room, and modern Android development practices.

## Project Structure & Module Organization
- **`app/src/main/java/com/example/kidsstory/`** - Main application code organized by Clean Architecture:
  - `data/` - Data layer (repositories, DAOs, network, local storage)
  - `domain/` - Domain layer (models, use cases, repository interfaces)
  - `presentation/` - Presentation layer (ViewModels, UI screens, navigation)
  - `di/` - Dependency injection modules (Hilt)
- **`app/src/main/res/`** - Android resources (layouts, strings, drawables)
- **`app/src/main/assets/stories/`** - Preset story JSON files
- **Root build files**: `build.gradle.kts`, `settings.gradle.kts`, `gradle/`

## Build, Test, and Development Commands

### Core Build Commands
- **`./gradlew build`** - Full build including compilation and unit tests
- **`./gradlew assembleDebug`** - Build debug APK only
- **`./gradlew assembleRelease`** - Build release APK (minified)
- **`./gradlew installDebug`** - Install debug build on connected device/emulator
- **`./gradlew clean`** - Clean all build outputs
- **`./gradlew clean build`** - Clean and full rebuild

### Testing Commands
- **`./gradlew test`** - Run all unit tests (JVM tests in `app/src/test/`)
- **`./gradlew testDebugUnitTest`** - Run debug unit tests specifically
- **`./gradlew connectedDebugAndroidTest`** - Run instrumentation tests on device
- **`./gradlew test --tests "*TestClass*"`** - Run specific test class
- **`./gradlew test --tests "*TestClass.testMethod*"`** - Run single test method

### Development Commands
- **`./gradlew compileDebugKotlin`** - Compile Kotlin sources only
- **`./gradlew lintDebug`** - Run Android lint checks
- **`./gradlew ktlintCheck`** - Run Kotlin code style checks (if configured)
- **`./gradlew check`** - Run all checks (lint, tests, etc.)

### Dependency Management
- **`./gradlew dependencyUpdates`** - Check for dependency updates (requires plugin)
- **`./gradlew app:dependencies`** - List all dependencies for app module

## Code Style & Conventions

### Kotlin Style Guidelines
- **Indentation**: 4 spaces (Kotlin standard)
- **Line Length**: 120 characters maximum
- **Naming Conventions**:
  - Classes/Interfaces: `PascalCase` (e.g., `StoryRepository`, `GetStoryByIdUseCase`)
  - Functions/Properties: `camelCase` (e.g., `getStoryById()`, `storyTitle`)
  - Constants: `UPPER_SNAKE_CASE` (e.g., `DEFAULT_TIMEOUT`)
  - File Names: Match primary class name (e.g., `StoryRepository.kt`)
- **Comments**: Use Chinese for user-facing strings, English for code comments
- **Documentation**: Comprehensive KDoc comments for public APIs

### Architecture Patterns
- **Clean Architecture**: Strict separation between layers
  - Domain models in `domain/model/` (pure Kotlin data classes)
  - Business logic in `domain/usecases/` (injected use cases)
  - Data access in `data/` (Room DAOs, network clients)
  - UI logic in `presentation/` (ViewModels with StateFlow)
- **Dependency Injection**: Hilt with constructor injection
- **Reactive Programming**: Kotlin Flow for async data streams
- **Repository Pattern**: Interface in domain, implementation in data layer

### Import Organization
```kotlin
// Group imports by package with blank lines between groups
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope

import com.example.kidsstory.domain.model.Story
import com.example.kidsstory.domain.usecases.GetAllStoriesUseCase

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import javax.inject.Inject
```

### Error Handling
- **ViewModels**: Use try-catch with StateFlow for UI state management
- **Use Cases**: Propagate exceptions to ViewModels for user feedback
- **Repositories**: Handle database/network errors gracefully
- **UI**: Show user-friendly error messages, never crash on exceptions

### Jetpack Compose Guidelines
- **State Management**: Use `StateFlow` in ViewModels, collect with `collectAsState()`
- **Modifiers**: Chain modifiers logically (padding → background → click)
- **Composable Functions**: Prefer stateless functions with parameters
- **Preview Functions**: Create `@Preview` functions for UI components
- **Accessibility**: Add `contentDescription` for screen readers

### Database & Data Layer
- **Room Entities**: Separate from domain models, use mappers for conversion
- **DAOs**: Interface-based with suspend functions for async operations
- **Migrations**: Version-controlled schema changes
- **Data Sources**: Separate local (Room) and remote (API) implementations

## Testing Guidelines

### Test Structure
- **Unit Tests**: `app/src/test/java/` - Pure JVM tests for business logic
- **Integration Tests**: `app/src/androidTest/java/` - Device/emulator tests
- **Test Naming**: `*Test` suffix (e.g., `StoryRepositoryTest`, `GetStoryByIdUseCaseTest`)

### Testing Patterns
```kotlin
// Use case testing example
@Test
fun `invoke returns story when found`() = runTest {
    // Given
    val expectedStory = mockStory()
    coEvery { repository.getStoryById("123") } returns expectedStory

    // When
    val result = useCase("123")

    // Then
    assertEquals(expectedStory, result)
    coVerify { repository.getStoryById("123") }
}
```

### Testing Libraries
- **Unit Tests**: JUnit 4/5, MockK for mocking, Kotlin Coroutines Test
- **UI Tests**: Espresso for instrumentation tests, Compose UI testing
- **Coverage**: Aim for 80%+ coverage on domain and data layers

## Security & Configuration

### API Keys & Secrets
- Store in `local.properties` (git-ignored)
- Access via `BuildConfig` fields
- Never commit real credentials
- Use environment-specific keys for different build variants

### Network Security
- Use HTTPS for all API calls
- Implement certificate pinning for production
- Validate SSL certificates
- Use OkHttp interceptors for logging (debug only)

### Data Privacy
- Minimize data collection for children
- Implement proper data retention policies
- Use encrypted local storage for sensitive data
- Comply with COPPA and privacy regulations

## Commit & Pull Request Guidelines

### Commit Messages
- **Format**: Short imperative summary (< 50 chars) + optional body
- **Examples**:
  - `Add AI story generation feature`
  - `Fix TTS audio playback issue`
  - `Update story filtering by category`
- **Scope**: Keep commits focused on single features/bugs
- **References**: Include issue numbers when applicable

### Pull Requests
- **Title**: Clear, descriptive summary of changes
- **Description**: Include context, testing steps, screenshots
- **Checklist**:
  - [ ] Code compiles without warnings
  - [ ] Unit tests pass
  - [ ] UI tests pass (if applicable)
  - [ ] Manual testing completed
  - [ ] Screenshots/videos included for UI changes
- **Review Process**: Require at least one approval for merges

## Development Workflow

### Feature Development
1. Create feature branch from `main`
2. Implement with TDD when possible
3. Add/update tests for new functionality
4. Run `./gradlew check` to verify
5. Create PR with comprehensive description

### Code Review Checklist
- [ ] Clean Architecture principles followed
- [ ] Dependency injection properly configured
- [ ] Error handling implemented
- [ ] Tests added/updated
- [ ] Code style guidelines followed
- [ ] Performance considerations addressed
- [ ] Security best practices applied

### Performance Considerations
- **Compose**: Use `remember` and `derivedStateOf` appropriately
- **Database**: Optimize queries, use proper indexing
- **Images**: Use Coil with proper sizing and caching
- **Memory**: Avoid memory leaks in ViewModels and Composables
- **Network**: Implement proper caching and pagination

## Known Issues & Solutions

### Preset Story Cover Images Not Displaying

**Problem**: Cover images for preset stories show gradient background instead of the actual image.

**Root Cause**: `PresetStoryDataSource.kt:loadCoverImage()` copies images from assets to internal storage and overrides the `coverImage` field with a file path:
```kotlin
// Source: app/src/main/java/com/example/kidsstory/data/local/PresetStoryDataSource.kt:63-100
val targetFile = File(context.filesDir, coverFileName)  // e.g., /data/user/0/.../preset_images/story_001_cover.png
context.assets.open(assetPath).use { inputStream ->
    FileOutputStream(targetFile).use { outputStream ->
        inputStream.copyTo(outputStream)
    }
}
return targetFile.absolutePath  // Overrides coverImage in StoryJson
```

**Solution**: Use `BitmapFactory.decodeFile()` to load images from file paths in `StoryLibraryScreen.kt`:
```kotlin
// Source: app/src/main/java/com/example/kidsstory/presentation/screens/library/StoryLibraryScreen.kt
@Composable
private fun loadAssetImage(assetPath: String): ImageBitmap? {
    val context = LocalContext.current
    return remember(assetPath) {
        var result: ImageBitmap? = null
        try {
            val file = if (assetPath.startsWith("/")) {
                File(assetPath)  // Absolute path from internal storage
            } else {
                File(context.filesDir, assetPath)
            }
            if (file.exists()) {
                result = BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        result
    }
}
```

**Why It Failed Previously**:
1. Code used `File(story.coverImage).exists()` which doesn't work for assets
2. Tried `file:///android_asset/` but Coil doesn't support this URI scheme
3. Old database had stale `coverImage` paths from before the fix

**Fix Steps**:
1. Update `StoryLibraryScreen.kt` to load images from file paths
2. Uninstall and reinstall the app to clear old database entries

### Preset Story Segment Images Not Displaying

**Problem**: Segment images for preset stories show gradient background instead of actual images.

**Root Cause**: In `StoryRepositoryImpl.kt:initializePresetStories()`, segment image paths were not properly loaded and saved to the database:

```kotlin
// WRONG - imagePath is always null
val segments = storyJson.segments.map { it.toEntity(storyJson.id, null) }
storySegmentDao.insertSegments(segments)
```

This resulted in:
1. Database `imageUrls` column being `null` for all segments
2. `StorySegment.image` field being `null` in domain layer
3. UI showing gradient background instead of images

**Solution**: Load images from assets and save absolute paths to database:

```kotlin
// CORRECT - load and save image paths
val segments = storyJson.segments.map { segment ->
    val imagePath = segment.image?.let {
        presetStoryDataSource.loadSegmentImage(storyJson.id, it)
    }
    segment.toEntity(storyJson.id, imagePath)
}
storySegmentDao.insertSegments(segments)
```

**Data Flow**:
1. `PresetStoryDataSource.loadSegmentImage()` copies image from assets to internal storage
2. Returns absolute path: `/data/user/0/com.example.kidsstory/files/preset_images/story_001_seg1.png`
3. Path is saved to Room database `story_segments.image_urls` column
4. Domain model `StorySegment.image` contains the path
5. UI uses `BitmapFactory.decodeFile()` to load image

**Why It Failed Previously**:
1. `initializePresetStories()` passed `null` for all segment image paths
2. Database had no image URLs, so domain layer had `null` images
3. UI checked `segment?.image`, found null, showed gradient

**Fix Steps**:
1. Update `StoryRepositoryImpl.initializePresetStories()` to load segment images
2. Uninstall and reinstall the app to reinitialize database with correct paths
