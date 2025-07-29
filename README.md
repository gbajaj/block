

# 🚀 employeedirectory: Take Home Android Interview Project for Block (Kotlin, Jetpack Compose, MVVM)

![MVVM Architecture](https://github.com/user-attachments/assets/f01ea363-b6ac-46e3-90f3-60915d08b05f)
# Square Employee Directory

A modern Android employee directory app built with Jetpack Compose, demonstrating clean architecture patterns and modern Android development practices.

## Features

- 📋 **Employee List**: Display employees with photos, names, teams, and employment types
- 🔄 **Pull-to-Refresh**: Intuitive refresh mechanism for updating employee data
- 🖼️ **Image Caching**: Efficient image loading and disk caching with Coil
- 📱 **Responsive UI**: Adaptive layout that handles different screen sizes and orientations
- ⚡ **Loading States**: Proper loading, empty, and error state handling
- 🧪 **Well Tested**: Comprehensive unit tests for business logic
- 🏗️ **Clean Architecture**: MVVM pattern with Repository pattern and dependency injection

## Screenshots
<p float="left">

<img width="300" alt="Working" src="https://github.com/user-attachments/assets/6c106558-129a-4392-9263-38df64c95890" />
<img width="300"  alt="empty" src="https://github.com/user-attachments/assets/c1017d18-c7c2-48e1-8510-bb021b2877a1" />
<img width="300" alt="p_t_refersh" src="https://github.com/user-attachments/assets/1a9bad5d-42e0-4e8a-a359-09f64b3e29bc" />
<img width="300" alt="Network Test" src="https://github.com/user-attachments/assets/ca47a31f-e948-4c41-bd3b-27269f2c8bef" />
<img width="300" alt="Malformed" src="https://github.com/user-attachments/assets/89de0951-50db-4fb5-9cd4-05fda8a79572" />

</p>
## Build tools & versions used

- **Android Studio**: Hedgehog | 2023.1.1 or later
- **Kotlin**: 1.9.22
- **Gradle**: 8.2.1
- **Target SDK**: 34
- **Min SDK**: 24
- **Compile SDK**: 34

### Key Dependencies
- **Jetpack Compose**: 2024.02.00 BOM
- **Hilt**: 2.48.1 (Dependency Injection)
- **Retrofit**: 2.9.0 (Networking)
- **Coil**: 2.5.0 (Image Loading)
- **Material 3**: 1.2.0 (UI Components)
- **Coroutines**: 1.7.3 (Async Programming)

## Steps to run the app

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 8 or higher
- Android SDK with API level 34

### Installation
1. Clone the repository:
   ```bash
   git clone [repository-url]
   cd square-employee-directory
   ```

2. Open the project in Android Studio

3. Sync the project with Gradle files

4. Run the app:
   - Click the "Run" button in Android Studio, or
   - Use command line: `./gradlew installDebug`

### Testing
Run unit tests:
```bash
./gradlew test
```

Run instrumented tests:
```bash
./gradlew connectedAndroidTest
```

## Project Structure

```
app/
├── src/main/java/com/square/employeedirectory/
│   ├── data/
│   │   ├── model/              # Data models (Employee, EmployeesResponse)
│   │   ├── remote/             # API service interfaces
│   │   └── repository/         # Repository implementations
│   ├── di/                     # Dependency injection modules
│   ├── ui/
│   │   ├── components/         # Reusable UI components
│   │   ├── model/             # UI state models
│   │   ├── theme/             # App theming
│   │   └── viewmodel/         # ViewModels
│   ├── MainActivity.kt
│   └── EmployeeDirectoryApplication.kt
└── src/test/                   # Unit tests
```

## Architecture

This app follows **Clean Architecture** principles with **MVVM** pattern:

### Data Layer
- **Repository**: Abstracts data sources and provides clean API to ViewModels
- **EmployeesApiService**: Retrofit interface for network calls
- **Models**: Data classes representing API responses

### Domain Layer
- **Use Cases**: Implicitly handled within ViewModels for this scope
- **Models**: Business logic models

### Presentation Layer
- **ViewModels**: Manage UI state and business logic
- **Composables**: UI components built with Jetpack Compose
- **UI State**: Immutable state classes for predictable UI updates

### Dependency Injection
- **Hilt**: Provides dependencies across all layers
- **Modules**: Network, Repository, and other dependency configurations

## What areas of the app did you focus on?

1. **Clean Architecture**: Implemented proper separation of concerns with clear data flow
2. **State Management**: Robust state handling with StateFlow and proper loading/error states
3. **Network Optimization**: Efficient API calls with proper error handling and image caching
4. **Testing**: Comprehensive unit tests for Repository and ViewModel layers
5. **User Experience**: Smooth pull-to-refresh, proper empty states, and responsive design

## What was the reason for your focus? What problems were you trying to solve?

### Architecture Focus
- **Problem**: Ensuring maintainable, testable, and scalable code
- **Solution**: Clean Architecture with MVVM provides clear separation and makes testing easier

### State Management Focus  
- **Problem**: Complex UI states (loading, success, error, empty) need proper handling
- **Solution**: Sealed classes with StateFlow provide predictable state updates

### Performance Focus
- **Problem**: Avoiding unnecessary network calls and optimizing image loading
- **Solution**: Repository pattern with Flow, configuration change handling, and Coil caching

### Testing Focus
- **Problem**: Ensuring reliability and catching regressions early
- **Solution**: Unit tests for business logic with Turbine for Flow testing

## How long did you spend on this project?

**Total Time**: ~5 hours

**Breakdown**:
- Setup & Architecture (1 hour): Project setup, dependencies, base architecture
- Data Layer (1 hour): Models, API service, Repository with tests
- UI Implementation (2 hours): Compose UI, ViewModels, state management
- Polish & Testing (1 hour): Error handling, edge cases, final testing

## Did you make any trade-offs for this project? What would you have done differently with more time?

### Trade-offs Made:
1. **UI Polish**: Used system components instead of custom designs for faster development
2. **Advanced Features**: Focused on core functionality over nice-to-have features
3. **Comprehensive Testing**: Focused on unit tests, skipped UI/integration tests due to time

### With More Time, I Would Add:
1. **Enhanced UI**: Custom animations, better visual design, dark theme support
2. **Advanced Features**: 
   - Search and filtering capabilities
   - Sort by name, team, or employee type
   - Employee detail screen with full biography
   - Offline support with Room database
3. **More Testing**: UI tests, integration tests, screenshot tests
4. **Performance**: 
   - Pagination for large datasets
   - More sophisticated caching strategies
   - Memory optimization
5. **Accessibility**: Better content descriptions, screen reader support
6. **Analytics**: Crash reporting, usage analytics

## What do you think is the weakest part of your project?

1. **UI Design**: The interface is functional but not visually polished - uses basic Material components without custom styling
2. **Error Granularity**: Could provide more specific error messages (network vs parsing vs server errors)
3. **Edge Case Handling**: Some edge cases around rapid refresh or network state changes could be better handled
4. **Accessibility**: Missing comprehensive accessibility features like content descriptions and screen reader optimization

## Did you copy any code or dependencies? Please make sure to attribute them here!

### Dependencies Used:
- **InterviewReady Repo**: Used https://github.com/gbajaj/interviewready I built for community for initial set up
- **Jetpack Compose**: Google's modern UI toolkit
- **Hilt**: Google's dependency injection library built on Dagger
- **Retrofit**: Square's HTTP client library
- **Coil**: Image loading library by Instacart
- **Turbine**: Cash App's Flow testing library
- **Mockito**: Mocking framework for unit tests

### Code Patterns:
- **MVVM with Repository Pattern**: Standard Android architecture pattern
- **Sealed Classes for State**: Common Kotlin pattern for representing state
- **Flow-based Data Layer**: Modern reactive programming approach

### Original Implementation:
All application code, UI components, and business logic were written specifically for this project. No code was copied from external sources beyond standard dependency usage.

## API Endpoints

The app consumes the following endpoint:

- **Main Data**: `https://s3.amazonaws.com/sq-mobile-interview/employees.json`

## Data Model

```kotlin
data class Employee(
    val uuid: String,                    // Unique identifier
    val full_name: String,              // Employee's full name
    val phone_number: String?,          // Optional phone number
    val email_address: String,          // Email address
    val biography: String?,             // Optional biography
    val photo_url_small: String?,       // Small photo URL for list view
    val photo_url_large: String?,       // Large photo URL
    val team: String,                   // Team name
    val employee_type: EmployeeType     // FULL_TIME, PART_TIME, CONTRACTOR
)
```

## Performance Considerations

- **Image Loading**: Images are loaded on-demand and cached to disk
- **Network Efficiency**: API calls are made only when necessary (app launch, pull-to-refresh)
- **Configuration Changes**: State is preserved during rotation and low memory situations
- **Memory Management**: Efficient Compose recomposition with proper keys and state hoisting

## Future Enhancements

- [ ] Dark theme support
- [ ] Employee search functionality
- [ ] Sort and filter options
- [ ] Offline mode with local database
- [ ] Employee detail screen
- [ ] Share employee contact information
- [ ] Accessibility improvements
- [ ] Performance monitoring

---

