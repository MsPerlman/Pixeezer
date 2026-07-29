# The Deezer Skin and the Gateway Auth (arl)

This document explains two related but distinct pieces of Pixer:

1. **The Deezer skin** — an alternate visual theme that makes Pixer look and behave like the
   official Deezer app, including a home-screen launcher icon swap.
2. **Deezer Gateway authentication** (`arl`) — an optional, user-supplied credential that unlocks
   a second, unofficial Deezer API which is not subject to the pagination limits of the API Pixer
   uses by default.

Both are opt-in. Neither requires the other: you can use the Deezer skin without ever touching
Gateway credentials, and you can configure Gateway credentials while keeping the default Pixer
look.

---

## Part 1 — The Deezer skin

### What it is

Pixer ships two visual identities:

- **Default** — Pixer's own Material 3 design.
- **Deezer** — a dark-only skin that mirrors the real Deezer app's color scheme, typography,
  corner radii, and several screen layouts (Home, Library, full player, artist page, playlist
  detail).

It's a selectable theme, not a separate app or build flavor. Switching between them is instant
and reversible from **Settings → Appearance → Theme**.

### Architecture

The whole thing is driven by a single string preference and a `CompositionLocal`.

**1. The stored preference.** `AppThemeMode` (`data/preferences/UserPreferencesRepository.kt`) is
just four string constants:

```kotlin
object AppThemeMode {
    const val FOLLOW_SYSTEM = "follow_system"
    const val LIGHT = "light"
    const val DARK = "dark"
    const val DEEZER = "deezer"
}
```

It's persisted via `ThemePreferencesRepository.setAppThemeMode(mode: String)`, backed by Jetpack
DataStore (key `"app_theme_mode"`).

**2. Resolving the skin.** `ui/theme/Skin.kt` turns that raw string into a typed enum:

```kotlin
enum class AppSkin { DEFAULT, DEEZER }

val LocalAppSkin = staticCompositionLocalOf { AppSkin.DEFAULT }

fun resolveAppSkin(appThemeMode: String): AppSkin =
    if (appThemeMode == AppThemeMode.DEEZER) AppSkin.DEEZER else AppSkin.DEFAULT

fun resolveUseDarkTheme(appThemeMode: String, systemDarkTheme: Boolean): Boolean =
    when (appThemeMode) {
        AppThemeMode.DARK, AppThemeMode.DEEZER -> true
        AppThemeMode.LIGHT -> false
        else -> systemDarkTheme
    }
```

Note that the Deezer skin forces dark mode — there is no light variant of `DeezerColorScheme`,
matching the real Deezer app, which is dark-only on mobile.

**3. Wiring it into the Compose tree.** `MainActivity.kt` collects the preference once at the
root of the UI and feeds both derived values into `PixelPlayerTheme`:

```kotlin
val appThemeMode by themePreferencesRepository.appThemeModeFlow
    .collectAsStateWithLifecycle(initialValue = AppThemeMode.FOLLOW_SYSTEM)
val useDarkTheme = resolveUseDarkTheme(appThemeMode, systemDarkTheme)
val appSkin = resolveAppSkin(appThemeMode)

PixelPlayerTheme(darkTheme = useDarkTheme, skin = appSkin) {
    // rest of the app
}
```

**4. `PixelPlayerTheme` (`ui/theme/Theme.kt`)** picks the color scheme and typography and
publishes the skin via `CompositionLocalProvider`:

```kotlin
val finalColorScheme = when {
    skin == AppSkin.DEEZER -> DeezerColorScheme
    // ...dynamic color / light / dark fallbacks for the Default skin...
}
val finalTypography = if (skin == AppSkin.DEEZER) DeezerTypography else Typography

CompositionLocalProvider(LocalPixelPlayerDarkTheme provides darkTheme, LocalAppSkin provides skin) {
    MaterialTheme(colorScheme = finalColorScheme, typography = finalTypography, shapes = Shapes) {
        content()
    }
}
```

The Deezer skin branch is checked **before** Android 12+ dynamic (Material You) colors —
otherwise a device with dynamic color enabled would silently override the Deezer palette.

`DeezerColorScheme` hand-sets every Material 3 color role (including `surfaceContainer*` and
`*Fixed*` tiers, which Compose does not auto-derive from primary/secondary/tertiary) so that
components reading those roles directly — e.g. the full player's play/pause button — render with
the exact violet-and-black palette the real app uses, instead of a Material-generated
approximation.

### How individual screens change

Any composable can read `LocalAppSkin.current` to branch. Two patterns are used throughout the
codebase:

**a) Inline styling tweaks** — same composable, different values:

```kotlin
// ui/theme/ShapeCache.kt
skin == AppSkin.DEEZER -> RoundedCornerShape(8.dp)   // Deezer's tighter corners
// vs. 18.dp for the Default skin
```

