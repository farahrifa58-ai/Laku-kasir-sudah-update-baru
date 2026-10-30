package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Categories
import com.example.data.model.Product
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.util.FormatUtils
import com.example.util.LakuStrings
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditProductScreen(
    productToEdit: Product?,
    lang: String,
    onBack: () -> Unit,
    onSave: (
        id: Long,
        name: String,
        category: String,
        unit: String,
        barcode: String,
        costPrice: Double,
        sellingPrice: Double,
        stock: Double,
        isUnlimitedStock: Boolean,
        minStockAlert: Double,
        supplierName: String,
        supplierPhone: String,
        imageUri: String?
    ) -> Unit
) {
    val context = LocalContext.current
    val isEdit = productToEdit != null

    var name by remember { mutableStateOf(productToEdit?.name ?: "") }
    var category by remember { mutableStateOf(productToEdit?.category ?: Categories.ALL_CATEGORIES.first()) }
    var unit by remember { mutableStateOf(productToEdit?.unit ?: "pcs") }
    var barcode by remember { mutableStateOf(productToEdit?.barcode ?: "") }
    var costPriceText by remember { mutableStateOf(if (productToEdit != null && productToEdit.costPrice > 0) productToEdit.costPrice.toInt().toString() else "") }
    var sellingPriceText by remember { mutableStateOf(if (productToEdit != null && productToEdit.sellingPrice > 0) productToEdit.sellingPrice.toInt().toString() else "") }
    var stockText by remember { mutableStateOf(if (productToEdit != null) (if (productToEdit.stock % 1.0 == 0.0) productToEdit.stock.toInt().toString() else productToEdit.stock.toString()) else "10") }
    var isUnlimitedStock by remember { mutableStateOf(productToEdit?.isUnlimitedStock ?: false) }
    var minStockAlertText by remember { mutableStateOf(if (productToEdit != null) productToEdit.minStockAlert.toInt().toString() else "5") }
    var supplierName by remember { mutableStateOf(productToEdit?.supplierName ?: "") }
    var supplierPhone by remember { mutableStateOf(productToEdit?.supplierPhone ?: "") }
    var imageUri by remember { mutableStateOf(productToEdit?.imageUri) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var unitDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val permanentPath = com.example.util.ImageUtils.saveImageToInternalStorage(context, uri, "prod")
            if (permanentPath != null) {
                imageUri = permanentPath
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEdit) LakuStrings.get("edit_product", lang) else LakuStrings.get("add_product", lang),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("add_product_back_button")) {
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
            // Upload Foto Produk Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(imageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Foto Produk",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp),
                            shape = CircleShape,
                            color = EmeraldGreenPrimary
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Ganti Foto",
                                tint = Color.White,
                                modifier = Modifier.padding(8.dp).size(20.dp)
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Upload Foto Produk",
                                tint = EmeraldGreenPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Upload Foto Produk",
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldGreenDark,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Ketuk untuk memilih foto dari galeri",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Nama Produk
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; errorMessage = null },
                label = { Text(LakuStrings.get("product_name", lang)) },
                placeholder = { Text("Contoh: Beras Ramos 5kg, Kopi Susu") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_name_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Kategori dan Satuan (Berdampingan)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Kategori Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                    modifier = Modifier.weight(1.3f)
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(LakuStrings.get("category", lang)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("category_dropdown"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        Categories.ALL_CATEGORIES.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, fontWeight = if (cat == category) FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    category = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Satuan Dropdown (Di samping Kategori)
                ExposedDropdownMenuBox(
                    expanded = unitDropdownExpanded,
                    onExpandedChange = { unitDropdownExpanded = !unitDropdownExpanded },
                    modifier = Modifier.weight(0.9f)
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(LakuStrings.get("unit", lang)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("unit_dropdown"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = unitDropdownExpanded,
                        onDismissRequest = { unitDropdownExpanded = false }
                    ) {
                        Categories.ALL_UNITS.forEach { u ->
                            DropdownMenuItem(
                                text = { Text(u, fontWeight = if (u == unit) FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    unit = u
                                    unitDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Kode / Barcode
            OutlinedTextField(
                value = barcode,
                onValueChange = { barcode = it },
                label = { Text(LakuStrings.get("barcode", lang)) },
                placeholder = { Text("Contoh: 899123456789 atau KODE-01") },
                trailingIcon = {
                    IconButton(onClick = {
                        val randomCode = "BRC-" + (100000..999999).random().toString()
                        barcode = randomCode
                    }) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Buat Barcode Otomatis", tint = EmeraldGreenPrimary)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("barcode_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Harga Modal / HPP & Harga Jual (Berdampingan)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = costPriceText,
                    onValueChange = { costPriceText = it.filter { ch -> ch.isDigit() } },
                    label = { Text(LakuStrings.get("cost_price", lang)) },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("cost_price_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = sellingPriceText,
                    onValueChange = { sellingPriceText = it.filter { ch -> ch.isDigit() } },
                    label = { Text(LakuStrings.get("selling_price", lang)) },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("selling_price_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Stok Barang (Pilihan Ketik & Unlimited) & Batas Peringatan Stok Menipis
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = LakuStrings.get("stock_unlimited", lang),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Aktifkan jika stok tidak terbatas (misal: jasa/masakan)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isUnlimitedStock,
                            onCheckedChange = { isUnlimitedStock = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EmeraldGreenPrimary
                            ),
                            modifier = Modifier.testTag("unlimited_stock_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = if (isUnlimitedStock) "∞" else stockText,
                            onValueChange = { stockText = it },
                            enabled = !isUnlimitedStock,
                            label = { Text(LakuStrings.get("stock", lang)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stock_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = minStockAlertText,
                            onValueChange = { minStockAlertText = it.filter { ch -> ch.isDigit() } },
                            enabled = !isUnlimitedStock,
                            label = { Text(LakuStrings.get("min_stock_alert", lang)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("min_stock_alert_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Info Supplier (Nama Supplier & Nomor WhatsApp yang bisa langsung dihubungi)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Informasi Supplier / Distributor",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = supplierName,
                        onValueChange = { supplierName = it },
                        label = { Text(LakuStrings.get("supplier_name", lang)) },
                        placeholder = { Text("Nama agen / distributor") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("supplier_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = supplierPhone,
                        onValueChange = { supplierPhone = it },
                        label = { Text(LakuStrings.get("supplier_phone", lang)) },
                        placeholder = { Text("Contoh: 081234567890") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        trailingIcon = {
                            if (supplierPhone.isNotBlank()) {
                                IconButton(onClick = {
                                    FormatUtils.openWhatsApp(
                                        context,
                                        supplierPhone,
                                        "Halo ${supplierName.ifBlank { "Supplier" }}, kami dari toko ingin menanyakan pesanan stok barang."
                                    )
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Hubungi WhatsApp",
                                        tint = EmeraldGreenPrimary
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("supplier_phone_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Simpan Button
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Nama produk wajib diisi!"
                        return@Button
                    }
                    val sellingPrice = sellingPriceText.toDoubleOrNull() ?: 0.0
                    val costPrice = costPriceText.toDoubleOrNull() ?: 0.0
                    val stock = stockText.toDoubleOrNull() ?: 0.0
                    val minAlert = minStockAlertText.toDoubleOrNull() ?: 5.0

                    onSave(
                        productToEdit?.id ?: 0L,
                        name,
                        category,
                        unit,
                        barcode,
                        costPrice,
                        sellingPrice,
                        stock,
                        isUnlimitedStock,
                        minAlert,
                        supplierName,
                        supplierPhone,
                        imageUri
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_product_button"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEdit) "Simpan Perubahan" else "Tambah ke Katalog",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
