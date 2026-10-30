package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val unit: String = "pcs",
    val barcode: String = "",
    val costPrice: Double = 0.0,      // Harga Modal / HPP
    val sellingPrice: Double = 0.0,   // Harga Jual
    val stock: Double = 0.0,          // Stok saat ini
    val isUnlimitedStock: Boolean = false, // Unlimited toggle
    val minStockAlert: Double = 5.0,  // Batas peringatan stok menipis
    val supplierName: String = "",    // Nama supplier
    val supplierPhone: String = "",   // Nomor WhatsApp supplier
    val imageUri: String? = null,     // Foto produk
    val createdAt: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean
        get() = !isUnlimitedStock && stock <= minStockAlert
}
