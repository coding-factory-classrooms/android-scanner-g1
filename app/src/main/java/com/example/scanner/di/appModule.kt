package com.example.scanner.di

import com.example.scanner.data.dao.TranslationDao
import com.example.scanner.data.database.AppDb
import com.example.scanner.data.repository.AudioRepository
import com.example.scanner.data.repository.AudioRepositoryImpl
import com.example.scanner.data.service.AudioRecorderService
import com.example.scanner.data.service.AudioRecorderServiceImpl
import com.example.scanner.data.service.DatabaseFixtureService
import com.example.scanner.data.service.DatabaseFixtureServiceImpl
import com.example.scanner.ui.viewmodel.AudioRecorderViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Services
    single<AudioRecorderService> { AudioRecorderServiceImpl() }
    
    // Database
    single { AppDb.create(androidContext()) }
    single<TranslationDao> { get<AppDb>().translationDao() }
    
    // Fixture Service
    single<DatabaseFixtureService> { DatabaseFixtureServiceImpl(get(), androidContext()) }
    
    // Repositories
    single<AudioRepository> { AudioRepositoryImpl(get()) }
    
    // ViewModels
    viewModel { AudioRecorderViewModel(get(), get(), get()) }
}
