package dev.javiersg.rfiddemo.data.repository

import dev.javiersg.rfiddemo.data.dao.TagDao
import dev.javiersg.rfiddemo.data.database.entity.TagEntity
import dev.javiersg.rfiddemo.data.sync.SyncScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LocalTagRepositoryImplTest {
    private lateinit var dao: TagDao
    private lateinit var syncScheduler: SyncScheduler
    private lateinit var repository: LocalTagRepositoryImpl

    @BeforeEach
    fun setUp() {
        dao = mockk(relaxed = true)
        syncScheduler = mockk(relaxed = true)
        repository = LocalTagRepositoryImpl(dao, syncScheduler)
    }

    @Test
    fun `processScannedTag inserta el tag y programa la sincronizacion`() =
        runTest {
            repository.processScannedTag("EPC1", rssi = -50, antenna = 1)

            coVerify {
                dao.upsertTag(
                    match { it.epc == "EPC1" && it.rssi == -50 && it.antenna == 1 && it.readCount == 1 },
                )
            }
            coVerify(exactly = 1) { syncScheduler.scheduleSync() }
        }

    @Test
    fun `processScannedTag ignora el mismo epc escaneado dos veces`() =
        runTest {
            repository.processScannedTag("EPC1", rssi = -50, antenna = 1)
            repository.processScannedTag("EPC1", rssi = -45, antenna = 1)

            coVerify(exactly = 1) { dao.upsertTag(any()) }
            coVerify(exactly = 1) { syncScheduler.scheduleSync() }
        }

    @Test
    fun `markAsSyncing delega en el dao`() =
        runTest {
            repository.markAsSyncing(listOf("A", "B"))

            coVerify { dao.markAsSyncing(listOf("A", "B"), any()) }
        }

    @Test
    fun `markAsSynced delega en el dao`() =
        runTest {
            repository.markAsSynced(listOf("A"))

            coVerify { dao.markAsSynced(listOf("A"), any()) }
        }

    @Test
    fun `markAsFailed delega en el dao`() =
        runTest {
            repository.markAsFailed(listOf("A"))

            coVerify { dao.markAsFailed(listOf("A"), any()) }
        }

    @Test
    fun `getPendingSyncTags devuelve los tags no sincronizados`() =
        runTest {
            val pending = listOf(TagEntity(epc = "A", rssi = -50, antenna = 1, readCount = 1))
            coEvery { dao.getUnsyncedTags() } returns pending

            val result = repository.getPendingSyncTags()

            assertEquals(pending, result)
        }

    @Test
    fun `getTags devuelve el flujo del dao`() =
        runTest {
            val tags = listOf(TagEntity(epc = "A", rssi = -50, antenna = 1, readCount = 1))
            every { dao.getAllTags() } returns flowOf(tags)

            val result = repository.getTags().first()

            assertEquals(tags, result)
        }

    @Test
    fun `clearTags limpia el dao y permite volver a procesar el epc`() =
        runTest {
            repository.processScannedTag("EPC1", rssi = -50, antenna = 1)
            repository.clearTags()
            repository.processScannedTag("EPC1", rssi = -50, antenna = 1)

            coVerify { dao.clearAll() }
            coVerify(exactly = 2) { dao.upsertTag(any()) }
        }
}
