# AI Chatbot Feature Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Port the web app's AI chat experience into Android — floating button opens a full-screen slide-in panel with SSE streaming, tool-step pills, recipe artifact cards, and suggestion chips.

**Architecture:** A new `feature/chat` Gradle module holds all UI and ViewModel. Networking lives in `core/data` (new `AgentChatService` + `ChatThreadStore`), reusing the existing Ktor `HttpClient` and `BuildConfig.BACKEND_BASE_URL`. `AppNavHost` manages a `chatOpen` boolean that layers `ChatScreen` as a `Box` overlay above the nav bar.

**Tech Stack:** Kotlin, Jetpack Compose, Hilt, Ktor 2.3 (channelFlow SSE), kotlinx.serialization, DataStore Preferences, Turbine (tests), `multiplatform-markdown-renderer-m3:0.27.0`

## Global Constraints

- `minSdk = 26`, `compileSdk = 35`, `jvmTarget = "17"`
- All new Kotlin files use package `ai.algo1.marketbasket.*`
- `feature/chat` namespace: `ai.algo1.marketbasket.feature.chat`
- `core/data` namespace: `ai.algo1.marketbasket.core.data`
- Colors must match web exactly: user bubble `#080816`, assistant bubble `#f8f9fb`, input bg `#f3f4f6`
- Suggestion strings verbatim: `"Give me a recipe for pasta"`, `"Add eggs and milk to my list"`, `"What's on my list?"`
- No voice input — mic button omitted entirely

---

## File Map

**New in `core/data`:**
- `core/data/src/main/java/ai/algo1/marketbasket/core/data/di/Qualifiers.kt` — `@StreamingHttpClient` annotation
- `core/data/src/main/java/ai/algo1/marketbasket/core/data/remote/AgentChatModels.kt` — serializable event DTOs + `AgentEvent` sealed class
- `core/data/src/main/java/ai/algo1/marketbasket/core/data/remote/AgentChatRepository.kt` — interface
- `core/data/src/main/java/ai/algo1/marketbasket/core/data/remote/AgentChatService.kt` — Ktor SSE implementation
- `core/data/src/main/java/ai/algo1/marketbasket/core/data/local/ChatThreadStore.kt` — DataStore wrapper

**Modified in `core/data`:**
- `core/data/src/main/java/ai/algo1/marketbasket/core/data/di/DataModule.kt` — add `@StreamingHttpClient` binding + `AgentChatRepository` binding

**New `feature/chat` module:**
- `feature/chat/build.gradle.kts`
- `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/ChatModels.kt` — domain types
- `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/ChatViewModel.kt`
- `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/TypingDots.kt`
- `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/LiveStepsStrip.kt`
- `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/ChatInput.kt`
- `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/MessageBubble.kt`
- `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/MessageList.kt`
- `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/WelcomeState.kt`
- `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/ChatScreen.kt`
- `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/artifacts/RecipeCard.kt`
- `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/artifacts/CookingModal.kt`

**Modified in `app`:**
- `app/build.gradle.kts` — add `:feature:chat` dep
- `app/src/main/java/ai/algo1/marketbasket/nav/AppNavHost.kt` — wire chat overlay

**Modified at root:**
- `settings.gradle.kts` — `include(":feature:chat")`
- `gradle/libs.versions.toml` — markdown dep

---

### Task 1: Gradle Scaffolding

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `settings.gradle.kts`
- Create: `feature/chat/build.gradle.kts`
- Modify: `app/build.gradle.kts`
- Create: `core/data/src/main/java/ai/algo1/marketbasket/core/data/di/Qualifiers.kt`
- Modify: `core/data/src/main/java/ai/algo1/marketbasket/core/data/di/DataModule.kt`

**Interfaces:**
- Produces: `:feature:chat` Gradle module, `@StreamingHttpClient` qualifier, streaming `HttpClient` Hilt binding

- [ ] **Step 1: Add markdown dependency to version catalog**

In `gradle/libs.versions.toml`, add inside `[versions]`:
```toml
multiplatform-markdown-renderer = "0.27.0"
```
And inside `[libraries]`:
```toml
multiplatform-markdown-renderer-m3 = { group = "com.mikepenz", name = "multiplatform-markdown-renderer-m3", version.ref = "multiplatform-markdown-renderer" }
```

- [ ] **Step 2: Register the new module in settings**

In `settings.gradle.kts`, append after the last `include(...)`:
```kotlin
include(":feature:chat")
```

- [ ] **Step 3: Create the feature/chat module directory and build file**

Create `feature/chat/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "ai.algo1.marketbasket.feature.chat"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.multiplatform.markdown.renderer.m3)
    debugImplementation(libs.androidx.ui.tooling)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
```

Also create the source directory:
```
mkdir -p feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/artifacts
mkdir -p feature/chat/src/test/java/ai/algo1/marketbasket/feature/chat
```

- [ ] **Step 4: Add :feature:chat dependency to the app module**

In `app/build.gradle.kts`, add inside `dependencies { }` after the last `implementation(project(...))`:
```kotlin
implementation(project(":feature:chat"))
```

- [ ] **Step 5: Create the @StreamingHttpClient qualifier**

Create `core/data/src/main/java/ai/algo1/marketbasket/core/data/di/Qualifiers.kt`:
```kotlin
package ai.algo1.marketbasket.core.data.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class StreamingHttpClient
```

- [ ] **Step 6: Add streaming HttpClient binding to DataModule**

In `core/data/src/main/java/ai/algo1/marketbasket/core/data/di/DataModule.kt`, add these imports at the top:
```kotlin
import ai.algo1.marketbasket.core.data.remote.AgentChatRepository
import ai.algo1.marketbasket.core.data.remote.AgentChatService
import java.util.concurrent.TimeUnit
```

Then add inside `object DataModule { }` after the existing `provideHttpClient`:
```kotlin
@Provides
@Singleton
@StreamingHttpClient
fun provideStreamingHttpClient(json: Json): HttpClient = HttpClient(OkHttp) {
    install(ContentNegotiation) { json(json) }
    engine {
        config {
            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(0, TimeUnit.SECONDS)   // 0 = no read timeout — required for SSE
            writeTimeout(30, TimeUnit.SECONDS)
        }
    }
}

@Provides
@Singleton
fun provideAgentChatRepository(impl: AgentChatService): AgentChatRepository = impl
```

- [ ] **Step 7: Verify the project syncs**

Run:
```bash
./gradlew :feature:chat:assembleDebug
```
Expected: `BUILD SUCCESSFUL` (the module is empty but the Gradle config is valid).

- [ ] **Step 8: Commit**

```bash
git add gradle/libs.versions.toml settings.gradle.kts \
    feature/chat/build.gradle.kts \
    app/build.gradle.kts \
    core/data/src/main/java/ai/algo1/marketbasket/core/data/di/
git commit -m "feat(chat): add feature/chat gradle module and streaming http client"
```

---

### Task 2: AgentChatModels + ChatModels

**Files:**
- Create: `core/data/src/main/java/ai/algo1/marketbasket/core/data/remote/AgentChatModels.kt`
- Create: `core/data/src/main/java/ai/algo1/marketbasket/core/data/remote/AgentChatRepository.kt`
- Create: `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/ChatModels.kt`
- Create: `core/data/src/test/java/ai/algo1/marketbasket/core/data/AgentChatModelsTest.kt`

**Interfaces:**
- Produces: `AgentEvent` sealed class, `RawAgentEvent` DTO, `MessageRow`, `AgentChatRepository` interface, all domain types in `ChatModels.kt`
- Consumed by: Tasks 3 (AgentChatService), 4 (ChatThreadStore), 5 (ChatViewModel)

- [ ] **Step 1: Write the failing test for event parsing**

