package com.example.scanner.di

import com.example.scanner.data.repository.AudioRepository
import com.example.scanner.data.repository.AudioRepositoryImpl
import com.example.scanner.data.service.AudioRecorderService
import com.example.scanner.data.service.AudioRecorderServiceImpl
import com.example.scanner.ui.viewmodel.AudioRecorderViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<AudioRecorderService> { AudioRecorderServiceImpl() }
    
    single<AudioRepository> { AudioRepositoryImpl(get()) }
    
    viewModel { AudioRecorderViewModel(get(), get()) }
}
