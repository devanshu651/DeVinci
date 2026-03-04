# DeVinci - Artistic Music Experience

DeVinci is a minimalist, premium music player designed for Android that blends artistic geometry with a powerful streaming engine. Inspired by the genius of Leonardo da Vinci, it offers a seamless experience for both offline local music and high-quality online streaming.

## 🎨 Aesthetics & Design
- **Premium Dark Mode**: A sophisticated deep black, royal purple, and soft gold palette.
- **Fibonacci-Inspired Logo**: A minimalist fusion of an abstract 'V' and a musical note.
- **Material 3 Interface**: Modern, rounded components with elegant transitions and royal purple ripple effects.
- **Artistic Player**: Full-screen player featuring golden-bordered album art and professional-grade controls.

## 🚀 Key Features
- **Hybrid Search**: Instant, debounced search across local files and a massive library of millions of online tracks.
- **Full Song Streaming**: Enjoy complete, high-quality tracks (up to 320kbps) from the cloud without 30-second limitations.
- **Smart Queue Management**:
  - **Play Next**: Jump any song to the top of your list.
  - **Add to Queue**: Build your listening session on the fly.
- **Persistent Playback**: Powered by Android Media3 (ExoPlayer), featuring a background service that keeps your music playing even when the app is closed.
- **Intelligent Scanning**: Automatically finds all music on your device while filtering out short notification and system sounds.
- **High-Res Artwork**: Dynamic image loading and caching using Glide for a rich visual experience.

## 🛠 Tech Stack
- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel) with Clean Architecture principles.
- **Asynchronous**: Kotlin Coroutines & StateFlow for reactive UI updates.
- **Networking**: Retrofit & OKHttp with custom User-Agent spoofing for reliable streaming.
- **Media Engine**: Google Media3 (ExoPlayer & MediaSession).
- **UI Components**: Material Design 3, CoordinatorLayout, BottomSheetBehavior.
- **Image Handling**: Glide.

## 📥 Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/YOUR_USERNAME/DeVinci.git
   ```
2. Open the project in Android Studio (Iguana or newer recommended).
3. Ensure you have Java 17+ configured.
4. Sync Gradle and run the app on a physical device or emulator (Android 7.0+).

## 👨‍💻 Developer
Designed & Developed with  by **Devanshu Raut**.

---
*“Simplicity is the ultimate sophistication.” — Leonardo da Vinci*