Create `core/data/src/test/java/ai/algo1/marketbasket/core/data/AgentChatModelsTest.kt`:
```kotlin
package ai.algo1.marketbasket.core.data

import ai.algo1.marketbasket.core.data.remote.RawAgentEvent
import ai.algo1.marketbasket.core.data.remote.AgentEvent
import ai.algo1.marketbasket.core.data.remote.toAgentEvent
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentChatModelsTest {
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    @Test fun `parses token event`() {
        val raw = """{"type":"token","text":"Hello"}"""
        val event = json.decodeFromString<RawAgentEvent>(raw).toAgentEvent()
        assertTrue(event is AgentEvent.Token)
        assertEquals("Hello", (event as AgentEvent.Token).text)
    }

    @Test fun `parses tool_pending event`() {
        val raw = """{"type":"tool_pending","tool":"search","label":"Searching"}"""
        val event = json.decodeFromString<RawAgentEvent>(raw).toAgentEvent()
        assertTrue(event is AgentEvent.ToolPending)
        assertEquals("search", (event as AgentEvent.ToolPending).tool)
        assertEquals("Searching", event.label)
    }

    @Test fun `parses tool_start event`() {
        val raw = """{"type":"tool_start","tool":"search","label":"Searching","input_summary":"pasta"}"""
        val event = json.decodeFromString<RawAgentEvent>(raw).toAgentEvent()
        assertTrue(event is AgentEvent.ToolStart)
        assertEquals("pasta", (event as AgentEvent.ToolStart).inputSummary)
    }

    @Test fun `parses tool_end success event`() {
        val raw = """{"type":"tool_end","tool":"search","success":true,"output_summary":"Found 3"}"""
        val event = json.decodeFromString<RawAgentEvent>(raw).toAgentEvent()
        assertTrue(event is AgentEvent.ToolEnd)
        assertTrue((event as AgentEvent.ToolEnd).success)
        assertEquals("Found 3", event.outputSummary)
    }

    @Test fun `parses final event with suggestions`() {
        val raw = """{"type":"final","response_text":"Done!","suggestions":["Next step","Try this"]}"""
        val event = json.decodeFromString<RawAgentEvent>(raw).toAgentEvent()
        assertTrue(event is AgentEvent.Final)
        assertEquals("Done!", (event as AgentEvent.Final).responseText)
        assertEquals(listOf("Next step", "Try this"), event.suggestions)
    }

    @Test fun `parses error event`() {
        val raw = """{"type":"error","message":"Oops"}"""
        val event = json.decodeFromString<RawAgentEvent>(raw).toAgentEvent()
        assertTrue(event is AgentEvent.Error)
        assertEquals("Oops", (event as AgentEvent.Error).message)
    }

    @Test fun `unknown type becomes Unknown`() {
        val raw = """{"type":"future_event","data":"x"}"""
        val event = json.decodeFromString<RawAgentEvent>(raw).toAgentEvent()
        assertTrue(event is AgentEvent.Unknown)
    }
}
```

- [ ] **Step 2: Run the test to confirm it fails**

```bash
./gradlew :core:data:test --tests "ai.algo1.marketbasket.core.data.AgentChatModelsTest" 2>&1 | tail -20
```
Expected: compilation error — `RawAgentEvent` not defined.

- [ ] **Step 3: Create AgentChatRepository interface**

Create `core/data/src/main/java/ai/algo1/marketbasket/core/data/remote/AgentChatRepository.kt`:
```kotlin
package ai.algo1.marketbasket.core.data.remote

import kotlinx.coroutines.flow.Flow

interface AgentChatRepository {
    fun streamChat(threadId: String, publicId: String, message: String): Flow<AgentEvent>
    suspend fun fetchThreadMessages(threadId: String, publicId: String): List<MessageRow>
}
```

- [ ] **Step 4: Create AgentChatModels**

Create `core/data/src/main/java/ai/algo1/marketbasket/core/data/remote/AgentChatModels.kt`:
```kotlin
package ai.algo1.marketbasket.core.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// ── Wire DTO ─────────────────────────────────────────────────────────────────

@Serializable
data class RawAgentEvent(
    val type: String,
    val text: String? = null,
    val tool: String? = null,
    val label: String? = null,
    @SerialName("input_summary") val inputSummary: String? = null,
    @SerialName("output_summary") val outputSummary: String? = null,
    val success: Boolean? = null,
    val kind: String? = null,
    val data: JsonElement? = null,
    @SerialName("response_text") val responseText: String? = null,
    val suggestions: List<String>? = null,
    val message: String? = null,
)

// ── Domain sealed class ───────────────────────────────────────────────────────

sealed class AgentEvent {
    data object Start : AgentEvent()
    data class Token(val text: String) : AgentEvent()
    data class ToolPending(val tool: String, val label: String) : AgentEvent()
    data class ToolStart(val tool: String, val label: String, val inputSummary: String?) : AgentEvent()
    data class ToolEnd(val tool: String, val success: Boolean, val outputSummary: String?) : AgentEvent()
    data class ArtifactEvent(val kind: String, val data: JsonElement) : AgentEvent()
    data class Final(val responseText: String?, val suggestions: List<String>?) : AgentEvent()
    data class Error(val message: String?) : AgentEvent()
    data class Unknown(val type: String) : AgentEvent()
}

fun RawAgentEvent.toAgentEvent(): AgentEvent = when (type) {
    "start"        -> AgentEvent.Start
    "token"        -> AgentEvent.Token(text ?: "")
    "tool_pending" -> AgentEvent.ToolPending(tool ?: "", label ?: "")
    "tool_start"   -> AgentEvent.ToolStart(tool ?: "", label ?: "", inputSummary)
    "tool_end"     -> AgentEvent.ToolEnd(tool ?: "", success ?: false, outputSummary)
    "artifact"     -> AgentEvent.ArtifactEvent(kind ?: "", data ?: kotlinx.serialization.json.JsonNull)
    "final"        -> AgentEvent.Final(responseText, suggestions)
    "error"        -> AgentEvent.Error(message)
    else           -> AgentEvent.Unknown(type)
}

// ── Thread hydration DTOs ─────────────────────────────────────────────────────

@Serializable
data class MessageRow(
    val role: String,
    val content: String,
    val artifact: JsonElement? = null,
)

@Serializable
data class ThreadMessagesResponse(
    val messages: List<MessageRow> = emptyList(),
)

@Serializable
data class StreamChatRequest(
    @SerialName("thread_id") val threadId: String,
    @SerialName("public_id") val publicId: String,
    val message: String,
)

// ── Recipe artifact DTO (for JSON decoding in ChatViewModel) ──────────────────

@Serializable
data class RecipeDataDto(
    val title: String,
    val cuisine: String,
    val servings: Int,
    @SerialName("prep_time_minutes") val prepTimeMinutes: Int,
    @SerialName("cook_time_minutes") val cookTimeMinutes: Int,
    val ingredients: List<RecipeIngredientDto>,
    val steps: List<String>,
    val tags: List<String>,
)

@Serializable
data class RecipeIngredientDto(val name: String, val quantity: String)
```

- [ ] **Step 5: Create ChatModels (domain types in feature/chat)**

Create `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/ChatModels.kt`:
```kotlin
package ai.algo1.marketbasket.feature.chat

enum class Role { User, Assistant }

enum class StepStatus { Pending, Running, Done, Error }

data class LiveStep(
    val tool: String,
    val label: String,
    val status: StepStatus,
    val inputSummary: String? = null,
    val outputSummary: String? = null,
)

sealed class Artifact {
    data class Recipe(val data: RecipeData) : Artifact()
}

data class RecipeData(
    val title: String,
    val cuisine: String,
    val servings: Int,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val ingredients: List<RecipeIngredient>,
    val steps: List<String>,
    val tags: List<String>,
)

data class RecipeIngredient(val name: String, val quantity: String)

data class ChatMessage(
    val id: String,
    val role: Role,
    val text: String,
    val artifacts: List<Artifact>,
    val steps: List<LiveStep>,
    val suggestions: List<String>,
)
```

- [ ] **Step 6: Run the tests and confirm they pass**

```bash
./gradlew :core:data:test --tests "ai.algo1.marketbasket.core.data.AgentChatModelsTest"
```
Expected: `BUILD SUCCESSFUL`, all 7 tests pass.

- [ ] **Step 7: Commit**

```bash
git add core/data/src/main/java/ai/algo1/marketbasket/core/data/remote/AgentChatModels.kt \
    core/data/src/main/java/ai/algo1/marketbasket/core/data/remote/AgentChatRepository.kt \
    feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/ChatModels.kt \
    core/data/src/test/java/ai/algo1/marketbasket/core/data/AgentChatModelsTest.kt
git commit -m "feat(chat): add AgentChatModels and ChatModels domain types"
```

---

### Task 3: ChatThreadStore

**Files:**
- Create: `core/data/src/main/java/ai/algo1/marketbasket/core/data/local/ChatThreadStore.kt`
- Create: `core/data/src/test/java/ai/algo1/marketbasket/core/data/ChatThreadStoreTest.kt`

**Interfaces:**
- Produces: `ChatThreadStore` — `suspend fun getOrCreateThreadId(publicId: String): String`
- Consumed by: Task 5 (ChatViewModel)

- [ ] **Step 1: Write the failing test**

