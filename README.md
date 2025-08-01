# 🌍 Travel Diary App

The **Travel Diary App** is a feature-rich Android application built to help users document their travel experiences with ease. Users can create personal travel logs by adding locations, dates, notes, and images. It supports secure login/registration and stores data in Firebase for real-time access.

## ✨ Features

* **🔐 User Authentication:** Secure login, registration, and Google Sign-In integration.
* **📝 CRUD Operations:** Add, view, edit, and delete travel entries.
* **👤 Profile Management:** Update and display user profile info like name, email, and profile image.
* **📸 Image Uploads:** Attach photos for each travel destination.
* **⚡ Real-time Firestore:** Seamless syncing and data storage using Firebase Firestore.
* **🎨 Material UI Components:** Clean, modern, and responsive UI following Material Design guidelines.
* **🔒 Data Security:** Robust Firebase Authentication for user and data protection.

## 🚀 Tech Stack

* **Language:** Kotlin
* **Backend & Auth:** Firebase Authentication
* **Database:** Firebase Firestore
* **UI Design:** Material Design Components

## 📦 Getting Started

1. **Clone the repository:**

   ```bash
   git clone https://github.com/YOUR-USERNAME/YOUR-REPO.git
   cd travel-diary
   ```

2. **Open in Android Studio:**

   * Open the project using Android Studio.

3. **Setup Firebase:**

   * Create a Firebase project.
   * Add the `google-services.json` to the `app/` folder.
   * Enable Authentication and Firestore in Firebase console.

4. **Run the App:**

   * Connect your emulator/device and click **Run** in Android Studio.

## 📁 Project Structure

```
TravelDiary/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/traveldiary/
│   │   │   │   ├── activities/     # All activity classes (Main, Login, Register, etc.)
│   │   │   │   ├── adapters/       # RecyclerView adapters
│   │   │   │   ├── models/         # Data models (User, TravelEntry)
│   │   │   │   └── utils/          # Helper utilities and Firebase handlers
│   │   │   └── res/                # Layouts, drawables, and UI resources
```

## 🔮 Future Enhancements

* [ ] Admin panel for content moderation
* [ ] Search functionality for easy location filtering
* [ ] Wishlist to bookmark future travel plans
* [ ] Offline support with local caching

## 📝 Contributing

Contributions and suggestions are welcome! Feel free to fork the repo, open issues, or submit pull requests.

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---

> Made with ❤️ to help you relive your adventures — Travel smarter, document better!
