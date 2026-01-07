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
# Build debug APK
./gradlew assembleDebug

# Install to connected device/emulator
./gradlew installDebug

# Run tests
./gradlew test

# Clean build
./gradlew clean build
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

## API Keys Configuration

For AI features (Week 3+), add to `local.properties`:
```properties
GEMINI_API_KEY=your_gemini_api_key_here
ELEVENLABS_API_KEY=your_elevenlabs_api_key_here
```

Access in code via `BuildConfig.GEMINI_API_KEY` (requires build.gradle.kts configuration).

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

- **Language**: All user-facing text in Traditional Chinese (台灣繁體中文)
- **Comments**: Traditional Chinese preferred for domain-specific comments
- **Variable names**: English camelCase
- **String resources**: Use `strings.xml` for UI text (currently minimal usage)
- **Compose**: Prefer stateless composables with hoisted state
- **Coroutines**: Use `viewModelScope` for ViewModel coroutines, `applicationScope` for Application-level tasks

## Next Steps (Week 3)

When implementing AI story generation:
1. Add Gemini API client in `data/remote/GeminiStoryGenerator.kt`
2. Implement `AIGenerationViewModel` with form state management
3. Create content safety filter with keyword blacklist
4. Cache generated stories permanently in Room database
5. Map generated stories to same domain model structure
6. Consider implementing image generation or using preset illustration library