Create `core/data/src/test/java/ai/algo1/marketbasket/core/data/ChatThreadStoreTest.kt`:
```kotlin
package ai.algo1.marketbasket.core.data

import ai.algo1.marketbasket.core.data.local.ChatThreadStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ChatThreadStoreTest {
    @get:Rule val tempFolder = TemporaryFolder()

    private fun store(): ChatThreadStore {
        val ds = PreferenceDataStoreFactory.create {
            tempFolder.newFile("chat_thread_test.preferences_pb")
        }
        return ChatThreadStore(ds)
    }

    @Test fun `creates UUID for new publicId`() = runTest {
        val s = store()
        val id = s.getOrCreateThreadId("user-abc")
        assertNotNull(id)
        assert(id.isNotBlank())
    }

    @Test fun `returns same UUID on second call`() = runTest {
        val s = store()
        val first = s.getOrCreateThreadId("user-abc")
        val second = s.getOrCreateThreadId("user-abc")
        assertEquals(first, second)
    }

    @Test fun `different publicIds get different threadIds`() = runTest {
        val s = store()
        val a = s.getOrCreateThreadId("user-aaa")
        val b = s.getOrCreateThreadId("user-bbb")
        assert(a != b)
    }
}
```

- [ ] **Step 2: Run to confirm failure**

```bash
./gradlew :core:data:test --tests "ai.algo1.marketbasket.core.data.ChatThreadStoreTest" 2>&1 | tail -10
```
Expected: compilation error — `ChatThreadStore` not defined.

- [ ] **Step 3: Create ChatThreadStore**

Create `core/data/src/main/java/ai/algo1/marketbasket/core/data/local/ChatThreadStore.kt`:
```kotlin
package ai.algo1.marketbasket.core.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatThreadStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun getOrCreateThreadId(publicId: String): String {
        val key = stringPreferencesKey("chat_thread_$publicId")
        return dataStore.data.map { it[key] }.first() ?: run {
            val newId = UUID.randomUUID().toString()
            dataStore.edit { it[key] = newId }
            newId
        }
    }
}
```

`ChatThreadStore` injects the same `DataStore<Preferences>` singleton that `LocalStore` uses. The key `"chat_thread_$publicId"` is namespaced, so it won't collide with any existing key in `LocalStore`.

The `DataStore<Preferences>` binding must be added to `DataModule`. Add these to `DataModule.kt`:

```kotlin
import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.hilt.android.qualifiers.ApplicationContext

// Add inside DataModule object:
@Provides
@Singleton
fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
    PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("market_basket") }
```

> **Note:** `LocalStore` currently uses `private val Context.dataStore by preferencesDataStore(name = "market_basket")`. This Hilt-provided `DataStore<Preferences>` points to the same file (`market_basket.preferences_pb`), so data is shared. Remove the private extension property from `LocalStore.kt` and change it to inject `DataStore<Preferences>`:

Update `LocalStore.kt` — replace the file-level `private val Context.dataStore` line and the class constructor:
```kotlin
// REMOVE this line:
// private val Context.dataStore by preferencesDataStore(name = "market_basket")

// Change class to:
@Singleton
class LocalStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,  // inject instead of extension prop
) {
    // ... rest unchanged, all context.dataStore references become just dataStore
    val publicId: Flow<String?> = dataStore.data.map { it[Keys.PUBLIC_ID] }
    val listCode: Flow<String?> = dataStore.data.map { it[Keys.LIST_CODE] }
    val connected: Flow<Boolean> = dataStore.data.map { it[Keys.CONNECTED] ?: false }
    val homeIntroSeen: Flow<Boolean> = dataStore.data.map { it[Keys.HOME_INTRO_SEEN] ?: false }

    suspend fun setPublicId(value: String) { dataStore.edit { it[Keys.PUBLIC_ID] = value } }
    suspend fun setListCode(value: String) { dataStore.edit { it[Keys.LIST_CODE] = value } }
    suspend fun setConnected(value: Boolean) { dataStore.edit { it[Keys.CONNECTED] = value } }
    suspend fun setHomeIntroSeen() { dataStore.edit { it[Keys.HOME_INTRO_SEEN] = true } }
}
```

Also add these imports to `LocalStore.kt`:
```kotlin
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
```
Remove the `preferencesDataStore` import.

- [ ] **Step 4: Run the tests and confirm they pass**

```bash
./gradlew :core:data:test --tests "ai.algo1.marketbasket.core.data.ChatThreadStoreTest"
```
Expected: `BUILD SUCCESSFUL`, all 3 tests pass.

- [ ] **Step 5: Verify the full core:data test suite still passes**

```bash
./gradlew :core:data:test
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add core/data/src/main/java/ai/algo1/marketbasket/core/data/local/ \
    core/data/src/main/java/ai/algo1/marketbasket/core/data/di/DataModule.kt \
    core/data/src/test/java/ai/algo1/marketbasket/core/data/ChatThreadStoreTest.kt
git commit -m "feat(chat): add ChatThreadStore and inject DataStore<Preferences> via Hilt"
```

---

### Task 4: AgentChatService

**Files:**
- Create: `core/data/src/main/java/ai/algo1/marketbasket/core/data/remote/AgentChatService.kt`
- Create: `core/data/src/test/java/ai/algo1/marketbasket/core/data/AgentChatServiceTest.kt`

**Interfaces:**
- Consumes: `AgentChatRepository`, `RawAgentEvent`, `toAgentEvent()`, `StreamChatRequest`, `ThreadMessagesResponse`, `@StreamingHttpClient HttpClient`, `HttpClient` (regular), `Json`, `BuildConfig.BACKEND_BASE_URL`
- Produces: `AgentChatService` — implements `AgentChatRepository`

- [ ] **Step 1: Write the failing test**

Create `core/data/src/test/java/ai/algo1/marketbasket/core/data/AgentChatServiceTest.kt`:
```kotlin
package ai.algo1.marketbasket.core.data

import ai.algo1.marketbasket.core.data.remote.AgentEvent
import ai.algo1.marketbasket.core.data.remote.RawAgentEvent
import ai.algo1.marketbasket.core.data.remote.toAgentEvent
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Unit-tests the SSE line parsing logic in isolation — no HTTP client needed.
class AgentChatServiceTest {
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    private fun parseLine(line: String): AgentEvent? {
        val trimmed = line.trimStart()
        if (!trimmed.startsWith("data:")) return null
        val raw = trimmed.removePrefix("data:").trim()
        if (raw == "[DONE]" || raw.isEmpty()) return null
        return try { json.decodeFromString<RawAgentEvent>(raw).toAgentEvent() } catch (_: Exception) { null }
    }

    @Test fun `parses token line`() {
        val event = parseLine("""data: {"type":"token","text":"Hi"}""")
        assertTrue(event is AgentEvent.Token)
        assertEquals("Hi", (event as AgentEvent.Token).text)
    }

    @Test fun `ignores non-data lines`() {
        val event = parseLine("event: message")
        assertEquals(null, event)
    }

    @Test fun `returns null for DONE sentinel`() {
        val event = parseLine("data: [DONE]")
        assertEquals(null, event)
    }

    @Test fun `returns null for malformed json`() {
        val event = parseLine("data: {broken}")
        assertEquals(null, event)
    }

    @Test fun `parses data line with leading whitespace`() {
        val event = parseLine("  data: {\"type\":\"token\",\"text\":\"x\"}")
        assertTrue(event is AgentEvent.Token)
    }
}
```

- [ ] **Step 2: Run to confirm failure**

```bash
./gradlew :core:data:test --tests "ai.algo1.marketbasket.core.data.AgentChatServiceTest" 2>&1 | tail -5
```
Expected: tests pass immediately (they only test the parsing logic already written in Task 2). If so, move directly to Step 3.

- [ ] **Step 3: Create AgentChatService**

Create `core/data/src/main/java/ai/algo1/marketbasket/core/data/remote/AgentChatService.kt`:
```kotlin
package ai.algo1.marketbasket.core.data.remote

import ai.algo1.marketbasket.core.data.BuildConfig
import ai.algo1.marketbasket.core.data.di.StreamingHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentChatService @Inject constructor(
    @StreamingHttpClient private val streamingClient: HttpClient,
    private val httpClient: HttpClient,
    private val json: Json,
) : AgentChatRepository {

    private val base = BuildConfig.BACKEND_BASE_URL.trimEnd('/')

    override fun streamChat(
        threadId: String,
        publicId: String,
        message: String,
    ): Flow<AgentEvent> = channelFlow {
        streamingClient.preparePost("$base/api/agent/chat/stream") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Accept, "text/event-stream")
            setBody(StreamChatRequest(threadId = threadId, publicId = publicId, message = message))
        }.execute { response ->
            val channel = response.bodyAsChannel()
            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: break
                val trimmed = line.trimStart()
                if (!trimmed.startsWith("data:")) continue
                val raw = trimmed.removePrefix("data:").trim()
                if (raw == "[DONE]") break
                if (raw.isEmpty()) continue
                try {
                    val rawEvent = json.decodeFromString<RawAgentEvent>(raw)
                    send(rawEvent.toAgentEvent())
                } catch (_: Exception) { /* skip malformed line */ }
            }
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun fetchThreadMessages(
        threadId: String,
        publicId: String,
    ): List<MessageRow> = try {
        httpClient.get("$base/api/agent/threads/$threadId") {
            parameter("public_id", publicId)
        }.body<ThreadMessagesResponse>().messages
    } catch (_: Exception) {
        emptyList()
    }
}
```

