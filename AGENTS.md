# AGENTS.md — screenlink-tv

## Project

`screenlink-tv` is the Android/Google TV receiver app for ScreenLink.

It runs on Google TV / Android TV devices and acts as a full-screen display client. It pairs with `screenlink-api`, receives remote commands, and renders images, videos, playlists, and idle states.

Related repositories:
- `screenlink-api`: NestJS backend.
- `screenlink-web`: Next.js dashboard.
- `screenlink-tv`: Android/Google TV receiver.

## Core Stack

- Kotlin
- Android TV
- Jetpack Compose
- Compose for TV where appropriate
- Gradle Kotlin DSL
- AndroidX
- Kotlin Coroutines + Flow
- OkHttp for HTTP/WebSocket
- Kotlinx Serialization
- Media3 / ExoPlayer for video playback
- Coil for image loading
- DataStore for local key/value persistence
- Room only if local cache metadata becomes complex
- Hilt for dependency injection
- JUnit
- MockK or Turbine for tests
- ktlint or Spotless
- Detekt

## Product Role

This app is not a mobile app.

It should behave like a reliable signage receiver:
- Launch into a TV-friendly fullscreen experience.
- Show a pairing code when not paired.
- Store the received device token securely enough for MVP.
- Connect to the backend WebSocket after pairing.
- Receive commands from the backend.
- Display images fullscreen.
- Play videos fullscreen.
- Play playlists in loop.
- Keep the screen awake while active.
- Recover from network failures.
- Continue showing cached/last known content when possible.

## Android TV Rules

- Optimize for TV, not phones.
- Support D-pad navigation.
- All focusable UI elements must be reachable with a remote.
- Keep the main playback/display experience fullscreen.
- Avoid touch-only UI assumptions.
- Avoid small text and dense controls.
- Make loading, pairing, offline, and error states readable from a distance.
- Avoid unnecessary dialogs during playback.
- Do not show sensitive token values.
- Do not log device tokens or full backend auth headers.
- Do not request unnecessary permissions.

## Architecture

Use a clean MVVM-style architecture.

Required top-level package structure:

```txt
app/src/main/java/com/screenlink/tv/
  core/
    config/
    di/
    logging/
    network/
      api/
      websocket/
      dto/
      interceptors/
    storage/
    model/
    result/
    util/
  data/
    pairing/
      datasources/
      repositories/
      mappers/
    device/
      datasources/
      repositories/
    commands/
      datasources/
      repositories/
    media/
      datasources/
      repositories/
    playlists/
      datasources/
      repositories/
  domain/
    pairing/
      models/
      repositories/
      usecases/
    device/
      models/
      repositories/
      usecases/
    commands/
      models/
      repositories/
      usecases/
    media/
      models/
      repositories/
      usecases/
    playlists/
      models/
      repositories/
      usecases/
  presentation/
    app/
    navigation/
    pairing/
      screens/
      components/
      viewmodels/
    player/
      screens/
      components/
      viewmodels/
    idle/
      screens/
      components/
      viewmodels/
    status/
      components/
  playback/
    image/
    video/
    playlist/
  common/
    components/
    theme/
```

Prefer folders even when there is only one file today.

## Layering Rules

Presentation:
- Compose screens and ViewModels.
- No direct HTTP or WebSocket logic.
- No token parsing.
- No raw persistence access.

Domain:
- Business models.
- Repository interfaces.
- Use cases.
- No Android framework dependencies when avoidable.

Data:
- Repository implementations.
- API clients.
- WebSocket clients.
- DTO mapping.
- DataStore access.
- Cache implementations.

Core:
- Shared infrastructure.
- Config.
- DI.
- Logging.
- Network primitives.
- Result wrappers.

Playback:
- Image renderer logic.
- Video player integration.
- Playlist sequencing.
- Playback state.

## ViewModel Rules

- ViewModels expose immutable UI state with `StateFlow`.
- Use sealed interfaces/classes for UI states when useful.
- Do not expose mutable flows.
- Handle errors explicitly.
- Do not put large business workflows directly in Composables.

## Compose Rules

