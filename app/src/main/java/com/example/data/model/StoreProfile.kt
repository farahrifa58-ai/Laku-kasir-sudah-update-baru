package com.example.data.model

data class StoreProfile(
    val storeName: String = "Laku Kasir Store",
    val ownerName: String = "Rafline",
    val address: String = "Jl. Sudirman No. 12, Jakarta",
    val phone: String = "081234567890",
    val receiptNotes: String = "Terima kasih telah berbelanja!\nBarang yang sudah dibeli tidak dapat ditukar atau dikembalikan.",
    val logoUri: String? = null,
    val themeMode: String = "system", // "system", "light", "dark"
    val language: String = "id",      // "id", "en"
    val appVersion: String = "1.1"
)