- [ ] **Step 4: Run all core:data tests**

```bash
./gradlew :core:data:test
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add core/data/src/main/java/ai/algo1/marketbasket/core/data/remote/AgentChatService.kt \
    core/data/src/test/java/ai/algo1/marketbasket/core/data/AgentChatServiceTest.kt
git commit -m "feat(chat): add AgentChatService SSE streaming client"
```

---

### Task 5: ChatViewModel

**Files:**
- Create: `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/ChatViewModel.kt`
- Create: `feature/chat/src/test/java/ai/algo1/marketbasket/feature/chat/ChatViewModelTest.kt`

**Interfaces:**
- Consumes: `AgentChatRepository` (streamChat, fetchThreadMessages), `ChatThreadStore.getOrCreateThreadId`, `ListRepository.publicId: StateFlow<String?>`, `Json`, all types from `ChatModels.kt`, `AgentEvent`, `RecipeDataDto`, `RecipeIngredientDto`
- Produces: `ChatViewModel` with `messages`, `streamingText`, `liveSteps`, `isLoading` StateFlows; `sendMessage(String)`, `abort()`, `handleAddIngredients(String, List<String>)`

- [ ] **Step 1: Write the failing test**

Create `feature/chat/src/test/java/ai/algo1/marketbasket/feature/chat/ChatViewModelTest.kt`:
```kotlin
package ai.algo1.marketbasket.feature.chat

import ai.algo1.marketbasket.core.data.local.ChatThreadStore
import ai.algo1.marketbasket.core.data.remote.AgentChatRepository
import ai.algo1.marketbasket.core.data.remote.AgentEvent
import ai.algo1.marketbasket.core.data.remote.MessageRow
import ai.algo1.marketbasket.core.data.repository.ListRepository
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ChatViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    // Minimal fake ListRepository that exposes publicId
    private class FakeListRepository(id: String?) : ListRepository {
        override val publicId = MutableStateFlow(id)
        // all other ListRepository members — leave unimplemented (throw)
        override val items get() = throw UnsupportedOperationException()
        override suspend fun load(publicId: String) = throw UnsupportedOperationException()
        override fun observeRealtime(scope: kotlinx.coroutines.CoroutineScope) = throw UnsupportedOperationException()
    }

    private class FakeChatThreadStore : ChatThreadStore(
        androidx.datastore.preferences.core.PreferenceDataStoreFactory.create {
            java.io.File(System.getProperty("java.io.tmpdir"), "test_chat_thread.preferences_pb")
        }
    ) {
        override suspend fun getOrCreateThreadId(publicId: String) = "test-thread-id"
    }

    private fun makeRepo(vararg events: AgentEvent): AgentChatRepository = object : AgentChatRepository {
        override fun streamChat(t: String, p: String, m: String): Flow<AgentEvent> = flowOf(*events)
        override suspend fun fetchThreadMessages(t: String, p: String): List<MessageRow> = emptyList()
    }

    private fun viewModel(repo: AgentChatRepository): ChatViewModel = ChatViewModel(
        listRepository = FakeListRepository("pub-123"),
        agentChatRepository = repo,
        chatThreadStore = FakeChatThreadStore(),
        json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true; explicitNulls = false },
    )

    @Before fun setUp() { Dispatchers.setMain(testDispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun `sendMessage appends user message immediately`() = runTest {
        val vm = viewModel(makeRepo(AgentEvent.Final(responseText = "OK", suggestions = null)))
        vm.messages.test {
            awaitItem() // initial empty list
            vm.sendMessage("hello")
            val afterSend = awaitItem()
            assertTrue(afterSend.any { it.role == Role.User && it.text == "hello" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun `token events accumulate in streamingText`() = runTest {
        val repo = makeRepo(
            AgentEvent.Token("Hel"),
            AgentEvent.Token("lo"),
            AgentEvent.Final(responseText = "Hello", suggestions = null),
        )
        val vm = viewModel(repo)
        vm.sendMessage("hi")
        testDispatcher.scheduler.advanceUntilIdle()
        // after final, streamingText clears
        assertEquals("", vm.streamingText.value)
        // assistant message added
        assertTrue(vm.messages.value.any { it.role == Role.Assistant && it.text == "Hello" })
    }

    @Test fun `abort clears loading state`() = runTest {
        val repo = object : AgentChatRepository {
            override fun streamChat(t: String, p: String, m: String): Flow<AgentEvent> =
                kotlinx.coroutines.flow.flow { kotlinx.coroutines.delay(10_000) }
            override suspend fun fetchThreadMessages(t: String, p: String) = emptyList<MessageRow>()
        }
        val vm = viewModel(repo)
        vm.sendMessage("hi")
        testDispatcher.scheduler.advanceTimeBy(100)
        vm.abort()
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(vm.isLoading.value)
        assertEquals("", vm.streamingText.value)
    }

    @Test fun `error event appends assistant error message`() = runTest {
        val vm = viewModel(makeRepo(AgentEvent.Error("Something went wrong. Please try again.")))
        vm.sendMessage("hi")
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.messages.value.any { it.role == Role.Assistant && it.text.contains("Something went wrong") })
        assertFalse(vm.isLoading.value)
    }
}
```

- [ ] **Step 2: Run to confirm failure**

```bash
./gradlew :feature:chat:test --tests "ai.algo1.marketbasket.feature.chat.ChatViewModelTest" 2>&1 | tail -10
```
Expected: compilation error — `ChatViewModel` not defined.

- [ ] **Step 3: Create ChatViewModel**