This shows up ~30 times across the codebase for things like card corner radius, nav bar
indicator color, and play button shape.

**b) Full layout swaps** — the shared screen composable early-exits into a Deezer-specific
composable:

```kotlin
// HomeScreen.kt
if (LocalAppSkin.current == AppSkin.DEEZER) {
    DeezerHomeLayout(/* ... */)
    return
}
// ...Default skin's own layout continues below...
```

The same pattern repeats for `DeezerLibraryHub` (vs. the default Library hub),
`DeezerFullPlayerBody` (vs. the default full player), `DeezerArtistDetailContent` (vs. the
default artist screen), and `DeezerPlaylistDetailContent` (vs. the default playlist detail
screen). Each `Deezer*` composable is a self-contained rebuild of that screen's UI matching
Deezer's own layout, reusing the same ViewModels and data as the Default-skin version — only the
presentation differs, never the underlying data source.

Because both variants live behind the exact same navigation route and read from the exact same
ViewModel, switching skins never triggers a re-navigation or data reload — the next recomposition
just renders different composables.

### Selecting it in Settings

`SettingsCategoryScreen.kt` exposes it as one more option in the existing theme picker
(`ThemeSelectorItem`, the same reusable "map of key → label" picker component used for light/dark/
system):

```kotlin
ThemeSelectorItem(
    options = mapOf(
        AppThemeMode.LIGHT to stringResource(R.string.setcat_theme_light),
        AppThemeMode.DARK to stringResource(R.string.setcat_theme_dark),
        AppThemeMode.FOLLOW_SYSTEM to stringResource(R.string.setcat_theme_follow_system),
        AppThemeMode.DEEZER to stringResource(R.string.setcat_theme_deezer)
    ),
    selectedKey = uiState.appThemeMode,
    onSelectionChanged = { settingsViewModel.setAppThemeMode(it) }
)
```

`setAppThemeMode` flows: `SettingsViewModel` → `ThemePreferencesRepository.setAppThemeMode()` →
DataStore write → `appThemeModeFlow` emits → `MainActivity` recomposes with the new skin.

### The launcher icon swap

The newest addition: selecting the Deezer theme also swaps the app's **home-screen icon**, not
just its in-app appearance, so the icon on your phone's launcher matches whichever look is
currently active.

**Why this needs more than a resource swap.** A regular Android app has exactly one launcher
icon, declared once in the manifest (`android:icon` on `<application>`). There's no
`stringResource()`-style runtime switch for it — the OS reads the icon from the installed
package's manifest, and that's fixed at install time unless you change which manifest *component*
is enabled.

**The mechanism: two `<activity-alias>` entries.** An activity-alias is a manifest entry that
points at a real activity (`android:targetActivity`) but can declare its own icon, label, and
intent-filters — effectively a second "front door" to the same activity. Only one alias is
`enabled` at a time; Android always shows the icon of whichever *enabled* alias currently carries
the `LAUNCHER` intent-filter.

`AndroidManifest.xml`:

```xml
<!-- MainActivity itself no longer carries the LAUNCHER intent-filter directly. -->
<activity android:name=".MainActivity" ...>
    <intent-filter>
        <action android:name="android.intent.action.MUSIC_PLAYER" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
    <!-- ...other non-launcher intent-filters... -->
</activity>

<activity-alias
    android:name=".LauncherDefault"
    android:targetActivity=".MainActivity"
    android:enabled="true"
    android:icon="@mipmap/ic_launcher"
    android:roundIcon="@mipmap/ic_launcher_round">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.LAUNCHER" />
        <category android:name="android.intent.category.APP_MUSIC" />
    </intent-filter>
</activity-alias>

<activity-alias
    android:name=".LauncherDeezer"
    android:targetActivity=".MainActivity"
    android:enabled="false"
    android:icon="@mipmap/ic_launcher_deezer"
    android:roundIcon="@mipmap/ic_launcher_deezer_round">
    <!-- same intent-filter as above -->
</activity-alias>
```

Both aliases point at `MainActivity`, both carry the same shortcuts metadata, and both are
declared `exported="true"` (required for anything the launcher needs to start). Only their
`enabled` default and `icon`/`roundIcon` differ. `LauncherDefault` starts enabled;
`LauncherDeezer` starts disabled, so a fresh install always shows the normal Pixer icon.

**The icon assets.** `ic_launcher_deezer` / `ic_launcher_deezer_round` are generated at all five
standard launcher densities (`mdpi` 48px, `hdpi` 72px, `xhdpi` 96px, `xxhdpi` 144px, `xxxhdpi`
192px), with the round variant produced by clipping the square source to a circle. These are
plain legacy PNG launcher icons (not an adaptive-icon foreground/background pair) — the source
art is already a flat, full-bleed square, so a legacy icon reproduces it faithfully without
needing safe-zone insetting.

