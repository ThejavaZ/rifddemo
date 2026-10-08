package dev.javiersg.rfiddemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
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