Create `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/ChatViewModel.kt`:
```kotlin
package ai.algo1.marketbasket.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.algo1.marketbasket.core.data.local.ChatThreadStore
import ai.algo1.marketbasket.core.data.remote.AgentChatRepository
import ai.algo1.marketbasket.core.data.remote.AgentEvent
import ai.algo1.marketbasket.core.data.remote.RecipeDataDto
import ai.algo1.marketbasket.core.data.repository.ListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.decodeFromJsonElement
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val listRepository: ListRepository,
    private val agentChatRepository: AgentChatRepository,
    private val chatThreadStore: ChatThreadStore,
    private val json: Json,
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _streamingText = MutableStateFlow("")
    val streamingText: StateFlow<String> = _streamingText.asStateFlow()

    private val _liveSteps = MutableStateFlow<List<LiveStep>>(emptyList())
    val liveSteps: StateFlow<List<LiveStep>> = _liveSteps.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _addedRecipeIds = MutableStateFlow<Set<String>>(emptySet())
    val addedRecipeIds: StateFlow<Set<String>> = _addedRecipeIds.asStateFlow()

    private var activeJob: Job? = null
    private var currentPublicId: String? = null
    private var currentThreadId: String? = null

    init {
        viewModelScope.launch {
            listRepository.publicId.filterNotNull().collect { publicId ->
                if (publicId != currentPublicId) {
                    currentPublicId = publicId
                    currentThreadId = chatThreadStore.getOrCreateThreadId(publicId)
                    hydrateMessages()
                }
            }
        }
    }

    private suspend fun hydrateMessages() {
        val pid = currentPublicId ?: return
        val tid = currentThreadId ?: return
        val rows = agentChatRepository.fetchThreadMessages(tid, pid)
        _messages.value = rows.mapNotNull { row ->
            when (row.role) {
                "user" -> ChatMessage(
                    id = UUID.randomUUID().toString(),
                    role = Role.User,
                    text = row.content,
                    artifacts = emptyList(),
                    steps = emptyList(),
                    suggestions = emptyList(),
                )
                "assistant" -> ChatMessage(
                    id = UUID.randomUUID().toString(),
                    role = Role.Assistant,
                    text = row.content,
                    artifacts = row.artifact?.let { parseArtifact("recipe", it) }?.let { listOf(it) } ?: emptyList(),
                    steps = emptyList(),
                    suggestions = emptyList(),
                )
                else -> null
            }
        }
    }

    fun sendMessage(text: String) {
        val pid = currentPublicId ?: return
        val tid = currentThreadId ?: return
        if (text.isBlank() || _isLoading.value) return

        activeJob?.cancel()

        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = Role.User,
            text = text,
            artifacts = emptyList(),
            steps = emptyList(),
            suggestions = emptyList(),
        )
        _messages.value = _messages.value + userMsg
        _isLoading.value = true
        _streamingText.value = ""
        _liveSteps.value = emptyList()

        activeJob = viewModelScope.launch {
            val stepsAcc = mutableListOf<LiveStep>()
            val artifactsAcc = mutableListOf<Artifact>()
            var textAcc = ""

            try {
                agentChatRepository.streamChat(tid, pid, text).collect { event ->
                    when (event) {
                        is AgentEvent.Token -> {
                            textAcc += event.text
                            _streamingText.value = textAcc
                        }
                        is AgentEvent.ToolPending -> {
                            val alreadyTracked = stepsAcc.any {
                                it.tool == event.tool && it.status != StepStatus.Done && it.status != StepStatus.Error
                            }
                            if (!alreadyTracked) {
                                stepsAcc.add(LiveStep(tool = event.tool, label = event.label, status = StepStatus.Pending))
                                _liveSteps.value = stepsAcc.toList()
                            }
                        }
                        is AgentEvent.ToolStart -> {
                            val idx = stepsAcc.indexOfLast {
                                it.tool == event.tool && (it.status == StepStatus.Pending || it.status == StepStatus.Running)
                            }
                            if (idx >= 0) {
                                stepsAcc[idx] = stepsAcc[idx].copy(status = StepStatus.Running, inputSummary = event.inputSummary)
                            } else {
                                stepsAcc.add(LiveStep(tool = event.tool, label = event.label, status = StepStatus.Running, inputSummary = event.inputSummary))
                            }
                            _liveSteps.value = stepsAcc.toList()
                        }
                        is AgentEvent.ToolEnd -> {
                            val idx = stepsAcc.indexOfLast {
                                it.tool == event.tool && (it.status == StepStatus.Running || it.status == StepStatus.Pending)
                            }
                            if (idx >= 0) {
                                stepsAcc[idx] = stepsAcc[idx].copy(
                                    status = if (event.success) StepStatus.Done else StepStatus.Error,
                                    outputSummary = event.outputSummary,
                                )
                                _liveSteps.value = stepsAcc.toList()
                            }
                        }
                        is AgentEvent.ArtifactEvent -> {
                            parseArtifact(event.kind, event.data)?.let { artifactsAcc.add(it) }
                        }
                        is AgentEvent.Final -> {
                            val finalText = event.responseText?.takeIf { it.isNotEmpty() } ?: textAcc
                            val assistantMsg = ChatMessage(
                                id = UUID.randomUUID().toString(),
                                role = Role.Assistant,
                                text = finalText,
                                artifacts = artifactsAcc.toList(),
                                steps = stepsAcc.toList(),
                                suggestions = event.suggestions ?: emptyList(),
                            )
                            _messages.value = _messages.value + assistantMsg
                        }
                        is AgentEvent.Error -> {
                            val errMsg = ChatMessage(
                                id = UUID.randomUUID().toString(),
                                role = Role.Assistant,
                                text = event.message ?: "Something went wrong. Please try again.",
                                artifacts = emptyList(),
                                steps = stepsAcc.toList(),
                                suggestions = emptyList(),
                            )
                            _messages.value = _messages.value + errMsg
                        }
                        else -> { /* Start, Unknown — ignore */ }
                    }
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                val networkErr = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    role = Role.Assistant,
                    text = "Network error — please check your connection and try again.",
                    artifacts = emptyList(),
                    steps = emptyList(),
                    suggestions = emptyList(),
                )
                _messages.value = _messages.value + networkErr
            } finally {
                _streamingText.value = ""
                _liveSteps.value = emptyList()
                _isLoading.value = false
            }
        }
    }

    fun abort() {
        activeJob?.cancel()
        activeJob = null
        _isLoading.value = false
        _streamingText.value = ""
        _liveSteps.value = emptyList()
    }

    fun handleAddIngredients(messageId: String, ingredients: List<String>) {
        _addedRecipeIds.value = _addedRecipeIds.value + messageId
        sendMessage("Add these ingredients to my list: ${ingredients.joinToString(", ")}")
    }

    private fun parseArtifact(kind: String, data: JsonElement): Artifact? {
        if (kind != "recipe" || data == JsonNull) return null
        return try {
            val dto = json.decodeFromJsonElement<RecipeDataDto>(data)
            Artifact.Recipe(
                RecipeData(
                    title = dto.title,
                    cuisine = dto.cuisine,
                    servings = dto.servings,
                    prepTimeMinutes = dto.prepTimeMinutes,
                    cookTimeMinutes = dto.cookTimeMinutes,
                    ingredients = dto.ingredients.map { RecipeIngredient(it.name, it.quantity) },
                    steps = dto.steps,
                    tags = dto.tags,
                )
            )
        } catch (_: Exception) { null }
    }
}
```

- [ ] **Step 4: Fix the fake in the test — ChatThreadStore is not open**

The test uses `FakeChatThreadStore` as a subclass, but `ChatThreadStore` is a concrete Hilt `@Singleton`. Change the test's fake to delegate via a wrapper instead:

Replace `FakeChatThreadStore` in the test with an interface approach — create `ChatThreadRepository` interface:

Add to `core/data/.../local/ChatThreadStore.kt` just above the class:
```kotlin
interface ChatThreadRepository {
    suspend fun getOrCreateThreadId(publicId: String): String
}
```

Then `ChatThreadStore` implements it: change `class ChatThreadStore` to `class ChatThreadStore @Inject constructor(...) : ChatThreadRepository`.

Update `ChatViewModel` to inject `ChatThreadRepository` instead of `ChatThreadStore`.

In the test, replace `FakeChatThreadStore` with:
```kotlin
private val fakeChatThreadRepo = object : ChatThreadRepository {
    override suspend fun getOrCreateThreadId(publicId: String) = "test-thread-id"
}
```
And update `viewModel()` to pass `fakeChatThreadRepo`.

Also add `ChatThreadRepository` binding in `DataModule.kt`:
```kotlin
import ai.algo1.marketbasket.core.data.local.ChatThreadRepository

@Provides
@Singleton
fun provideChatThreadRepository(impl: ChatThreadStore): ChatThreadRepository = impl
```

- [ ] **Step 5: Run the tests and confirm they pass**

```bash
./gradlew :feature:chat:test --tests "ai.algo1.marketbasket.feature.chat.ChatViewModelTest"
```
Expected: `BUILD SUCCESSFUL`, all 4 tests pass.

- [ ] **Step 6: Commit**

```bash
git add feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/ChatViewModel.kt \
    feature/chat/src/test/java/ai/algo1/marketbasket/feature/chat/ChatViewModelTest.kt \
    core/data/src/main/java/ai/algo1/marketbasket/core/data/local/ChatThreadStore.kt \
    core/data/src/main/java/ai/algo1/marketbasket/core/data/di/DataModule.kt
git commit -m "feat(chat): add ChatViewModel with SSE state management"
```

---

### Task 6: TypingDots + LiveStepsStrip

**Files:**
- Create: `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/TypingDots.kt`
- Create: `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/LiveStepsStrip.kt`

**Interfaces:**
- Consumes: `LiveStep`, `StepStatus` from `ChatModels.kt`
- Produces: `@Composable fun TypingDots()`, `@Composable fun LiveStepsStrip(steps: List<LiveStep>)`

- [ ] **Step 1: Create TypingDots**

Create `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/TypingDots.kt`:
```kotlin
package ai.algo1.marketbasket.feature.chat

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DotColor = Color(0xFF9CA3AF)

@Composable
fun TypingDots(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "typing_dots")
    val alphas = (0..2).map { i ->
        transition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 600, delayMillis = i * 200),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "dot_alpha_$i",
        )
    }
    Row(
        modifier = modifier.padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        alphas.forEach { alpha ->
            val a by alpha
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(a)
                    .background(DotColor, CircleShape),
            )
        }
    }
}
```

- [ ] **Step 2: Create LiveStepsStrip**

