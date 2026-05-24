
• Current Flow

  - The merged branch already centralizes finance persistence in app/src/main/java/com/
    example/pbd/data/repository/FinanceRepository.kt:19. IncomeViewModel builds a
    Transaction and calls repository.saveIncome(...) at app/src/main/java/com/example/
    pbd/ui/screens/income/IncomeViewModel.kt:141. saveIncome is just a thin wrapper over
    saveTransaction at app/src/main/java/com/example/pbd/data/repository/
    FinanceRepository.kt:35.
  - Local persistence is Room-first. saveTransaction inserts into transactions
    immediately through TransactionDao.insertTransaction(...) at app/src/main/java/com/
    example/pbd/data/repository/FinanceRepository.kt:84 and app/src/main/java/com/
    example/pbd/data/local/TransactionDao.kt:12.
  - Firestore sync is best-effort background work, not part of the immediate save result.
    After the Room insert, saveTransaction launches a detached
    CoroutineScope(Dispatchers.IO).launch and calls pushToFirestore(...) at app/src/main/
    java/com/example/pbd/data/repository/FinanceRepository.kt:91. If upload succeeds, it
    marks the local row synced with markAsSynced(id) at app/src/main/java/com/example/
    pbd/data/repository/FinanceRepository.kt:95.
  - Result semantics are important: saveIncome(): Result<Unit> only tells the caller
    whether local save setup succeeded. It does not mean Firestore sync succeeded,
    because the remote write happens in a separate launched coroutine after the function
    returns.
  - There is no real update flow yet. Both local and remote persistence are effectively
    upsert-by-id: Room uses @Insert(onConflict = REPLACE) at app/src/main/java/com/
    example/pbd/data/local/TransactionDao.kt:12 and Firestore
    uses .document(transaction.id).set(...) at app/src/main/java/com/example/pbd/data/
    repository/FinanceRepository.kt:132.

  Room + Firestore Structure

  - The Room database is app/src/main/java/com/example/pbd/data/local/AppDatabase.kt:15
    with two active entities: Transaction and RecurringExpense. It does not currently
    persist users or dashboard data locally.
  - Transaction already has the fields the income/currency engine needs: amount,
    currency, exchangeRate, baseAmountLKR, timestamp, and isSynced in app/src/main/java/
    com/example/pbd/data/model/Transaction.kt:8. Enum fields are stored through Room
    converters in app/src/main/java/com/example/pbd/data/local/Converters.kt:11.
  - Locally, unsynced work is tracked only by isSynced; pending uploads are fetched with
    getUnsyncedTransactions() at app/src/main/java/com/example/pbd/data/local/
    TransactionDao.kt:18.
  - Firestore transaction documents live in the top-level transactions collection. The
    written schema is exactly the map in app/src/main/java/com/example/pbd/data/
    repository/FinanceRepository.kt:120: userId, type, amount, currency, exchangeRate,
    baseAmountLKR, category, subCategory, note, timestamp.
  - User documents live in top-level users, keyed by Firebase Auth UID, via app/src/main/
    java/com/example/pbd/data/repository/AuthRepository.kt:21 and app/src/main/java/com/
    example/pbd/data/repository/AuthRepository.kt:55. The current user schema is id,
    name, email, baseCurrency, totalBalanceLKR from app/src/main/java/com/example/pbd/
    data/model/User.kt:3.
  - DashboardRepository reads financial data directly from Firestore, not Room.
    Transactions are queried from transactions filtered by userId and ordered by
    timestamp DESC at app/src/main/java/com/example/pbd/data/repository/
    DashboardRepository.kt:27. It reconstructs the same Transaction model from the
    Firestore document fields at app/src/main/java/com/example/pbd/data/repository/
    DashboardRepository.kt:38.

  Sync + DI Implications

  - SyncWorker is the offline retry path. It runs every 15 minutes with a CONNECTED
    network constraint from app/src/main/java/com/example/pbd/PbdApplication.kt:28 and
    app/src/main/java/com/example/pbd/PbdApplication.kt:33. It injects FinanceRepository
    via Koin and calls syncUnsyncedTransactions() at app/src/main/java/com/example/pbd/
    data/worker/SyncWorker.kt:15.
  - Pending transaction behavior is simple: failed uploads remain in Room with isSynced =
    false, and the worker retries them later. That is the intended offline-safe behavior.
  - One architectural nuance: syncUnsyncedTransactions() catches per-item exceptions
    internally at app/src/main/java/com/example/pbd/data/repository/
    FinanceRepository.kt:107, so SyncWorker will still return Result.success() even if
    some items remain unsynced. That means retry relies more on the periodic schedule
    than on WorkManager backoff.
  - Koin is now the preferred pattern on main. Firebase singletons, Room DB/DAOs,
    repositories, and the income/expense/dashboard ViewModels are registered in app/src/
    main/java/com/example/pbd/di/AppModule.kt:19. IncomeViewModel and ExpenseViewModel
    already consume injected FinanceRepository and FirebaseAuth at app/src/main/java/com/
    example/pbd/di/AppModule.kt:37 and app/src/main/java/com/example/pbd/di/
    AppModule.kt:38.
  - The architecture is still mixed. TransactionHistoryViewModel manually constructs
    FinanceRepository and Firebase/Room instances at app/src/main/java/com/example/pbd/
    ui/screens/transactions/TransactionHistoryViewModel.kt:20, while dashboard/auth/
    worker use DI. For new income persistence work, follow the injected FinanceRepository
    path, not the manual construction pattern.

  Transaction Model Fit

  - The current Transaction model already fits income conversion cleanly. amount stores
    the entered amount, currency stores the source currency, exchangeRate stores the
    conversion rate, and baseAmountLKR stores the normalized value the dashboard uses for
    totals at app/src/main/java/com/example/pbd/data/model/Transaction.kt:13 and app/src/
    main/java/com/example/pbd/data/repository/DashboardRepository.kt:44.
  - IncomeViewModel already populates those fields correctly before persistence at app/
    src/main/java/com/example/pbd/ui/screens/income/IncomeViewModel.kt:141.
  - For the currently described feature, no schema alignment is required before
    implementing persistence. The local entity, Firestore document shape, and dashboard
    read model already all include the needed conversion fields.
  - Schema changes are only needed if the income engine wants additional metadata beyond
    the current model, such as original exchange source, quote timestamp, conversion
    precision, or multi-base support. None of that exists in the current architecture.

  Integration Recommendations

  - Implement income persistence through IncomeViewModel -> FinanceRepository.saveIncome
    -> FinanceRepository.saveTransaction. Do not create a new income repository, sync
    worker, Firestore helper, or local table.
  - Reuse the existing Transaction model and keep baseAmountLKR as the normalized balance
    field, because the dashboard already reads and sums against that contract.
    IncomeViewModel and ExpenseViewModel.
  - If transaction history or other screens need income visibility later, extend the
    existing Room-backed flows in FinanceRepository.allTransactions or
    TransactionHistoryViewModel; do not add a parallel data source.

  Concise Integration Plan

  1. Local transaction persistence: keep creating a Transaction in IncomeViewModel with
     type = INCOME, amount, currency, exchangeRate, and baseAmountLKR, then persist via
     FinanceRepository.saveIncome.
  2. Firestore sync: reuse saveTransaction and pushToFirestore; do not add a separate
     income sync path.
  3. Converted currency value persistence: continue storing the normalized LKR value in
     baseAmountLKR, since that already matches both Room and Firestore readers.
  4. Offline-safe saving: preserve the current Room-first flow with isSynced = false
     until Firestore upload succeeds, and let SyncWorker handle delayed retry through
     syncUnsyncedTransactions().

  