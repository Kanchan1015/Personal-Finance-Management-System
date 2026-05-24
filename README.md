# Personal Finance Management System

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-purple.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Android Compile SDK](https://img.shields.io/badge/Android%20CompileSDK-35-green.svg?style=flat&logo=android)](https://developer.android.com)
[![Room Database](https://img.shields.io/badge/Room%20DB-2.6.1-blue.svg?style=flat&logo=sqlite)](https://developer.android.com/training/data-storage/room)
[![Koin DI](https://img.shields.io/badge/Koin%20DI-3.5.3-orange.svg?style=flat)](https://insert-koin.io)
[![Retrofit API](https://img.shields.io/badge/Retrofit-2.11.0-red.svg?style=flat)](https://square.github.io/retrofit/)
[![Firebase](https://img.shields.io/badge/Firebase%20BOM-32.8.0-yellow.svg?style=flat&logo=firebase)](https://firebase.google.com)

A robust, modern Android personal finance application built with **Kotlin** and **Jetpack Compose**. Designed with an **offline-first local persistence architecture**, it offers seamless tracking of incomes and expenses across currencies, savings goals tracking, recurring spending automation, customizable budget alerts, and an in-app Notification Center.

---

## 🌟 Architecture & Core Flows

This system utilizes an offline-first **MVVM (Model-View-ViewModel)** architectural pattern. Data persists locally in **Room** instantly to ensure instant UI response times and complete offline usability, while asynchronous background synchronizations push updates to **Google Cloud Firestore**.

```text
       ┌────────────────────────┐
       │   Compose UI Screens   │
       └───────────┬────────────┘
                   │  (Observes StateFlow)
                   ▼
       ┌────────────────────────┐
       │    ViewModel Layer     │
       └───────────┬────────────┘
                   │  (Orchestrates Data & DI)
                   ▼
       ┌────────────────────────┐
       │    Repository Layer    │
       └──────┬───────────┬─────┘
              │           │
              ▼ (Instant) ▼ (Async background sync)
       ┌──────────┐   ┌───────────────┐
       │ Local DB │   │  Cloud Sync   │
       │  (Room)  │   │  (Firestore)  │
       └──────────┘   └───────────────┘
```

### The Income & Currency Conversion Engine

One of the application's most powerful components is the dynamic **Currency Conversion Engine**:
* **Multi-Currency Support**: Users can enter transactions in **LKR**, **USD**, or **USDT**.
* **Live Calculations**: Converted live into `LKR` using the Retrofit network service pointing to `https://open.er-api.com/`.
* **Data Integrity**: Preserves the original entered amount, the source currency, and the precise exchange rate used.
* **Normalized Metrics**: Stores a standardized `baseAmountLKR` in the database, ensuring all totals, balances, dashboard summaries, and financial reports remain completely accurate, stable, and unified.

---

## 🚀 Key Implemented Features

### 1. Authentication & Security
* **Flexible Registration & Login**: Leverages **Firebase Authentication** for email/password and federated Google Sign-In.
* **Input Validation**: Responsive UI validation checks for email syntax, password complexity, and name fields.
* **Password Recovery**: Integrated password reset email workflow.
* **Profile Provisioning**: Automatic Firestore-backed user profile matching and configuration on first login.

### 2. Smart Income & Expense Management
* **Interactive Entries**: Intuitive screens for registering assets and spending.
* **Income Attributes**: Currency conversion preview cards with live rates, loading indicator, and friendly error handlers.
* **Category Tagging**: Income supports categories (`Salary`, `Freelance`, `Crypto`). Expenses are mapped to primary categories and subcategories for precise breakdown charts.
* **Notes**: Supporting metadata comments for every transaction.

### 3. Room-First Offline Architecture
* **Immediate Persistence**: Newly created incomes/expenses write to Room instantly. The form closes with a success indicator immediately—never blocking on remote networks.
* **Background Sync**: Launches an asynchronous transaction upload task to Firestore in a detached background coroutine.
* **Reliable Retry Logic**: When connection fails, the transaction is marked with `isSynced = false`. A periodic `SyncWorker` retries syncing only when network constraints are satisfied.

### 4. Dynamic Dashboard & Transaction History
* **Local-First Totals**: Calculations read from Room data to guarantee that newly saved local records appear instantly without waiting for cloud synchronization.
* **Rich Component Layouts**: Includes Balance cards, goals breakdown cards, spending overview summaries, and active progress gauges.
* **Complete Financial History**: Displays a fully unified history of both incomes and expenses, sorted newest-first, with built-in swipe-to-delete support and on-the-fly item updates.

### 5. Automated Recurring Expenses
* **Template Scheduling**: Define local templates for fixed, predictable recurring monthly overheads.
* **Background Automation**: A daily runner processes pending templates, auto-logs due transactions, and triggers system notifications.

### 6. Goal Tracking
* **Firestore Goals Engine**: Establish savings goals, calculate progress percentages, and estimate remaining monthly targets to stay on track.
* **Milestone Alerts**: Celebration popups and alerts are triggered at `25%`, `50%`, `75%`, and `100%` goal completion milestones.

### 7. Notification System & Preferences
* **Notification Center**: In-app persisted feed with category icons (Milestones, Alerts, Reports) to review historical warnings even after dismissing system notifications.
* **Budget Limits**: Configurable local budget threshold that triggers system-level warnings when monthly spending approaches or exceeds the limit.
* **Large Transaction Alerts**: Flags individual expenses exceeding a configurable limit.
* **Local Preferences**: A specialized Shared Preferences wrapper stores toggles, custom limits, and scheduled daily summary notification times.

---

## 💾 Room Database Schema Evolution

The local database (`finance_database`) contains three active entities: `Transaction`, `RecurringExpense`, and `NotificationEntity`. The Room Database (`AppDatabase.kt`) implements the following version history:

* **v1**: Initial local SQLite structure for basic financial records.
* **v2**: Added `exchangeRate` and `baseAmountLKR` columns to enable the unified multi-currency conversion pipeline.
* **v3**: Added `subCategory` to `Transaction` to improve UI reporting and transaction list grouping.
* **v4**: Added the `recurring_expenses` table to support template automation.
* **v5**: Added the `notifications` table to support the persistent in-app Notification Center history.

> [!NOTE]
> During active development, the database uses `fallbackToDestructiveMigration()` to simplify schema additions. For production deployment, standard migrations will replace the destructive strategy.

---

## 🛠️ Tech Stack & Version Specifications

Sourced directly from `gradle/libs.versions.toml`:

### Core Environment
* **Language**: Kotlin `1.9.22`
* **Java Virtual Machine**: Java 17
* **Android SDK**: Compile SDK `35`, Target SDK `35`, Min SDK `26`
* **Build System**: Android Gradle Plugin `8.13.2` with KSP `1.9.22-1.0.17`

### Jetpack Compose & UI
* **Jetpack Compose BOM**: `2024.04.01`
* **Material Design**: Material 3
* **Navigation**: Navigation Compose `2.7.7`
* **Icons**: Google Material Icons Extended

### Persistence & Backend
* **Local Database**: Room DB `2.6.1`
* **Dependency Injection**: Koin `3.5.3` (with `koin-android` and `koin-androidx-compose`)
* **Cloud Infrastructure**: Firebase BOM `32.8.0` (utilizing `firebase-auth-ktx` and `firebase-firestore-ktx`)
* **Task Scheduling**: WorkManager `2.9.0`
* **Network Client**: Retrofit `2.11.0` (with Gson Converter)

---

## ⚙️ Dependency Injection & App Wiring

The application relies on **Koin** for modern, lightweight, and structured dependency injection. The entire dependency map is registered under `di/AppModule.kt` and initialized once inside `PbdApplication.onCreate()`.

### Modular Architecture Mapping

#### 📂 User Interface (`ui/screens`)
Individual screens register distinct, focused ViewModels that consume domain logic cleanly:
* `auth`: `AuthViewModel`
* `profile`: `ProfileViewModel`
* `dashboard`: `DashboardViewModel`
* `income`: `IncomeViewModel`
* `expense`: `ExpenseViewModel`
* `transactions`: `TransactionHistoryViewModel` *(DI constructors fully wired)*
* `goal`: `GoalDetailViewModel`
* `notifications`: `NotificationViewModel`

#### 📂 Domain Repositories (`data/repository`)
* `AuthRepository`: Encapsulates user registration, email validation, Google Sign-In, and profile persistence.
* `FinanceRepository`: Manages SQLite Room transaction operations, handles network rate conversions, and routes asynchronous cloud sync.
* `DashboardRepository`: Reads goal calculations and profile analytics.
* `GoalRepository`: Drives savings goals updates, performs atomic transactions, and registers milestones.
* `NotificationRepository`: Manages persisted local Notification center entities.

---

## ⏳ Background Workers & Automation

The application enqueues background routines using **WorkManager** to handle network constraints and background execution safely:

| Worker Name | Trigger Frequency | Network Constraint | Core Responsibility |
|:---|:---|:---|:---|
| **`SyncWorker`** | Every 15 Minutes | **`CONNECTED`** | Queries Room for records with `isSynced = false` and uploads them to Firestore. Marks local records synced on success. |
| **`RecurringExpenseWorker`** | Every 12 Hours | None | Scans active recurring templates, logs due expenses in Room, and issues reminder notifications. |
| **`WeeklyReportWorker`** | Every 7 Days | None | Computes a financial summary for the week (Total Income vs Total Expenses) and posts a progress summary. |
| **`GoalDeadlineWorker`** | Every 24 Hours | None | Assesses active savings goal deadlines and fires system warnings as target dates approach. |

---

## 📲 Setup & Build Instructions

### 1. Firebase Configuration
The system requires a connected Google Firebase project to support Authentication and Firestore:
1. Create a project in the [Firebase Console](https://console.firebase.google.com).
2. Register an Android Application with the package name `com.example.pbd`.
3. Enable **Email/Password** and **Google** providers in the Firebase Authentication settings.
4. Enable **Cloud Firestore** in test mode or with the provided security rules (`firestore.rules`).
5. Download the configuration file `google-services.json` and place it in the app module directory:
   ```text
   app/google-services.json
   ```

### 2. Local Environment Requirements
* Android Studio (Koala or newer recommended)
* Android SDK 35 tools installed
* JDK 17 configured in Android Studio settings

### 3. Build & Run from Command Line
Compile debug sources:
```bash
./gradlew compileDebugSources
```

Run local unit tests:
```bash
./gradlew test
```

Assemble debug APK:
```bash
./gradlew assembleDebug
```
The resulting package will be generated under:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📈 Technical Development Status

### Active Implementations
* **Robust Offline-First Engine**: Local inputs are available immediately; background networks handle cloud synchronization transparently.
* **Unified Koin DI Wiring**: Clean modular separation across all screens and ViewModels.
* **Comprehensive Notification Pipelines**: Android channels for budget ceilings, recurring events, weekly reporting, and goal achievements.
* **Unified Currency Flow**: Transparent handling of foreign currencies utilizing dynamic online rates with LKR standardization.

### Known Limitations & Roadmap
* **Exchange Rate Caching**: Remote exchange rates are not cached locally, meaning offline users cannot preview conversion cards for foreign currencies (though they can still log transactions in LKR).
* **Granular Sync Status Indicator**: Sync completes silently in the background; a visible sync indicator will be added to the UI in future iterations.
* **Schema Migration Strategy**: Destructive room schema replacements will be updated to standard SQLite migration paths in final release versions.

---

## 📚 Internal Documentation References

Additional development documentation is available in the `docs` folder:
* **[Flow Architecture](file:///c:/Users/Naveeth/Documents/GitHub/Personal-Finance-Management-System/docs/flow.md)**: Deep dive into the Room + Firestore synchronization contract and state management.
* **[Income Feature Guide](file:///c:/Users/Naveeth/Documents/GitHub/Personal-Finance-Management-System/docs/add-income-feature.md)**: Exhaustive walkthrough of the multi-currency conversion preview and persistence flow.
* **[Feature Documentation](file:///c:/Users/Naveeth/Documents/GitHub/Personal-Finance-Management-System/docs/feature.doc)**: Structural outline of the core database schema and UI controllers.
* **[Firestore Security Contract](file:///c:/Users/Naveeth/Documents/GitHub/Personal-Finance-Management-System/firestore.rules)**: Active security policies and user-scoping rules for Cloud database access.

---

## 📄 License

No license file is currently included in this repository. Add one if the project is intended for public distribution or reuse.
