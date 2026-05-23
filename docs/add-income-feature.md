# Add Income Feature

## Feature Owner Scope

This feature now covers the `AddIncomeScreen` flow, ViewModel state handling, repository setup, and live currency conversion foundation for the Income & Currency Engine module.

Current work being done:

- Integrate the income entry screen into the app's existing MVVM structure
- Reuse the current navigation flow, repository conventions, and data-layer package structure
- Support reactive currency conversion from foreign currencies into `LKR`
- Prepare the module for later Firebase persistence, Room caching, and offline support
- Keep the implementation cleanly separated across UI, ViewModel, repository, and remote API layers

## What Was Implemented For This Feature

Files created or updated for this feature:

- [app/src/main/java/com/example/pbd/ui/screens/income/AddIncomeScreen.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/ui/screens/income/AddIncomeScreen.kt:1)
- [app/src/main/java/com/example/pbd/ui/screens/income/IncomeUiState.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/ui/screens/income/IncomeUiState.kt:1)
- [app/src/main/java/com/example/pbd/ui/screens/income/IncomeViewModel.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/ui/screens/income/IncomeViewModel.kt:1)
- [app/src/main/java/com/example/pbd/data/repository/FinanceRepository.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/data/repository/FinanceRepository.kt:1)
- [app/src/main/java/com/example/pbd/data/remote/ExchangeRateApi.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/data/remote/ExchangeRateApi.kt:1)
- [app/src/main/java/com/example/pbd/data/remote/RetrofitClient.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/data/remote/RetrofitClient.kt:1)
- [app/src/main/java/com/example/pbd/data/model/ExchangeRateResponse.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/data/model/ExchangeRateResponse.kt:1)
- [app/src/main/java/com/example/pbd/data/model/Transaction.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/data/model/Transaction.kt:1)
- [gradle/libs.versions.toml](/Users/kanchan/Desktop/Personal-Finance-Management-System/gradle/libs.versions.toml:1)
- [app/build.gradle.kts](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/build.gradle.kts:1)

Implemented architecture and UI elements:

- Screen title section styled to match the existing form screens
- Proper back button in the header
- Amount input field
- Numeric keyboard for amount entry
- Currency dropdown with:
  - `LKR`
  - `USD`
  - `USDT`
- Income type dropdown with:
  - `Salary`
  - `Freelance`
  - `Crypto`
- `Save Income` button
- `IncomeViewModel` connected to the screen using the existing Compose + ViewModel factory pattern
- `StateFlow`-driven reactive UI state using `IncomeUiState`
- Finance repository integration for save and exchange-rate lookup flow
- Clean `Compose UI -> IncomeViewModel -> FinanceRepository` interaction path
- Live exchange-rate preview for foreign currencies
- Loading indicators for both save state and exchange-rate fetching state
- Disabled save action while conversion is unresolved or a save is in progress
- Validation and inline error feedback for invalid input and conversion failures
- `@Preview` for the screen

Implementation notes:

- The screen keeps the existing `NavHostController`-based entry point
- The screen is modularized into smaller private composables
- The screen now forwards user actions to `IncomeViewModel` and only handles rendering + interaction
- The ViewModel exposes immutable UI state and keeps business logic out of Compose
- The repository currently simulates income save asynchronously and returns `Result` values
- Firebase persistence is not yet connected for the income path

## MVVM / Data Progress

Completed implementation after the initial UI-only milestone:

- Added `IncomeUiState` as a dedicated feature state model with immutable exposure through `StateFlow`
- Added `IncomeViewModel` using the project's existing `AndroidViewModel` + factory pattern
- Connected `AddIncomeScreen` to `IncomeViewModel` and reactive state observation
- Extended `Transaction` model defaults to remain compatible with the current Room + Firestore usage pattern
- Reused the existing `FinanceRepository` as the feature repository entry point
- Kept the architecture aligned to a clean `UI -> ViewModel -> Repository` flow

Current architecture flow:

- Compose UI
- `IncomeViewModel`
- `FinanceRepository`
- `RetrofitClient`
- `ExchangeRateApi`

## Repository / Networking Progress

Completed repository and remote-layer work:

