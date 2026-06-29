# Ideas — Video Feed Feature Design

**Date:** 2026-06-29  
**Status:** Approved

---

## Overview

Implement the Ideas tab in the Android app, starting with the Videos sub-tab. The feature is a direct port of the web app's `src/features/videos/` implementation into a new `feature:ideas` Gradle module. Logic, data, and UI behaviour are replicated exactly — only the rendering layer differs (Jetpack Compose + ExoPlayer instead of React + `<video>`).

---

## Architecture

### New Gradle module: `feature:ideas`

Follows the same pattern as `feature:deals`:
- `build.gradle.kts` — Android library, Compose, Hilt, + Media3 ExoPlayer deps
- `AndroidManifest.xml`
- Source under `src/main/java/ai/algo1/marketbasket/feature/ideas/`

### Wiring changes

| File | Change |
|---|---|
| `gradle/libs.versions.toml` | Add `media3 = "1.4.1"`, two library aliases |
| `settings.gradle.kts` | Add `:feature:ideas` |
| `app/build.gradle.kts` | Add `implementation(project(":feature:ideas"))` |
| `AppNavHost.kt` | `Destination.Ideas` → `IdeasRoute()` instead of `PlaceholderScreen` |

No changes to `AppChrome.kt` — `Destination.Ideas` already falls through to the no-header case in the Scaffold.

---

## Data Layer

### `VideoData.kt`

Direct port of `videoData.ts`. Two data classes, one hardcoded list — no repository, no network call, no Supabase.

```kotlin
data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val image: String,
    val time: String,
    val servings: Int,
    val difficulty: String,
    val ingredients: List<String>,
    val instructions: List<String>,
    val videoUrl: String,
    val creator: String,
)

data class RecipeVideo(val id: String, val uri: String, val recipe: Recipe)

data class VideoActionsState(
    val favorited: Boolean = false,
    val bookmarked: Boolean = false,
    val addedToList: Boolean = false,
)
```

All 10 recipes are hardcoded in a top-level `val recipeVideos: List<RecipeVideo>`, identical to the web app's `recipeData` array including titles, descriptions, ingredient lists, instructions, video URLs, and creator handles.

---

## ViewModel: `IdeasViewModel`

Manages:
- A single `ExoPlayer` instance (created in `init`, released in `onCleared`)
- `currentPageIndex: Int` — updated when the pager page settles
- `videoActions: Map<String, VideoActionsState>` — keyed by video ID
- `selectedRecipeForList: Recipe?` — which recipe's "add to list" modal is open

On `currentPageIndex` change: call `player.setMediaItem(MediaItem.fromUri(url))`, `player.prepare()`, `player.play()`.

Exposes:
- `fun onPageSettled(index: Int)`
- `fun onFavoriteToggle(videoId: String)`
- `fun onBookmarkToggle(videoId: String)`
- `fun onAddToListPress(recipe: Recipe)`
- `fun onAddIngredientsConfirmed(addToList: suspend (Recipe) -> Unit)`
- `fun onAddToListDismiss()`

---

## UI Components (all in `IdeasScreen.kt`)

### `IdeasRoute`
Collects VM state with `collectAsStateWithLifecycle`, passes to `IdeasScreen`.

### `IdeasScreen`
- `Column(fillMaxSize)`
- Top: `IdeasTabBar` — "Videos | Recipes | Meal Planning" in white on transparent/dark background, selected tab has white underline indicator
- Content: when Videos tab selected → `VideosTab`; Recipes/MealPlanning → `PlaceholderTab`

### `VideosTab`
- `VerticalPager(state = pagerState, count = recipeVideos.size, beyondBoundsPageCount = 1)`
- `LaunchedEffect(pagerState.settledPage)` → `viewModel.onPageSettled(page)`
- Each page: `VideoPage`