**The runtime switch.** Whichever alias should be active is toggled with
`PackageManager.setComponentEnabledSetting()`, which can enable/disable a manifest component for
the currently-installed app without requiring a reinstall or even killing the running process
(`DONT_KILL_APP` flag). This lives in `ThemePreferencesRepository`, right where the theme
preference itself gets written — so every code path that changes the theme (the Settings screen
*and* the first-run setup wizard) gets the icon swap for free, with no duplicated logic:

```kotlin
suspend fun setAppThemeMode(themeMode: String) {
    dataStore.edit { preferences -> preferences[Keys.APP_THEME_MODE] = themeMode }
    applyLauncherIcon(themeMode == AppThemeMode.DEEZER)
}

private fun applyLauncherIcon(isDeezer: Boolean) {
    val packageManager = context.packageManager
    val deezerAlias = ComponentName(context.packageName, "com.lostf1sh.pixelplayeross.LauncherDeezer")
    val defaultAlias = ComponentName(context.packageName, "com.lostf1sh.pixelplayeross.LauncherDefault")
    val (toEnable, toDisable) = if (isDeezer) deezerAlias to defaultAlias else defaultAlias to deezerAlias
    packageManager.setComponentEnabledSetting(toEnable, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
    packageManager.setComponentEnabledSetting(toDisable, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
}
```

One subtlety: the alias class names are hardcoded as
`com.lostf1sh.pixelplayeross.LauncherDeezer` / `...LauncherDefault` — that's the Gradle
**`namespace`** (`com.lostf1sh.pixelplayeross`), which is what Android Gradle Plugin uses to
resolve the manifest's `.LauncherDeezer` shorthand into a fully-qualified component name at
build time. It is *not* the same as the app's **`applicationId`** (`com.minugarc.pixer`, with a
`.debug` suffix on debug builds), which is what `context.packageName` returns at runtime. The two
are deliberately different in this project. `ComponentName(pkg, cls)` takes the runtime package
(`context.packageName`) for identity and the build-time class name for lookup, so this works
correctly regardless of build type or any future `applicationId` change — you'd only need to
update the hardcoded strings if the `namespace` itself ever changes, which is rare.

After a fresh install, Android normally takes a few seconds (occasionally requires a launcher
restart on some OEM skins) to notice a component's `enabled` state changed and refresh the
home-screen shortcut; this is standard OS behavior for this API, not specific to Pixer.

### The notification icon swap

The playback notification's small icon (the monochrome glyph shown in the status bar and on the
lock screen while something is playing) follows the same theme switch, on the same trigger — no
separate setting. Unlike the launcher icon, this one can't be swapped with a manifest trick:
Media3's notification is built once per session and its icon is set imperatively on the
`MediaNotification.Provider`, so the fix is to react to the theme flow and re-set the icon (and
force a notification refresh) whenever it changes, in `data/service/MusicService.kt`:

```kotlin
serviceScope.launch {
    themePreferencesRepository.appThemeModeFlow.collect { mode ->
        val shouldUseDeezerIcon = mode == AppThemeMode.DEEZER
        if (shouldUseDeezerIcon != isDeezerNotificationIcon) {
            isDeezerNotificationIcon = shouldUseDeezerIcon
            localOnlyProvider.setSmallIcon(
                if (shouldUseDeezerIcon) R.drawable.ic_stat_deezer else R.drawable.monochrome_player
            )
            mediaSession?.let { refreshMediaSessionUi(it, force = true) }
        }
    }
}
```

Android notification small icons must be a flat, single-colour silhouette on a transparent
background (the system tints them itself; any colour information in the source is ignored) —
unlike the launcher icon, which is a full-colour asset. `res/drawable-*dpi/ic_stat_deezer.png` is
generated by thresholding the source art on luminance rather than just resizing it: dark pixels
become an opaque white shape, bright/background pixels become fully transparent, producing a
proper silhouette instead of a flattened colour swatch.

### The pin-to-Home system

Deezer's Home screen lets you pin favourite playlists, artists, tracks, and even the Flow/Discovery
tiles so they always show up in a dedicated grid at the top. Pixer's version works the same way,
capped at **15 pinned items**.

**The model** (`data/preferences/PinnedHomeItem.kt`) is a flat, serializable record covering every
pinnable entity type:

```kotlin
enum class PinnedItemType { FLOW, DISCOVERY, MIX, PLAYLIST, ARTIST, ALBUM, TRACK }

data class PinnedHomeItem(
    val type: PinnedItemType,
    val id: String,
    val label: String,
    val imageUrl: String? = null,
    val fallbackTrackArtUrls: List<String> = emptyList(),
    val addedAt: Long = System.currentTimeMillis()
)
```

