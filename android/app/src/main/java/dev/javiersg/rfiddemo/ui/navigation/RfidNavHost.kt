package dev.javiersg.rfiddemo.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.javiersg.rfiddemo.ui.barcode.BarcodeScannerScreen
import dev.javiersg.rfiddemo.ui.home.HomeScreen
import dev.javiersg.rfiddemo.ui.inventory.InventoryScreen
import dev.javiersg.rfiddemo.ui.inventory.InventoryViewModel
import dev.javiersg.rfiddemo.ui.login.LoginScreen
import dev.javiersg.rfiddemo.ui.login.LoginViewModel
import dev.javiersg.rfiddemo.ui.reader.ReaderScreen
import dev.javiersg.rfiddemo.ui.reader.ReaderViewModel
import dev.javiersg.rfiddemo.ui.settings.SettingsScreen
import dev.javiersg.rfiddemo.ui.settings.SettingsViewModel

@Composable
fun RfidNavHost(
    hasSession: Boolean,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = if (hasSession) Routes.HOME else Routes.LOGIN,
        modifier = modifier,
    ) {
        composable(Routes.LOGIN) {
            val viewModel: LoginViewModel = hiltViewModel()
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.HOME) {
            HomeScreen(onNavigate = { route -> navController.navigate(route) })
        }

        composable(Routes.READER) {
            val viewModel: ReaderViewModel = hiltViewModel()
            ReaderScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.INVENTORY_WITH_QUERY,
            arguments =
                listOf(
                    navArgument(Routes.ARG_QUERY) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                ),
        ) { entry ->
            val viewModel: InventoryViewModel = hiltViewModel()
            InventoryScreen(
                viewModel = viewModel,
                initialQuery = entry.arguments?.getString(Routes.ARG_QUERY).orEmpty(),
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.BARCODE) {
            BarcodeScannerScreen(
                onBarcodeScanned = { code ->
                    navController.navigate("${Routes.INVENTORY}?query=${Uri.encode(code)}")
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.SETTINGS) {
            val viewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = viewModel,
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
