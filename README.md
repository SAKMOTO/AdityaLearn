# AdityaLearn 📚🚀

AdityaLearn is a modern Android learning companion app built for **Aditya Engineering & Technology** students and **Android developers**.  
It combines **AI-powered assistance**, **college bus tracking**, and **interactive learning tools** into a single, beautiful app and a practical reference project for Android devs.

---

## ✨ Features

- **Multi‑AI Chat Assistant**
  - Ask questions once and compare answers from:
    - Google Gemini
    - OpenAI (ChatGPT)
    - Hugging Face models
    - Groq models
    - DeepSeek
  - Clean, card‑based UI with per‑model answers.
  - Basic XP / gamification hooks for engagement.

- **College Bus / Travel Tracking**
  - `CollegeBusTrackingService` for foreground notifications.
  - `TrackCollegeActivity` and `StudentTrackActivity` for status views.
  - Uses Google Maps & Location Services to show relevant locations.

- **AI Image Generation**
  - `ImageActivity` uses Hugging Face Inference API (Stable Diffusion).
  - Generate images from text prompts.
  - Shows progress indicator and result preview.

- **AI 3D Model Generation**
  - `ModelActivity` calls a Hugging Face 3D model endpoint.
  - Generates a `.glb` 3D model file and opens it with external 3D viewers.

- **AI Text‑to‑Speech**
  - `SpeechActivity` uses a Hugging Face TTS model.
  - Converts text input to speech using audio returned from the API.
  - Plays audio using `MediaPlayer` and caches the file locally.

- **AI Image‑to‑Video**
  - `VideoActivity` uses Stable Video Diffusion via Hugging Face.
  - Converts an image URL + optional prompt into an MP4 video.
  - Streams video output from cache using `VideoView`.

- **Gamified & Smart UI**
  - Modern Android UI with:
    - Home screen cards
    - AI tools menu
    - Bottom navigation menus
  - Lottie animations, blur effects, and rich visuals.
  - Notification hooks (e.g., bus ETA).

- **Firebase Integration**
  - Firebase Realtime Database / Firestore / Storage / Auth / Analytics.
  - Uses `google-services.json` and Firebase BoM for version management.

---

## 🛠 Tech Stack

- **Language:** Java (activities) + Kotlin (Gradle build scripts)
- **Minimum SDK:** 26  
- **Target SDK / Compile SDK:** 35  
- **Build System:** Gradle (Kotlin DSL)

**Core Libraries & Services**

- AndroidX: AppCompat, RecyclerView, CardView, WorkManager, CameraX
- Google Play Services: Maps, Location
- Material Components
- **Networking:** OkHttp, Retrofit, Volley
- **Serialization:** Gson, org.json
- **Firebase:** Database, Firestore, Storage, Auth, Analytics, FirebaseUI
- **ML & Vision:** Google ML Kit
- **UI Enhancements:** Lottie, BlurView
- **Office/Docs:** Apache POI (Excel support)

**AI / Cloud APIs**

- Google Gemini
- OpenAI Chat Completions
- Hugging Face Inference API
- Groq (OpenAI‑compatible endpoint)
- DeepSeek

> **Important:** Real API keys are **never committed to the repository**.  
> The app reads them from `local.properties` via `BuildConfig`.

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/SAKMOTO/AdityaLearn.git
cd AdityaLearn
```

Open the project in **Android Studio** (latest stable version recommended).

---

### 2. Configure Firebase

1. Create a Firebase project from the Firebase console.
2. Add an Android app with package name:
   - `com.example.adityalearn`
3. Download the `google-services.json` file.
4. Place it in:

   ```text
   app/google-services.json
   ```

5. Ensure the `google-services` plugin is enabled (already configured in `build.gradle.kts`).

---

### 3. Set Up API Keys (Required for AI Features)

API keys are loaded from **`local.properties`** and exposed through `BuildConfig` fields.  
This keeps secrets out of Git and out of the public repo.

Open `local.properties` (next to `settings.gradle.kts`) and add:

```properties
# Android SDK path (already present)
sdk.dir=/path/to/Android/sdk

