package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.local.AppDatabase
import com.example.data.model.Expense
import com.example.data.model.Product
import com.example.data.model.StoreProfile
import com.example.data.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupRestoreHelper {

    suspend fun createBackupJson(
        context: Context,
        storeProfile: StoreProfile,
        products: List<Product>,
        transactions: List<Transaction>,
        expenses: List<Expense>
    ): File? = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject()
            root.put("app", "Laku Kasir Pro")
            root.put("version", "1.1")
            root.put("timestamp", System.currentTimeMillis())

            // Store Profile
            val profileObj = JSONObject().apply {
                put("storeName", storeProfile.storeName)
                put("ownerName", storeProfile.ownerName)
                put("address", storeProfile.address)
                put("phone", storeProfile.phone)
                put("receiptNotes", storeProfile.receiptNotes)
                put("themeMode", storeProfile.themeMode)
                put("language", storeProfile.language)
            }
            root.put("storeProfile", profileObj)

            // Products
            val prodArr = JSONArray()
            products.forEach { p ->
                val pObj = JSONObject().apply {
                    put("id", p.id)
                    put("name", p.name)
                    put("category", p.category)
                    put("unit", p.unit)
                    put("barcode", p.barcode)
                    put("costPrice", p.costPrice)
                    put("sellingPrice", p.sellingPrice)
                    put("stock", p.stock)
                    put("isUnlimitedStock", p.isUnlimitedStock)
                    put("minStockAlert", p.minStockAlert)
                    put("supplierName", p.supplierName)
                    put("supplierPhone", p.supplierPhone)
                }
                prodArr.put(pObj)
            }
            root.put("products", prodArr)

            // Transactions
            val txArr = JSONArray()
            transactions.forEach { t ->
                val tObj = JSONObject().apply {
                    put("id", t.id)
                    put("transactionCode", t.transactionCode)
                    put("timestamp", t.timestamp)
                    put("subtotal", t.subtotal)
                    put("discount", t.discount)
                    put("ppnRate", t.ppnRate)
                    put("ppnAmount", t.ppnAmount)
                    put("total", t.total)
                    put("paymentMethod", t.paymentMethod)
                    put("cashReceived", t.cashReceived)
                    put("changeAmount", t.changeAmount)
                    put("customerName", t.customerName)
                    put("customerPhone", t.customerPhone)
                    put("isPaid", t.isPaid)
                    put("itemsJson", t.itemsJson)
                }
                txArr.put(tObj)
            }
            root.put("transactions", txArr)

            // Expenses
            val expArr = JSONArray()
            expenses.forEach { e ->
                val eObj = JSONObject().apply {
                    put("id", e.id)
                    put("title", e.title)
                    put("category", e.category)
                    put("amount", e.amount)
                    put("timestamp", e.timestamp)
                    put("note", e.note)
                }
                expArr.put(eObj)
            }
            root.put("expenses", expArr)

            val timeStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val filename = "LakuKasir_Backup_$timeStr.json"
            val cacheDir = File(context.cacheDir, "receipts").apply { mkdirs() }
            val backupFile = File(cacheDir, filename)

            val fos = FileOutputStream(backupFile)
            fos.write(root.toString(2).toByteArray(Charsets.UTF_8))
            fos.flush()
            fos.close()

            backupFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareBackupToWhatsApp(context: Context, backupFile: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                backupFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "📦 File Cadangan Data Toko Laku Kasir Pro (${backupFile.name}). Simpan file ini untuk memulihkan data penjualan Anda."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.whatsapp")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback WhatsApp Business or chooser
            try {
                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    backupFile
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "📦 File Cadangan Data Toko Laku Kasir Pro (${backupFile.name})"
                    )
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setPackage("com.whatsapp.w4b")
                }
                context.startActivity(intent)
            } catch (ex: Exception) {
                shareBackupUniversal(context, backupFile, "Kirim Cadangan via WhatsApp")
            }
        }
    }

    fun backupToGoogleDrive(context: Context, backupFile: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                backupFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Cadangan Laku Kasir Pro")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.google.android.apps.docs")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            shareBackupUniversal(context, backupFile, "Simpan ke Google Drive / Cloud")
        }
    }

    fun shareBackupUniversal(context: Context, backupFile: File, title: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                backupFile
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, title))
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membagikan cadangan data", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun restoreBackupFromJsonUri(
        context: Context,
        uri: Uri,
        onSuccess: (productsCount: Int, transactionsCount: Int) -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw Exception("Tidak dapat membaca file cadangan")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val sb = java.lang.StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            reader.close()
            inputStream.close()

            val root = JSONObject(sb.toString())
            val db = AppDatabase.getDatabase(context)

            // Restore products
            val prodArr = root.optJSONArray("products") ?: JSONArray()
            for (i in 0 until prodArr.length()) {
                val p = prodArr.getJSONObject(i)
                val product = Product(
                    id = p.optLong("id", 0L),
                    name = p.optString("name", "Produk"),
                    category = p.optString("category", "Lainnya"),
                    unit = p.optString("unit", "pcs"),
                    barcode = p.optString("barcode", ""),
                    costPrice = p.optDouble("costPrice", 0.0),
                    sellingPrice = p.optDouble("sellingPrice", 0.0),
                    stock = p.optDouble("stock", 0.0),
                    isUnlimitedStock = p.optBoolean("isUnlimitedStock", false),
                    minStockAlert = p.optDouble("minStockAlert", 5.0),
                    supplierName = p.optString("supplierName", ""),
                    supplierPhone = p.optString("supplierPhone", "")
                )
                db.productDao().insert(product)
            }

            // Restore transactions
            val txArr = root.optJSONArray("transactions") ?: JSONArray()
            for (i in 0 until txArr.length()) {
                val t = txArr.getJSONObject(i)
                val transaction = Transaction(
                    id = t.optLong("id", 0L),
                    transactionCode = t.optString("transactionCode", "LK-RESTORE"),
                    timestamp = t.optLong("timestamp", System.currentTimeMillis()),
                    subtotal = t.optDouble("subtotal", 0.0),
                    discount = t.optDouble("discount", 0.0),
                    ppnRate = t.optDouble("ppnRate", 0.0),
                    ppnAmount = t.optDouble("ppnAmount", 0.0),
                    total = t.optDouble("total", 0.0),
                    paymentMethod = t.optString("paymentMethod", "Tunai"),
                    cashReceived = t.optDouble("cashReceived", 0.0),
                    changeAmount = t.optDouble("changeAmount", 0.0),
                    customerName = t.optString("customerName", ""),
                    customerPhone = t.optString("customerPhone", ""),
                    isPaid = t.optBoolean("isPaid", true),
                    itemsJson = t.optString("itemsJson", "[]")
                )
                db.transactionDao().insert(transaction)
            }

            withContext(Dispatchers.Main) {
                onSuccess(prodArr.length(), txArr.length())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onError("Gagal memulihkan file cadangan: ${e.message}")
            }
        }
    }
}
