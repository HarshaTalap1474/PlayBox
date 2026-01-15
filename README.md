# PlayBox - Android Media Player

**PlayBox** is a lightweight, local media player application for Android built using Java. It scans the device's external storage for audio files and provides a user-friendly interface to play, pause, and navigate through a playlist.

This project was developed to fulfill the requirement: *"Build a media player app capable of playing audio files from local storage."*

## 📱 Features

* **Local Audio Scanning:** Automatically fetches all audio files (`.mp3`, `.wav`, `.m4a`, etc.) from the device storage.
* **Playback Controls:**
    * **Play/Pause:** Toggle audio state.
    * **Next/Previous:** Skip to the next or previous track.
    * **Auto-Play:** Automatically starts the next song when the current one finishes.
* **Dynamic UI:**
    * Displays the currently playing song title.
    * Play/Pause button icon updates dynamically based on the state.
    * Scrollable list of all available tracks.
* **Permission Handling:** Supports Android runtime permissions for both older (API < 33) and newer (Android 13+) devices.

## 🛠 Tech Stack

* **Language:** Java
* **IDE:** Android Studio
* **UI Design:** XML (Relative Layout & Linear Layout)
* **Core APIs:**
    * `MediaPlayer` (Audio handling)
    * `ContentResolver` (File fetching)
    * `Cursor` (Database iteration)

## 🚀 How to Run

1.  **Clone or Download** this project to your computer.
2.  Open **Android Studio**.
3.  Select **File > Open** and navigate to the project folder.
4.  Wait for Gradle to sync (this downloads necessary dependencies).
5.  Connect your Android device via USB or start an Emulator.
    * *Note: If using an Emulator, ensure you have dragged and dropped some .mp3 files onto the emulator first, as they default to empty storage.*
6.  Click the **Run** (Green Play) button.

## 🔒 Permissions Explained

This app requires access to storage to find your music. It handles two different permission models depending on the Android version:

* **Android 12 and below:** Uses `READ_EXTERNAL_STORAGE`.
* **Android 13 (API 33) and above:** Uses `READ_MEDIA_AUDIO`.

*When you first launch the app, you must tap "Allow" on the permission dialog, or the song list will remain empty.*

## 📂 Project Structure

* **MainActivity.java**: Contains all the logic for fetching songs, handling button clicks, and managing the `MediaPlayer` state.
* **activity_main.xml**: Defines the layout including the `ListView` for songs and `ImageButtons` for controls.
* **AndroidManifest.xml**: Declares the necessary storage permissions.

## 🔮 Future Scope

Possible enhancements for future versions:
* Add a **SeekBar** to show progress and scrub through audio.
* Display **Album Art** fetched from audio metadata.
* Create a notification bar controller for background playback.
* Implement a "Shuffle" and "Repeat" mode.

## 👤 Author

**Harshavardhan Talap**
Student, BE EnTC at DPYCOE Akurdi.