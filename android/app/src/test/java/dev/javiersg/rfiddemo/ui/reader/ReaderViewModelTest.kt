package dev.javiersg.rfiddemo.ui.reader

import app.cash.turbine.test
import dev.javiersg.rfiddemo.domain.model.ReaderStatus
import dev.javiersg.rfiddemo.domain.model.RfidTag
import dev.javiersg.rfiddemo.domain.usecase.ConnectReaderUseCase
import dev.javiersg.rfiddemo.domain.usecase.DisconnectReaderUseCase
import dev.javiersg.rfiddemo.domain.usecase.ObserveReaderStatusUseCase
import dev.javiersg.rfiddemo.domain.usecase.ReadTagsUseCase
import dev.javiersg.rfiddemo.domain.usecase.StartReadingUseCase
import dev.javiersg.rfiddemo.domain.usecase.StopReadingUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReaderViewModelTest {
    private val statusFlow = MutableStateFlow(ReaderStatus.DISCONNECTED)
    private val tagsFlow = MutableSharedFlow<RfidTag>()

    private lateinit var readTags: ReadTagsUseCase
    private lateinit var startReading: StartReadingUseCase
    private lateinit var stopReading: StopReadingUseCase
    private lateinit var connectReader: ConnectReaderUseCase
    private lateinit var disconnectReader: DisconnectReaderUseCase
    private lateinit var observeReaderStatus: ObserveReaderStatusUseCase

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        statusFlow.value = ReaderStatus.DISCONNECTED

        readTags = mockk()
        startReading = mockk()
        stopReading = mockk()
        connectReader = mockk()
        disconnectReader = mockk()
        observeReaderStatus = mockk()

        every { readTags() } returns tagsFlow
        every { observeReaderStatus() } returns statusFlow
        coEvery { startReading() } just Runs
        coEvery { stopReading() } just Runs
        coEvery { connectReader() } just Runs
        coEvery { disconnectReader() } just Runs
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        ReaderViewModel(
            readTags = readTags,
            startReading = startReading,
            stopReading = stopReading,
            connectReader = connectReader,
            disconnectReader = disconnectReader,
            observeReaderStatus = observeReaderStatus,
            logger = mockk(relaxed = true),
        )

    @Test
    fun `el estado inicial es desconectado sin error`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.uiState.test {
                val state = awaitItem()
                assertEquals(ReaderStatus.DISCONNECTED, state.status)
                assertNull(state.errorMessage)
                assertTrue(state.tags.isEmpty())
                assertEquals(0, state.totalCount)
            }
        }

    @Test
    fun `refleja los cambios de estado del lector y el mensaje de error`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem()

                statusFlow.value = ReaderStatus.CONNECTED
                val connected = awaitItem()
                assertEquals(ReaderStatus.CONNECTED, connected.status)
                assertNull(connected.errorMessage)

                statusFlow.value = ReaderStatus.ERROR
                val error = awaitItem()
                assertEquals(ReaderStatus.ERROR, error.status)
                assertEquals("Error de comunicación con el lector", error.errorMessage)
            }
        }

    @Test
    fun `acumula lecturas y actualiza el rssi de un epc repetido`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem()

                tagsFlow.emit(RfidTag(epc = "EPC1", rssi = -50))
                val first = awaitItem()
                assertEquals(1, first.tags.size)
                assertEquals(1, first.totalCount)

                tagsFlow.emit(RfidTag(epc = "EPC1", rssi = -40))
                val updated = awaitItem()
                assertEquals(1, updated.tags.size)
                assertEquals(-40, updated.tags.first().rssi)
                assertEquals(2, updated.totalCount)

                tagsFlow.emit(RfidTag(epc = "EPC2", rssi = -60))
                val second = awaitItem()
                assertEquals(2, second.tags.size)
                assertEquals(3, second.totalCount)
            }
        }

    @Test
    fun `clearTags reinicia la lista y el contador`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem()
                tagsFlow.emit(RfidTag(epc = "EPC1", rssi = -50))
                awaitItem()

                viewModel.clearTags()
                val cleared = awaitItem()
                assertTrue(cleared.tags.isEmpty())
                assertEquals(0, cleared.totalCount)
            }
        }

    @Test
    fun `onStartScan invoca StartReadingUseCase`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.onStartScan()

            coVerify(exactly = 1) { startReading() }
        }
}
