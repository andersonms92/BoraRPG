package com.borarpg.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class IoDispatcher

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class DefaultDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @Provides @IoDispatcher fun io(): CoroutineDispatcher = Dispatchers.IO

    @Provides @DefaultDispatcher fun computation(): CoroutineDispatcher = Dispatchers.Default
}
