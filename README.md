# Personal Finance Management System

An Android personal finance application built with Kotlin and Jetpack Compose for tracking income, expenses, goals, recurring spending, and dashboard summaries with offline-first transaction persistence.

## Overview

This project is structured as a modern Android app using:

- Jetpack Compose for UI
- MVVM-style presentation flow
- Room for local persistence
- Firebase Authentication for user identity
- Cloud Firestore for cloud-backed data
- WorkManager for background sync and recurring-expense processing
- Retrofit for live exchange-rate retrieval
- Koin for dependency injection

The app’s most notable implemented flow is the Income & Currency Engine:

- users can add income in `LKR`, `USD`, or `USDT`
- foreign currency is converted live into `LKR`
- the original amount and currency are preserved
- a normalized `baseAmountLKR` is stored for totals, balance calculation, and dashboard usage
- transactions are saved Room-first so the app remains usable offline

## Implemented Features

### Authentication

- user registration with Firebase Authentication
- user login with Firebase Authentication
- Firestore-backed user profile creation and retrieval
- logout support

### Income Management

- Add Income screen with:
  - amount input
  - currency selection
  - income-type selection
  - live exchange-rate preview
- supported income currencies:
  - `LKR`
  - `USD`
  - `USDT`
- supported income categories:
  - `Salary`
  - `Freelance`
  - `Crypto`
- validation and loading/error handling during conversion and save

### Expense Management

- Add Expense screen
- category and subcategory-based expense recording
- note support
- direct `LKR`-based expense persistence
- optional recurring-expense scheduling

### Income & Currency Engine

- live exchange-rate lookup through `https://open.er-api.com/`
- conversion of foreign income into normalized `LKR`
- persistence of:
  - original amount
  - original currency
  - exchange rate used
  - converted `baseAmountLKR`
- Room-first save flow with background Firestore sync

### Offline-First Transaction Persistence

- transactions are inserted into Room immediately
- Firestore upload happens asynchronously
- failed sync leaves transactions locally available with `isSynced = false`
- `SyncWorker` retries unsynced transactions when connectivity returns

### Dashboard

- total income calculation
- total expense calculation
- net balance calculation
- recent transactions section
- expense category breakdown
- active goal display and goal progress
- Room-first transaction totals so newly saved income appears immediately without waiting for Firestore sync

### Transaction History

- shows both `INCOME` and `EXPENSE`
- grouped and filtered transaction list
- totals based on normalized `LKR` values

### Goal Tracking

- Firestore-backed goal storage
- active-goal listing
- progress percentage
- monthly target estimation
- deadline-based calculations

### Recurring Expense Automation

- local recurring-expense templates
- periodic background processing
- automatic generation of due expense transactions

## Tech Stack

### Core

- Kotlin
- Java 17
- Android SDK 35
- Minimum SDK 26

### Android Libraries

- Jetpack Compose
- Material 3
- Navigation Compose
- Lifecycle ViewModel
- Room
- WorkManager

### Backend / Cloud

- Firebase Authentication
- Cloud Firestore

### Networking

- Retrofit
- Gson Converter

### Dependency Injection

- Koin

## Project Architecture

The codebase generally follows this application flow:

```text
Compose UI
↓
ViewModel
↓
Repository
↓
Room / Firestore / Remote API
↓
Background Workers where needed
```

### Main Layers

#### UI Layer

Located under:

- `app/src/main/java/com/example/pbd/ui/screens`

Contains:

- Compose screens
- local UI interaction logic
- state collection from ViewModels

#### ViewModel Layer

Handles:

- screen state
- validation
- user-driven events
- orchestration between UI and repositories

Key ViewModels include:

- `AuthViewModel`
- `IncomeViewModel`
- `ExpenseViewModel`
- `DashboardViewModel`
- `ProfileViewModel`
- `GoalDetailViewModel`
- `TransactionHistoryViewModel`

#### Repository Layer

Located under:

- `app/src/main/java/com/example/pbd/data/repository`

Responsible for:

- Room operations
- Firestore reads and writes
- exchange-rate requests
- transaction sync behavior

Key repositories:

- `AuthRepository`
- `FinanceRepository`
- `DashboardRepository`
- `GoalRepository`