### `VideoPage`
```
Box(fillMaxSize, black background) {
    AndroidView(factory = { PlayerView(it) }) { playerView ->
        playerView.player = player  // bound only for current page
        playerView.useController = false
    }
    // top scrim gradient (black→transparent, 112dp)
    // bottom scrim gradient (transparent→black/80, 288dp)
    VideoActionButtons(...)      // right side, above bottom nav
    RecipeInfoOverlay(...)       // bottom left, expandable
}
```

Player binding: the `AndroidView` update block sets `playerView.player = if (isCurrentPage) player else null`, so only the visible page renders the active player. ExoPlayer handles its own surface internally.

### `VideoActionButtons`
Right-side column, `bottom = 112dp` (above bottom nav + some padding), mirroring the web's `bottom: bottomNavReserve + 32`:
- Heart icon — `favorited` state → filled red vs white
- Bookmark icon — `bookmarked` state → filled red vs white
- List icon (3-line with dots, same SVG path as web) — `addedToList` state → filled red vs white
- Share icon — triggers Android `ShareCompat`

All icons are `Canvas`-drawn `ImageVector`s using the same path data as the web SVGs. Icon size 32dp, tap target 44dp.

### `RecipeInfoOverlay`

**Collapsed state** (default):
- Positioned at bottom-left of the video, right of action buttons
- Title: single line, truncated with ellipsis, 26sp bold
- Description: truncated at 85 chars (same threshold as web), followed by `"... "` + "See more" in red (`#C7353A`)
- Tapping anywhere on the overlay → expands

**Expanded state:**
- Full-screen overlay, `background = Color(0x85000000)`
- Scrollable `Column` with `padding(top = statusBarHeight + 16dp)`
- Title (28sp, normal weight), creator (`by @handle` in red), full description
- Metadata row: time · servings · difficulty (semi-transparent white chip)
- "Ingredients" section with bullet list (red `•`)
- "Instructions" section with numbered steps (red circle badge)
- Tapping anywhere → collapses; scroll is captured before collapse propagates

Animated with `animateDpAsState` / `animateFloatAsState` using the same cubic-bezier easing as the web (`CubicBezierEasing(0.22f, 1f, 0.36f, 1f)`).

### `AddIngredientsDialog`

Triggered by the list action button. Matches the web's `AddToListModal` exactly:
- `Dialog` composable with full dark scrim
- Centered card (`RoundedCornerShape(16dp)`, `Color(0xFF0A0A0A)`)
- Header: "Add to shopping list" + recipe title subtitle
- Scrollable ingredient list, each row separated by a divider, red square icon
- Footer: `Cancel` (gray) + `Add ingredients` (red) buttons side by side
- While adding: button shows "Adding..." and is disabled
- On confirm: calls the list-add callback, sets `addedToList = true` for that video, dismisses

---

## Behaviour Fidelity

| Web behaviour | Android equivalent |
|---|---|
| `snap-y snap-mandatory` vertical scroll | `VerticalPager` with snap |
| Play current, pause others | Single ExoPlayer, null-bound to non-current pages |
| Preload ±1 adjacent | `beyondBoundsPageCount = 1` on Pager |
| 85-char collapse threshold | Same constant, `String.take(85).trimEnd()` |
| Action state per video ID | `Map<String, VideoActionsState>` in VM |
| `addedToList` stays true after add | `setActionValue(id, addedToList = true)` |
| Share via Web Share API | Android `ShareCompat.IntentBuilder` |
| Recipes / Meal Planning stubs | `Text("Coming soon")` placeholder |

---

## Internet Permission

`AndroidManifest.xml` of `feature:ideas` declares `<uses-permission android:name="android.permission.INTERNET" />` — the app manifest already has this so no conflict; declaring in the feature manifest is fine but optional since the app manifest covers it.

---

## What is NOT in scope

- Fetching video/recipe data from Supabase or any API (hardcoded same as web)
- Recipes tab content
- Meal Planning tab content
- Persisting favorites/bookmarks across sessions
- Actual shopping-list integration (the `onAddIngredients` callback is wired to the existing `ListRepository` via the app module's DI graph — the feature exposes a callback, the host wires it)
