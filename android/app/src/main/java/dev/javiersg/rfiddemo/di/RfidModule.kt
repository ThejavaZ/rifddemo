package dev.javiersg.rfiddemo.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.javiersg.rfiddemo.BuildConfig
import dev.javiersg.rfiddemo.data.api.InventoryApiClient
import dev.javiersg.rfiddemo.data.dao.TagDao
import dev.javiersg.rfiddemo.data.repository.LocalTagRepositoryImpl
import dev.javiersg.rfiddemo.data.repository.RfidRepositoryImpl
import dev.javiersg.rfiddemo.data.rfid.MockRfidReader
import dev.javiersg.rfiddemo.data.rfid.ZebraRfidReader
import dev.javiersg.rfiddemo.data.sync.KtorTagSyncGateway
import dev.javiersg.rfiddemo.data.sync.SyncScheduler
import dev.javiersg.rfiddemo.data.sync.TagSyncGateway
import dev.javiersg.rfiddemo.domain.repository.LocalTagRepository
import dev.javiersg.rfiddemo.domain.repository.RfidReader
import dev.javiersg.rfiddemo.domain.repository.RfidRepository
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RfidModule {
    @Provides
    @Singleton
    @MockRfid
    fun provideMockReader(): RfidReader = MockRfidReader()

    @Provides
    @Singleton
    @ZebraRfid
    fun provideZebraReader(
        @ApplicationContext context: Context,
    ): RfidReader = ZebraRfidReader(context)

    // Alternancia Mock/Zebra según BuildConfig.USE_MOCK_READER; Provider evita instanciar el no elegido.
    @Provides
    @Singleton
    fun provideRfidReader(
        @MockRfid mock: Provider<RfidReader>,
        @ZebraRfid zebra: Provider<RfidReader>,
    ): RfidReader = if (BuildConfig.USE_MOCK_READER) mock.get() else zebra.get()

    @Provides
    @Singleton
    fun provideLocalTagRepository(
        tagDao: TagDao,
        syncScheduler: SyncScheduler,
    ): LocalTagRepository = LocalTagRepositoryImpl(tagDao, syncScheduler)

    @Provides
    @Singleton
    fun provideRfidRepository(
        reader: RfidReader,
        localTagRepository: LocalTagRepository,
    ): RfidRepository = RfidRepositoryImpl(reader, localTagRepository)

    @Provides
    @Singleton
    fun provideSyncGateway(apiClient: InventoryApiClient): TagSyncGateway = KtorTagSyncGateway(apiClient)
}
