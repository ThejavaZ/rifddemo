package dev.javiersg.rfiddemo.domain.usecase

import dev.javiersg.rfiddemo.domain.model.ReaderStatus
import dev.javiersg.rfiddemo.domain.model.RfidTag
import dev.javiersg.rfiddemo.domain.repository.RfidRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ReaderUseCasesTest {
    private val repository = mockk<RfidRepository>()

    @Test
    fun `ReadTagsUseCase expone el flujo del repository`() =
        runTest {
            val tags = flowOf(RfidTag(epc = "EPC1", rssi = -50), RfidTag(epc = "EPC2", rssi = -60))
            every { repository.tags } returns tags

            val result = ReadTagsUseCase(repository).invoke().toList()

            assertEquals(listOf("EPC1", "EPC2"), result.map { it.epc })
        }

    @Test
    fun `ObserveReaderStatusUseCase devuelve el estado actual`() {
        every { repository.readerStatus } returns kotlinx.coroutines.flow.MutableStateFlow(ReaderStatus.CONNECTED)

        val status = ObserveReaderStatusUseCase(repository).invoke().value

        assertEquals(ReaderStatus.CONNECTED, status)
    }

    @Test
    fun `StartReadingUseCase delega en el repository`() =
        runTest {
            coEvery { repository.startReading() } just Runs

            StartReadingUseCase(repository).invoke()

            coVerify(exactly = 1) { repository.startReading() }
        }

    @Test
    fun `StopReadingUseCase delega en el repository`() =
        runTest {
            coEvery { repository.stopReading() } just Runs

            StopReadingUseCase(repository).invoke()

            coVerify(exactly = 1) { repository.stopReading() }
        }

    @Test
    fun `ConnectReaderUseCase delega en el repository`() =
        runTest {
            coEvery { repository.connect() } just Runs

            ConnectReaderUseCase(repository).invoke()

            coVerify(exactly = 1) { repository.connect() }
        }

    @Test
    fun `DisconnectReaderUseCase delega en el repository`() =
        runTest {
            coEvery { repository.disconnect() } just Runs

            DisconnectReaderUseCase(repository).invoke()

            coVerify(exactly = 1) { repository.disconnect() }
        }
}
