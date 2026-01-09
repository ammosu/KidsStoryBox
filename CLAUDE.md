# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

故事寶盒 (Kids Story App) is an Android application for children aged 3-6 that provides bilingual (Traditional Chinese/English) story playback with multi-character voices and AI-generated stories. The project uses **Clean Architecture** with Kotlin and Jetpack Compose.

**Current Status**: Week 2 completed - Story library and TTS player are functional with 5 preset stories.

## Build Commands

### Using Android Studio (Recommended)
- Open the project in Android Studio Hedgehog (2023.1.1) or newer
- Build & Run: Click the green play button or use Shift+F10
- The project requires JDK 17+ and Android SDK 34

### Using Gradle (Command Line)
```bash
# Build Commands
./gradlew build                 # Full build including compilation and unit tests
./gradlew assembleDebug         # Build debug APK only
./gradlew assembleRelease       # Build release APK (minified)
./gradlew installDebug          # Install to connected device/emulator
./gradlew clean                 # Clean all build outputs
./gradlew clean build           # Clean and full rebuild

# Testing Commands
./gradlew test                           # Run all unit tests
./gradlew testDebugUnitTest              # Run debug unit tests specifically
./gradlew connectedDebugAndroidTest      # Run instrumentation tests on device
./gradlew test --tests "*TestClass*"     # Run specific test class

# Development Commands
./gradlew compileDebugKotlin    # Compile Kotlin sources only
./gradlew lintDebug             # Run Android lint checks
./gradlew check                 # Run all checks (lint, tests, etc.)
./gradlew app:dependencies      # List all dependencies for app module
```

**Note**: If you encounter gradle-wrapper.jar issues, download it from:
```bash
cd gradle/wrapper
curl -L -o gradle-wrapper.jar https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar
```

## Architecture

### Clean Architecture Layers

**Domain Layer** (`domain/`) - Business logic core
- `model/`: Core entities (Story, StorySegment, Language, CharacterRole, StoryCategory)
- `repository/`: Repository interfaces
- `usecases/`: Single-purpose use cases following the Operator pattern (invoke operator)

**Data Layer** (`data/`) - Data management
- `database/`: Room database with 4 entities (StoryEntity, StorySegmentEntity, CharacterEntity, PlayHistoryEntity)
- `local/`: Local data sources (PresetStoryDataSource loads JSON from assets)
- `mapper/`: Bidirectional mapping between JSON ↔ Entity ↔ Domain Model
- `repository/`: Repository implementations
- `tts/`: Android TTS service (currently unused - TTS is integrated directly in ViewModel)

**Presentation Layer** (`presentation/`) - UI with Jetpack Compose
- `screens/library/`: Story list with filtering and language switching
- `screens/player/`: Story player with TTS playback controls
- `screens/ai_generation/`: AI story generation (placeholder, not yet implemented)
- `theme/`: Material 3 theme with child-friendly colors (pink/teal)

**Dependency Injection** (`di/`)
- `DatabaseModule`: Room database and DAOs
- `DataStoreModule`: Preferences storage
- `RepositoryModule`: Repository bindings and Gson provider

### Key Architecture Patterns

1. **UseCase Pattern**: Each use case has a single responsibility with `invoke()` operator
   ```kotlin
   class GetStoryByIdUseCase @Inject constructor(
       private val repository: StoryRepository
   ) {
       suspend operator fun invoke(storyId: String): Story? =
           repository.getStoryById(storyId)
   }
   ```

2. **Mapper Pattern**: Extension functions for clean conversion
   - JSON → Entity: `StoryJson.toEntity()`
   - Entity → Domain: `StoryEntity.toDomain(segments)`
   - Domain → Entity: `Story.toEntity()`

3. **StateFlow for UI State**: ViewModels expose `StateFlow<UiState>` for reactive UI updates

4. **Repository Pattern**: Single source of truth with Flow-based reactive data

## Critical Implementation Details

### TTS Integration
The project has **two TTS implementations**:
1. **`data/tts/AndroidTTSService.kt`**: Singleton service (currently unused)
2. **`StoryPlayerViewModel`**: Direct TTS integration (currently active)

The ViewModel implements `TextToSpeech.OnInitListener` and manages TTS lifecycle:
- Auto-advances to next segment on completion using `UtteranceProgressListener`
- Supports language switching (Locale.TAIWAN / Locale.US)
- Adjustable speech rate (0.5x-1.5x) and pitch (0.5-1.5)
- Properly cleans up TTS in `onCleared()`