- Keep Composables small and focused.
- Stateless Composables when possible.
- Stateful route/screen Composables should delegate to ViewModels.
- Use stable UI state models.
- Avoid business logic inside Composables.
- TV remote focus must be considered for every interactive control.
- Use previews where useful.

## Networking

Use OkHttp for:
- REST API calls.
- WebSocket connection to `screenlink-api`.

The TV connects to the backend with:
- `screenId`
- `deviceToken`
- `appVersion`

Device token must be stored locally after pairing.

WebSocket must:
- Reconnect automatically.
- Send heartbeat events.
- Receive `screen.command`.
- Send `screen.command_ack`.
- Send `screen.command_error`.
- Update local connection state.

Do not block the UI thread.

## Supported Commands

The app must support these command types:

- `DISPLAY_IMAGE`
- `DISPLAY_VIDEO`
- `PLAY_PLAYLIST`
- `CLEAR_SCREEN`
- `SYNC_CONTENT`
- `PING`

For unsupported commands:
- Do not crash.
- Send `screen.command_error`.
- Include a safe error message.

## Pairing Flow

Initial state:
1. App starts.
2. If no stored device token exists, call `POST /pairing/request`.
3. Display the returned pairing code.
4. Poll or wait for pairing confirmation depending on API support.
5. Store `screenId` and `deviceToken`.
6. Connect to WebSocket.

If token becomes invalid:
- Clear local credentials.
- Return to pairing screen.

## Playback Rules

Images:
- Render fullscreen.
- Preserve aspect ratio unless product requirements say otherwise.
- Use a neutral background for letterboxing.

Videos:
- Use Media3 / ExoPlayer.
- Render fullscreen.
- Recover gracefully from unsupported media.
- Send command error if playback fails.

Playlists:
- Play items in order.
- Loop by default.
- Respect image duration.
- Videos should play to completion unless a duration is explicitly provided.
- New command should interrupt current playback safely.

Idle/Clear:
- `CLEAR_SCREEN` should show a black or neutral idle screen.
- Do not exit the app.

## Screen Awake

Keep the screen awake while the receiver is active and displaying content.

Prefer safe Android window flags for active playback/display. Use wake locks only if strictly necessary and follow Android best practices.

## Logging

Use a centralized logger wrapper.

Never log:
- raw device tokens
- auth headers
- full signed URLs if they are sensitive
- personally identifiable user data

Logs should be useful for debugging:
- pairing requested
- pairing completed
- WebSocket connected/disconnected
- command received
- command acknowledged
- playback failed

## Error Handling

Use explicit result types or sealed classes for recoverable operations.

The app should never crash because:
- backend is down
- TV has no internet
- command payload is invalid
- media URL fails
- WebSocket disconnects
- unsupported command arrives

Show user-friendly TV-readable states:
- pairing
- connecting
- offline
- syncing
- playing
- idle
- error

## Build Quality

Use:
- Kotlin strict compiler settings where practical.
- ktlint or Spotless for formatting.
- Detekt for static analysis.
- No unused imports.
- No large multipurpose classes.
- No business logic in Activity.
- No networking logic in Composables.
- No hardcoded production URLs in code.

Recommended build variants:
- `debug`
- `release`

Recommended config:
- `API_BASE_URL`
- `WS_BASE_URL`
- `APP_VERSION_NAME`

## Testing

Add tests for:
- pairing repository/use case
- credential persistence
- command parsing
- unsupported command handling
- playlist sequencing
- WebSocket event mapping
- ViewModel state transitions

Use fake repositories for ViewModel tests.

## First MVP

The first MVP is complete when:

1. App launches on Google TV / Android TV emulator or physical Google TV.
2. App requests a pairing code from `screenlink-api`.
3. Pairing code is displayed fullscreen and TV-readable.
4. App stores `screenId` and `deviceToken` after pairing.
5. App connects to WebSocket using device credentials.
6. App receives `DISPLAY_IMAGE`.
7. App renders the image fullscreen.
8. App receives `DISPLAY_VIDEO`.
9. App plays video fullscreen.
10. App sends `screen.command_ack`.
11. App sends `screen.command_error` on failure.
12. App reconnects after WebSocket disconnect.
13. App shows offline/connecting state safely.
