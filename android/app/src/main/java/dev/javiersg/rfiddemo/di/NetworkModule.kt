package dev.javiersg.rfiddemo.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.javiersg.rfiddemo.data.remote.AuthApiClient
import dev.javiersg.rfiddemo.data.remote.InventoryApiClient
import dev.javiersg.rfiddemo.data.remote.TokenStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideTokenStore(
        @ApplicationContext context: Context,
    ): TokenStore = TokenStore(context)

    @Provides
    @Singleton
    fun provideInventoryApiClient(tokenStore: TokenStore): InventoryApiClient =
        InventoryApiClient(tokenStore = tokenStore)

    @Provides
    @Singleton
    fun provideAuthApiClient(tokenStore: TokenStore): AuthApiClient = AuthApiClient(tokenStore = tokenStore)
}
