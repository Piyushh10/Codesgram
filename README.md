# Codesgram

[![Android](https://img.shields.io/badge/platform-Android-green?logo=android)](https://developer.android.com/) [![Kotlin](https://img.shields.io/badge/language-Kotlin-blueviolet?logo=kotlin)](https://kotlinlang.org/) [![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-ff69b4?logo=jetpack-compose)](https://developer.android.com/jetpack/compose)

Codesgram is a modern Android app that brings together coding enthusiasts to chat, collaborate, compete with peers, and track their LeetCode progress—all in one place. With real-time chat, user profiles, leaderboard and deep LeetCode integration powered by a custom backend, Codesgram is perfect for students, interview preppers, and anyone passionate about coding.

---

## 🎥 Demo 

[![Demo](https://img.youtube.com/vi/-Cvvxf7GKgE/mqdefault.jpg)](https://youtu.be/VShRal99HYc)


_Click the image above to watch the 2-minute demo_

---

## Table of Contents

- [Features](#features)
- [Screenshots](#screenshots)
- [Architecture](#architecture)
- [User Flow](#user-flow)
- [LeetCode Integration](#leetcode-integration)
- [Backend Service](#backend-service)
- [Tech Stack](#tech-stack)
- [Setup & Installation](#setup--installation)
- [Roadmap](#roadmap)
- [Contact](#contact)

---

## Features

- User authentication and profiles
- Group and personal chat functionality
- LeetCode statistics and progress tracking (via custom backend)
- Beautiful, modern UI
- More features coming soon!
- Leaderboard
- Exclusive code sharing on chat with different UI and copy functionality
- profile includes links of socials

### 🔒 Authentication & Onboarding
- Google Sign-In with Firebase Authentication
- Onboarding flow that prompts for LeetCode username and creates a user profile

### 💬 Real-Time Chat
- **Personal Chat:** One-on-one messaging with other users
- **Group Chat:** Automatic group assignment based on your LeetCode stats (Beginner, Intermediate, Expert)
- Messages are stored and synced in real-time using Firebase Realtime Database
- Toast notifications for message status

### 👤 User Profiles
- Each user has a profile with avatar, email, and LeetCode stats, social links, location information
- Profiles are stored in Firebase and updated on sign-in

### 📊 LeetCode Stats & Progress
- Fetches and displays detailed LeetCode stats: total solved, rank, contest rating, recent submissions, and more
- Visualizes progress by difficulty (Easy/Medium/Hard)
- Shows recent submissions and contest history

### 🖥️ Modern UI
- Built with Jetpack Compose for a smooth, responsive experience
- Dark mode and visually appealing layouts

---

## Screenshots

<!-- Add screenshots of your app here -->
<p align="center">
  <img src="assets/screens/cgm11.jpg" alt="Home Screen" width="250"/>
  <img src="assets/screens/cgm1.jpg" alt="Home Screen" width="250"/>
  <img src="assets/screens/cgm16.jpg" alt="Home Screen" width="250"/>
  <img src="assets/screens/cgm23.jpg" alt="Login Screen" width="250"/>
  <img src="assets/screens/cgm2.jpg" alt="Login Screen" width="250"/>
  <img src="assets/screens/cgm6.jpg" alt="Login Screen" width="250"/>
  <img src="assets/screens/cgm17.jpg" alt="Login Screen" width="250"/>
  <img src="assets/screens/cgm18.jpg" alt="Login Screen" width="250"/>
  <img src="assets/screens/cgm19.jpg" alt="Login Screen" width="250"/>
  <img src="assets/screens/cgm20.jpg" alt="Login Screen" width="250"/>
  <img src="assets/screens/cgm21.jpg" alt="Login Screen" width="250"/>
  <img src="assets/screens/cgm22.jpg" alt="Login Screen" width="250"/>
  
  <img src="assets/screens/cgm24.jpg" alt="Login Screen" width="250"/>
  
  <!-- Add more screenshots as needed -->
</p>

---

## Architecture

Codesgram uses a robust, scalable architecture:

- **MVVM Pattern:** Separation of UI, business logic, and data
- **Jetpack Compose:** Declarative UI framework
- **Firebase:** Authentication, Realtime Database for chat and user data
- **Retrofit:** For RESTful API calls to the custom LeetCode backend
- **Repository Pattern:** Abstracts data sources (network, database)
- **ViewModels:** Manage UI-related data and handle business logic

---

## User Flow

1. **Sign In:** User authenticates via Google
2. **Onboarding:** User enters LeetCode username (if first time)
3. **Home:** User lands on chat or group chat screen
4. **Profile:** User can view/edit their profile and see LeetCode stats
5. **Chat:** User can send/receive messages in real-time (personal or group)
6. **LeetCode Stats:** User can view their coding progress, recent submissions, and contest ratings

---

## LeetCode Integration

- Uses a **custom-built backend service** to fetch LeetCode data securely and reliably
- Endpoints include: user profile, badges, solved problems, contest details, submissions, language stats, and more
- Data is fetched using Retrofit and displayed in real-time in the app

---

## Backend Service

Codesgram’s LeetCode features are powered by a custom backend:

- **Tech Stack:** Node.js, Express, TypeScript, Vercel
- **Features:** REST API for user stats, problems, contests, discussions, and more
- **Source:** [Codesgram Backend GitHub Repo](https://github.com/Piyushh10/Codesgram_backend)
- **Benefits:** More reliable, customizable, and secure than public APIs

---

## Tech Stack

- **Kotlin** (primary language)
- **Jetpack Compose** (UI)
- **Firebase Auth & Realtime Database** (backend for chat and user data)
- **Retrofit** (networking)
- **MVVM Architecture**
- **Coil** (image loading)
- **Material 3** (design system)
- **Custom Node.js/Express Backend** (for LeetCode data)

---

## Setup & Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/Codesgram.git
   ```
2. **Open in Android Studio:**
   - Open Android Studio
   - Select `Open an existing project` and choose the cloned folder
3. **Configure Firebase:**
   - Add your `google-services.json` to `app/`
   - Set up Firebase Auth and Realtime Database in the Firebase console
4. **Build the project:**
   - Let Gradle sync and build the project
5. **Run the app:**
   - Connect your Android device or use an emulator
   - Click the Run button

---

## Usage
- Sign up or log in with your credentials
- Join group chats or start personal conversations
- View and track your LeetCode stats (powered by your own backend)
- Explore user profiles and more

---

## Roadmap

- [ ] Add push notifications for new messages
- [ ] Support for code sharing in chat
- [ ] More detailed LeetCode analytics
- [ ] In-app friend requests and group management
- [ ] UI/UX enhancements

---

## Contact
For questions or feedback, please contact [shivnanip815@gmail.com].
