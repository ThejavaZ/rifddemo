package dev.javiersg.rfiddemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.javiersg.rfiddemo.data.ZebraRfidManager
import dev.javiersg.rfiddemo.ui.scanner.RfidViewModel
import dev.javiersg.rfiddemo.ui.scanner.ScannerScreen
import dev.javiersg.rfiddemo.ui.theme.RfiddemoTheme

class MainActivity : ComponentActivity() {

    private val viewModel: RfidViewModel by viewModels {
        val appContainer = (application as RfidApplication).container
        RfidViewModel.provideFactory(appContainer.rfidRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val rfidManager = remember { ZebraRfidManager(context) }
            val connectionState by rfidManager.connectionState.collectAsState()

            LaunchedEffect(Unit) {
                rfidManager.initSdk()
            }

            RfiddemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ScannerScreen(viewModel = viewModel)
                }
            }
        }
    }
}