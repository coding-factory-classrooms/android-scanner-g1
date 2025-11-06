package com.example.scanner.di

import android.app.Application
import com.example.scanner.data.dao.TranslationDao
import com.example.scanner.data.database.AppDb
import com.example.scanner.data.repository.AudioRepository
import com.example.scanner.data.repository.AudioRepositoryImpl
import com.example.scanner.data.repository.TranslationRepository
import com.example.scanner.data.repository.TranslationRepositoryImpl
import com.example.scanner.data.service.ApiClient
import com.example.scanner.data.service.AudioRecorderService
import com.example.scanner.data.service.TranslationApiService
import com.example.scanner.data.service.AudioRecorderServiceImpl
import com.example.scanner.data.service.DatabaseFixtureService
import com.example.scanner.data.service.DatabaseFixtureServiceImpl
import com.example.scanner.ui.viewmodel.AudioDetailsViewModel
import com.example.scanner.ui.viewmodel.AudioRecorderViewModel
import com.example.scanner.ui.viewmodel.ListTranslationViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Application
    single<Application> { androidContext() as Application }
    
    // Services
    single<AudioRecorderService> { AudioRecorderServiceImpl() }
    single<TranslationApiService> { ApiClient.create().create(TranslationApiService::class.java) }
    
    // Database
    single { AppDb.create(androidContext()) }
    single<TranslationDao> { get<AppDb>().translationDao() }
    
    // Fixture Service
    single<DatabaseFixtureService> { DatabaseFixtureServiceImpl(get(), androidContext()) }
    
    // Repositories
    single<AudioRepository> { AudioRepositoryImpl(get()) }
    single<TranslationRepository> { TranslationRepositoryImpl(get(), get()) }
    
    // ViewModels
    viewModel { AudioRecorderViewModel(get<Application>(), get(), get(), get()) }
    viewModel { ListTranslationViewModel(get()) }
    viewModel { AudioDetailsViewModel(get()) }
}
