package dev.javiersg.rfiddemo.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.javiersg.rfiddemo.data.remote.AuthApiClient
import dev.javiersg.rfiddemo.diagnostics.DiagnosticLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
)

@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val authApiClient: AuthApiClient,
        private val logger: DiagnosticLogger,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(LoginUiState())
        val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

        fun onLogin(
            username: String,
            password: String,
        ) {
            if (username.isBlank() || password.isBlank()) {
                _uiState.update { it.copy(error = "Usuario y contraseña requeridos") }
                return
            }
            viewModelScope.launch {
                _uiState.update { it.copy(loading = true, error = null) }
                authApiClient
                    .login(username, password)
                    .onSuccess {
                        logger.log("login ok user=$username")
                        _uiState.update { it.copy(loading = false, success = true) }
                    }.onFailure {
                        logger.log("login failed user=$username")
                        _uiState.update { it.copy(loading = false, error = "Credenciales inválidas") }
                    }
            }
        }
    }
