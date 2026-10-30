package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.StoreProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("laku_kasir_prefs", Context.MODE_PRIVATE)

    private val _storeProfile = MutableStateFlow(loadStoreProfile())
    val storeProfile: StateFlow<StoreProfile> = _storeProfile.asStateFlow()

    private fun loadStoreProfile(): StoreProfile {
        return StoreProfile(
            storeName = prefs.getString("store_name", "Laku Kasir Store") ?: "Laku Kasir Store",
            ownerName = prefs.getString("owner_name", "Rafline") ?: "Rafline",
            address = prefs.getString("address", "Jl. Merdeka No. 45, Jakarta") ?: "Jl. Merdeka No. 45, Jakarta",
            phone = prefs.getString("phone", "081234567890") ?: "081234567890",
            receiptNotes = prefs.getString(
                "receipt_notes",
                "Terima kasih telah berbelanja di Laku Kasir!\nBarang yang sudah dibeli tidak dapat ditukar."
            ) ?: "Terima kasih telah berbelanja!",
            logoUri = prefs.getString("logo_uri", null),
            themeMode = prefs.getString("theme_mode", "system") ?: "system",
            language = prefs.getString("language", "id") ?: "id",
            appVersion = "1.1"
        )
    }

    fun saveStoreProfile(profile: StoreProfile) {
        prefs.edit()
            .putString("store_name", profile.storeName)
            .putString("owner_name", profile.ownerName)
            .putString("address", profile.address)
            .putString("phone", profile.phone)
            .putString("receipt_notes", profile.receiptNotes)
            .putString("logo_uri", profile.logoUri)
            .putString("theme_mode", profile.themeMode)
            .putString("language", profile.language)
            .apply()
        _storeProfile.value = profile
    }

    fun setThemeMode(mode: String) {
        val updated = _storeProfile.value.copy(themeMode = mode)
        saveStoreProfile(updated)
    }

    fun setLanguage(lang: String) {
        val updated = _storeProfile.value.copy(language = lang)
        saveStoreProfile(updated)
    }
}
