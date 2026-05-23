package com.example.pbd.di

import com.example.pbd.data.local.AppDatabase
import com.example.pbd.data.repository.AuthRepository
import com.example.pbd.data.repository.AuthRepositoryImpl
import com.example.pbd.data.repository.DashboardRepository
import com.example.pbd.data.repository.FinanceRepository
import com.example.pbd.data.repository.GoalRepository
import com.example.pbd.ui.screens.expense.ExpenseViewModel
import com.example.pbd.ui.screens.auth.AuthViewModel
import com.example.pbd.ui.screens.dashboard.DashboardViewModel
import com.example.pbd.ui.screens.goal.GoalDetailViewModel
import com.example.pbd.ui.screens.income.IncomeViewModel
import com.example.pbd.ui.screens.profile.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // single { ... } means Koin will create only ONE instance of this for the whole app's lifecycle
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    
    // Room Database & Daos
    single { AppDatabase.getDatabase(androidContext()) }
    single { get<AppDatabase>().transactionDao() }
    single { get<AppDatabase>().recurringExpenseDao() }
    single { GoalRepository(get(), get()) }
    
    // Repositories
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single { FinanceRepository(get(), get(), get()) }
    single { DashboardRepository(get(), get()) }
    
    // ViewModels
    viewModel { AuthViewModel(get()) }
    viewModel { ProfileViewModel(get()) }
    viewModel { IncomeViewModel(get(), get()) }
    viewModel { ExpenseViewModel(get(), get()) }
    viewModel { DashboardViewModel(get()) }
    viewModel { GoalDetailViewModel(get()) }
}
