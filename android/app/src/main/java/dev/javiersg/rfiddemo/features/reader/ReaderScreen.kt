package dev.javiersg.rfiddemo.features.reader

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.javiersg.rfiddemo.domain.model.ReaderStatus
import dev.javiersg.rfiddemo.domain.model.RfidTag
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Feedback háptico y auditivo en cada lectura nueva de etiqueta.
    val haptics = LocalHapticFeedback.current
    val tone = remember { runCatching { ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80) }.getOrNull() }
    DisposableEffect(Unit) {
        onDispose { tone?.release() }
    }
    var lastTotalCount by remember { mutableStateOf(uiState.totalCount) }
    LaunchedEffect(uiState.totalCount) {
        if (uiState.totalCount > lastTotalCount) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            tone?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
        }
        lastTotalCount = uiState.totalCount
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Lector RFID") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                        )
                    }
                },
                actions = {
                    ConnectionBadge(
                        status = uiState.status,
                        modifier = Modifier.padding(end = 16.dp),
                    )
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ConnectionControls(
                status = uiState.status,
                onConnect = { viewModel.onConnect() },
                onDisconnect = { viewModel.onDisconnect() },
            )

            MetricsBar(
                tagCount = uiState.tags.size,
                totalCount = uiState.totalCount,
                onClearTags = { viewModel.clearTags() },
            )

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
            ) {
                when {
                    uiState.errorMessage != null ->
                        ErrorState(
                            message = uiState.errorMessage.orEmpty(),
                            modifier = Modifier.align(Alignment.Center),
                        )

                    uiState.status == ReaderStatus.CONNECTING ->
                        LoadingState(
                            modifier = Modifier.align(Alignment.Center),
                        )

                    uiState.tags.isEmpty() ->
                        EmptyState(
                            modifier = Modifier.align(Alignment.Center),
                        )

                    else ->
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(
                                items = uiState.tags,
                                key = { it.epc },
                            ) { tag ->
                                TagReadItem(tag = tag)
                            }
                        }
                }
            }

            ScanControls(
                status = uiState.status,
                onStartScan = { viewModel.onStartScan() },
                onStopScan = { viewModel.onStopScan() },
            )
        }
    }
}

@Composable
private fun ConnectionBadge(
    status: ReaderStatus,
    modifier: Modifier = Modifier,
) {
    val (label, color) =
        when (status) {
            ReaderStatus.DISCONNECTED -> "Desconectado" to MaterialTheme.colorScheme.outline
            ReaderStatus.CONNECTING -> "Conectando" to MaterialTheme.colorScheme.tertiary
            ReaderStatus.CONNECTED -> "Conectado" to MaterialTheme.colorScheme.primary
            ReaderStatus.SCANNING -> "Escaneando" to MaterialTheme.colorScheme.tertiary
            ReaderStatus.ERROR -> "Error" to MaterialTheme.colorScheme.error
        }

    Row(
        modifier =
            modifier
                .background(
                    color = color.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(50),
                ).padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(8.dp)
                    .background(color = color, shape = CircleShape),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
        )
    }
}

@Composable
private fun ConnectionControls(
    status: ReaderStatus,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
) {
    val connected = status == ReaderStatus.CONNECTED || status == ReaderStatus.SCANNING

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (connected) {
            OutlinedButton(
                onClick = onDisconnect,
                modifier = Modifier.weight(1f),
            ) {
                Text("Desconectar")
            }
        } else {
            OutlinedButton(
                onClick = onConnect,
                enabled = status != ReaderStatus.CONNECTING,
                modifier = Modifier.weight(1f),
            ) {
                Text("Conectar")
            }
        }
    }
}

@Composable
private fun ScanControls(
    status: ReaderStatus,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = onStartScan,
            enabled = status == ReaderStatus.CONNECTED,
            modifier = Modifier.weight(1f),
        ) {
            Text("Start")
        }
        OutlinedButton(
            onClick = onStopScan,
            enabled = status == ReaderStatus.SCANNING,
            modifier = Modifier.weight(1f),
        ) {
            Text("Stop")
        }
    }
}

@Composable
private fun MetricsBar(
    tagCount: Int,
    totalCount: Int,
    onClearTags: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column {
                Text("Únicos", style = MaterialTheme.typography.labelSmall)
                Text("$tagCount", style = MaterialTheme.typography.titleLarge)
            }
            Column {
                Text("Total Lecturas", style = MaterialTheme.typography.labelSmall)
                Text("$totalCount", style = MaterialTheme.typography.titleLarge)
            }
        }
        OutlinedButton(onClick = onClearTags) {
            Text("Limpiar")
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Text(
        text = "No se han detectado lectura de tags.",
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier,
    )
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    CircularProgressIndicator(modifier = modifier)
}

@Composable
private fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun TagReadItem(tag: RfidTag) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tag.epc,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "RSSI: ${tag.rssi} dBm",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = formatTimestamp(tag.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(epochMillis: Long): String =
    DateTimeFormatter
        .ofPattern("dd/MM/yyyy HH:mm:ss.SSS")
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(epochMillis))
