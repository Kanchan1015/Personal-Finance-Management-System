# Add Income Feature

## Feature Owner Scope

This feature now covers the complete Income & Currency Engine flow:

- income entry from the `AddIncomeScreen`
- live currency conversion into `LKR`
- `IncomeViewModel` state handling
- Room-first transaction persistence
- background Firestore sync
- dashboard balance usage through normalized `LKR` values
- transaction history visibility for both income and expense

## What Is Implemented

Main files involved:

- [app/src/main/java/com/example/pbd/ui/screens/income/AddIncomeScreen.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/ui/screens/income/AddIncomeScreen.kt:1)
- [app/src/main/java/com/example/pbd/ui/screens/income/IncomeUiState.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/ui/screens/income/IncomeUiState.kt:1)
- [app/src/main/java/com/example/pbd/ui/screens/income/IncomeViewModel.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/ui/screens/income/IncomeViewModel.kt:1)
- [app/src/main/java/com/example/pbd/data/repository/FinanceRepository.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/data/repository/FinanceRepository.kt:1)
- [app/src/main/java/com/example/pbd/data/local/TransactionDao.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/data/local/TransactionDao.kt:1)
- [app/src/main/java/com/example/pbd/data/model/Transaction.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/data/model/Transaction.kt:1)
- [app/src/main/java/com/example/pbd/data/model/ExchangeRateResponse.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/data/model/ExchangeRateResponse.kt:1)
- [app/src/main/java/com/example/pbd/data/remote/ExchangeRateApi.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/data/remote/ExchangeRateApi.kt:1)
- [app/src/main/java/com/example/pbd/data/remote/RetrofitClient.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/data/remote/RetrofitClient.kt:1)
- [app/src/main/java/com/example/pbd/data/repository/DashboardRepository.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/data/repository/DashboardRepository.kt:1)
- [app/src/main/java/com/example/pbd/ui/screens/dashboard/DashboardViewModel.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/ui/screens/dashboard/DashboardViewModel.kt:1)
- [app/src/main/java/com/example/pbd/data/worker/SyncWorker.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/data/worker/SyncWorker.kt:1)
- [app/src/main/java/com/example/pbd/PbdApplication.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/PbdApplication.kt:1)
- [app/src/main/java/com/example/pbd/ui/screens/transactions/TransactionHistoryViewModel.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/ui/screens/transactions/TransactionHistoryViewModel.kt:1)
- [app/src/main/java/com/example/pbd/ui/screens/dashboard/components/BalanceCard.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/ui/screens/dashboard/components/BalanceCard.kt:1)

## UI / User Flow

Implemented screen behavior:

- amount input with decimal validation
- currency dropdown with `LKR`, `USD`, `USDT`
- income type dropdown with `Salary`, `Freelance`, `Crypto`
- conversion preview card
- loading state while fetching exchange rates
- inline conversion error state
- disabled save while conversion is unresolved
- success toast and back navigation after save

Feature flow from the user side:

1. User opens `Add Income`
2. User enters an amount
3. User selects a currency
4. Screen triggers live conversion automatically
5. Preview shows converted `LKR` value
6. User taps `Save Income`
7. Transaction is saved locally first
8. Firestore sync happens in the background
9. Dashboard totals update immediately from Room data

## MVVM / Data Flow

Current architecture:

- Compose UI
- `IncomeViewModel`
- `FinanceRepository`
- Room
- background Firestore sync
- dashboard consumption through `DashboardViewModel`

Key implementation points:

- `AddIncomeScreen` only handles rendering and user interaction
- `IncomeViewModel` manages validation, conversion state, and save state
- `IncomeUiState` keeps exchange-rate and save status in one feature state object
- `IncomeViewModel` builds a `Transaction` with:
  - original `amount`
  - original `currency`
  - resolved `exchangeRate`
  - normalized `baseAmountLKR`
  - `type = INCOME`
- `FinanceRepository.saveIncome()` reuses the common transaction save pipeline

## Repository / Persistence Progress

Completed persistence behavior:

