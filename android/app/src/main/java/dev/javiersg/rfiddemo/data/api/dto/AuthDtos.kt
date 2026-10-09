package dev.javiersg.rfiddemo.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val username: String,
    val password: String,
)

@Serializable
data class AuthResponseDto(
    val token: String,
    val expiresAt: String,
)
