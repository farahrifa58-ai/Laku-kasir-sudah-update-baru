package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.Categories
import com.example.data.model.Product
import com.example.data.model.StoreProfile
import com.example.ui.CartItem
import com.example.ui.LakuKasirViewModel
import com.example.ui.Screen
import com.example.ui.components.AppBottomBar
import com.example.ui.components.AppTopBar
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.WarningYellow
import com.example.util.FormatUtils
import com.example.util.LakuStrings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    viewModel: LakuKasirViewModel,
    storeProfile: StoreProfile,
    onNavigate: (Screen) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCheckout: () -> Unit
) {
    val context = LocalContext.current
    val lang = storeProfile.language
    val products by viewModel.filteredPosProducts.collectAsState()
    val allProductsCount by viewModel.allProducts.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val searchQuery by viewModel.posSearchQuery.collectAsState()
    val selectedCategory by viewModel.posSelectedCategory.collectAsState()

    var showCartSheet by remember { mutableStateOf(false) }
    var productForKgDialog by remember { mutableStateOf<Product?>(null) }

    val categoriesList = listOf("Semua") + Categories.ALL_CATEGORIES

    Scaffold(
        topBar = {
            AppTopBar(
                title = LakuStrings.get("app_title", lang),
                storeProfile = storeProfile,
                onSettingsClick = onOpenSettings
            )
        },
        bottomBar = {
            AppBottomBar(
                currentScreen = Screen.POS,
                cartItemCount = cart.size,
                lang = lang,
                onNavigate = onNavigate
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.posSearchQuery.value = it },
                    placeholder = { Text("Cari produk atau barcode...", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Cari", tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.posSearchQuery.value = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Hapus")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pos_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(categoriesList) { cat ->
                        val isSelected = cat.equals(selectedCategory, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.posSelectedCategory.value = cat },
                            label = { Text(cat, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldGreenPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Product Catalog List or Empty State
                if (allProductsCount.isEmpty()) {
                    // Empty Catalog State: User requested new users add products manually
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldGreenPrimary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Inventory,
                                        contentDescription = null,
                                        tint = EmeraldGreenPrimary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Belum Ada Produk",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = EmeraldGreenDark
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Aplikasi siap digunakan. Silakan tambahkan barang dagangan pertama Anda secara manual untuk mulai melayani transaksi.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = {
                                        viewModel.productToEdit.value = null
                                        onNavigate(Screen.ADD_EDIT_PRODUCT)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("pos_empty_add_product_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Tambah Produk Pertama", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                } else if (products.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada produk yang cocok dengan pencarian / kategori ini.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = if (cart.isNotEmpty()) 80.dp else 16.dp)
                    ) {
                        items(products, key = { it.id }) { product ->
                            PosProductCard(
                                product = product,
                                cartQuantity = cart.firstOrNull { it.product.id == product.id }?.quantity ?: 0.0,
                                onAddClick = {
                                    if (product.unit.equals("kg", ignoreCase = true)) {
                                        productForKgDialog = product
                                    } else {
                                        viewModel.addToCart(product, 1.0)
                                    }
                                },
                                onIncrement = {
                                    val currentQty = cart.firstOrNull { it.product.id == product.id }?.quantity ?: 0.0
                                    viewModel.addToCart(product, 1.0)
                                },
                                onDecrement = {
                                    val currentQty = cart.firstOrNull { it.product.id == product.id }?.quantity ?: 0.0
                                    viewModel.updateCartQuantity(product.id, currentQty - 1.0)
                                }
                            )
                        }
                    }
                }
            }

            // Bottom Floating Cart Bar
            if (cart.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    shadowElevation = 8.dp,
                    color = EmeraldGreenDark
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCartSheet = true }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("floating_cart_bar"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Badge(
                                containerColor = Color.White,
                                contentColor = EmeraldGreenDark
                            ) {
                                Text(
                                    text = "${cart.size}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${cart.size} Produk di Keranjang",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = FormatUtils.formatRupiah(viewModel.cartSubtotal),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = Color.White
                                )
                            }
                        }

                        Button(
                            onClick = { showCartSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = EmeraldGreenDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Keranjang",
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreenDark,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Kg / Fractional Weight Selector Dialog
    if (productForKgDialog != null) {
        val kgProduct = productForKgDialog!!
        KgWeightDialog(
            product = kgProduct,
            onDismiss = { productForKgDialog = null },
            onConfirm = { selectedWeight ->
                viewModel.addToCart(kgProduct, selectedWeight)
                productForKgDialog = null
            }
        )
    }

    // Cart Bottom Sheet
    if (showCartSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCartSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            CartSheetContent(
                viewModel = viewModel,
                lang = lang,
                onClose = { showCartSheet = false },
                onCheckout = {
                    showCartSheet = false
                    onOpenCheckout()
                }
            )
        }
    }
}

@Composable
fun PosProductCard(
    product: Product,
    cartQuantity: Double,
    onAddClick: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image or Icon
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(EmeraldGreenPrimary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(product.imageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = null,
                        tint = EmeraldGreenPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${product.category} • ${product.unit}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = FormatUtils.formatRupiah(product.sellingPrice),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = EmeraldGreenDark
                    )
                    Text(
                        text = "/${product.unit}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Stock Badge
                if (product.isUnlimitedStock) {
                    Text(
                        text = "Stok: Unlimited",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = EmeraldGreenPrimary
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (product.isLowStock) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                        }
                        Text(
                            text = if (product.isLowStock) "Stok menipis: ${FormatUtils.formatQty(product.stock, product.unit)}"
                            else "Stok: ${FormatUtils.formatQty(product.stock, product.unit)}",
                            fontSize = 11.sp,
                            fontWeight = if (product.isLowStock) FontWeight.Bold else FontWeight.Normal,
                            color = if (product.isLowStock) DangerRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Add or Counter
            if (cartQuantity > 0.0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(EmeraldGreenPrimary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = onDecrement,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Kurangi",
                            tint = EmeraldGreenDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = if (cartQuantity % 1.0 == 0.0) "${cartQuantity.toInt()}" else String.format(java.util.Locale.US, "%.2f", cartQuantity),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = EmeraldGreenDark,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    IconButton(
                        onClick = onIncrement,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah",
                            tint = EmeraldGreenDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_to_cart_${product.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = "Tambah",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (product.unit.equals("kg", ignoreCase = true)) "Pilih kg" else "Tambah",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// Dialog khusus produk dengan satuan "kg" (pilihan 0.25, 0.50, 1.0, 2.0 atau ketik angka custom)
@Composable
fun KgWeightDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    val quickOptions = listOf(0.25, 0.50, 1.0, 2.0, 3.0, 5.0)
    var customWeightText by remember { mutableStateOf("1.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = "Pilih Berat Satuan (kg)", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(
                    text = product.name,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Pilihan Cepat Kilogram:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                // Quick buttons for 0.25, 0.50, 1.0, 2.0, etc.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickOptions.take(4).forEach { weight ->
                        OutlinedButton(
                            onClick = { customWeightText = weight.toString() },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = if (customWeightText == weight.toString()) {
                                ButtonDefaults.outlinedButtonColors(
                                    containerColor = EmeraldGreenPrimary,
                                    contentColor = Color.White
                                )
                            } else {
                                ButtonDefaults.outlinedButtonColors()
                            }
                        ) {
                            Text(
                                text = "$weight kg",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickOptions.drop(4).forEach { weight ->
                        OutlinedButton(
                            onClick = { customWeightText = weight.toString() },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = if (customWeightText == weight.toString()) {
                                ButtonDefaults.outlinedButtonColors(
                                    containerColor = EmeraldGreenPrimary,
                                    contentColor = Color.White
                                )
                            } else {
                                ButtonDefaults.outlinedButtonColors()
                            }
                        ) {
                            Text(
                                text = "$weight kg",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = customWeightText,
                    onValueChange = { customWeightText = it },
                    label = { Text("Atau Ketik Berat (kg)") },
                    suffix = { Text("kg") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                val weightVal = customWeightText.toDoubleOrNull() ?: 0.0
                val totalEst = weightVal * product.sellingPrice
                Text(
                    text = "Estimasi Harga: ${FormatUtils.formatRupiah(totalEst)}",
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreenDark,
                    fontSize = 14.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val weight = customWeightText.toDoubleOrNull() ?: 1.0
                    if (weight > 0.0) {
                        onConfirm(weight)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary)
            ) {
                Text("Tambahkan", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// Cart Sheet Content with discount, customer info, and items
@Composable
fun CartSheetContent(
    viewModel: LakuKasirViewModel,
    lang: String,
    onClose: () -> Unit,
    onCheckout: () -> Unit
) {
    val cart by viewModel.cart.collectAsState()
    val customerName by viewModel.customerName.collectAsState()
    val customerPhone by viewModel.customerPhone.collectAsState()
    val discountText by viewModel.manualDiscount.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = EmeraldGreenPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = LakuStrings.get("cart", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            TextButton(onClick = { viewModel.clearCart() }) {
                Text("Kosongkan", color = DangerRed, fontSize = 13.sp)
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Items List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cart) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.product.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${FormatUtils.formatRupiah(item.product.sellingPrice)} x ${FormatUtils.formatQty(item.quantity, item.product.unit)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = FormatUtils.formatRupiah(item.subtotal),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = EmeraldGreenDark
                        )
                    }

                    // Quantity controls
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val delta = if (item.product.unit.equals("kg", ignoreCase = true)) 0.25 else 1.0
                                viewModel.updateCartQuantity(item.product.id, item.quantity - delta)
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Kurang", tint = EmeraldGreenDark)
                        }

                        Text(
                            text = if (item.quantity % 1.0 == 0.0) "${item.quantity.toInt()}" else String.format(java.util.Locale.US, "%.2f", item.quantity),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )

                        IconButton(
                            onClick = {
                                val delta = if (item.product.unit.equals("kg", ignoreCase = true)) 0.25 else 1.0
                                viewModel.updateCartQuantity(item.product.id, item.quantity + delta)
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah", tint = EmeraldGreenDark)
                        }

                        IconButton(
                            onClick = { viewModel.removeFromCart(item.product.id) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = DangerRed)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Diskon Manual, Nama Pelanggan, dan WhatsApp
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Diskon Manual
                OutlinedTextField(
                    value = discountText,
                    onValueChange = { viewModel.manualDiscount.value = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Diskon Manual (Nominal Rp)") },
                    prefix = { Text("Rp ") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Percent, contentDescription = null, tint = EmeraldGreenPrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                // Nama Pelanggan & Nomor WhatsApp
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { viewModel.customerName.value = it },
                        label = { Text("Nama Pelanggan") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        placeholder = { Text("Opsional") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { viewModel.customerPhone.value = it },
                        label = { Text("WhatsApp") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        placeholder = { Text("08...") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Total Summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Subtotal", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(FormatUtils.formatRupiah(viewModel.cartSubtotal), fontWeight = FontWeight.Bold)
        }

        if (viewModel.discountAmount > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Diskon", color = DangerRed)
                Text("- ${FormatUtils.formatRupiah(viewModel.discountAmount)}", color = DangerRed, fontWeight = FontWeight.Bold)
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Total Pembayaran", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                FormatUtils.formatRupiah(viewModel.totalPayable),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = EmeraldGreenDark
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tombol Lanjut ke Pembayaran
        Button(
            onClick = onCheckout,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("proceed_to_checkout_button")
        ) {
            Text(
                text = LakuStrings.get("checkout", lang),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
