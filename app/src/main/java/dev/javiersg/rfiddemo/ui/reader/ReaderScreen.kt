package dev.javiersg.rfiddemo.ui.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.javiersg.rfiddemo.data.model.ConnectionState
import dev.javiersg.rfiddemo.data.model.RfidTag

@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header de Conexión
            ConnectionHeader(
                connectionState = uiState.connectionState,
                onToggleConnection = { viewModel.toggleConnection() }
            )

            // Barra de Métricas
            MetricsBar(
                tagCount = uiState.tags.size,
                totalReads = uiState.totalReads,
                onClearTags = { viewModel.clearTags() }
            )

            // Lista de Tags
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (uiState.tags.isEmpty()) {
                    Text(
                        text = "No se han detectado lectura de tags.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.tags,
                            key = { it.epc }
                        ) { tag ->
                            TagReadItem(tag = tag)
                        }
                    }
                }
            }

            // Controles de Lectura
            Button(
                onClick = { viewModel.toggleScanning() },
                enabled = uiState.connectionState == ConnectionState.CONNECTED,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (uiState.isScanning) "DETENER LECTURA" else "INICIAR LECTURA"
                )
            }
        }
    }
}

@Composable
private fun ConnectionHeader(
    connectionState: ConnectionState,
    onToggleConnection: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Estado del Lector",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = connectionState.name,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            when (connectionState) {
                ConnectionState.CONNECTING -> {
                    CircularProgressIndicator(modifier = Modifier.height(24.dp))
                }
                else -> {
                    OutlinedButton(onClick = onToggleConnection) {
                        Text(
                            text = if (connectionState == ConnectionState.CONNECTED) "Desconectar" else "Conectar"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricsBar(
    tagCount: Int,
    totalReads: Long,
    onClearTags: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column {
                Text("Únicos", style = MaterialTheme.typography.labelSmall)
                Text("$tagCount", style = MaterialTheme.typography.titleLarge)
            }
            Column {
                Text("Total Lecturas", style = MaterialTheme.typography.labelSmall)
                Text("$totalReads", style = MaterialTheme.typography.titleLarge)
            }
        }
        OutlinedButton(onClick = onClearTags) {
            Text("Limpiar")
        }
    }
}

@Composable
private fun TagReadItem(tag: RfidTag) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tag.epc,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "RSSI: ${tag.rssi} dBm",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Antena: ${tag.antenna}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Text(
                text = "${tag.peakCount}",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}