package dev.javiersg.rfiddemo.ui.scanner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.javiersg.rfiddemo.ui.scanner.components.MetricCard
import dev.javiersg.rfiddemo.ui.scanner.components.TagItem
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    viewModel: RfidViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RFID Tag Scanner") },
                actions = {
                    IconButton(onClick = { viewModel.clearScannedTags() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Limpiar etiquetas"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Botón de prueba para simular lectura física antes de conectar el SDK
            FloatingActionButton(
                onClick = {
                    val mockEpc = "E28011900000002000" + Random.nextInt(10, 99)
                    val mockRssi = Random.nextInt(-70, -30)
                    viewModel.processTagScan(mockEpc, mockRssi)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Simular lectura"
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Fila de Métricas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Tags Únicos",
                    value = uiState.uniqueTags.toString(),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Total Lecturas",
                    value = uiState.totalReads.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lista o Estado Vacío
            if (uiState.tags.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay etiquetas leídas aún.\nPresiona + para simular una lectura.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(
                        items = uiState.tags,
                        key = { it.epc }
                    ) { tag ->
                        TagItem(tag = tag)
                    }
                }
            }
        }
    }
}