### Data Initialization
On first launch, `KidsStoryApplication` initializes preset stories:
```kotlin
class KidsStoryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            initializePresetStoriesUseCase()
        }
    }
}
```

Stories are loaded from `assets/stories/story_*.json` files with JSON structure:
```json
{
  "id": "story_001",
  "title": "故事標題",
  "titleEn": "Story Title",
  "category": "FRIENDSHIP",
  "ageRange": "3-6",
  "segments": [
    {
      "sequenceNumber": 1,
      "contentZh": "中文內容",
      "contentEn": "English content",
      "characterRole": "NARRATOR",
      "duration": 5
    }
  ]
}
```

### Database Schema
**Room Database Version 1** with 4 tables:

- `stories`: Main story metadata with bilingual titles
- `story_segments`: Story content split into segments with character roles
- `characters`: Character definitions (not yet used)
- `play_history`: Playback tracking (not yet implemented)

The database uses **foreign key constraints** and requires migration strategies when schema changes.

### Bilingual Support Strategy
All text is stored in both Chinese and English:
- Domain models: `title` + `titleEn`, `contentZh` + `contentEn`
- UI switches based on `Language` enum (CHINESE, ENGLISH)
- Fallback logic: If preferred language is blank, fall back to the other
- TTS language changes are synchronized with UI language

## API Keys & Security Configuration

### API Keys Management
For AI features (Week 3+), add to `local.properties`:
```properties
GEMINI_API_KEY=your_gemini_api_key_here
ELEVENLABS_API_KEY=your_elevenlabs_api_key_here
```

Access in code via `BuildConfig.GEMINI_API_KEY` (requires build.gradle.kts configuration).

**Security Best Practices**:
- Store in `local.properties` (git-ignored)
- Never commit real credentials to repository
- Use environment-specific keys for different build variants
- Access via `BuildConfig` fields only

### Network Security
- Use HTTPS for all API calls
- Implement certificate pinning for production
- Validate SSL certificates
- Use OkHttp interceptors for logging (debug only)

### Data Privacy & Child Safety
- Minimize data collection for children (COPPA compliance)
- No data collection from children without parental consent
- Use encrypted local storage for sensitive data
- Implement proper data retention policies
- No external links or web views in child-accessible areas
- Content filtering required for AI-generated stories

## Development Roadmap

**Week 1**: ✅ Architecture setup (Room, Hilt, Navigation, Theme)
**Week 2**: ✅ Preset stories + Basic TTS player
**Week 3**: ⏳ Google Gemini API integration for AI story generation
**Week 4**: ⏳ ElevenLabs multi-character TTS integration
**Week 5**: ⏳ Offline support and download management
**Week 6**: ⏳ UI/UX optimization and Play Store preparation

## Important Constraints

