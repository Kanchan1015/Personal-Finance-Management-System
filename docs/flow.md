• Current Flow

  - Income creation is still centralized through app/src/main/java/com/example/pbd/data/
    repository/FinanceRepository.kt:19. `IncomeViewModel` builds a `Transaction` and
    calls `repository.saveIncome(...)` at app/src/main/java/com/example/pbd/ui/screens/
    income/IncomeViewModel.kt:141. `saveIncome()` remains a thin wrapper over
    `saveTransaction()` in app/src/main/java/com/example/pbd/data/repository/
    FinanceRepository.kt:39.
  - Local persistence is Room-first. `saveTransaction()` inserts into the `transactions`
    table immediately through `TransactionDao.insertTransaction(...)` at app/src/main/
    java/com/example/pbd/data/repository/FinanceRepository.kt:84 and app/src/main/java/
    com/example/pbd/data/local/TransactionDao.kt:12.
  - Firestore sync is still best-effort background work, not part of the immediate save
    result. After the Room insert, `saveTransaction()` launches a detached background
    coroutine and calls `pushToFirestore(...)` at app/src/main/java/com/example/pbd/data/
    repository/FinanceRepository.kt:91. If upload succeeds, it marks the local row synced
    with `markAsSynced(id)` at app/src/main/java/com/example/pbd/data/repository/
    FinanceRepository.kt:95.
  - Result semantics remain important: `saveIncome(): Result<Unit>` only tells the caller
    whether local save setup succeeded. It does not mean Firestore sync succeeded,
    because the remote write happens after the function returns.
  - There is still no separate update flow. Both local and remote persistence are
    effectively upsert-by-id: Room uses `@Insert(onConflict = REPLACE)` at app/src/main/
    java/com/example/pbd/data/local/TransactionDao.kt:12 and Firestore uses
    `.document(transaction.id).set(...)` at app/src/main/java/com/example/pbd/data/
    repository/FinanceRepository.kt:120.

  Room + Firestore Structure

  - The Room database is app/src/main/java/com/example/pbd/data/local/AppDatabase.kt:15
    with two active entities: `Transaction` and `RecurringExpense`.
  - `Transaction` already contains the full income/currency contract: `amount`,
    `currency`, `exchangeRate`, `baseAmountLKR`, `timestamp`, and `isSynced` in
    app/src/main/java/com/example/pbd/data/model/Transaction.kt:8.
  - Locally, pending sync work is tracked by `isSynced`; unsynced rows are fetched with
    `getUnsyncedTransactions()` at app/src/main/java/com/example/pbd/data/local/
    TransactionDao.kt:23.
  - Room now also exposes `getTransactionsByUser(userId)` at app/src/main/java/com/
    example/pbd/data/local/TransactionDao.kt:18 so dashboard totals can be calculated
    immediately from local user-scoped data.
  - Firestore transaction documents still live in the top-level `transactions`
    collection. The written schema is the map in app/src/main/java/com/example/pbd/data/
    repository/FinanceRepository.kt:109: `userId`, `type`, `amount`, `currency`,
    `exchangeRate`, `baseAmountLKR`, `category`, `subCategory`, `note`, `timestamp`.

  Dashboard + History Update

  - The dashboard bug was caused by reading only from Firestore. That has now been
    corrected in app/src/main/java/com/example/pbd/ui/screens/dashboard/
    DashboardViewModel.kt:1.
  - `DashboardViewModel` now consumes `FinanceRepository` and `FirebaseAuth`, resolves
    the current user ID, and collects `financeRepository.getTransactionsByUser(userId)`.
  - This makes the dashboard Room-first for transaction totals, so newly saved income
    appears in total balance immediately even when Firestore sync is delayed or offline.
  - `DashboardRepository` still remains in use for Firestore-backed data that is not part
    of the local transaction flow, such as goals and user profile reads.
  - `TransactionHistoryViewModel` no longer filters to `EXPENSE` only. It now exposes all
    locally stored transactions, which makes income visible in transaction history.

  Sync + DI Implications

  - `SyncWorker` remains the offline retry path. It runs every 15 minutes with a
    `CONNECTED` network constraint from app/src/main/java/com/example/pbd/
    PbdApplication.kt:28 and app/src/main/java/com/example/pbd/PbdApplication.kt:33.
  - Pending transaction behavior is now stronger from a UX perspective: failed uploads
    still remain in Room with `isSynced = false`, but the dashboard can already show the
    saved income because it reads from Room.
  - Koin wiring now includes the updated dashboard dependencies. `DashboardViewModel`
    receives `DashboardRepository`, `FinanceRepository`, and `FirebaseAuth` through
    app/src/main/java/com/example/pbd/di/AppModule.kt:39.
  - `TransactionHistoryViewModel` is still manually constructing its repository instead of
    using DI. That is functional, but still an architectural inconsistency worth cleaning
    up later.

  Transaction Model Fit

  - The current `Transaction` model still fits the feature cleanly. `amount` stores the
    entered amount, `currency` stores the source currency, `exchangeRate` stores the
    conversion rate, and `baseAmountLKR` stores the normalized value used by balances and
    totals.
  - `IncomeViewModel` already populates those values correctly before persistence at
    app/src/main/java/com/example/pbd/ui/screens/income/IncomeViewModel.kt:141.
  - No schema change was required to fix the dashboard issue. The fix was a data-source
    correction, not a model change.

  Current Integration Summary

  1. Income entry: `AddIncomeScreen` collects amount, currency, and income type.
  2. Conversion: `IncomeViewModel` fetches the live `LKR` exchange rate and calculates
     `baseAmountLKR`.
  3. Save: `FinanceRepository.saveIncome()` persists the `Transaction` locally first.
  4. Sync: Firestore upload happens in the background and marks rows synced on success.
  5. Dashboard: `DashboardViewModel` reads Room data immediately for balance totals.
  6. Retry: `SyncWorker` uploads any unsynced rows when connectivity is available.
