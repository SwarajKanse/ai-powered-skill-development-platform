<h1 align="center">🎓 AI-Powered Skill Development Platform</h1>

<p align="center">
  An intelligent Android application that leverages <strong>Google Gemini AI</strong> to deliver personalized course recommendations, helping users discover and track the right learning paths based on their unique goals and interests.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Language-Java-ED8B00?logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/AI-Google%20Gemini-4285F4?logo=google&logoColor=white" />
  <img src="https://img.shields.io/badge/Backend-Firebase-FFCA28?logo=firebase&logoColor=black" />
  <img src="https://img.shields.io/badge/Min%20SDK-29-blue" />
  <img src="https://img.shields.io/badge/Target%20SDK-35-blue" />
</p>

---

## ✨ Features

- **🤖 AI-Powered Recommendations** — Uses Google Gemini 1.5 Flash to recommend personalized course subjects based on your career goals, field of study, education level, and skills to master.
- **🔐 Firebase Authentication** — Secure sign-in with Google OAuth and Firebase Auth.
- **📚 Coursera Course Integration** — Fetches real courses from the Coursera API via Retrofit, mapped to Gemini's subject recommendations.
- **📋 Onboarding Questionnaire** — Collects user preferences (education level, career goals, work type, skills) to personalize the experience.
- **🏠 Smart Dashboard** — Displays a welcoming, personalized home screen with recommended courses, real-time search/filter, and a navigation drawer showing your profile and undertaken courses.
- **📖 Course Details** — Detailed view for each course including description and progress.
- **⚙️ Settings** — Profile management with editable questionnaire responses.
- **📴 Offline Support** — Falls back to cached or default courses when network is unavailable.

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java |
| Build System | Gradle (Kotlin DSL) |
| AI | Google Gemini 1.5 Flash (`generativeai:0.9.0`) |
| Auth | Firebase Authentication + Google Sign-In |
| Database | Firebase Firestore |
| Networking | Retrofit 2 + OkHttp + Gson |
| Image Loading | Glide |
| UI | AndroidX, Material Design Components, RecyclerView, DrawerLayout |

---

## 🏗️ Project Structure

```
app/src/main/java/com/example/ai_powered_skill_development_platform/
├── AuthActivity.java               # Firebase + Google Sign-In
├── QuestionnaireActivity.java      # User onboarding / interest capture
├── DashboardActivity.java          # Main screen with nav drawer & recommendations
├── CourseDetailsActivity.java      # Individual course detail view
├── SettingsActivity.java           # Profile & preference settings
├── AboutActivity.java              # About screen
│
├── GeminiRecommendationService.java  # Gemini AI integration & subject recommendation
├── CourseRepository.java             # Data layer - fetches & aggregates courses
├── CourseraApiService.java           # Retrofit interface for Coursera API
├── CourseraCourseResponse.java       # Coursera API response model
├── CourseraDomainMapper.java         # Maps Coursera data to domain models
├── RetrofitClient.java               # Retrofit singleton setup
│
├── Course.java                     # Course domain model
├── LearningPath.java               # Learning path model
├── User.java                       # User model
│
├── CourseAdapter.java              # RecyclerView adapter for course list
├── CourseDetailsAdapter.java       # RecyclerView adapter for course details cards
├── CourseListAdapter.java          # Compact course list adapter
├── LearningPathAdapter.java        # Learning path list adapter
└── UndertakenCoursesAdapter.java   # Side drawer course history adapter
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or newer
- **JDK 11+**
- A **Google Firebase** project with Authentication and Firestore enabled
- A **Google Gemini API Key** ([Get one here](https://aistudio.google.com/app/apikey))

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/SwarajKanse/ai-powered-skill-development-platform.git
   cd ai-powered-skill-development-platform
   ```

2. **Add Firebase configuration**
   - Go to [Firebase Console](https://console.firebase.google.com/), create/select a project.
   - Add an Android app with package name `com.example.ai_powered_skill_development_platform`.
   - Download `google-services.json` and place it in the `app/` directory.
   - Enable **Google Sign-In** under Authentication → Sign-in methods.

3. **Add your Gemini API Key**

   In `gradle.properties`, add:
   ```properties
   geminiApiKey=YOUR_GEMINI_API_KEY_HERE
   ```
   > ⚠️ **Never commit your API key** — `gradle.properties` is already in `.gitignore`.

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or simply open the project in Android Studio and click **Run ▶️**.

---

## 📱 App Flow

```
Login (Auth Screen)
     ↓
Questionnaire (first-time only)
  → Education Level, Field of Study, Career Goal, Skills to Master, Work Type
     ↓
Dashboard
  → Gemini AI generates 3 recommended subject areas
  → CourseRepository fetches matching Coursera courses
  → Courses displayed in horizontal RecyclerView
  → Search bar for filtering
  → Navigation drawer: profile info + undertaken courses
     ↓
Course Details → Track individual course content
     ↓
Settings → Edit profile / re-submit questionnaire
```

---

## 🔑 Key Architecture Decisions

- **Gemini API Key** is injected via `BuildConfig` at compile time (set in `gradle.properties`) — never hardcoded.
- **Offline fallback**: `GeminiRecommendationService` detects network availability and falls back to smart defaults (personalized based on field of study, rotated daily).
- **Repository pattern**: `CourseRepository` abstracts all data sources (Gemini, Coursera API, Firestore, local cache) behind a single callback interface.
- **Kotlin/Java interop**: Gemini's coroutine-based SDK is bridged to Java `CompletableFuture` via Kotlin `Continuation`.

---

## ⚙️ Configuration Reference

| Property | Location | Description |
|----------|----------|-------------|
| `geminiApiKey` | `gradle.properties` | Your Google Gemini API key |
| `google-services.json` | `app/` | Firebase project configuration |
| `applicationId` | `app/build.gradle.kts` | `com.example.ai_powered_skill_development_platform` |
| `minSdk` | `app/build.gradle.kts` | API 29 (Android 10) |
| `targetSdk` | `app/build.gradle.kts` | API 35 (Android 15) |

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

---

<p align="center">Built with ❤️ by <a href="https://github.com/SwarajKanse">Swaraj Kanse</a></p>