- Room-first transaction saving is implemented
- Firestore transaction sync is implemented
- unsynced rows are retained locally with `isSynced = false`
- sync retry is handled by `SyncWorker`
- the same `Transaction` model is used across UI, Room, Firestore, dashboard, and history

Current save behavior:

- `saveIncome()` delegates to `saveTransaction()`
- `saveTransaction()` inserts into Room immediately
- Firestore upload starts in a background coroutine
- successful upload marks the row as synced in Room
- failed upload leaves the row unsynced for later retry

Important note:

- a successful save result means local persistence succeeded
- it does not guarantee Firestore sync already finished

## Live Currency Conversion Progress

Completed conversion engine behavior:

- real exchange-rate fetch from `open.er-api.com`
- dynamic base currency support through the API response map
- automatic conversion preview for foreign currencies
- direct local path for `LKR` without network dependency
- save blocking when conversion is unavailable

Implemented conversion rules:

- if currency is `LKR`, then:
  - `exchangeRate = 1.0`
  - `baseAmountLKR = amount`
- if currency is foreign, then:
  - repository fetches the latest `LKR` rate for that base currency
  - ViewModel calculates `convertedAmountLKR = amount * rate`
  - that value becomes the saved `baseAmountLKR`

## Dashboard / History Integration Update

Recent feature update completed:

- dashboard totals now use Room-first transaction data
- income appears in total balance immediately after saving
- dashboard no longer waits for Firestore sync before showing the new income
- Room now exposes `getTransactionsByUser(userId)` for user-scoped transaction reads
- `FinanceRepository` exposes the same user-scoped flow to the dashboard layer
- `DashboardViewModel` now uses `FinanceRepository` + `FirebaseAuth`

Transaction history update completed:

- `TransactionHistoryViewModel` now shows both `INCOME` and `EXPENSE`
- income is no longer hidden from the history screen

## Offline / Sync Behavior

Implemented offline-safe behavior:

- user can save income without internet
- transaction is still inserted into Room immediately
- dashboard totals can still reflect that saved income from Room
- Firestore sync is retried later through WorkManager

Current sync behavior:

- `SyncWorker` runs every 15 minutes
- it requires network connectivity
- it uploads pending unsynced transactions
- successful sync marks local rows as synced

## Navigation Status

Current navigation support:

- `Screen.AddIncome` route is active
- `AddIncomeScreen` is registered in navigation
- Home screen contains an `Add Income` quick action

Relevant files:

- [app/src/main/java/com/example/pbd/navigation/Screen.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/navigation/Screen.kt:1)
- [app/src/main/java/com/example/pbd/navigation/AppNavigation.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/navigation/AppNavigation.kt:1)
- [app/src/main/java/com/example/pbd/ui/screens/home/HomeScreen.kt](/Users/kanchan/Desktop/Personal-Finance-Management-System/app/src/main/java/com/example/pbd/ui/screens/home/HomeScreen.kt:1)

## Recent UI Cleanup

Recent cleanup completed:

- deprecated icon usage updated to `Icons.AutoMirrored.Filled.*`
- applied for back arrows and trend icons in the affected screens/components

## App Status As Of Now

Implemented in this feature path:

- full income screen UI flow
- reactive currency conversion
- ViewModel state handling
- Room-first save flow
- Firestore sync flow
- background retry with WorkManager
- dashboard total-balance integration using normalized `LKR` values
- income visibility in transaction history

Still pending / future improvements:

- offline caching of exchange-rate data
- clearer user-facing sync state for pending Firestore uploads
- possible stronger retry/backoff reporting for sync failures
- migration of remaining manually wired ViewModels toward the same DI pattern

## Short Technical Summary

What makes the feature strong:

- clean `UI -> ViewModel -> Repository -> Room -> Sync` structure
- normalized `baseAmountLKR` design keeps balances and totals simple
- offline-safe transaction capture
- dashboard reflects saved income immediately after local save

What remains for improvement:

- exchange-rate caching is not implemented yet
- Firestore sync completion is background-only and not surfaced as a separate user status
