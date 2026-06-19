# ScreenLink TV

Android TV receiver for ScreenLink. The app pairs with `screenlink-api`, connects to the Socket.IO `/devices` namespace, and renders remote signage commands fullscreen.

## Requirements

- Android Studio with JDK 17
- Android SDK 34
- An Android TV / Google TV emulator or device running Android 8.0 (API 26) or newer
- A running `screenlink-api`

## Configuration

Debug defaults target a backend running on the development computer:

```properties
API_BASE_URL=http://10.0.2.2:3000/
WS_BASE_URL=http://10.0.2.2:3000
```

Override them in the root `local.properties` or as Gradle properties. Physical TVs must use an address reachable from the TV, such as the computer's LAN IP. Do not commit production values or credentials.

`APP_VERSION_NAME` is generated from the Android `versionName`.

## Build and quality checks

```bash
./gradlew spotlessCheck detekt testDebugUnitTest assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Android TV emulator

1. In Android Studio Device Manager, create a TV device with a Google TV or Android TV API 34 image.
2. Start `screenlink-api` on port 3000.
3. Run the `app` configuration or execute `./gradlew installDebug`.
4. Open **ScreenLink TV** from the TV launcher.

The emulator maps `10.0.2.2` to the host computer.

## Physical Google TV

Enable Developer Options and USB/network debugging on the TV, configure the backend LAN address, then run:

```bash
adb connect TV_IP_ADDRESS:5555
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## MVP flow

1. The receiver requests a six-digit code using `POST /pairing/request`.
2. It displays the code and polls `GET /pairing/status/:screenId` every 2.5 seconds.
3. After dashboard confirmation, it stores `screenId` and the one-time `deviceToken` in DataStore.
4. It connects to `/devices` with `screenId`, `deviceToken`, and `appVersion` as Socket.IO auth.
5. It receives `screen.command`, supports image, video, playlist, clear, sync, and ping commands, and emits acknowledgement or safe error events.
6. Invalid credentials are cleared automatically and return the receiver to pairing.

The app never displays or logs the device token. `CLEAR_SCREEN` returns to a black idle screen without closing the receiver.
