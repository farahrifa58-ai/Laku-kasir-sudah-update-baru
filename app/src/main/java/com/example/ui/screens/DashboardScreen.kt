package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Categories
import com.example.data.model.Expense
import com.example.data.model.StoreProfile
import com.example.data.model.Transaction
import com.example.ui.LakuKasirViewModel
import com.example.ui.Screen
import com.example.ui.components.AppBottomBar
import com.example.ui.components.AppTopBar
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.StatusDebt
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.TealAccent
import com.example.ui.theme.WarmOrange
import com.example.util.FormatUtils
import com.example.util.LakuStrings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: LakuKasirViewModel,
    storeProfile: StoreProfile,
    onNavigate: (Screen) -> Unit,
    onOpenSettings: () -> Unit
) {
    val lang = storeProfile.language
    val products by viewModel.allProducts.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val expenses by viewModel.allExpenses.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val selectedPeriod by viewModel.dashboardPeriod.collectAsState()

    var showAddExpenseDialog by remember { mutableStateOf(false) }

    // Filter transactions and expenses based on selected period
    val now = Calendar.getInstance()
    val startOfToday = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val startOfWeek = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -7)
    }.timeInMillis

    val startOfMonth = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val filteredTransactions = transactions.filter {
        when (selectedPeriod) {
            "today" -> it.timestamp >= startOfToday
            "week" -> it.timestamp >= startOfWeek
            "month" -> it.timestamp >= startOfMonth
            else -> true
        }
    }

    val filteredExpenses = expenses.filter {
        when (selectedPeriod) {
            "today" -> it.timestamp >= startOfToday
            "week" -> it.timestamp >= startOfWeek
            "month" -> it.timestamp >= startOfMonth
            else -> true
        }
    }

    // 1. Omzet Penjualan (Revenue)
    val omzetPenjualan = filteredTransactions.sumOf { it.total }

    // 2. Modal Terjual (COGS / HPP)
    val modalTerjual = filteredTransactions.sumOf { it.totalCost }

    // 3. Laba Kotor (Gross Profit = Omzet - Modal Terjual)
    val labaKotor = maxOf(0.0, omzetPenjualan - modalTerjual)

    // 4. Biaya Pengeluaran Toko
    val biayaPengeluaran = filteredExpenses.sumOf { it.amount }

    // 5. Estimasi Laba Bersih = Laba Kotor - Biaya Pengeluaran
    val estimasiLabaBersih = labaKotor - biayaPengeluaran

    // 6. Nilai Aset Stok = Total Modal * Stok yang masih ada di gudang/toko
    val nilaiAsetStok = products.filter { !it.isUnlimitedStock }.sumOf { it.costPrice * it.stock }

    Scaffold(
        topBar = {
            AppTopBar(
                title = LakuStrings.get("nav_dashboard", lang),
                storeProfile = storeProfile,
                onSettingsClick = onOpenSettings
            )
        },
        bottomBar = {
            AppBottomBar(
                currentScreen = Screen.DASHBOARD,
                cartItemCount = cart.size,
                lang = lang,
                onNavigate = onNavigate
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp)
        ) {
            // Filter Periode Tabs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val periods = listOf(
                        "today" to "Hari Ini",
                        "week" to "7 Hari",
                        "month" to "Bulan Ini",
                        "all" to "Semua"
                    )
                    periods.forEach { (key, label) ->
                        val isSelected = selectedPeriod == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.dashboardPeriod.value = key },
                            label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldGreenPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // 6 Metric Cards
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Row 1: Omzet & Laba Bersih
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Omzet Penjualan",
                            value = FormatUtils.formatRupiah(omzetPenjualan),
                            subtitle = "${filteredTransactions.size} Transaksi",
                            icon = Icons.Default.Payments,
                            cardColor = EmeraldGreenPrimary,
                            textColor = Color.White,
                            modifier = Modifier.weight(1f)
                        )

                        MetricCard(
                            title = "Estimasi Laba Bersih",
                            value = FormatUtils.formatRupiah(estimasiLabaBersih),
                            subtitle = if (estimasiLabaBersih >= 0) "Surplus Bersih" else "Defisit",
                            icon = Icons.Default.TrendingUp,
                            cardColor = if (estimasiLabaBersih >= 0) EmeraldGreenDark else DangerRed,
                            textColor = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 2: Modal Terjual & Laba Kotor
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Modal Terjual (HPP)",
                            value = FormatUtils.formatRupiah(modalTerjual),
                            subtitle = "Harga modal barang",
                            icon = Icons.Default.ArrowDownward,
                            cardColor = MaterialTheme.colorScheme.surface,
                            textColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        MetricCard(
                            title = "Laba Kotor",
                            value = FormatUtils.formatRupiah(labaKotor),
                            subtitle = "Omzet - HPP",
                            icon = Icons.Default.ArrowUpward,
                            cardColor = MaterialTheme.colorScheme.surface,
                            textColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 3: Biaya Pengeluaran Toko & Nilai Aset Stok
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Biaya Pengeluaran",
                            value = FormatUtils.formatRupiah(biayaPengeluaran),
                            subtitle = "${filteredExpenses.size} Biaya operasional",
                            icon = Icons.Default.MoneyOff,
                            cardColor = MaterialTheme.colorScheme.surface,
                            textColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        MetricCard(
                            title = "Nilai Aset Stok",
                            value = FormatUtils.formatRupiah(nilaiAsetStok),
                            subtitle = "Total nilai gudang",
                            icon = Icons.Default.Inventory2,
                            cardColor = MaterialTheme.colorScheme.surface,
                            textColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Quick "+ Catat Pengeluaran Toko" Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pengeluaran Operasional Toko",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Button(
                        onClick = { showAddExpenseDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = EmeraldGreenDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Catat Biaya", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreenDark)
                    }
                }
            }

            // Sales Trend Chart (Interactive, Easy to Understand)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.ShowChart, contentDescription = null, tint = EmeraldGreenPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Grafik Tren Penjualan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Text(
                            text = "Ringkasan pemasukan harian toko Anda",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Render Canvas Bar Chart for last 7 days
                        SalesBarChart(transactions = transactions)
                    }
                }
            }

            // Top Selling Category breakdown
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Penjualan Berdasarkan Kategori",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val allSoldItems = transactions.flatMap { it.items }
                        val categoryTotals = allSoldItems.groupBy { it.category }
                            .mapValues { entry -> entry.value.sumOf { it.subtotal } }
                            .toList()
                            .sortedByDescending { it.second }

                        if (categoryTotals.isEmpty()) {
                            Text(
                                text = "Belum ada data transaksi untuk kategori.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            val maxCat = categoryTotals.firstOrNull()?.second ?: 1.0
                            categoryTotals.take(5).forEach { (cat, total) ->
                                val progress = (total / maxCat).toFloat().coerceIn(0.05f, 1f)
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = cat, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text(text = FormatUtils.formatRupiah(total), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreenDark)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(EmeraldGreenPrimary.copy(alpha = 0.15f))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(progress)
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(EmeraldGreenPrimary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Expense Dialog
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            onConfirm = { title, cat, amt, note ->
                viewModel.addExpense(title, cat, amt, note)
                showAddExpenseDialog = false
            }
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    cardColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor.copy(alpha = 0.8f),
                    maxLines = 1
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor.copy(alpha = 0.9f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = textColor.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
    }
}

@Composable
fun SalesBarChart(transactions: List<Transaction>) {
    // Group sales by the last 7 days
    val calendar = Calendar.getInstance()
    val sdfDay = SimpleDateFormat("dd/MM", Locale.getDefault())
    val sdfKey = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    val daysList = (6 downTo 0).map { dayOffset ->
        val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -dayOffset) }
        val key = sdfKey.format(c.time)
        val label = sdfDay.format(c.time)
        key to label
    }

    val dailyTotals = transactions.groupBy {
        sdfKey.format(Date(it.timestamp))
    }.mapValues { entry ->
        entry.value.sumOf { it.total }
    }

    val chartData = daysList.map { (key, label) ->
        label to (dailyTotals[key] ?: 0.0)
    }

    val maxAmount = chartData.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 100000.0

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barWidth = size.width / (chartData.size * 2)
                val spacing = size.width / chartData.size

                chartData.forEachIndexed { index, (_, amount) ->
                    val barHeight = ((amount / maxAmount) * (size.height - 20.dp.toPx())).toFloat()
                    val x = index * spacing + spacing / 4
                    val y = size.height - barHeight

                    // Bar Background
                    drawRoundRect(
                        color = Color(0xFFDCFCE7),
                        topLeft = Offset(x, 0f),
                        size = Size(barWidth, size.height),
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )

                    // Active Bar
                    if (barHeight > 0f) {
                        drawRoundRect(
                            color = Color(0xFF16A34A),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(6.dp.toPx())
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Days labels below
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            chartData.forEach { (label, amount) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    if (amount > 0) {
                        Text(
                            text = "${(amount / 1000).toInt()}k",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreenDark
                        )
                    }
                }
            }
        }
    }
}

// Dialog Catat Pengeluaran Toko
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, amount: Double, note: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(Categories.EXPENSE_CATEGORIES.first()) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Biaya Pengeluaran Toko", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Pengeluaran") },
                    placeholder = { Text("Misal: Bayar Listrik, Beli Plastik") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori Biaya") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        Categories.EXPENSE_CATEGORIES.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Jumlah Biaya (Rp)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Keterangan Tambahan") },
                    placeholder = { Text("Opsional") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amt > 0) {
                        onConfirm(title, category, amt, note)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary)
            ) {
                Text("Simpan", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