#### Local Data Layer

Located under:

- `app/src/main/java/com/example/pbd/data/local`

Contains:

- Room database setup
- DAOs
- Room type converters

Main persisted entities:

- `Transaction`
- `RecurringExpense`

#### Remote / Worker Layer

Contains:

- Retrofit exchange-rate API integration
- `SyncWorker` for unsynced transactions
- `RecurringExpenseWorker` for scheduled recurring expense processing

## Important Data Models

### Transaction

The central financial record model. It stores:

- `id`
- `userId`
- `type`
- `amount`
- `currency`
- `exchangeRate`
- `baseAmountLKR`
- `category`
- `subCategory`
- `note`
- `timestamp`
- `isSynced`

`baseAmountLKR` is the normalized value used for totals, dashboard balance, and reporting.

### RecurringExpense

Stores recurring expense templates for later automatic logging through WorkManager.

### User

Represents the authenticated user profile stored in Firestore.

### Goal

Represents a user savings goal with progress and deadline tracking.

## Income & Currency Flow

The implemented income flow works like this:

```text
User opens Add Income screen
↓
Enters amount and chooses currency
↓
UI requests live exchange rate
↓
IncomeViewModel resolves conversion
↓
Preview shows converted LKR value
↓
User saves income
↓
FinanceRepository saves transaction to Room first
↓
Repository pushes transaction to Firestore in background
↓
SyncWorker retries later if Firestore upload fails
↓
Dashboard uses baseAmountLKR for total balance
```

This design keeps the app responsive and allows newly added income to appear in dashboard totals immediately.

## Current Navigation

Registered app routes include:

- splash
- home
- login
- register
- profile
- add income
- add expense
- dashboard
- transaction history
- goal detail

## Project Structure

```text
app/
  src/main/java/com/example/pbd/
    data/
      local/
      model/
      remote/
      repository/
      worker/
    di/
    navigation/
    ui/
      screens/
      theme/
docs/
```

## Setup Instructions

### Prerequisites

- Android Studio with current Android SDK support
- JDK 17
- Gradle support through the included wrapper
- Firebase project configured for:
  - Authentication
  - Cloud Firestore

### Firebase Configuration

This app expects Firebase services to be available through Android Firebase setup.

Required:

1. Create a Firebase project
2. Register the Android app package
3. Enable Firebase Authentication
4. Enable Cloud Firestore
5. Place the Firebase config file in:

```text
app/google-services.json
```

### Clone and Run

```bash
git clone <your-repository-url>
cd Personal-Finance-Management-System
./gradlew assembleDebug
```

To install from Android Studio:

1. Open the project
2. Let Gradle sync complete
3. Run on an emulator or Android device

## Useful Commands

Build debug sources:

```bash
./gradlew compileDebugSources
```

Run tests:

```bash
./gradlew test
```

Build APK:

```bash
./gradlew assembleDebug
```

## Current Status

Implemented and working in the repository today:

- authentication flow
- profile loading
- income entry with currency conversion
- expense entry
- recurring expense scheduling
- Room-based transaction persistence
- Firestore background sync
- dashboard totals and recent transaction summaries
- goal retrieval and goal detail calculations
- transaction history for both income and expense

Current limitations / future improvements:

- exchange-rate responses are not cached locally
- Firestore sync completion is not surfaced to the user as a separate sync status
- some parts of the architecture are still mixed, especially `TransactionHistoryViewModel`, which still manually builds its repository instead of using the same DI pattern as other modules
- Room currently uses destructive migration fallback during development
- instrumentation and unit test coverage is still minimal

## Documentation

Additional internal project notes are available in:

- [docs/add-income-feature.md](/Users/kanchan/Desktop/Personal-Finance-Management-System/docs/add-income-feature.md:1)
- [docs/flow.md](/Users/kanchan/Desktop/Personal-Finance-Management-System/docs/flow.md:1)

## Build Configuration

Current project configuration:

- `compileSdk = 35`
- `targetSdk = 35`
- `minSdk = 26`
- `versionCode = 1`
- `versionName = 1.0`

## License

No license file is currently included in this repository. Add one if the project is intended for public distribution or reuse.
