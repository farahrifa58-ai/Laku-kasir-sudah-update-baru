package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import com.example.util.ReceiptExportHelper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.StoreProfile
import com.example.data.model.Transaction
import com.example.ui.Screen
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.StatusDebt
import com.example.ui.theme.StatusPaid
import com.example.util.FormatUtils
import com.example.util.LakuStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    transaction: Transaction,
    storeProfile: StoreProfile,
    lang: String,
    onBack: () -> Unit,
    onNewTransaction: () -> Unit
) {
    val context = LocalContext.current

    // Build receipt share text
    val receiptText = buildString {
        appendLine("===============================")
        appendLine("       ${storeProfile.storeName.uppercase()}")
        appendLine("   Owner: ${storeProfile.ownerName}")
        appendLine("   ${storeProfile.address}")
        appendLine("   WA: ${storeProfile.phone}")
        appendLine("===============================")
        appendLine("No Transaksi: ${transaction.transactionCode}")
        appendLine("Tanggal     : ${FormatUtils.formatDateTime(transaction.timestamp)}")
        if (transaction.customerName.isNotBlank()) {
            appendLine("Pelanggan   : ${transaction.customerName}")
        }
        if (transaction.customerPhone.isNotBlank()) {
            appendLine("No HP       : ${transaction.customerPhone}")
        }
        appendLine("-------------------------------")
        transaction.items.forEach { item ->
            appendLine("${item.productName}")
            appendLine("  ${FormatUtils.formatQty(item.quantity, item.unit)} x ${FormatUtils.formatRupiah(item.sellingPrice)} = ${FormatUtils.formatRupiah(item.subtotal)}")
        }
        appendLine("-------------------------------")
        appendLine("Subtotal    : ${FormatUtils.formatRupiah(transaction.subtotal)}")
        if (transaction.discount > 0) {
            appendLine("Diskon      : -${FormatUtils.formatRupiah(transaction.discount)}")
        }
        if (transaction.ppnAmount > 0) {
            appendLine("PPN         : +${FormatUtils.formatRupiah(transaction.ppnAmount)}")
        }
        appendLine("TOTAL       : ${FormatUtils.formatRupiah(transaction.total)}")
        appendLine("Metode      : ${transaction.paymentMethod}")
        if (transaction.paymentMethod == "Tunai") {
            appendLine("Diterima    : ${FormatUtils.formatRupiah(transaction.cashReceived)}")
            appendLine("Kembalian   : ${FormatUtils.formatRupiah(transaction.changeAmount)}")
        }
        appendLine("Status      : ${if (transaction.isPaid) "LUNAS" else "KASBON / BELUM LUNAS"}")
        appendLine("===============================")
        appendLine("${storeProfile.receiptNotes}")
        appendLine("===============================")
        appendLine("Dibuat dengan Laku Kasir Pro")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(LakuStrings.get("receipt", lang), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("receipt_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        FormatUtils.shareText(context, "Struk ${transaction.transactionCode}", receiptText)
                    }, modifier = Modifier.testTag("receipt_share_button")) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Bagikan")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Receipt Card Thermal Paper Style
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("receipt_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo Toko sesuai yang di-upload di pengaturan
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreenPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (storeProfile.logoUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(storeProfile.logoUri)
                                    .error(R.drawable.logo_laku_kasir)
                                    .fallback(R.drawable.logo_laku_kasir)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Logo Toko Struk",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.logo_laku_kasir),
                                contentDescription = "Logo Toko",
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Nama Toko, Pemilik, Alamat, Nomor WhatsApp
                    Text(
                        text = storeProfile.storeName,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = EmeraldGreenDark,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Pemilik: ${storeProfile.ownerName}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = storeProfile.address,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "WA: ${storeProfile.phone}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Detail Transaksi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("No. Struk", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(transaction.transactionCode, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tanggal", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(FormatUtils.formatDateTime(transaction.timestamp), fontSize = 12.sp)
                    }

                    if (transaction.customerName.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Pelanggan", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(transaction.customerName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (transaction.customerPhone.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("No. WhatsApp", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(transaction.customerPhone, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Item List
                    transaction.items.forEach { item ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = item.productName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${FormatUtils.formatQty(item.quantity, item.unit)} x ${FormatUtils.formatRupiah(item.sellingPrice)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = FormatUtils.formatRupiah(item.subtotal),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Financial summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", fontSize = 12.sp)
                        Text(FormatUtils.formatRupiah(transaction.subtotal), fontSize = 12.sp)
                    }

                    if (transaction.discount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Diskon", fontSize = 12.sp, color = Color.Red)
                            Text("- ${FormatUtils.formatRupiah(transaction.discount)}", fontSize = 12.sp, color = Color.Red)
                        }
                    }

                    if (transaction.ppnAmount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (transaction.ppnRate > 0) "PPN (${transaction.ppnRate.toInt()}%)" else "PPN", fontSize = 12.sp)
                            Text("+ ${FormatUtils.formatRupiah(transaction.ppnAmount)}", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(thickness = 1.5.dp, color = EmeraldGreenPrimary)
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TOTAL AKHIR", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        Text(
                            FormatUtils.formatRupiah(transaction.total),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = EmeraldGreenDark
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Metode Pembayaran", fontSize = 12.sp)
                        Text(transaction.paymentMethod, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (transaction.paymentMethod == "Tunai") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tunai Diterima", fontSize = 12.sp)
                            Text(FormatUtils.formatRupiah(transaction.cashReceived), fontSize = 12.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Kembalian", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(FormatUtils.formatRupiah(transaction.changeAmount), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreenDark)
                        }
                    }

                    // Status Badge
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (transaction.isPaid) StatusPaid.copy(alpha = 0.12f) else StatusDebt.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = if (transaction.isPaid) "LUNAS" else "KASBON (BELUM LUNAS)",
                            color = if (transaction.isPaid) StatusPaid else StatusDebt,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Pesan Terima Kasih untuk pembeli yang bisa diatur di pengaturan
                    Text(
                        text = storeProfile.receiptNotes,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Laku Kasir Pro v1.1",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Action Buttons Section: Cetak Struk, Download PDF, Download Image, Share to WhatsApp
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Aksi Cetak & Bagikan Struk",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = EmeraldGreenDark
                    )

                    // Tombol Utama: CETAK STRUK (Print)
                    Button(
                        onClick = {
                            ReceiptExportHelper.printReceipt(context, transaction, storeProfile)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("receipt_print_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Cetak",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cetak Struk (Thermal / Printer / PDF)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }

                    // Row: Download PDF & Download Image
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                ReceiptExportHelper.downloadReceiptPdf(context, transaction, storeProfile)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("download_pdf_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = EmeraldGreenDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Unduh PDF",
                                color = EmeraldGreenDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Button(
                            onClick = {
                                ReceiptExportHelper.downloadReceiptImage(context, transaction, storeProfile)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("download_image_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = EmeraldGreenDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Unduh Gambar",
                                color = EmeraldGreenDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Share WhatsApp Buttons
                    Button(
                        onClick = {
                            val caption = "Struk pembelian ${storeProfile.storeName} (${transaction.transactionCode}) - Total: ${FormatUtils.formatRupiah(transaction.total)}"
                            ReceiptExportHelper.shareReceiptImageToWhatsApp(context, transaction, storeProfile, caption)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("receipt_share_image_wa_button")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (transaction.customerPhone.isNotBlank()) "Kirim Gambar ke WA (${transaction.customerPhone})" else "Kirim Gambar Struk ke WhatsApp",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val caption = "Struk PDF ${storeProfile.storeName} (${transaction.transactionCode}) - Total: ${FormatUtils.formatRupiah(transaction.total)}"
                            ReceiptExportHelper.shareReceiptPdfToWhatsApp(context, transaction, storeProfile, caption)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("receipt_share_pdf_wa_button")
                    ) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFF25D366))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kirim Dokumen PDF ke WhatsApp",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E7E34),
                            fontSize = 13.sp
                        )
                    }

                    // Universal Share Teks
                    OutlinedButton(
                        onClick = {
                            FormatUtils.shareText(context, "Struk ${transaction.transactionCode}", receiptText)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Bagikan Format Teks Struk", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }

            // Transaksi Baru Button
            Button(
                onClick = onNewTransaction,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("receipt_new_transaction_button")
            ) {
                Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mulai Transaksi Baru",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
