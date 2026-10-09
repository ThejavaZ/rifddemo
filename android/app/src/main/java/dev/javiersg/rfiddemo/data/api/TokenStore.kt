@file:Suppress("DEPRECATION")

package dev.javiersg.rfiddemo.data.api

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Almacena el JWT en [EncryptedSharedPreferences] (AES-256 respaldado por Android Keystore).
 * La API está deprecada en favor de DataStore, pero el requerimiento es Keystore explícito.
 */
@Suppress("DEPRECATION")
class TokenStore(
    context: Context,
) {
    private val prefs: SharedPreferences =
        EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)
        set(value) {
            prefs.edit().putString(KEY_ACCESS_TOKEN, value).apply()
        }

    fun clear() {
        prefs.edit().remove(KEY_ACCESS_TOKEN).apply()
    }

    private companion object {
        const val FILE_NAME = "rfid_secure_token"
        const val KEY_ACCESS_TOKEN = "access_token"
    }
}