It's persisted as a JSON-encoded list in DataStore (`UserPreferencesRepository.togglePinnedHomeItem`)
and exposed to the UI as `LibraryViewModel.pinnedHomeItems: StateFlow<List<PinnedHomeItem>>`, shared
across every screen via the activity-scoped `LibraryViewModel` (`hiltViewModel<LibraryViewModel>(activity)`)
so a pin toggled from anywhere is instantly reflected in the Home grid without any manual refresh.

**The cap** lives in `LibraryViewModel.togglePin()`, not in the DataStore layer — it's a plain
pre-check against the already-loaded `pinnedHomeItems.value` before writing:

```kotlin
fun togglePin(item: PinnedHomeItem): Boolean {
    val current = pinnedHomeItems.value
    val alreadyPinned = current.any { it.type == item.type && it.id == item.id }
    if (!alreadyPinned && current.size >= MAX_PINNED_HOME_ITEMS) {
        return false
    }
    viewModelScope.launch { userPreferencesRepository.togglePinnedHomeItem(item) }
    return true
}

companion object {
    const val MAX_PINNED_HOME_ITEMS = 15
}
```

Every call site checks the `Boolean` it returns and shows a toast (`pin_limit_reached_message`,
"You can only pin up to %1$d items to Home") when it's `false`, rather than silently no-opping.