# AI / Cloud API keys
GEMINI_API_KEY=your_real_gemini_key_here
OPENAI_API_KEY=your_real_openai_key_here
HUGGINGFACE_API_KEY=your_real_hf_token_here
GROQ_API_KEY=your_real_groq_key_here
DEEPSEEK_API_KEY=your_real_deepseek_key_here
```

These values are wired in `app/build.gradle.kts`:

- `BuildConfig.GEMINI_API_KEY`
- `BuildConfig.OPENAI_API_KEY`
- `BuildConfig.HUGGINGFACE_API_KEY`
- `BuildConfig.GROQ_API_KEY`
- `BuildConfig.DEEPSEEK_API_KEY`

And used in:

- `MultiAIChatActivity`
- `ImageActivity`
- `ModelActivity`
- `SpeechActivity`
- `VideoActivity`

> If a key is missing or empty, the corresponding AI feature will fail with API errors.

---

### 4. Build & Run

1. In Android Studio, click **Sync Project with Gradle Files**.
2. Select a **physical Android device** (recommended for network‑heavy AI calls).
3. Click **Run ▶** to install and launch the app.

> Emulators sometimes have network/DNS issues with external AI APIs; a real device with internet works more reliably.

---

## 📱 Main Screens & Flows

### Home / MainActivity

- Shows the primary navigation to:
  - Multi‑AI Chat
  - Image / Video / TTS / 3D tools
  - College / Bus tracking
- Uses modern cards, icons, and header drawables for a polished look.

### MultiAIChatActivity

- Accepts a user question.
- Sends it to multiple providers:
  - Gemini, DeepSeek, Groq, Hugging Face (and optionally OpenAI).
- Displays answers in card views with provider names and icons.
- Includes simple XP tracking via `SharedPreferences` (`game_stats`).

### AI Image / Video / Audio Activities

- **ImageActivity**
  - Calls Stable Diffusion on Hugging Face Inference API.
  - Shows loading spinner, then decoded PNG result.

- **VideoActivity**
  - Calls Stable Video Diffusion with an image URL + prompt.
  - Saves MP4 to cache and plays via `VideoView`.

- **SpeechActivity**
  - Sends text to Hugging Face TTS.
  - Saves an MP3 file and plays it using `MediaPlayer`.

- **ModelActivity**
  - Sends a prompt to a 3D model endpoint.
  - Saves `.glb` output and opens it via an external 3D viewer.

### Bus / Travel Tracking

- **TrackCollegeActivity** & **StudentTrackActivity**
  - Integrate with `CollegeBusTrackingService` to display status.
  - Use notifications and possibly maps/location services.
- Additional helpers for train‑tracking style responses (mocked UX).

---

## 🔐 Security & API Keys

- **No API keys** are committed to Git history:
  - Secrets are loaded from `local.properties` only.
  - `BuildConfig` fields are generated at build time.

- The Git history was cleaned using `git filter-repo` to:
  - Remove old commits containing leaked keys.
  - Re‑add `MultiAIChatActivity` with secure `BuildConfig` access only.

If you fork or reuse this project:

- **Always** use your own keys.
- **Never** hardcode secrets in source files.
- Prefer environment‑specific configs (`local.properties`, CI secrets, etc.).

---

## 🧱 Project Structure

```text
AdityaLearn/
  app/
    src/
      main/
        java/com/example/adityalearn/
          MainActivity.java
          MultiAIChatActivity.java
          ImageActivity.java
          VideoActivity.java
          SpeechActivity.java
          ModelActivity.java
          TrackCollegeActivity.java
          StudentTrackActivity.java
          CollegeBusTrackingService.java
          ...other activities & services...
        res/
          layout/
          drawable/
          menu/
          xml/
        AndroidManifest.xml
    build.gradle.kts
  build.gradle.kts
  settings.gradle.kts
  gradle.properties
  local.properties   # (ignored; holds your local API keys)
```

---

## 🤝 Contributing

Contributions are welcome! Suggestions include:

- Improving UI/UX (animations, theming, accessibility).
- Adding more AI models/providers.
- Enhancing error handling and offline modes.
- Extending bus/travel tracking features.

Basic workflow:

```bash
git clone https://github.com/SAKMOTO/AdityaLearn.git
git checkout -b feature/your-feature-name
# make changes
git commit -am "Describe your change"
git push origin feature/your-feature-name
```

Then open a **Pull Request** on GitHub.

---

## 📄 License

This project is licensed under the **MIT License**.

- You are free to use, modify, and distribute this code (including in commercial and closed-source projects), as long as you include the original license notice.
- The software is provided **"as is"**, without warranty of any kind.

See the [`LICENSE`](LICENSE) file for the full license text.

---

## 💬 Contact / Feedback

If you have questions, ideas, or issues:

- Open a GitHub **Issue** in this repo.
- Or create a **Discussion** (if enabled) describing your use case or problem.

AdityaLearn is meant to be a growing, AI‑powered learning companion—feel free to extend it and make it your own. 🚀