Create `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/LiveStepsStrip.kt`:
```kotlin
package ai.algo1.marketbasket.feature.chat

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LiveStepsStrip(steps: List<LiveStep>, modifier: Modifier = Modifier) {
    if (steps.isEmpty()) return
    Column(modifier = modifier.padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        steps.forEachIndexed { i, step -> StepPill(step = step, key = "$i-${step.tool}") }
    }
}

@Composable
private fun StepPill(step: LiveStep, key: String) {
    when (step.status) {
        StepStatus.Pending -> PendingPill(step.label)
        StepStatus.Running -> RunningPill(step.inputSummary ?: step.label)
        StepStatus.Done    -> DonePill(step.outputSummary ?: step.label)
        StepStatus.Error   -> ErrorPill(step.outputSummary ?: step.label)
    }
}

@Composable
private fun PendingPill(label: String) {
    val transition = rememberInfiniteTransition(label = "pending_shimmer")
    val alpha by transition.animateFloat(
        initialValue = 1f, targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "pending_alpha",
    )
    PillRow(bgColor = Color(0xFFF3F4F6), textColor = Color(0xFF6B7280), modifier = Modifier.alpha(alpha)) {
        Box(Modifier.size(8.dp).background(Color(0xFFD1D5DB), CircleShape))
        Text("$label…", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280))
    }
}

@Composable
private fun RunningPill(label: String) {
    val transition = rememberInfiniteTransition(label = "running_spin")
    val rotation by transition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Restart),
        label = "running_rotation",
    )
    PillRow(bgColor = Color(0xFFEFF6FF), textColor = Color(0xFF2563EB)) {
        Box(
            Modifier.size(10.dp).rotate(rotation)
                .background(Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            // Spinner ring drawn via inner/outer contrast
            Box(Modifier.size(10.dp).background(Color(0xFF2563EB), CircleShape))
            Box(Modifier.size(6.dp).background(Color(0xFFEFF6FF), CircleShape))
        }
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2563EB))
    }
}

@Composable
private fun DonePill(label: String) {
    PillRow(bgColor = Color(0xFFF0FDF4), textColor = Color(0xFF15803D)) {
        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF15803D))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF15803D))
    }
}

@Composable
private fun ErrorPill(label: String) {
    PillRow(bgColor = Color(0xFFFEF2F2), textColor = Color(0xFFDC2626)) {
        Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFDC2626))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFFDC2626))
    }
}

@Composable
private fun PillRow(
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) { content() }
}
```

- [ ] **Step 3: Build to verify no compile errors**

```bash
./gradlew :feature:chat:assembleDebug
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/TypingDots.kt \
    feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/LiveStepsStrip.kt
git commit -m "feat(chat): add TypingDots and LiveStepsStrip composables"
```

---

### Task 7: ChatInput

**Files:**
- Create: `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/ChatInput.kt`

**Interfaces:**
- Produces: `@Composable fun ChatInput(onSend: (String) -> Unit, onAbort: () -> Unit, isLoading: Boolean)`

- [ ] **Step 1: Create ChatInput**

Create `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/ChatInput.kt`:
```kotlin
package ai.algo1.marketbasket.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val InputBg    = Color(0xFFF3F4F6)
private val BrandInk   = Color(0xFF080816)
private val PlaceColor = Color(0xFF9CA3AF)

@Composable
fun ChatInput(
    onSend: (String) -> Unit,
    onAbort: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, bottom = 16.dp, top = 8.dp)
            .background(InputBg, RoundedCornerShape(22.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        // Text field
        Box(modifier = Modifier.weight(1f).heightIn(min = 36.dp, max = 120.dp)) {
            if (text.isEmpty()) {
                Text(
                    "Ask about recipes, your list…",
                    color = PlaceColor,
                    fontSize = 15.sp,
                    modifier = Modifier.align(Alignment.CenterStart),
                )
            }
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                textStyle = TextStyle(color = BrandInk, fontSize = 15.sp),
                cursorBrush = SolidColor(BrandInk),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Send / Stop button
        val canSend = text.isNotBlank() && !isLoading
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(BrandInk)
                .alpha(if (isLoading || canSend) 1f else 0.3f)
                .clickable(enabled = isLoading || canSend) {
                    if (isLoading) {
                        onAbort()
                    } else {
                        val trimmed = text.trim()
                        if (trimmed.isNotBlank()) {
                            text = ""
                            onSend(trimmed)
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            if (isLoading) {
                // Stop icon: white square
                Box(Modifier.size(12.dp).background(Color.White, RoundedCornerShape(2.dp)))
            } else {
                // Send icon: arrow drawn via Canvas
                SendArrow()
            }
        }
    }
}

@Composable
private fun SendArrow() {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        val w = size.width
        val h = size.height
        val path = androidx.compose.ui.graphics.Path().apply {
            // Arrow pointing up-right
            moveTo(w * 0.5f, h * 0.15f)
            lineTo(w * 0.85f, h * 0.55f)
            lineTo(w * 0.62f, h * 0.55f)
            lineTo(w * 0.62f, h * 0.85f)
            lineTo(w * 0.38f, h * 0.85f)
            lineTo(w * 0.38f, h * 0.55f)
            lineTo(w * 0.15f, h * 0.55f)
            close()
        }
        drawPath(path, color = androidx.compose.ui.graphics.Color.White)
    }
}
```

> **Note:** The import `androidx.compose.ui.foundation.clickable` should be `androidx.compose.foundation.clickable`. Fix the import if the IDE flags it.

- [ ] **Step 2: Build to verify**

```bash
./gradlew :feature:chat:assembleDebug
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/ChatInput.kt
git commit -m "feat(chat): add ChatInput composable"
```

---

### Task 8: MessageBubble

**Files:**
- Create: `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/MessageBubble.kt`

**Interfaces:**
- Consumes: `ChatMessage`, `Role`, `LiveStep`, `Artifact`, all from `ChatModels.kt`; `LiveStepsStrip`, `TypingDots`; `RecipeCard` (Task 9 — import forward reference, ensure Task 9 is done before building)
- Produces: `@Composable fun MessageBubble(message: ChatMessage, streamingText: String?, isStreaming: Boolean, liveSteps: List<LiveStep>, onAddIngredients: (String, List<String>) -> Unit, addedRecipeIds: Set<String>, onSuggestion: (String) -> Unit)`

- [ ] **Step 1: Create MessageBubble**

Create `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/MessageBubble.kt`:
```kotlin
package ai.algo1.marketbasket.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import ai.algo1.marketbasket.feature.chat.artifacts.RecipeCard

private val UserBubbleBg      = Color(0xFF080816)
private val AssistantBubbleBg = Color(0xFFF8F9FB)
private val BrandInk           = Color(0xFF080816)
private val ChipBg             = Color(0xFFFFFFFF)
private val ChipBorder         = Color(0xFFE8E9ED)
private val ChipText           = Color(0xFF374151)

@Composable
fun MessageBubble(
    message: ChatMessage,
    streamingText: String? = null,
    isStreaming: Boolean = false,
    liveSteps: List<LiveStep> = emptyList(),
    onAddIngredients: (messageId: String, ingredients: List<String>) -> Unit,
    addedRecipeIds: Set<String>,
    onSuggestion: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    if (message.role == Role.User) {
        Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Box(
                modifier = Modifier
                    .widthIn(max = screenWidth * 0.80f)
                    .background(
                        UserBubbleBg,
                        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 6.dp),
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Text(message.text, color = Color.White, fontSize = 15.sp)
            }
        }
        return
    }

    // Assistant bubble
    val displayText = if (isStreaming) (streamingText ?: "") else message.text
    val displaySteps = if (isStreaming) liveSteps else message.steps.filter { it.status != StepStatus.Done }

    Column(modifier = modifier.fillMaxWidth().widthIn(max = screenWidth * 0.92f)) {
        if (displaySteps.isNotEmpty()) {
            LiveStepsStrip(steps = displaySteps, modifier = Modifier.padding(bottom = 4.dp))
        }

        if (displayText.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .background(
                        AssistantBubbleBg,
                        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 6.dp, bottomEnd = 18.dp),
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                if (isStreaming) {
                    // Plain text while streaming (no markdown parsing mid-stream)
                    Text(
                        text = displayText,
                        color = BrandInk,
                        fontSize = 15.sp,
                    )
                } else {
                    Markdown(
                        content = displayText,
                        colors = markdownColor(text = BrandInk),
                        typography = markdownTypography(text = androidx.compose.ui.text.TextStyle(fontSize = 15.sp)),
                    )
                }
            }
        }

        // Artifacts
        message.artifacts.forEach { artifact ->
            when (artifact) {
                is Artifact.Recipe -> RecipeCard(
                    artifact = artifact,
                    addedToList = addedRecipeIds.contains(message.id),
                    onAddIngredients = { ings -> onAddIngredients(message.id, ings) },
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        // Suggestion chips
        if (!isStreaming && message.suggestions.isNotEmpty()) {
            SuggestionChips(
                suggestions = message.suggestions,
                onSuggestion = onSuggestion,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SuggestionChips(
    suggestions: List<String>,
    onSuggestion: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        suggestions.forEach { s ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(ChipBg)
                    .clickable { onSuggestion(s) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(s, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = ChipText)
            }
        }
    }
}
```