**Where the toggle lives**, matching the real app's placement: at the very bottom of the "..." sheet
for a track (`SongInfoBottomSheet.kt`, after the divider that separates actions from metadata rows)
and for a playlist (`PlaylistDetailScreen.kt`'s options sheet, after "Set default transition"); as a
dedicated pin icon next to Like/Share in the artist header (`DeezerArtistDetailContent.kt` — an
artist has no other "..." menu, so the icon lives directly in the header row instead of behind one);
and via long-press on Flow tiles and recommended-playlist tiles directly on Home
(`DeezerHomeLayout.kt`). All of them construct a `PinnedHomeItem` with `type` matching the entity
and call the same `LibraryViewModel.togglePin()` — there's exactly one code path that writes pins,
regardless of where the tap happened.

**Rendering** is `DeezerPinnedGrid` (`DeezerHomeLayout.kt`), a 2-row × 4-column grid (`items.take(8).chunked(4)`
— only the 8 most relevant pins are shown on Home even if more are pinned; the DataStore itself has
no display cap, only the 15-item write cap), with long-press-to-unpin. Tapping a tile routes through
`resolvePinnedTileClick()`, a `when` over `PinnedItemType` that knows how to open each kind (navigate
for playlists/artists/albums, start playback for tracks/Flow, etc.).

### The floating navbar and mini-player ("liquid glass")

The bottom navigation bar and the mini-player pill sitting above it use a translucent,
frosted-looking treatment in the Deezer skin — instead of a flat opaque `Surface`/`Row` colour.

An early version of this used real backdrop blur via the [Haze](https://github.com/chrisbanes/haze)
library (`Modifier.hazeSource` on the scrollable content behind them, `Modifier.hazeEffect` on the
navbar/pill to blur it). That was reverted: at the alpha version available at the time
(`2.0.0-alpha03`), combining Haze's effect capture with this app's own heavily-animated,
independently-`graphicsLayer`-transformed sheet system (drag gestures, expand/collapse, predictive
back) produced visibly mispositioned navbar/player rendering. The Haze dependency was removed
entirely rather than patched around, since a translucent-but-not-truly-blurred look is a reasonable,
much lower-risk approximation of the same "glass" aesthetic:

- **Navbar** (`MainActivity.kt`, the `Surface` inside the `bottomBar` slot): `color =
  NavigationBarDefaults.containerColor.copy(alpha = 0.82f)` instead of the opaque default. No
  border — an earlier attempt at a subtle `1.dp` white edge-highlight (a common real-glass cue) read
  as a stray, unfinished-looking outline against the app's black background and was removed.
- **Mini-player pill** (`UnifiedPlayerSheetShared.kt`, `MiniPlayerContentInternal`): a vertical
  `Brush.verticalGradient` from 92%- to 78%-alpha `primaryContainer` instead of a flat opaque fill,
  clipped to the same `RoundedCornerShape(32.dp)` pill shape as before.
- **The container behind the pill** (`UnifiedPlayerSheetV2.kt`, `playerAreaBackground`): while the
  full (expanded) player still sits on flat black — matching the real app, per the existing code
  comment — the *collapsed/mini* state now tints that background with `miniPlayerScheme.primaryContainer`
  at 78% alpha instead of flat black. This isn't just cosmetic: the container's clip shape has
  independently-animated, asymmetric corner radii (e.g. a 10dp bottom radius vs. the pill's own
  uniform 32dp), so the pill's more-aggressively-rounded corners never perfectly line up with the
  container's. Whatever peeks through at that seam is now the same colour and scheme as the pill
  itself (`miniPlayerScheme`, the album-art-derived `ColorScheme` provided as `LocalMaterialTheme`
  specifically for the mini/full player — *not* the app-wide `MaterialTheme.colorScheme`, which is a
  different value and was the reason a first attempt at this fix still showed a mismatched-colour
  sliver at the corners) rather than a jarring flat-black or mismatched-purple wedge.

### The full-player cover: size and neighbour peek

The expanded player's album-art carousel already supported three presentation styles as a
user-facing setting for the default skin — `CarouselStyle.NO_PEEK` (one full-width cover),
`ONE_PEEK`, and `TWO_PEEK` (Material3's multi-browse carousel, showing slivers of the
previous/next item on both sides) — but the Deezer player hardcoded `NO_PEEK`, unlike the real
Deezer app, which always shows the adjacent tracks' covers peeking at both edges.

`FullPlayerContent.kt` now forces `TWO_PEEK` specifically for the Deezer player instead:

```kotlin
carouselStyle = if (useDeezerPlayer) CarouselStyle.TWO_PEEK else carouselStyle,
```

`DeezerFullPlayerBody.kt`'s `DeezerPlayerCover` also widened the carousel's bounding box — from
`minOf(availableWidth - 40.dp, availableHeight * 0.34f)` to `minOf(availableWidth - 16.dp,
availableHeight * 0.5f)` — since `TWO_PEEK` reserves part of that width for the two neighbouring
slivers, so the box needs to be noticeably wider than a `NO_PEEK` box for the focal cover to end up
at least as large as before (larger, per the real app). The lyrics-chip overlay's own bounding box
(`coverSide`) was adjusted from `carouselWidth - 16.dp` to `carouselWidth * 0.6f - 16.dp` to track
the focal item's actual on-screen size under `TWO_PEEK` (whose focal-item height is `maxWidth *
0.6f`, per `FullPlayerAlbumCoverSection`) rather than the old single-cover-fills-the-box size.

---

## Part 2 — Deezer Gateway authentication (`arl`)

### Why this exists

Pixer's default Deezer integration uses Deezer's **TV/Cast API**
(`api.deezer.com/platform/gcast/...`, OAuth-based). It works well for browsing, search, and
playback, but it has real, observed limits that aren't bugs in Pixer's code — they're limits of
that specific API surface:

| Symptom | TV API | Gateway API |
|---|---|---|
| Loved tracks shown | stops around ~1,149 (endpoint quietly stops paging reliably) | full count (verified 8,925/8,925 on a real account) |
| "Mixes inspired by" tiles on Home | 5 (indexes 6–12 404 for many accounts) | all 12 |
| Playlist track list | capped at 50 (one page) | full playlist, any length |
| Favorite playlists (playlists you *follow*, not just own) | not exposed at all | full list |

None of this is a pagination bug to fix in Pixer — the TV API endpoints genuinely stop returning
more data past a point for some accounts. The fix was to add a second, optional data source: the
same **web gateway** (`gateway.php`) that deezer.com's own web player uses internally, which
doesn't share those limits.

### What `arl` actually is, and where `sid`/`network` went

The Gateway (`gateway.php`) authenticates every call with three parameters: `arl`, `sid`, and
`network`. An earlier version of this feature required the user to supply all three, captured from
the official Deezer Android app's own traffic via a TLS man-in-the-middle proxy with certificate
pinning bypassed (a real reverse-engineering workflow, not something practical to redo casually).
That turned out to be solving a harder problem than necessary.

- **`arl`** is a long-lived session cookie (a ~192-character hex string) set on `deezer.com` once
  you log in through a browser. It's the closest thing to a "stay logged in" token, and the only
  one of the three that identifies *you* — it's what actually authenticates the session.
- **`sid`** is a short-lived session id scoped to one gateway session.
- **`network`** turned out, on testing, to not be checked at all by `api.deezer.com/1.0/gateway.php`
  as long as it's present as an (empty) parameter — real calls succeed with `network=""`.
- **`sid`**, unlike `network`, *is* checked — but it doesn't have to be captured from the mobile
  app. Deezer's own web player obtains a fresh one on every page load with a single call:
  `POST www.deezer.com/ajax/gw-light.php?method=deezer.getUserData`, sent with nothing but the
  `arl` cookie. The response both sets a `sid` cookie and includes the same value as
  `results.SESSION_ID` in the JSON body, alongside the account's numeric `results.USER.USER_ID`.
  This is exactly the bootstrap step every unofficial Deezer client (deemix, deezer-py, Refreezer,
  and others) performs — Pixer now does the same thing itself, in-app, instead of asking the user
  to hunt down a mobile-app-specific session id by hand.

Net effect: **the user only ever needs to supply `arl`.** Pixer derives `sid` on its own the first
time it's needed and caches it in memory for the process lifetime; `network` is always sent empty.
Nothing here talks to Deezer's login endpoints or ever sees an email/password — it's the same
"paste a long-lived cookie, get a session" model the TV API's own OAuth setup already uses,
just applied to a second API surface.

### How to get `arl`

1. Log into [www.deezer.com](https://www.deezer.com) in a desktop browser.
2. Open DevTools (**F12**) → **Application** (Chrome/Edge) or **Storage** (Firefox) → **Cookies**
   → `https://www.deezer.com`.
3. Find the cookie named **`arl`** and copy its **Value**.
4. Paste it into Pixer's **Settings → Account** Gateway section (or the setup wizard's Gateway
   step) and tap **Save & Test**.

That's the entire process — no proxy, no Frida, no native-app traffic capture. **Save & Test**
immediately runs the `deezer.getUserData` bootstrap and a real data call before reporting success,
so a bad or expired `arl` is caught right away rather than failing silently on the next sync.

If Pixer ever reports a previously-working `arl` as invalid, the underlying Deezer session was
invalidated (e.g. "log out of all devices" from Deezer's account settings, or the cookie's natural
expiry) — grab a fresh one from the browser the same way.

### How the bootstrap and calls work in code

**1. `DeezerGatewayRepository.bootstrapSession(arl)`** makes the one-off `deezer.getUserData` call
directly with OkHttp (a different host than the rest of the Gateway traffic, so it bypasses the
`DeezerGatewayApiService` Retrofit interface for this single call):

```kotlin
private suspend fun bootstrapSession(arl: String): Session? = withContext(Dispatchers.IO) {
    val request = Request.Builder()
        .url("https://www.deezer.com/ajax/gw-light.php?method=deezer.getUserData&input=3&api_version=1.0&api_token=")
        .header("Cookie", "arl=$arl")
        .post("{}".toRequestBody("application/json".toMediaType()))
        .build()
    okHttpClient.newCall(request).execute().use { response ->
        val envelope = gson.fromJson(response.body?.string(), GwEnvelope::class.java)
        val userData = envelope?.results?.let { gson.fromJson(it, GwUserData::class.java) } ?: return@withContext null
        Session(sid = userData.sessionId ?: return@withContext null, userId = userData.user?.userId ?: return@withContext null)
    }
}
```

The resulting `Session(sid, userId)` is cached in memory (`cachedSession`, guarded by a `Mutex` so
concurrent calls don't trigger duplicate bootstraps) and reused for every subsequent Gateway call
in the same app process. If a call comes back with an auth error, `call()` transparently refreshes
the session once and retries — handling the case where the cached `sid` has expired mid-session,
without surfacing anything to the user.

**2. The Retrofit interface** (`data/network/deezer/DeezerGatewayApiService.kt`) is unchanged in
shape — every gateway "method" (Deezer's own RPC-style naming, e.g. `song.getFavoriteIds`) still
goes through the same POST call on `api.deezer.com`, distinguished only by the `method` query
parameter:

```kotlin
interface DeezerGatewayApiService {
    @POST("1.0/gateway.php")
    suspend fun call(
        @Query("method") method: String,
        @Query("api_key") apiKey: String,
        @Query("sid") sid: String,
        @Query("arl") arl: String,
        @Query("network") network: String,
        @Query("input") input: String = "3",
        @Query("output") output: String = "3",
        @Query("gateway_input") gatewayInput: String? = null,
        @Body body: Map<String, Any?> = emptyMap()
    ): GwEnvelope
}
```

It's wired to the exact same Retrofit/OkHttp client as Pixer's existing TV API
(`baseUrl("https://api.deezer.com/")`, from `AppModule.provideDeezerRetrofit`) — no special
headers, interceptors, or certificate pinning workaround needed, because auth travels as plain
query parameters rather than headers or cookies. `network` is always passed as `""` now; `sid`
comes from the cached bootstrap session rather than user input.

`api_key` is hardcoded to the same public API key Deezer's own web player embeds in its
JavaScript bundle (`4VCYIJUCDLOUELGD1V8WBVYBNVDYOXEWSLLZDONGBBDFVXTZJRXPR29JRLQFO6ZE`) — it
identifies the *client type* to Deezer, not the user, and is the same value every unofficial
Deezer client hardcodes for this reason.

**3. `DeezerGatewayRepository`** wraps that interface with the actual methods Pixer needs:

- `getAllFavoriteTrackIds()` / `hydrateSongs(ids)` — `song.getFavoriteIds` then
  `song_getListData` in batches of 100, for the full loved-tracks list.
- `getFavoriteAlbums()` / `getFavoriteArtists()` / `getFavoritePlaylists()` — `album.getFavorites`,
  `artist.getFavorites`, `playlist.getFavorites`.
- `getPlaylistSongs(playlistId)` — `playlist.getSongs` with `nb=10000`, for full (untruncated)
  playlists.
- `getHomeMixSeeds()` — `app_page_get` with a `gateway_input` payload requesting the `"home"`
  page, then picks out the section titled *"Mixes inspired by"* and returns its 12 seed tracks.
- `getMixTracklist(seedSngId)` — `song.getSearchTrackMix` for one seed, to build out that mix's
  actual tracklist.
- `testCredentials()` — forces a fresh session bootstrap, then a cheap `song.getFavoriteIds` call
  (`nb=1`), used purely to validate a freshly-pasted `arl`.
- `currentUserId()` — the account's numeric Deezer user id, itself sourced from the bootstrap
  session rather than a separately-stored value, so Gateway features work independently of
  whether the user ever completed the TV API's own OAuth sign-in.

**4. Where it plugs into existing sync logic** — this is the important design decision: **the
Gateway is additive, never a replacement.** `PlaylistPreferencesRepository` and `LibraryViewModel`
always try the Gateway path first when it's configured, and silently fall back to the pre-existing
TV API path otherwise — so nothing changes for anyone who never sets up Gateway credentials.

```kotlin
// PlaylistPreferencesRepository.syncLovedTracks()
suspend fun syncLovedTracks() {
    if (deezerGatewayRepository.isConfigured() && syncLovedTracksViaGateway()) {
        return   // full, untruncated result — done
    }
    // ...original paginated TV-API loop, unchanged, runs if Gateway isn't set up or fails...
}
```

```kotlin
// PlaylistViewModel.loadPlaylistDetails() — playlist track list
val gatewaySongs = if (deezerGatewayRepository.isConfigured()) {
    deezerGatewayRepository.getPlaylistSongs(rawId)
} else {
    emptyList()
}
val songsList = if (gatewaySongs.isNotEmpty()) gatewaySongs.map { it.toSong() } else fallbackSongsList
```

`syncFavoritePlaylists()` is the one exception with no fallback branch — followed-but-not-owned
playlists simply aren't available through the TV API at all, so that sync is a pure no-op if
Gateway credentials aren't configured, rather than falling back to anything.

**5. Mapping to existing types.** `data/network/deezer/DeezerGatewayMappers.kt` converts the
gateway's flat `GwSong` DTO into the exact same `SongEntity` / `Song` shapes the rest of the app
already uses, so no screen, ViewModel, or Room query needed to change to consume Gateway data —
only the sync/fetch layer knows the Gateway exists. Cover art is a special case: the gateway
returns bare MD5 hashes (`ALB_PICTURE`, `ART_PICTURE`) instead of full URLs, so the mapper builds
the actual image URL itself:
`https://e-cdns-images.dzcdn.net/images/cover/<md5>/<size>x<size>-000000-80-0-0.jpg`.

### Where the credential is stored, and the save/validate flow

`arl` is stored **as a plain string in Jetpack DataStore** (`UserPreferencesRepository`), the same
trust model already used for the existing Deezer OAuth access token — no extra encryption layer,
no Android Keystore wrapping. This is a deliberate, pre-existing project convention, not something
specific to Gateway auth. Nothing else (no `sid`, no `network`) is persisted — the session id lives
only in memory for the life of the app process and is re-derived from `arl` on next launch.

```kotlin
suspend fun saveDeezerGatewayCredentials(arl: String) {
    dataStore.edit { preferences -> preferences[Keys.DEEZER_GATEWAY_ARL] = arl }
}
```

The Settings/setup UI (`GatewayCredentialsSection.kt`, backed by
`GatewayCredentialsStateHolder.kt`, shared between `AccountViewModel` and `SetupViewModel` so both
the main Settings screen and the first-run wizard get identical behavior) never saves blindly:

```kotlin
fun saveAndTest() {
    // ...
    scope.launch {
        userPreferencesRepository.saveDeezerGatewayCredentials(arl)
        val ok = deezerGatewayRepository.testCredentials()   // forces a fresh bootstrap + real call
        if (!ok) {
            userPreferencesRepository.clearDeezerGatewayCredentials()   // don't keep a bad value around
        }
        _uiState.update { it.copy(isTesting = false, testResult = ok, isConfigured = ok) }
    }
}
```

It saves, immediately fires a cheap validation call (which itself exercises the full bootstrap →
real-call path), and wipes what it just saved if that call fails — so the app never ends up
"configured" with an `arl` that doesn't actually work, and the UI can show a clear pass/fail state
right away instead of silently failing on the next real sync.

### Security notes

- `arl` is equivalent to being logged into your Deezer account in a browser — whoever has it can
  read (not modify) your account's library, favorites, and playlists via the Deezer API. Treat it
  like a password, not like an API key you'd casually share.
- It's entered manually by the user and stored **only** on-device. Nothing is bundled with the
  app, nothing is sent anywhere except straight to `deezer.com`/`api.deezer.com`.
- The feature is fully optional; the app is 100% functional without it, just subject to the TV
  API's limits described above.
- To revoke access, log out of all sessions from your Deezer account settings (web or mobile) —
  this invalidates the underlying `arl` cookie everywhere it's in use, including in Pixer. You'll
  need to grab a fresh one to set it up again afterward.

---

## File map

| File | Role |
|---|---|
| `ui/theme/Skin.kt` | `AppSkin` enum, `LocalAppSkin`, `resolveAppSkin`/`resolveUseDarkTheme` |
| `ui/theme/Theme.kt` | `DeezerColorScheme`, `DeezerTypography` selection, `PixelPlayerTheme` |
| `ui/theme/ShapeCache.kt` | Per-skin corner radii and shapes |
| `presentation/screens/Deezer*.kt` | Deezer-specific layouts (Home, Library hub, full player, artist, playlist detail) |
| `data/preferences/UserPreferencesRepository.kt` | `AppThemeMode` constants; Gateway credential DataStore flows |
| `data/preferences/ThemePreferencesRepository.kt` | Theme mode persistence; launcher-icon `PackageManager` toggle |
| `AndroidManifest.xml` | `LauncherDefault` / `LauncherDeezer` activity-aliases |
| `res/mipmap-*/ic_launcher_deezer*.png` | Generated Deezer launcher icon assets |
| `res/drawable-*dpi/ic_stat_deezer.png` | Generated Deezer notification small-icon silhouette |
| `data/service/MusicService.kt` | Notification small-icon swap on theme change (`isDeezerNotificationIcon`) |
| `data/service/LocalOnlyMediaNotificationProvider.kt` | Wraps Media3's notification provider; `setSmallIcon()` |
| `data/preferences/PinnedHomeItem.kt` | `PinnedItemType` enum, `PinnedHomeItem` model |
| `presentation/viewmodel/LibraryViewModel.kt` | `pinnedHomeItems` flow, `togglePin()` + the 15-item cap; also Gateway-first Home mix seeds/tracklists |
| `presentation/components/PinToHomeIconButton.kt` | Standalone pin icon button (album detail screen) |
| `presentation/components/SongInfoBottomSheet.kt` | Track "..." sheet, incl. its pin/unpin row |
| `presentation/screens/PlaylistDetailScreen.kt` | Playlist "..." sheet, incl. its pin/unpin row |
| `MainActivity.kt` | Floating navbar `Surface` (translucent "glass" styling) |
| `presentation/components/UnifiedPlayerSheetShared.kt` | Mini-player pill gradient styling |
| `presentation/components/UnifiedPlayerSheetV2.kt` | Player-area background colour (`playerAreaBackground`) |
| `presentation/components/player/DeezerFullPlayerBody.kt` | Full-player cover sizing (`DeezerPlayerCover`) |
| `presentation/components/player/FullPlayerContent.kt` | `CarouselStyle.TWO_PEEK` selection for the Deezer player |
| `data/network/deezer/DeezerGatewayApiService.kt` | Retrofit interface for `gateway.php` |
| `data/network/deezer/DeezerGatewayModels.kt` | Gateway response DTOs (`GwSong`, `GwEnvelope`, etc.) |
| `data/network/deezer/DeezerGatewayMappers.kt` | DTO → `Song`/`SongEntity`/`Playlist` conversion |
| `data/repository/DeezerGatewayRepository.kt` | All gateway method calls, credential resolution, validation |
| `data/preferences/PlaylistPreferencesRepository.kt` | Gateway-first-with-fallback sync for loved tracks/albums/artists, favorite playlists |
| `presentation/viewmodel/PlaylistViewModel.kt` | Gateway-first full playlist track list |
| `presentation/viewmodel/GatewayCredentialsStateHolder.kt` | Shared arl input state + save-and-test |
| `presentation/components/GatewayCredentialsSection.kt` | Shared UI for entering the `arl` cookie |
| `presentation/screens/AccountScreen.kt`, `SetupScreen.kt` | The two places the shared UI is mounted |
| `di/AppModule.kt`, `di/Qualifiers.kt` | Retrofit/DI wiring (`@DeezerRetrofit`) |
