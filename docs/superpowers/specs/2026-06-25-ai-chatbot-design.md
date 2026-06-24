# AI Chatbot — Android Implementation Spec

**Date:** 2026-06-25  
**Source reference:** `market-basket-list-app` web app — `src/features/chat/`  
**Goal:** Exact 1:1 port of the web chat experience into `market-basket-android`, triggered by the existing `FloatingChatLauncher` button.

---

## 1. Scope

- Full chat panel: welcome state, message list, streaming, tool-step pills, recipe artifact card, suggestion chips
- Text-only input (voice deferred to a future phase — mic button omitted)
- Thread persistence across app sessions (one thread per `publicId`)
- SSE streaming from the existing backend (`BACKEND_BASE_URL/api/agent/chat/stream`)

---

## 2. Architecture

### Layer diagram

```
AppNavHost  (app module)
├── chatOpen: Boolean  (remember { mutableStateOf(false) })
├── FloatingChatLauncher(onClick = { chatOpen = true })
└── Box overlay: if (chatOpen) ChatScreen(onClose = { chatOpen = false })

ChatScreen / ChatPanel  (feature/chat)
└── ChatViewModel  (@HiltViewModel)
    ├── publicId: String?          ← ListRepository.publicId (StateFlow)
    ├── threadId: String           ← ChatThreadStore (DataStore, UUID per publicId)
    ├── messages: List<ChatMessage>
    ├── streamingText: String      ← accumulates SSE tokens in-flight
    ├── liveSteps: List<LiveStep>  ← tool-call pills during a turn
    ├── isLoading: Boolean
    ├── sendMessage(text: String)  ← appends user msg optimistically, starts SSE job
    └── abort()                    ← cancels the in-flight coroutine Job

AgentChatService  (core/data — new)
├── streamChat(threadId, publicId, message): Flow<AgentEvent>
│     Ktor streaming POST → BACKEND_BASE_URL/api/agent/chat/stream
│     Body: { thread_id, public_id, message }
│     Parses: data: {...} lines identical to web SSE format
└── fetchThreadMessages(threadId, publicId): List<MessageRow>
      Ktor GET → BACKEND_BASE_URL/api/agent/threads/{threadId}?public_id=...

ChatThreadStore  (core/data — new)
└── getOrCreateThreadId(publicId: String): String
      DataStore key: "chat_thread_$publicId" → UUID v4, generated once and persisted
```

### publicId plumbing

`ChatViewModel` injects `ListRepository` (already a Hilt singleton in `core/data`) and collects `publicId` via `collectAsStateWithLifecycle`. No prop-drilling through the nav layer.

### Entry point change in AppNavHost

`AppNavHost` gains one `remember { mutableStateOf(false) }` for `chatOpen`. The existing `FloatingChatLauncher(onClick = {})` call gets `onClick = { chatOpen = true }`. A `ChatScreen` composable is rendered as a `Box` child at the same level as `FloatingBottomNav`, above everything else via `zIndex`.

---

## 3. SSE Event Schema

Identical to the web app's `AgentEvent` discriminated union:

| `type`        | Key fields                                              |
|---------------|---------------------------------------------------------|
| `start`       | —                                                       |
| `token`       | `text: String`                                          |
| `tool_pending`| `tool: String`, `label: String`                        |
| `tool_start`  | `tool`, `label`, `input_summary: String?`              |
| `tool_end`    | `tool`, `success: Boolean`, `output_summary: String?`  |
| `artifact`    | `kind: String` (`"recipe"`), `data: JsonObject`        |
| `final`       | `response_text: String?`, `suggestions: List<String>?` |
| `error`       | `message: String?`                                     |

Parsed via `kotlinx.serialization` with a `@SerialName`-discriminated sealed class. Unknown event types are silently skipped.

---

## 4. Domain Models

Defined in `feature/chat/ChatModels.kt` (no dependency on `core/data` serialization types):

```kotlin
data class ChatMessage(
    val id: String,           // crypto random UUID
    val role: Role,           // User | Assistant
    val text: String,
    val artifacts: List<Artifact>,
    val steps: List<LiveStep>,
    val suggestions: List<String>,
)

enum class Role { User, Assistant }

data class LiveStep(
    val tool: String,
    val label: String,
    val status: StepStatus,   // Pending | Running | Done | Error
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
```

---

## 5. UI Components

### ChatScreen
Full-`fillMaxSize` white composable. Enters with a right-to-left slide animation (`animateFloatAsState` on `translationX`, 350ms, `FastOutSlowInEasing`). Rendered as a `Box` overlay in `AppNavHost`.

### ChatPanel header
- Gray rounded `IconButton` (40dp) with back-arrow chevron → calls `onClose`
- "Food & grocery assistant" in `#9ca3af`, 11sp, next to the button
- Thin bottom divider `#f0f0f4`

### WelcomeState
- `R.drawable.market_basket` (branded red script logo), width = 192dp
- "Good Morning, Market Basket Shopper!" (greeting varies by time of day via existing `Greetings.kt`)
- 3 full-width suggestion cards: white background, thin `#e5e7eb` border, 16dp rounded corners, 16sp normal-weight text in `#525252`
- Suggestions: "Give me a recipe for pasta", "Add eggs and milk to my list", "What's on my list?"