- [ ] **Step 2: Build to verify**

```bash
./gradlew :feature:chat:assembleDebug
```
Expected: `BUILD SUCCESSFUL`. (If `RecipeCard` is not yet defined, a forward reference compile error will occur — complete Task 9 first, then rebuild.)

- [ ] **Step 3: Commit**

```bash
git add feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/MessageBubble.kt
git commit -m "feat(chat): add MessageBubble with markdown rendering"
```

---

### Task 9: RecipeCard + CookingModal

**Files:**
- Create: `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/artifacts/RecipeCard.kt`
- Create: `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/artifacts/CookingModal.kt`

**Interfaces:**
- Consumes: `Artifact.Recipe`, `RecipeData`, `RecipeIngredient` from `ChatModels.kt`
- Produces: `@Composable fun RecipeCard(artifact: Artifact.Recipe, addedToList: Boolean, onAddIngredients: (List<String>) -> Unit)`, `@Composable fun CookingModal(title: String, steps: List<String>, open: Boolean, onClose: () -> Unit)`

- [ ] **Step 1: Create CookingModal**

Create `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/artifacts/CookingModal.kt`:
```kotlin
package ai.algo1.marketbasket.feature.chat.artifacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingModal(
    title: String,
    steps: List<String>,
    open: Boolean,
    onClose: () -> Unit,
) {
    if (!open) return
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF080816))
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color(0xFF6B7280))
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                itemsIndexed(steps) { index, step ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier.size(24.dp).let {
                                it // CircleShape background added inline below
                            },
                            contentAlignment = Alignment.Center,
                        ) {
                            androidx.compose.foundation.Canvas(modifier = Modifier.size(24.dp)) {
                                drawCircle(color = androidx.compose.ui.graphics.Color(0xFFF3F4F6))
                            }
                            Text(
                                "${index + 1}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B7280),
                            )
                        }
                        Text(step, fontSize = 14.sp, color = Color(0xFF374151), modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: Create RecipeCard**

Create `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/artifacts/RecipeCard.kt`:
```kotlin
package ai.algo1.marketbasket.feature.chat.artifacts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.algo1.marketbasket.feature.chat.Artifact
import ai.algo1.marketbasket.feature.chat.RecipeIngredient

private val GreenGradient = Brush.linearGradient(listOf(Color(0xFFF0FDF4), Color(0xFFDCFCE7)))
private val CardBorder = Color(0xFFE8E9ED)
private val TagBg = Color(0xB3FFFFFF)
private val TagText = Color(0xFF15803D)
private val MetaText = Color(0xFF374151)
private val IngText = Color(0xFF374151)
private val IngStrikeText = Color(0xFF9CA3AF)
private val CheckActive = Color(0xFF16A34A)
private val CheckBorder = Color(0xFFD1D5DB)
private val StepsBlue = Color(0xFF2563EB)
private val BrandInk = Color(0xFF080816)
private val MintGreen = Color(0xFF34D399)
private val AddedBg = Color(0xFFDCFCE7)
private val AddedText = Color(0xFF15803D)

@Composable
fun RecipeCard(
    artifact: Artifact.Recipe,
    addedToList: Boolean,
    onAddIngredients: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val data = artifact.data
    val totalTime = data.prepTimeMinutes + data.cookTimeMinutes
    var checkedIngredients by remember { mutableStateOf(emptySet<Int>()) }
    var stepsOpen by remember { mutableStateOf(false) }
    var cookingOpen by remember { mutableStateOf(false) }

    CookingModal(title = data.title, steps = data.steps, open = cookingOpen, onClose = { cookingOpen = false })

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 2.dp,
    ) {
        Column {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GreenGradient)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    data.tags.take(3).forEach { tag ->
                        Box(
                            modifier = Modifier.background(TagBg, RoundedCornerShape(999.dp)).padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(tag.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TagText)
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(data.title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = BrandInk)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetaChip(Icons.Filled.AccessTime, "$totalTime min")
                    MetaChip(Icons.Filled.Group, "${data.servings} servings")
                    MetaChip(null, data.cuisine)
                }
            }

            // Ingredients
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text("Ingredients".uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280), letterSpacing = 0.8.sp)
                Spacer(Modifier.height(8.dp))
                data.ingredients.forEachIndexed { i, ing ->
                    IngredientRow(
                        ingredient = ing,
                        checked = checkedIngredients.contains(i),
                        onToggle = {
                            checkedIngredients = if (checkedIngredients.contains(i))
                                checkedIngredients - i else checkedIngredients + i
                        },
                    )
                    if (i < data.ingredients.lastIndex) Spacer(Modifier.height(6.dp))
                }
            }

            // Steps toggle
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    "${if (stepsOpen) "Hide" else "Show"} steps",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StepsBlue,
                    modifier = Modifier.clickable { stepsOpen = !stepsOpen }.padding(vertical = 4.dp),
                )
            }
            AnimatedVisibility(visible = stepsOpen) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    data.steps.forEachIndexed { i, step ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                Modifier.size(20.dp).clip(CircleShape).background(Color(0xFFF3F4F6)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("${i + 1}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
                            }
                            Text(step, fontSize = 14.sp, color = MetaText, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Start Cooking
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MintGreen)
                        .clickable { cookingOpen = true }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Start Cooking", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandInk)
                }
                // Add to list / Added
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (addedToList) AddedBg else BrandInk)
                        .clickable(enabled = !addedToList) {
                            val names = data.ingredients.map { "${it.quantity} ${it.name}".trim() }
                            onAddIngredients(names)
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (addedToList) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = AddedText)
                        }
                        Text(
                            if (addedToList) "Added" else "Add to list",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (addedToList) AddedText else Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IngredientRow(ingredient: RecipeIngredient, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onToggle() },
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .border(2.dp, if (checked) CheckActive else CheckBorder, RoundedCornerShape(4.dp))
                .background(if (checked) CheckActive else Color.Transparent, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(10.dp), tint = Color.White)
        }
        Text(
            "${ingredient.quantity} ${ingredient.name}",
            fontSize = 14.sp,
            color = if (checked) IngStrikeText else IngText,
            textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MetaChip(icon: ImageVector?, label: String) {
    Row(
        modifier = Modifier
            .background(Color(0x99FFFFFF), RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = MetaText)
        }
        Text(label, fontSize = 12.sp, color = MetaText)
    }
}
```

- [ ] **Step 3: Build to verify**

```bash
./gradlew :feature:chat:assembleDebug
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/artifacts/
git commit -m "feat(chat): add RecipeCard and CookingModal artifact composables"
```

---

### Task 10: MessageList

**Files:**
- Create: `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/MessageList.kt`

**Interfaces:**
- Consumes: `ChatMessage`, `LiveStep`, `MessageBubble`, `TypingDots`
- Produces: `@Composable fun MessageList(messages: List<ChatMessage>, streamingText: String, liveSteps: List<LiveStep>, isLoading: Boolean, onAddIngredients: (String, List<String>) -> Unit, addedRecipeIds: Set<String>, onSuggestion: (String) -> Unit)`

- [ ] **Step 1: Create MessageList**

Create `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/MessageList.kt`:
```kotlin
package ai.algo1.marketbasket.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val AssistantBubbleBg = Color(0xFFF8F9FB)

