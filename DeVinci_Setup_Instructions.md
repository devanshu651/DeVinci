# DeVinci - Android Music Player Setup Guide

Welcome to **DeVinci**, your fully-functional Android Music Player. The codebase for DeVinci has been generated in your `c:\Users\Lenovo\OneDrive\Desktop\DeVinci` directory!

## Architecture Highlights
- **MVVM** Architecture
- **Media3 (ExoPlayer + MediaSession)** for seamless foreground/background playback, notification controls, and lock screen support.
- Configurable **MP3 Scanning** tied to `/storage/emulated/0/D/YourMusicFolderName`.
- Smooth **BottomSheet UI** transition from Mini Player to Full Player, inspired by Spotify.

## Step-by-Step Setup Instructions

1. **Open the Project in Android Studio:**
   - Launch Android Studio.
   - Click `File -> Open`.
   - Navigate to `c:\Users\Lenovo\OneDrive\Desktop\DeVinci` and select the folder. Allow it to sync.
   *(Since only the core `app/src` files and `build.gradle.kts` were generated for your convenience, it is highly recommended to create a New Android Studio Project, and copy these specific files into the matching directories if Gradle complains about missing settings scripts).*

2. **Configure Folder Path:**
   - Open `app/src/main/java/com/example/devinci/utils/Constants.kt`
   - You can edit `MUSIC_FOLDER_PATH` directly if your music directory ever changes.

3. **Required Device Setup:**
   - Place your `.mp3` files strictly inside the external directory `/storage/emulated/0/D/YourMusicFolderName` (or an emulator's equivalent mapped path).

4. **Build and Run:**
   - Connect your Android device or start an emulator (Android 7.0+ / API 24+).
   - Click the **Run** button (Green Play Icon) in Android Studio.
   - The app will prompt for the **Storage / Music & Audio** permissions. Accept this to allow the app to scan the local `.mp3` files.
   - Start playing! Close the app or swipe it away; the foreground service will keep your session alive in the background and on your lock screen.
