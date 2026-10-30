package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Categories
import com.example.data.model.Transaction
import com.example.ui.LakuKasirViewModel
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.StatusDebt
import com.example.util.FormatUtils
import com.example.util.LakuStrings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CheckoutScreen(
    viewModel: LakuKasirViewModel,
    lang: String,
    onBack: () -> Unit,
    onSuccess: (Transaction) -> Unit
) {
    val cart by viewModel.cart.collectAsState()
    val manualDiscount by viewModel.manualDiscount.collectAsState()
    val isPpnAuto by viewModel.isPpnAuto.collectAsState()
    val ppnRate by viewModel.ppnRate.collectAsState()
    val manualPpnAmountText by viewModel.manualPpnAmount.collectAsState()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsState()
    val cashReceivedText by viewModel.cashReceivedInput.collectAsState()

    // Highly reactive financial calculations
    val cartSubtotal = remember(cart) { cart.sumOf { it.subtotal } }
    val discountVal = remember(manualDiscount) { manualDiscount.toDoubleOrNull() ?: 0.0 }
    val ppnVal = remember(isPpnAuto, ppnRate, manualPpnAmountText, cartSubtotal, discountVal) {
        when {
            isPpnAuto -> {
                val afterDiscount = maxOf(0.0, cartSubtotal - discountVal)
                afterDiscount * (ppnRate / 100.0)
            }
            manualPpnAmountText.isNotBlank() -> manualPpnAmountText.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
    }
    val totalPayable = remember(cartSubtotal, discountVal, ppnVal) {
        maxOf(0.0, (cartSubtotal - discountVal) + ppnVal)
    }

    val cashReceived = remember(cashReceivedText) {
        cashReceivedText.toDoubleOrNull() ?: 0.0
    }

    val hasCashInput = remember(cashReceivedText) {
        cashReceivedText.isNotBlank() && (cashReceivedText.toDoubleOrNull() ?: 0.0) > 0.0
    }

    val changeAmount = remember(selectedPaymentMethod, cashReceived, totalPayable) {
        if (selectedPaymentMethod == "Tunai" && cashReceived > totalPayable) {
            cashReceived - totalPayable
        } else {
            0.0
        }
    }

    val shortageAmount = remember(selectedPaymentMethod, cashReceived, totalPayable) {
        if (selectedPaymentMethod == "Tunai" && cashReceived > 0.0 && cashReceived < totalPayable) {
            totalPayable - cashReceived
        } else {
            0.0
        }
    }

    val isExactCash = remember(selectedPaymentMethod, cashReceived, totalPayable) {
        selectedPaymentMethod == "Tunai" && cashReceived > 0.0 && kotlin.math.abs(cashReceived - totalPayable) < 0.01
    }

    val quickAddOptions = listOf(10000.0, 20000.0, 50000.0, 100000.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = LakuStrings.get("payment", lang),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("checkout_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Total Card Highlight
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldGreenDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TOTAL TAGIHAN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.85f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = FormatUtils.formatRupiah(totalPayable),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    if (discountVal > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Hemat Diskon: ${FormatUtils.formatRupiah(discountVal)}",
                            fontSize = 12.sp,
                            color = Color(0xFFFFD54F),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Pilihan Metode Pembayaran: Tunai, Transfer, QRIS, Debit/Kredit, Kasbon
            Text(
                text = LakuStrings.get("payment_method", lang),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Categories.PAYMENT_METHODS.forEach { method ->
                    val isSelected = selectedPaymentMethod == method
                    val icon: ImageVector = when (method) {
                        "Tunai" -> Icons.Default.Money
                        "Transfer" -> Icons.Default.AccountBalance
                        "QRIS" -> Icons.Default.QrCode
                        "Debit/Kredit" -> Icons.Default.CreditCard
                        "Kasbon" -> Icons.Default.PendingActions
                        else -> Icons.Default.Money
                    }

                    Surface(
                        onClick = {
                            viewModel.selectedPaymentMethod.value = method
                            if (method == "Tunai" && cashReceivedText.isBlank()) {
                                viewModel.setExactCash()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) {
                            if (method == "Kasbon") StatusDebt else EmeraldGreenPrimary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        },
                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.testTag("payment_method_$method")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = method,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Jika Tunai: Fitur uang tunai yang diterima, uang pas, nominal cepat, dan kembalian
            if (selectedPaymentMethod == "Tunai") {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pembayaran Tunai (Cash)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = EmeraldGreenDark
                            )
                            // Shortcut tombol Uang Pas
                            Surface(
                                onClick = { viewModel.setExactCash() },
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldGreenPrimary,
                                modifier = Modifier.testTag("exact_cash_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Uang Pas", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = cashReceivedText,
                            onValueChange = { viewModel.cashReceivedInput.value = it.filter { ch -> ch.isDigit() } },
                            label = { Text(LakuStrings.get("cash_received", lang)) },
                            placeholder = { Text("Contoh: ${totalPayable.toInt()}") },
                            prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cash_received_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Quick cash nominal buttons (+10k, +20k, +50k, +100k)
                        Text(
                            text = "Tambah Cepat:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickAddOptions.forEach { amount ->
                                OutlinedButton(
                                    onClick = { viewModel.addQuickCash(amount) },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "+${(amount / 1000).toInt()}k",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // TAMPILAN KETERANGAN UANG KEMBALIAN
                        // 1. KASUS UANG LEBIH (Kembalian muncul dengan jelas & menonjol)
                        if (changeAmount > 0.0) {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                border = BorderStroke(1.5.dp, EmeraldGreenPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("change_info_card")
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = EmeraldGreenDark,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "💵 KETERANGAN UANG KEMBALIAN",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = EmeraldGreenDark
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Kembalikan ke Pembeli:",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = FormatUtils.formatRupiah(changeAmount),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 26.sp,
                                        color = EmeraldGreenDark
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White,
                                        border = BorderStroke(1.dp, Color(0xFFA5D6A7))
                                    ) {
                                        Text(
                                            text = "Diterima: ${FormatUtils.formatRupiah(cashReceived)} • Tagihan: ${FormatUtils.formatRupiah(totalPayable)} • Lebih: ${FormatUtils.formatRupiah(changeAmount)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = EmeraldGreenDark,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        } else if (isExactCash) {
                            // 2. KASUS UANG PAS
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFE8F5E9),
                                border = BorderStroke(1.dp, Color(0xFFA5D6A7)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreenDark, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "✓ Uang Pas Diterima (Tidak Ada Kembalian)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = EmeraldGreenDark
                                    )
                                }
                            }
                        } else if (shortageAmount > 0.0) {
                            // 3. KASUS UANG KURANG
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFFEBEE),
                                border = BorderStroke(1.dp, Color(0xFFEF9A9A)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "⚠️ Uang Tunai Kurang!",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color.Red
                                        )
                                        Text(
                                            text = "Kekurangan: ${FormatUtils.formatRupiah(shortageAmount)}",
                                            fontSize = 12.sp,
                                            color = Color(0xFFB71C1C)
                                        )
                                    }
                                }
                            }
                        } else {
                            // 4. Belum memasukkan uang tunai
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = EmeraldGreenPrimary.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = LakuStrings.get("change", lang),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Rp 0",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = EmeraldGreenDark
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (selectedPaymentMethod == "Kasbon") {
                // Info Kasbon
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = StatusDebt.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Catatan Kasbon (Hutang Belanja)",
                            fontWeight = FontWeight.Bold,
                            color = StatusDebt,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Transaksi akan tercatat belum lunas. Anda dapat menandai lunas kapan saja di menu Riwayat Transaksi.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Fitur PPN (Bisa diisi manual atau otomatis persentase PPN)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Pajak Pertambahan Nilai (PPN)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Hitung PPN otomatis atau isi nominal manual",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = isPpnAuto,
                            onCheckedChange = { viewModel.isPpnAuto.value = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EmeraldGreenPrimary
                            ),
                            modifier = Modifier.testTag("ppn_auto_switch")
                        )
                    }

                    if (isPpnAuto) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tarif PPN: ${ppnRate.toInt()}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "+ ${FormatUtils.formatRupiah(ppnVal)}",
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreenDark,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = manualPpnAmountText,
                            onValueChange = { viewModel.manualPpnAmount.value = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Nominal PPN Manual (Opsional)") },
                            prefix = { Text("Rp ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = { Text("0") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // RINGKASAN PEMBAYARAN & KEMBALIAN DI ATAS TOMBOL SELESAIKAN
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (changeAmount > 0.0) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = if (changeAmount > 0.0) BorderStroke(1.dp, EmeraldGreenPrimary) else null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Metode:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(selectedPaymentMethod, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Tagihan:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(FormatUtils.formatRupiah(totalPayable), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    if (selectedPaymentMethod == "Tunai" && hasCashInput) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Uang Diterima:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(FormatUtils.formatRupiah(cashReceived), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        if (changeAmount > 0.0) {
                            HorizontalDivider(thickness = 1.dp, color = EmeraldGreenPrimary.copy(alpha = 0.3f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "UANG KEMBALIAN:",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = EmeraldGreenDark
                                )
                                Text(
                                    text = FormatUtils.formatRupiah(changeAmount),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = EmeraldGreenDark
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Tombol Selesaikan Transaksi
            Button(
                onClick = {
                    viewModel.processCheckout { savedTx ->
                        onSuccess(savedTx)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_checkout_button")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Konfirmasi & Cetak Struk",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
