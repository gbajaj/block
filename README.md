

# 🚀 employeedirectory: Android Interview Project Skeleton (Kotlin, Jetpack Compose, MVVM)

![MVVM Architecture](https://github.com/user-attachments/assets/f01ea363-b6ac-46e3-90f3-60915d08b05f)

## ✨ Overview

This repository serves as a meticulously crafted **Android project skeleton** designed to give interviewees a significant head start in common coding challenges. In many Android technical interviews, candidates are asked to implement features like fetching data from a network, displaying it in a list, and showing details for a selected item. This project provides the **essential boilerplate, dependencies, and architectural structure pre-configured**, allowing you to **focus immediately on solving the actual problem logic** rather than spending valuable time on initial setup.

It sets up a modern Android stack using **Kotlin, Jetpack Compose, and the MVVM architectural pattern**, ensuring you're ready to showcase your problem-solving skills in a contemporary environment.

-----

## 🛠️ Key Technologies & Architecture

This skeleton project comes pre-configured with the following modern Android technologies and architectural choices:

  * **Kotlin:** The primary programming language for concise and expressive Android development.
  * **Jetpack Compose:** Google's modern toolkit for building native Android UI. The project provides basic UI composables for handling various data states.
  * **MVVM (Model-View-ViewModel):** A robust architectural pattern that separates concerns, making the codebase testable and maintainable.
  * **Retrofit:** A type-safe HTTP client for Android and Java, pre-configured for network requests.
  * **Coil:** A fast, lightweight image loading library for Android, built with Kotlin Coroutines.
  * **StateFlow:** Part of Kotlin Coroutines, used for reactive state management within the MVVM architecture.
  * **Dependency Injection (DI):** Managed via an `appmodule` for easy provision of dependencies like API services and repositories.

**Note:** `Room` (for local database persistence) has deliberately **not been configured** in this skeleton. Interviewees can choose to integrate it if the problem statement requires local data caching, or bypass it if not needed.

-----

## 📁 Project Structure

The project is structured with clear separation of concerns, following best practices for modularity in Android development:

  * `data/api/`: Contains the `UserApi` interface, a placeholder for defining your network endpoints. It currently points to a fake endpoint and should be replaced with the actual API endpoint for your task.
  * `data/model/`: Houses data classes that represent your network responses or domain entities. A sample `User` data class is provided as a placeholder.
  * `data/repository/`: Contains repository interfaces and implementations, abstracting data sources (like network or local database).
  * `di/appmodule/`: Manages dependency injection setup for the application.
  * `ui/base/`: Contains base classes or common utilities for UI components.
  * `ui/components/`: Reusable Jetpack Compose UI elements.
  * `ui/screens/`: Holds the main composable screens (e.g., for the list and detail views).
  * `ui/viewmodel/`: Contains the ViewModels that expose data to the UI and handle UI logic.

-----

## 🚦 Pre-configured UI States

To accelerate your UI development, basic **Jetpack Compose UI components** are provided to handle common data fetching states. These components offer a starting point for:

  * **Initial State:** What the screen looks like before any data is loaded.
  * **Loading State:** Showing a progress indicator while data is being fetched.
  * **Error State:** Displaying an error message if the network request fails.
  * **Success State:** Ready for you to implement the actual data display (e.g., your list of items).
  * **Retry State:** A mechanism to re-attempt data fetching after an error.

These foundational UI elements mean you can jump straight into binding your fetched data to the UI.

-----

## 🚀 How to Use This Skeleton

Follow these steps to get your interview project up and running quickly:

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/gbajaj/employeedirectory.git
    ```
2.  **Open in Android Studio:**
      * Launch Android Studio.
      * Select `Open an existing Android Studio project` and navigate to the cloned `employeedirectory` directory.
3.  **Sync Gradle:**
      * Allow Gradle to sync the project dependencies.
4.  **Replace Placeholder API:**
      * Navigate to `data/api/UserApi.kt` and replace the fake endpoint URL with the actual API endpoint provided in your interview problem.
      * Adjust the `User` data class in `data/model/User.kt` to match the structure of the data you'll be fetching from the API.
5.  **Implement Logic:**
      * Focus on implementing the data fetching logic within the `data/repository/` and `ui/viewmodel/` layers.
      * Populate the `ui/screens/` with your specific UI implementation for the list and detail views, leveraging the provided state-handling components.
6.  **Run on a device/emulator:**
      * Connect an Android device or start an emulator.
      * Click the 'Run' button in Android Studio.

-----
## Detailed project stucture
```
.
├── AndroidManifest.xml
├── java
│   └── com
│       └── gauravbajaj
│           └── employeedirectory
│               ├── MainActivity.kt
│               ├── MyApplication.kt
│               ├── data
│               │   ├── api
│               │   │   └── UserApi.kt
│               │   ├── model
│               │   │   └── User.kt
│               │   └── repository
│               │       └── UserRepository.kt
│               ├── di
│               │   ├── AppModule.kt
│               │   └── Qualifiers.kt
│               ├── navigation
│               │   ├── Navigation.kt
│               │   └── Screens.kt
│               ├── ui
│               │   ├── base
│               │   │   ├── ScreenContent.kt
│               │   │   └── UIState.kt
│               │   ├── components
│               │   │   ├── ErrorMessage.kt
│               │   │   └── LoadingIndicator.kt
│               │   ├── screens
│               │   │   ├── DetailsScreen.kt
│               │   │   └── HomeScreen.kt
│               │   ├── theme
│               │   │   ├── Color.kt
│               │   │   ├── Theme.kt
│               │   │   └── Type.kt
│               │   └── viewmodel
│               │       └── HomeViewModel.kt
│               └── utils
├── print.txt
└── res
    ├── drawable
    │   ├── ic_launcher_background.xml
    │   └── ic_launcher_foreground.xml
    ├── mipmap-anydpi-v26
    │   ├── ic_launcher.xml
    │   └── ic_launcher_round.xml
    ├── mipmap-hdpi
    │   ├── ic_launcher.webp
    │   └── ic_launcher_round.webp
    ├── mipmap-mdpi
    │   ├── ic_launcher.webp
    │   └── ic_launcher_round.webp
    ├── mipmap-xhdpi
    │   ├── ic_launcher.webp
    │   └── ic_launcher_round.webp
    ├── mipmap-xxhdpi
    │   ├── ic_launcher.webp
    │   └── ic_launcher_round.webp
    ├── mipmap-xxxhdpi
    │   ├── ic_launcher.webp
    │   └── ic_launcher_round.webp
    ├── raw
    │   └── users.json
    ├── values
    │   ├── colors.xml
    │   ├── strings.xml
    │   └── themes.xml
    └── xml
        ├── backup_rules.xml
        └── data_extraction_rules.xml
```

---

## Screenshots

Taken from Google Pixel 9 Pro

<p float="left">
  <img src="https://github.com/user-attachments/assets/d4c6a742-101c-4033-b980-71df4abdc2cb" width="300" />
  <img src="https://github.com/user-attachments/assets/a1ba5323-14bf-4cfb-8acd-3548d0b47798" width="300" /> 
 
 <img src="https://github.com/user-attachments/assets/cca7b4dd-14ac-4ca9-80bc-5d832525d25f" width="300" />
 
  <img src="https://github.com/user-attachments/assets/7f5f60f9-0622-4c3f-b62a-922e9f18c8f7" width="300" />
 <img src="https://github.com/user-attachments/assets/86471ca2-627c-4df4-bd4a-41b273fb85dc" width="300" />

 
</p>


-----


## 📞 Contact

For any questions or suggestions regarding this skeleton project, feel free to reach out.

  * Project Link: [https://github.com/gbajaj/employeedirectory](https://github.com/gbajaj/employeedirectory)

-----
