package com.example.pbd.di

import com.example.pbd.data.repository.AuthRepository
import com.example.pbd.data.repository.AuthRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.dsl.module

val appModule = module {
    // single { ... } means Koin will create only ONE instance of this for the whole app's lifecycle
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    
    // Repositories
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
}