@Composable
fun MessageList(
    messages: List<ChatMessage>,
    streamingText: String,
    liveSteps: List<LiveStep>,
    isLoading: Boolean,
    onAddIngredients: (messageId: String, ingredients: List<String>) -> Unit,
    addedRecipeIds: Set<String>,
    onSuggestion: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when messages or streaming text change
    val itemCount = messages.size + (if (isLoading) 1 else 0)
    LaunchedEffect(messages.size, streamingText, liveSteps.size) {
        if (itemCount > 0) listState.animateScrollToItem(itemCount - 1)
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
    ) {
        itemsIndexed(messages, key = { _, msg -> msg.id }) { _, message ->
            MessageBubble(
                message = message,
                onAddIngredients = onAddIngredients,
                addedRecipeIds = addedRecipeIds,
                onSuggestion = onSuggestion,
            )
        }

        if (isLoading) {
            item(key = "loading") {
                val showTyping = streamingText.isEmpty() && liveSteps.isEmpty()
                val showStreaming = streamingText.isNotEmpty() || liveSteps.isNotEmpty()

                if (showTyping) {
                    Box(
                        modifier = Modifier
                            .background(AssistantBubbleBg, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 6.dp, bottomEnd = 18.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) { TypingDots() }
                }

                if (showStreaming) {
                    val streamingMsg = ChatMessage(
                        id = "streaming",
                        role = Role.Assistant,
                        text = streamingText,
                        artifacts = emptyList(),
                        steps = emptyList(),
                        suggestions = emptyList(),
                    )
                    MessageBubble(
                        message = streamingMsg,
                        streamingText = streamingText,
                        isStreaming = true,
                        liveSteps = liveSteps,
                        onAddIngredients = onAddIngredients,
                        addedRecipeIds = addedRecipeIds,
                        onSuggestion = onSuggestion,
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 2: Build to verify**

```bash
./gradlew :feature:chat:assembleDebug
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/MessageList.kt
git commit -m "feat(chat): add MessageList with auto-scroll"
```

---

### Task 11: WelcomeState

**Files:**
- Create: `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/WelcomeState.kt`

**Interfaces:**
- Produces: `@Composable fun WelcomeState(suggestions: List<String>, onSuggestion: (String) -> Unit)`

- [ ] **Step 1: Create WelcomeState**

Create `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/WelcomeState.kt`:
```kotlin
package ai.algo1.marketbasket.feature.chat

import ai.algo1.marketbasket.core.domain.util.Greetings
import java.util.Calendar
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.algo1.marketbasket.R

private val SuggestionBorder = Color(0xFFE5E7EB)
private val SuggestionText   = Color(0xFF525252)

@Composable
fun WelcomeState(
    suggestions: List<String>,
    onSuggestion: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(64.dp))

        Image(
            painter = painterResource(R.drawable.market_basket),
            contentDescription = "Market Basket",
            contentScale = ContentScale.Fit,
            modifier = Modifier.width(192.dp),
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "${Greetings.greetingFor(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))}, Market Basket Shopper!",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
        )

        Spacer(Modifier.height(32.dp))

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            suggestions.forEach { suggestion ->
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { onSuggestion(suggestion) },
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, SuggestionBorder),
                    shadowElevation = 1.dp,
                ) {
                    Text(
                        text = suggestion,
                        fontSize = 16.sp,
                        color = SuggestionText,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    )
                }
            }
        }
    }
}
```

> **Note:** `Greetings.greetingFor(hour)` is defined in `core/domain/util/Greetings.kt` as `fun greetingFor(hour: Int): String`. We pass the current hour from `Calendar.HOUR_OF_DAY` at composition time. This is fine for a greeting that only needs to be correct when the screen opens.

- [ ] **Step 2: Build to verify**

```bash
./gradlew :feature:chat:assembleDebug
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/WelcomeState.kt
git commit -m "feat(chat): add WelcomeState composable"
```

---

### Task 12: ChatScreen

**Files:**
- Create: `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/ChatScreen.kt`

**Interfaces:**
- Consumes: `ChatViewModel`, `WelcomeState`, `MessageList`, `ChatInput`, all `ChatModels`, `hiltViewModel()`
- Produces: `@Composable fun ChatScreen(onClose: () -> Unit)` — the full slide-in panel

- [ ] **Step 1: Create ChatScreen**

Create `feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/ChatScreen.kt`:
```kotlin
package ai.algo1.marketbasket.feature.chat

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val HeaderBg    = Color.White
private val DividerColor = Color(0xFFF0F0F4)
private val SubtitleColor = Color(0xFF9CA3AF)
private val BackButtonBg  = Color(0xFFF3F4F6)

private val SUGGESTIONS = listOf(
    "Give me a recipe for pasta",
    "Add eggs and milk to my list",
    "What's on my list?",
)

@Composable
fun ChatScreen(
    onClose: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val streamingText by viewModel.streamingText.collectAsStateWithLifecycle()
    val liveSteps by viewModel.liveSteps.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val addedRecipeIds by viewModel.addedRecipeIds.collectAsStateWithLifecycle()

    // Slide-in from right animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val screenWidth = LocalConfiguration.current.screenWidthDp.toFloat()
    val translationX by animateFloatAsState(
        targetValue = if (visible) 0f else screenWidth,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "chat_slide_in",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { this.translationX = translationX }
            .background(Color.White),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Column(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().background(HeaderBg),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(40.dp).background(BackButtonBg, CircleShape),
                    ) {
                        Icon(
                            Icons.Filled.ChevronLeft,
                            contentDescription = "Back",
                            tint = Color(0xFF080816),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Food & grocery assistant",
                        fontSize = 11.sp,
                        color = SubtitleColor,
                        fontWeight = FontWeight.Normal,
                    )
                }
                Divider(color = DividerColor, thickness = 1.dp)
            }

            // Body
            if (messages.isEmpty() && !isLoading) {
                WelcomeState(
                    suggestions = SUGGESTIONS,
                    onSuggestion = viewModel::sendMessage,
                    modifier = Modifier.weight(1f),
                )
            } else {
                MessageList(
                    messages = messages,
                    streamingText = streamingText,
                    liveSteps = liveSteps,
                    isLoading = isLoading,
                    onAddIngredients = viewModel::handleAddIngredients,
                    addedRecipeIds = addedRecipeIds,
                    onSuggestion = viewModel::sendMessage,
                    modifier = Modifier.weight(1f),
                )
            }

            // Input
            ChatInput(
                onSend = viewModel::sendMessage,
                onAbort = viewModel::abort,
                isLoading = isLoading,
            )
        }
    }
}
```

- [ ] **Step 2: Build to verify**

```bash
./gradlew :feature:chat:assembleDebug
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add feature/chat/src/main/java/ai/algo1/marketbasket/feature/chat/ChatScreen.kt
git commit -m "feat(chat): add ChatScreen with slide-in animation"
```

---

### Task 13: AppNavHost Wiring

**Files:**
- Modify: `app/src/main/java/ai/algo1/marketbasket/nav/AppNavHost.kt`

**Interfaces:**
- Consumes: `ChatScreen` from `feature/chat`, `FloatingChatLauncher` (already present at `AppChrome.kt:163`)
- Produces: Tapping `FloatingChatLauncher` opens `ChatScreen`; back arrow or system back closes it

- [ ] **Step 1: Update AppNavHost to wire the chat overlay**

In `app/src/main/java/ai/algo1/marketbasket/nav/AppNavHost.kt`, add the import:
```kotlin
import ai.algo1.marketbasket.feature.chat.ChatScreen
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
```

Then add inside `AppNavHost()`, right after `val openListSearch = { ... }` block:
```kotlin
var chatOpen by remember { mutableStateOf(false) }
```

Then update `FloatingChatLauncher` call (currently line 100-102):
```kotlin
// BEFORE:
FloatingChatLauncher(
    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 28.dp, bottom = 104.dp),
)

// AFTER:
FloatingChatLauncher(
    onClick = { chatOpen = true },
    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 28.dp, bottom = 104.dp),
)
```

Then add the `ChatScreen` overlay inside the same `Box`, after the `FloatingChatLauncher` call:
```kotlin
if (chatOpen) {
    ChatScreen(onClose = { chatOpen = false })
}
```

Full updated `AppNavHost` function for reference — the only changes are the three additions above.

- [ ] **Step 2: Handle system back button for chat**

Inside `AppNavHost()`, add a `BackHandler` to intercept the system back when chat is open. Add this import:
```kotlin
import androidx.activity.compose.BackHandler
```

Then add after the `var chatOpen` declaration:
```kotlin
BackHandler(enabled = chatOpen) { chatOpen = false }
```

- [ ] **Step 3: Build the full app**

```bash
./gradlew :app:assembleDebug
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Manual smoke test**

Install and launch on a device or emulator:
```bash
./gradlew :app:installDebug
```

Verify:
1. The "Ask Basket" floating button is visible on all tabs
2. Tapping it slides in the chat panel from the right (350ms)
3. The welcome state shows the Market Basket logo, greeting, and 3 suggestion chips
4. Tapping a suggestion chip sends the message and shows the typing dots
5. The system back button closes the chat panel
6. The back arrow in the header also closes it

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/ai/algo1/marketbasket/nav/AppNavHost.kt
git commit -m "feat(chat): wire FloatingChatLauncher to ChatScreen overlay"
```
