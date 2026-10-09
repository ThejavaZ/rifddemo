package dev.javiersg.rfiddemo.ui.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.javiersg.rfiddemo.data.remote.AuthApiClient
import dev.javiersg.rfiddemo.diagnostics.DiagnosticLogger
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val authApiClient: AuthApiClient,
        private val logger: DiagnosticLogger,
    ) : ViewModel() {
        fun exportDiagnostics(): String = logger.dump()

        fun clearDiagnostics() {
            logger.log("diagnostics cleared")
            logger.clear()
        }

        fun onLogout() {
            authApiClient.logout()
            logger.log("logout")
        }
    }