### MessageList
- `LazyColumn`, `fillMaxWidth`, `fillMaxHeight`
- `LaunchedEffect(messages.size, streamingText)` → `listState.animateScrollToItem(lastIndex)`
- Renders past `ChatMessage`s as `MessageBubble`
- While `isLoading && streamingText.isEmpty() && liveSteps.isEmpty()`: shows `TypingDots`
- While `isLoading && (streamingText.isNotEmpty() || liveSteps.isNotEmpty())`: shows streaming bubble

### MessageBubble
**User:** `#080816` background, white text, 15sp, `RoundedCornerShape(18.dp)` with bottom-right corner = 6dp. Right-aligned. Max width 80% of screen.

**Assistant:** `#f8f9fb` background, `#080816` text, 15sp, `RoundedCornerShape(18.dp)` with bottom-left corner = 6dp. Left-aligned. Max width 92%.
- Markdown rendered via `multiplatform-markdown-renderer-m3`
- `LiveStepsStrip` above the text bubble (if steps present)
- `RecipeCard` below text (if artifact present)
- Suggestion chips row below (if `suggestions.isNotEmpty()` and not streaming)

### LiveStepsStrip
Vertical `Column` of pill rows:
- **Pending:** `#f3f4f6` bg, `#6b7280` text, pulsing dot, shimmer alpha animation
- **Running:** `#eff6ff` bg, `#2563eb` text, spinning 2dp border circle
- **Done:** `#f0fdf4` bg, `#15803d` text, checkmark icon, input summary text
- **Error:** `#fef2f2` bg, `#dc2626` text, X icon

### TypingDots
Three 8dp circles in `#9ca3af`, pulsing in sequence with 200ms stagger using `InfiniteTransition`.

### ChatInput
`#f3f4f6` pill-shaped `Row` (22dp corner radius), 3dp/2dp vertical padding:
- `BasicTextField` placeholder "Ask about recipes, your list…", auto-grows up to 120dp height
- **Send button** (when not loading): 36dp dark circle, arrow-up icon, disabled opacity when text empty
- **Stop button** (when `isLoading`): 36dp dark circle, white square (stop icon)

### RecipeCard
White card, `RoundedCornerShape(16.dp)`, `1.dp` border `#e8e9ed`, soft shadow:

1. **Header** — `Brush.linearGradient([#f0fdf4 → #dcfce7])`: tag chips (white/70 bg, `#15803d` text), title 17sp bold, meta row (clock, servings, cuisine icons)
2. **Ingredients** — each row: 16dp checkbox (toggles strikethrough on tap), quantity (medium weight) + name
3. **Steps toggle** — text button "Show/Hide steps" in `#2563eb`; expands to numbered list
4. **Actions row** — two equal buttons:
   - "Start Cooking": `#34d399` bg, `#080816` text → opens `CookingModal`
   - "Add to list": `#080816` bg, white text → sends follow-up message; swaps to "✓ Added" (`#dcfce7` bg, `#15803d` text) after tap

### CookingModal
`ModalBottomSheet` with drag handle. Scrollable numbered step list. Close button top-right.

---

## 6. New Files

### `core/data` additions
| File | Purpose |
|------|---------|
| `AgentChatService.kt` | Ktor SSE streaming + thread hydration |
| `AgentChatModels.kt` | `@Serializable` sealed `AgentEvent` + `MessageRow` |
| `ChatThreadStore.kt` | DataStore wrapper — `getOrCreateThreadId(publicId)` |

### `feature/chat/` (new module)
| File | Purpose |
|------|---------|
| `build.gradle.kts` | Module config + markdown dep |
| `ChatModels.kt` | Domain types (ChatMessage, LiveStep, Artifact, etc.) |
| `ChatViewModel.kt` | All state + SSE orchestration |
| `ChatScreen.kt` | ChatPanel + WelcomeState |
| `ChatInput.kt` | Text input + send/stop |
| `MessageList.kt` | LazyColumn + auto-scroll |
| `MessageBubble.kt` | User/assistant bubble rendering |
| `LiveStepsStrip.kt` | Tool-call step pills |
| `TypingDots.kt` | Three-dot typing indicator |
| `artifacts/RecipeCard.kt` | Recipe artifact card |
| `artifacts/CookingModal.kt` | Step-by-step cooking bottom sheet |

### `app` changes
| File | Change |
|------|--------|
| `AppNavHost.kt` | `chatOpen` state, wire `FloatingChatLauncher`, add `ChatScreen` overlay |
| `build.gradle.kts` | `implementation(project(":feature:chat"))` |
| `settings.gradle.kts` | `include(":feature:chat")` |

---

## 7. New Gradle Dependency

```toml
# gradle/libs.versions.toml
multiplatform-markdown-renderer = "0.27.0"

multiplatform-markdown-renderer-m3 = { group = "com.mikepenz", name = "multiplatform-markdown-renderer-m3", version.ref = "multiplatform-markdown-renderer" }
```

Added to `feature/chat/build.gradle.kts` only.

---

## 8. Out of Scope

- Voice input (mic button) — deferred
- New-thread / reset-thread UI — not in the web app's main panel
- Push notifications for chat responses
