package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionCode: String,     // e.g. LK-20261029-001
    val timestamp: Long = System.currentTimeMillis(),
    val subtotal: Double,            // Total before discount and tax
    val discount: Double = 0.0,      // Nominal discount
    val discountNote: String = "",   // e.g. "Diskon Promo"
    val ppnRate: Double = 0.0,       // e.g. 11.0 (%)
    val ppnAmount: Double = 0.0,     // Nominal PPN
    val total: Double,               // Total amount to pay
    val paymentMethod: String,       // Tunai, Transfer, QRIS, Debit/Kredit, Kasbon
    val cashReceived: Double = 0.0,  // Uang diterima
    val changeAmount: Double = 0.0,  // Kembalian
    val customerName: String = "",   // Nama pelanggan
    val customerPhone: String = "",  // WhatsApp pelanggan
    val isPaid: Boolean = true,      // Kasbon is initially false until marked paid
    val itemsJson: String = "[]"     // Serialized list of TransactionItem
) {
    val items: List<TransactionItem>
        get() = TransactionItem.listFromJsonString(itemsJson)

    val totalCost: Double
        get() = items.sumOf { it.costPrice * it.quantity }

    val grossProfit: Double
        get() = total - totalCost
}