1. **Target Audience**: 3-6 year old children
   - Large buttons and icons
   - Simple navigation
   - Bright, friendly colors (Pink #E91E63, Teal #00BCD4)
   - No dark mode (children prefer bright interfaces)

2. **Bilingual First**: All new features must support both Chinese and English
   - Always provide `title` + `titleEn`
   - Always provide `contentZh` + `contentEn` for story segments
   - Test language switching in UI

3. **Offline First**: Core playback must work without internet
   - Preset stories are bundled in APK
   - AI-generated stories must be cached permanently
   - Room database is the single source of truth

4. **Child Safety**:
   - No external links or web views
   - No ads in MVP
   - Content filtering required for AI-generated stories
   - COPPA compliance (no data collection from children)

## Performance Considerations

### Compose Performance
- Use `remember` for expensive calculations
- Use `derivedStateOf` for computed state
- Avoid recomposition with stable types
- Use `key()` for dynamic lists

### Database Optimization
- Optimize queries with proper indexing
- Use `@Transaction` for multi-table operations
- Implement pagination for large datasets
- Use Flow for reactive data updates

### Image Loading
- Use Coil with proper sizing and caching
- Load images asynchronously
- Use `BitmapFactory.decodeFile()` for local files
- Implement memory-efficient image handling

### Memory Management
- Avoid memory leaks in ViewModels and Composables
- Properly clean up resources in `onCleared()`
- Use weak references when necessary
- Monitor memory usage with Android Profiler

### Network Efficiency
- Implement proper caching strategies
- Use pagination for API calls
- Compress images before upload
- Handle offline scenarios gracefully

## Common Issues & Solutions

### Gradle Build Issues
- If seeing "Java X incompatible with Gradle Y" errors, check `gradle/wrapper/gradle-wrapper.properties`
- Current: Gradle 8.13, AGP 8.13.2, Kotlin 1.9.20
- Requires Java 21+ (OpenJDK recommended)

### Missing App Icons
- Vector drawables in `res/drawable/`: `ic_launcher_background.xml`, `ic_launcher_foreground.xml`
- Adaptive icons in `res/mipmap-anydpi-v26/`
- Copy to all density folders (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)

### Compose Syntax
- Use trailing lambda syntax for Composables:
  ```kotlin
  // Good
  ExtendedFloatingActionButton(onClick = { }) {
      Text("Button")
  }

  // Bad
  ExtendedFloatingActionButton(onClick = { }, text = { Text("Button") })
  ```

### TTS Language Not Working
- Ensure device has TTS data installed for both Chinese and English
- Check `StoryPlayerViewModel.setTtsLanguage()` for error handling
- Android emulators may not have all TTS voices installed

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

## File Naming Conventions

- Entities: `*Entity.kt` (e.g., `StoryEntity.kt`)
- DAOs: `*Dao.kt` (e.g., `StoryDao.kt`)
- ViewModels: `*ViewModel.kt` (e.g., `StoryPlayerViewModel.kt`)
- Screens: `*Screen.kt` (e.g., `StoryLibraryScreen.kt`)
- UseCases: `*UseCase.kt` (e.g., `GetAllStoriesUseCase.kt`)
- JSON models: `*Json.kt` (e.g., `StoryJson.kt`)
- Domain models: Plain nouns (e.g., `Story.kt`, `Language.kt`)

## Testing Strategy (Not Yet Implemented)

When adding tests:
- Unit tests for UseCases: Mock repository, test business logic
- ViewModel tests: Use `kotlinx-coroutines-test` for Flow testing
- DAO tests: Use in-memory database with `@RunWith(AndroidJUnit4::class)`
- Mapper tests: Verify bidirectional conversion accuracy

## Code Style

### Kotlin Style Guidelines
- **Indentation**: 4 spaces (Kotlin standard)
- **Line Length**: 120 characters maximum
- **Naming Conventions**:
  - Classes/Interfaces: `PascalCase` (e.g., `StoryRepository`, `GetStoryByIdUseCase`)
  - Functions/Properties: `camelCase` (e.g., `getStoryById()`, `storyTitle`)
  - Constants: `UPPER_SNAKE_CASE` (e.g., `DEFAULT_TIMEOUT`)
  - File Names: Match primary class name (e.g., `StoryRepository.kt`)

### Language & Documentation
- **Language**: All user-facing text in Traditional Chinese (台灣繁體中文)
- **Comments**: Use Chinese for user-facing strings, English for code comments
- **Documentation**: Comprehensive KDoc comments for public APIs
- **Variable names**: English camelCase
- **String resources**: Use `strings.xml` for UI text (currently minimal usage)

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

### Compose Best Practices
- **State Management**: Use `StateFlow` in ViewModels, collect with `collectAsState()`
- **Modifiers**: Chain modifiers logically (padding → background → click)
- **Stateless Components**: Prefer stateless composables with hoisted state
- **Preview Functions**: Create `@Preview` functions for UI components
- **Accessibility**: Add `contentDescription` for screen readers
- **Performance**: Use `remember` and `derivedStateOf` appropriately

### Coroutines & Async
- **ViewModels**: Use `viewModelScope` for ViewModel coroutines
- **Application**: Use `applicationScope` for Application-level tasks
- **Error Handling**: Use try-catch with StateFlow for UI state management
- **Flow**: Prefer Flow for reactive data streams

## Commit & Version Control Guidelines

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

### Feature Development Process
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
- [ ] Bilingual support verified (Chinese + English)
- [ ] Child safety requirements met

## Next Steps (Week 3)

When implementing AI story generation:
1. Add Gemini API client in `data/remote/GeminiStoryGenerator.kt`
2. Implement `AIGenerationViewModel` with form state management
3. Create content safety filter with keyword blacklist
4. Cache generated stories permanently in Room database
5. Map generated stories to same domain model structure
6. Consider implementing image generation or using preset illustration library

## Additional Resources

- **Project Structure**: See `app/src/main/java/com/example/kidsstory/` for organized codebase
- **Preset Stories**: Located in `app/src/main/assets/stories/story_*.json`
- **Build Files**: Root-level `build.gradle.kts`, `settings.gradle.kts`
- **For More Details**: Refer to AGENTS.md for comprehensive development guidelines