- Added `saveIncome()` architecture flow inside `FinanceRepository`
- Implemented temporary async save simulation using `delay(...)`
- Reused `Result`-based success/failure handling for repository responses
- Added Retrofit and Gson converter dependencies through the existing version catalog
- Created `ExchangeRateApi` for `GET /v6/latest/{baseCurrency}`
- Created `RetrofitClient` singleton with base URL `https://open.er-api.com/`
- Added `ExchangeRateResponse` model with:
  - `result`
  - `baseCode`
  - dynamic `rates` map

Networking notes:

- The exchange-rate endpoint now supports base currencies such as `USD`, `USDT`, and `LKR`
- The `rates` map is intentionally dynamic so later conversion logic can support additional currencies without model changes
- Repository exchange-rate fetching currently extracts the `LKR` rate dynamically from the API response

## Live Currency Conversion Progress

Completed conversion engine behavior:

- Real-time exchange-rate fetching from the income flow
- Dynamic foreign-currency to `LKR` conversion
- Reactive conversion preview in the income screen
- Separate loading state for exchange-rate requests
- Separate error state for exchange-rate failures
- Direct no-network conversion path when the selected currency is already `LKR`
- Temporary debugging logs added around exchange-rate fetch and state update flow for verification

Conversion preview behavior currently implemented:

- Amount change triggers conversion refresh when the value is valid
- Currency change triggers conversion refresh automatically
- Foreign currencies display a preview such as `50 USD ≈ 15000 LKR`
- `LKR` shows direct conversion without waiting for network results
- Save is blocked while the exchange rate is still loading or when conversion resolution has failed

## Navigation Status

The route already existed in the project and is still used as-is:

- `Screen.AddIncome` in [app/src/main/java/com/example/pbd/navigation/Screen.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/navigation/Screen.kt:7)
- Registered in [app/src/main/java/com/example/pbd/navigation/AppNavigation.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/navigation/AppNavigation.kt:52)

Additional navigation work completed:

- Added a visible `Add Income` quick action on the home screen in [app/src/main/java/com/example/pbd/ui/screens/home/HomeScreen.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/ui/screens/home/HomeScreen.kt:109)

## Existing UI Patterns Reused

The implementation follows the same style already used in the project, mainly matching the expense flow:

- Dark full-screen background layout
- Rounded card-style input containers
- Section label styling
- Gradient primary action button
- Top-right compact navigation control
- Similar spacing, text sizing, and form composition used in the existing expense screen
- Compose state observation through `collectAsState()`
- One-off success handling using `LaunchedEffect`
- ViewModel factory usage from the composable entry point
- Inline error rendering and loading-aware action button behavior

Reference screen reused for visual consistency:

- [app/src/main/java/com/example/pbd/ui/screens/expense/AddExpenseScreen.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/ui/screens/expense/AddExpenseScreen.kt:75)

## App Status As Of Now

Implemented in the app overall:

- App entry and Compose setup
- Navigation graph for multiple screens
- Expense entry screen UI and save flow
- Room-based local transaction storage
- Firestore sync repository flow for transactions
- Background sync with WorkManager
- Expense transaction history screen
- Home screen UI with quick actions
- Income MVVM flow
- Income repository save foundation
- Retrofit networking foundation for exchange-rate fetching
- Live exchange-rate system for income conversion
- Reactive conversion preview and exchange-rate error handling

Partially implemented:

- Home screen summary cards use placeholder values
- Income save currently uses temporary async simulation only
- Income persistence is not yet connected to Firebase
- Room integration for income-specific persistence flow is still pending
- Offline caching / retry behavior for exchange-rate data is not yet implemented

Not implemented yet:

- Login flow
- Register flow
- Profile screen
- Dashboard screen
- Goal detail feature implementation
- Full Firebase persistence for the income path
- Offline exchange-rate caching
- Completed Room-backed income save flow

## Next Suggested Steps For This Feature

- Replace temporary income save simulation with real Firebase persistence
- Connect the income save flow to Room/offline sync behavior where needed
- Remove temporary exchange-rate debugging logs after verification is complete
- Feed converted income data into dashboard/home summary calculations
- Add retry / fallback handling for exchange-rate failures if required
