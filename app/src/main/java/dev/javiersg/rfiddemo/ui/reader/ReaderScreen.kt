package dev.javiersg.rfiddemo.data.reader

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.javiersg.rfiddemo.domain.repository.ConnectionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RFID Scanner Demo", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Header con estado de conexión y métricas rápidas
            ConnectionHeader(
                connectionState = uiState.connectionState,
                onToggleConnection = { viewModel.toggleConnection() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            MetricsBar(
                uniqueCount = uiState.tags.size,
                totalReads = uiState.totalReads,
                onClear = { viewModel.clearTags() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lista reactiva de Tags
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = uiState.tags,
                    key = { it.tag.epc }
                ) { item ->
                    TagCard(item = item)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de lectura (Trigger)
            Button(
                onClick = { viewModel.toggleScanning() },
                enabled = uiState.connectionState == ConnectionState.CONNECTED,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isScanning) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (uiState.isScanning) "STOP SCANNING" else "START SCANNING",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ConnectionHeader(
    connectionState: ConnectionState,
    onToggleConnection: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Hardware Status", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = connectionState.name,
                    fontWeight = FontWeight.Bold,
                    color = when (connectionState) {
                        ConnectionState.CONNECTED -> Color(0xFF2E7D32)
                        ConnectionState.CONNECTING -> Color(0xFFED6C02)
                        else -> Color.Gray
                    }
                )
            }

            OutlinedButton(onClick = onToggleConnection) {
                Text(if (connectionState == ConnectionState.CONNECTED) "Disconnect" else "Connect")
            }
        }
    }
}

@Composable
fun MetricsBar(
    uniqueCount: Int,
    totalReads: Int,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row {
            Text(text = "Unique: ", fontWeight = FontWeight.Bold)
            Text(text = "$uniqueCount")
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "Total Reads: ", fontWeight = FontWeight.Bold)
            Text(text = "$totalReads")
        }

        OutlinedButton(onClick = onClear) {
            Text("Clear")
        }
    }
}

@Composable
fun TagCard(item: TagReadItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    text = item.tag.epc,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "RSSI: ${item.tag.rssi} dBm",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(
                    text = "x${item.count}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}