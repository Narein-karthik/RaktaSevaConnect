# RaktaSevaConnect 🩸

RaktaSevaConnect is a real-time blood donation and emergency request Android application developed using Kotlin, Jetpack Compose, Firebase Authentication, Firestore, and Firebase Cloud Messaging.

The application helps connect blood donors with patients and hospitals during emergency situations by providing instant blood request alerts, donor management, and real-time request tracking.

---

# 📱 Features

## 👤 User Authentication
- Firebase Authentication login/signup
- Secure user accounts

## 🩸 Blood Request System
- Create emergency blood requests
- View all active requests in real time
- Request status tracking

## ✅ Donor Acceptance System
- Donors can accept blood requests
- Accepted donors displayed inside request cards
- Request owner can mark donor as completed

## 👤 Profile Management
- Edit profile details
- Upload and save profile picture
- Blood group management

## 📍 Location Features
- Saves donor location using device GPS
- Nearby donor support preparation

## 🔔 Notifications
- Firebase Cloud Messaging integration
- Real-time emergency alert support

## ☁️ Firebase Integration
- Firestore realtime database
- Cloud-based user and request storage

---

# 🛠️ Tech Stack

- Kotlin
- Jetpack Compose
- Firebase Authentication
- Firebase Firestore
- Firebase Cloud Messaging (FCM)
- Android Studio

---

# 📂 Project Structure

```text
app/
 ├── manifests/
 ├── java/com.example.raktasevaconnect/
 │    ├── MainActivity.kt
 │    ├── MyFirebaseMessagingService.kt
 │    └── ui/theme/
 ├── res/
 └── Gradle Scripts/


---

# 🚀 Setup Instructions

## 1️⃣ Clone Repository

```bash
git clone https://github.com/Narein-karthik/RaktaSevaConnect.git
```

---

## 2️⃣ Open in Android Studio

- Open Android Studio
- Click Open Project
- Select cloned folder

---

## 3️⃣ Firebase Setup

Add your Firebase `google-services.json` file inside:

```text
app/google-services.json
```

Enable:
- Firebase Authentication
- Cloud Firestore
- Firebase Cloud Messaging

---

## 4️⃣ Run Application

- Connect emulator/device
- Click ▶ Run in Android Studio

---

# 📸 Screenshots

(Add screenshots here)

---

# 🎯 Project Goal

RaktaSevaConnect aims to provide a fast and reliable emergency blood donation platform that helps connect nearby donors with patients and hospitals during critical situations.

---

# 👨‍💻 Developer

### Narein Karthik

MindMatrix VTU Internship Project 🚀
