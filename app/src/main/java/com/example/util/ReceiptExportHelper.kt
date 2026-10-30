package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.StoreProfile
import com.example.data.model.Transaction
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ReceiptExportHelper {

    /**
     * Renders a realistic receipt image into a Bitmap
     */
    fun generateReceiptBitmap(
        context: Context,
        transaction: Transaction,
        storeProfile: StoreProfile
    ): Bitmap {
        val width = 600
        // Calculate dynamic height based on number of items and text
        val baseHeight = 750
        val itemsHeight = transaction.items.size * 55
        val notesLines = storeProfile.receiptNotes.split("\n").size * 25
        val height = baseHeight + itemsHeight + notesLines

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 22f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 24f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#15803D") // Emerald green
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val subHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.LTGRAY
            strokeWidth = 2f
        }

        var y = 35f

        // Draw Store Logo if available
        val logoBitmap = ImageUtils.loadBitmap(context, storeProfile.logoUri)
            ?: android.graphics.BitmapFactory.decodeResource(context.resources, com.example.R.drawable.logo_laku_kasir)
        if (logoBitmap != null) {
            val logoSize = 90
            val scaledLogo = Bitmap.createScaledBitmap(logoBitmap, logoSize, logoSize, true)
            canvas.drawBitmap(scaledLogo, (width - logoSize) / 2f, y, null)
            y += logoSize + 20f
        }

        // Top Store Header
        canvas.drawText(storeProfile.storeName.uppercase(), width / 2f, y, headerPaint)
        y += 32f
        canvas.drawText("Pemilik: ${storeProfile.ownerName}", width / 2f, y, subHeaderPaint)
        y += 28f
        canvas.drawText(storeProfile.address, width / 2f, y, subHeaderPaint)
        y += 28f
        canvas.drawText("WA: ${storeProfile.phone}", width / 2f, y, subHeaderPaint)
        y += 35f

        // Divider
        canvas.drawLine(30f, y, width - 30f, y, linePaint)
        y += 30f

        // Metadata
        fun drawRow(label: String, value: String, isBold: Boolean = false) {
            val p = if (isBold) boldPaint else textPaint
            canvas.drawText(label, 30f, y, textPaint)
            val valWidth = p.measureText(value)
            canvas.drawText(value, width - 30f - valWidth, y, p)
            y += 32f
        }

        drawRow("No. Struk", transaction.transactionCode, true)
        drawRow("Tanggal", FormatUtils.formatDateTime(transaction.timestamp))
        if (transaction.customerName.isNotBlank()) {
            drawRow("Pelanggan", transaction.customerName)
        }
        if (transaction.customerPhone.isNotBlank()) {
            drawRow("No. HP", transaction.customerPhone)
        }

        y += 10f
        canvas.drawLine(30f, y, width - 30f, y, linePaint)
        y += 30f

        // Items
        boldPaint.textSize = 22f
        canvas.drawText("RINCIAN BARANG", 30f, y, boldPaint)
        y += 30f

        transaction.items.forEach { item ->
            canvas.drawText(item.productName, 30f, y, boldPaint)
            y += 26f
            val qtyPrice = "${FormatUtils.formatQty(item.quantity, item.unit)} x ${FormatUtils.formatRupiah(item.sellingPrice)}"
            val subtotal = FormatUtils.formatRupiah(item.subtotal)
            canvas.drawText(qtyPrice, 30f, y, textPaint)
            val subWidth = textPaint.measureText(subtotal)
            canvas.drawText(subtotal, width - 30f - subWidth, y, textPaint)
            y += 30f
        }

        y += 5f
        canvas.drawLine(30f, y, width - 30f, y, linePaint)
        y += 30f

        // Financials
        drawRow("Subtotal", FormatUtils.formatRupiah(transaction.subtotal))
        if (transaction.discount > 0) {
            drawRow("Diskon", "- ${FormatUtils.formatRupiah(transaction.discount)}")
        }
        if (transaction.ppnAmount > 0) {
            drawRow(if (transaction.ppnRate > 0) "PPN (${transaction.ppnRate.toInt()}%)" else "PPN", "+ ${FormatUtils.formatRupiah(transaction.ppnAmount)}")
        }

        y += 5f
        linePaint.strokeWidth = 3f
        linePaint.color = Color.parseColor("#16A34A")
        canvas.drawLine(30f, y, width - 30f, y, linePaint)
        linePaint.color = Color.LTGRAY
        linePaint.strokeWidth = 2f
        y += 35f

        val totalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#15803D")
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("TOTAL", 30f, y, totalPaint)
        val totVal = FormatUtils.formatRupiah(transaction.total)
        val totWidth = totalPaint.measureText(totVal)
        canvas.drawText(totVal, width - 30f - totWidth, y, totalPaint)
        y += 38f

        drawRow("Metode Bayar", transaction.paymentMethod)
        if (transaction.paymentMethod == "Tunai") {
            drawRow("Diterima", FormatUtils.formatRupiah(transaction.cashReceived))
            drawRow("Kembalian", FormatUtils.formatRupiah(transaction.changeAmount), true)
        }
        drawRow("Status", if (transaction.isPaid) "LUNAS" else "KASBON / BELUM LUNAS", true)

        y += 10f
        canvas.drawLine(30f, y, width - 30f, y, linePaint)
        y += 35f

        // Thank you note
        val centerNotePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        storeProfile.receiptNotes.split("\n").forEach { line ->
            canvas.drawText(line, width / 2f, y, centerNotePaint)
            y += 28f
        }

        y += 15f
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.GRAY
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Laku Kasir Pro v1.1 - by Rafline Dev", width / 2f, y, footerPaint)

        return bitmap
    }

    /**
     * Saves receipt image to shared cache and returns content Uri for sharing
     */
    fun saveReceiptImageToCache(
        context: Context,
        transaction: Transaction,
        storeProfile: StoreProfile
    ): Uri? {
        return try {
            val bitmap = generateReceiptBitmap(context, transaction, storeProfile)
            val cacheDir = File(context.cacheDir, "receipts").apply { mkdirs() }
            val file = File(cacheDir, "Struk_${transaction.transactionCode}.png")
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Downloads / saves receipt image into the public Pictures/LakuKasir directory
     */
    fun downloadReceiptImage(
        context: Context,
        transaction: Transaction,
        storeProfile: StoreProfile
    ): Boolean {
        return try {
            val bitmap = generateReceiptBitmap(context, transaction, storeProfile)
            val filename = "Struk_${transaction.transactionCode}_${System.currentTimeMillis()}.png"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LakuKasir")
                }
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    val out: OutputStream? = resolver.openOutputStream(imageUri)
                    if (out != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        out.flush()
                        out.close()
                        Toast.makeText(context, "Gambar struk disimpan ke Galeri/Pictures!", Toast.LENGTH_LONG).show()
                        return true
                    }
                }
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val folder = File(picturesDir, "LakuKasir").apply { mkdirs() }
                val file = File(folder, filename)
                val fos = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
                fos.close()
                Toast.makeText(context, "Gambar disimpan ke: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                return true
            }
            false
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal mengunduh gambar: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    /**
     * Generates a PDF file of the receipt and returns content Uri for sharing
     */
    fun saveReceiptPdfToCache(
        context: Context,
        transaction: Transaction,
        storeProfile: StoreProfile
    ): Uri? {
        return try {
            val bitmap = generateReceiptBitmap(context, transaction, storeProfile)
            val pdfDocument = PdfDocument()

            // Page description: 72 points per inch (600px -> 600 width)
            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
            val page = pdfDocument.startPage(pageInfo)

            val canvas = page.canvas
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            pdfDocument.finishPage(page)

            val cacheDir = File(context.cacheDir, "receipts").apply { mkdirs() }
            val file = File(cacheDir, "Struk_${transaction.transactionCode}.pdf")
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
            fos.flush()
            fos.close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Downloads / saves receipt PDF to public Downloads/LakuKasir directory
     */
    fun downloadReceiptPdf(
        context: Context,
        transaction: Transaction,
        storeProfile: StoreProfile
    ): Boolean {
        return try {
            val bitmap = generateReceiptBitmap(context, transaction, storeProfile)
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            page.canvas.drawBitmap(bitmap, 0f, 0f, null)
            pdfDocument.finishPage(page)

            val filename = "Struk_${transaction.transactionCode}_${System.currentTimeMillis()}.pdf"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/LakuKasir")
                }
                val pdfUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (pdfUri != null) {
                    val out: OutputStream? = resolver.openOutputStream(pdfUri)
                    if (out != null) {
                        pdfDocument.writeTo(out)
                        pdfDocument.close()
                        out.flush()
                        out.close()
                        Toast.makeText(context, "PDF Struk berhasil diunduh ke folder Downloads!", Toast.LENGTH_LONG).show()
                        return true
                    }
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val folder = File(downloadsDir, "LakuKasir").apply { mkdirs() }
                val file = File(folder, filename)
                val fos = FileOutputStream(file)
                pdfDocument.writeTo(fos)
                pdfDocument.close()
                fos.flush()
                fos.close()
                Toast.makeText(context, "PDF disimpan ke: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                return true
            }
            false
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal mengunduh PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    /**
     * Shares receipt image directly to WhatsApp or WhatsApp Business
     */
    fun shareReceiptImageToWhatsApp(
        context: Context,
        transaction: Transaction,
        storeProfile: StoreProfile,
        caption: String = ""
    ) {
        val uri = saveReceiptImageToCache(context, transaction, storeProfile)
        if (uri == null) {
            Toast.makeText(context, "Gagal membuat gambar struk", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                if (caption.isNotBlank()) {
                    putExtra(Intent.EXTRA_TEXT, caption)
                }
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.whatsapp")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to WhatsApp Business or general share if WhatsApp is not directly installed
            try {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    if (caption.isNotBlank()) {
                        putExtra(Intent.EXTRA_TEXT, caption)
                    }
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setPackage("com.whatsapp.w4b")
                }
                context.startActivity(intent)
            } catch (ex: Exception) {
                // Universal share fallback
                val chooser = Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        if (caption.isNotBlank()) {
                            putExtra(Intent.EXTRA_TEXT, caption)
                        }
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    },
                    "Bagikan Gambar Struk"
                )
                context.startActivity(chooser)
            }
        }
    }

    /**
     * Shares receipt PDF to WhatsApp
     */
    fun shareReceiptPdfToWhatsApp(
        context: Context,
        transaction: Transaction,
        storeProfile: StoreProfile,
        caption: String = ""
    ) {
        val uri = saveReceiptPdfToCache(context, transaction, storeProfile)
        if (uri == null) {
            Toast.makeText(context, "Gagal membuat PDF struk", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                if (caption.isNotBlank()) {
                    putExtra(Intent.EXTRA_TEXT, caption)
                }
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.whatsapp")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    if (caption.isNotBlank()) {
                        putExtra(Intent.EXTRA_TEXT, caption)
                    }
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setPackage("com.whatsapp.w4b")
                }
                context.startActivity(intent)
            } catch (ex: Exception) {
                val chooser = Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        if (caption.isNotBlank()) {
                            putExtra(Intent.EXTRA_TEXT, caption)
                        }
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    },
                    "Bagikan PDF Struk"
                )
                context.startActivity(chooser)
            }
        }
    }

    /**
     * Prints receipt using Android Print Spooler (supports Bluetooth thermal printer, Wi-Fi printer, or PDF)
     */
    fun printReceipt(
        context: Context,
        transaction: Transaction,
        storeProfile: StoreProfile
    ) {
        try {
            val pdfUri = saveReceiptPdfToCache(context, transaction, storeProfile)
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? android.print.PrintManager
            if (printManager != null && pdfUri != null) {
                val printAdapter = object : android.print.PrintDocumentAdapter() {
                    override fun onLayout(
                        oldAttributes: android.print.PrintAttributes?,
                        newAttributes: android.print.PrintAttributes?,
                        cancellationSignal: android.os.CancellationSignal?,
                        callback: LayoutResultCallback?,
                        extras: android.os.Bundle?
                    ) {
                        if (cancellationSignal?.isCanceled == true) {
                            callback?.onLayoutCancelled()
                            return
                        }
                        val info = android.print.PrintDocumentInfo.Builder("Struk_${transaction.transactionCode}.pdf")
                            .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                            .setPageCount(1)
                            .build()
                        callback?.onLayoutFinished(info, newAttributes != oldAttributes)
                    }

                    override fun onWrite(
                        pages: Array<out android.print.PageRange>?,
                        destination: android.os.ParcelFileDescriptor?,
                        cancellationSignal: android.os.CancellationSignal?,
                        callback: WriteResultCallback?
                    ) {
                        try {
                            val input = context.contentResolver.openInputStream(pdfUri)
                            val output = java.io.FileOutputStream(destination?.fileDescriptor)
                            input?.copyTo(output)
                            input?.close()
                            output.close()
                            callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                        } catch (e: Exception) {
                            callback?.onWriteFailed(e.message)
                        }
                    }
                }
                printManager.print(
                    "Struk ${transaction.transactionCode}",
                    printAdapter,
                    android.print.PrintAttributes.Builder().build()
                )
            } else {
                Toast.makeText(context, "Layanan cetak tidak tersedia di perangkat ini", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal mencetak struk: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
