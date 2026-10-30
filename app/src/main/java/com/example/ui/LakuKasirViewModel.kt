package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.model.Expense
import com.example.data.model.Product
import com.example.data.model.StoreProfile
import com.example.data.model.Transaction
import com.example.data.model.TransactionItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CartItem(
    val product: Product,
    val quantity: Double
) {
    val subtotal: Double
        get() = product.sellingPrice * quantity

    val costSubtotal: Double
        get() = product.costPrice * quantity
}

enum class Screen {
    SPLASH,
    POS,
    CHECKOUT,
    PRODUCTS,
    HISTORY,
    DASHBOARD,
    SETTINGS,
    ADD_EDIT_PRODUCT,
    RECEIPT_VIEW
}

class LakuKasirViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val productDao = db.productDao()
    private val transactionDao = db.transactionDao()
    private val expenseDao = db.expenseDao()
    private val prefsManager = PreferencesManager(application)

    val storeProfile: StateFlow<StoreProfile> = prefsManager.storeProfile

    // Navigation state
    private val _currentScreen = MutableStateFlow(Screen.SPLASH)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Splash display helper
    private val _splashFinished = MutableStateFlow(false)
    val splashFinished: StateFlow<Boolean> = _splashFinished.asStateFlow()

    // Products Flow
    val allProducts: StateFlow<List<Product>> = productDao.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Transactions Flow
    val allTransactions: StateFlow<List<Transaction>> = transactionDao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Expenses Flow
    val allExpenses: StateFlow<List<Expense>> = expenseDao.getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Product search and filter in POS
    val posSearchQuery = MutableStateFlow("")
    val posSelectedCategory = MutableStateFlow("Semua")

    // Filtered Products for POS
    val filteredPosProducts = combine(
        allProducts,
        posSearchQuery,
        posSelectedCategory
    ) { products, query, cat ->
        products.filter { p ->
            val matchQuery = query.isBlank() ||
                    p.name.contains(query, ignoreCase = true) ||
                    p.barcode.contains(query, ignoreCase = true)
            val matchCategory = cat == "Semua" || p.category.equals(cat, ignoreCase = true)
            matchQuery && matchCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart State
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    // Checkout Details
    val customerName = MutableStateFlow("")
    val customerPhone = MutableStateFlow("")
    val manualDiscount = MutableStateFlow("")
    val isPpnAuto = MutableStateFlow(false)
    val ppnRate = MutableStateFlow(11.0)
    val manualPpnAmount = MutableStateFlow("")
    val selectedPaymentMethod = MutableStateFlow("Tunai")
    val cashReceivedInput = MutableStateFlow("")

    // Active product for edit
    val productToEdit = MutableStateFlow<Product?>(null)

    // Last completed transaction for Receipt view
    val activeReceiptTransaction = MutableStateFlow<Transaction?>(null)

    // Period filter for Dashboard: "today", "week", "month", "all"
    val dashboardPeriod = MutableStateFlow("today")

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun finishSplash() {
        _splashFinished.value = true
        _currentScreen.value = Screen.POS
    }

    // Cart operations
    fun addToCart(product: Product, quantity: Double = 1.0) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = currentList[index]
            val newQty = existing.quantity + quantity
            currentList[index] = existing.copy(quantity = newQty)
        } else {
            currentList.add(CartItem(product, quantity))
        }
        _cart.value = currentList
    }

    fun updateCartQuantity(productId: Long, newQuantity: Double) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            if (newQuantity <= 0.0) {
                currentList.removeAt(index)
            } else {
                currentList[index] = currentList[index].copy(quantity = newQuantity)
            }
            _cart.value = currentList
        }
    }

    fun removeFromCart(productId: Long) {
        _cart.value = _cart.value.filterNot { it.product.id == productId }
    }

    fun clearCart() {
        _cart.value = emptyList()
        customerName.value = ""
        customerPhone.value = ""
        manualDiscount.value = ""
        manualPpnAmount.value = ""
        isPpnAuto.value = false
        cashReceivedInput.value = ""
        selectedPaymentMethod.value = "Tunai"
    }

    // Calculations for Cart & Checkout
    val cartSubtotal: Double
        get() = _cart.value.sumOf { it.subtotal }

    val discountAmount: Double
        get() = manualDiscount.value.toDoubleOrNull() ?: 0.0

    val calculatedPpnAmount: Double
        get() = when {
            isPpnAuto.value -> {
                val afterDiscount = maxOf(0.0, cartSubtotal - discountAmount)
                (afterDiscount * (ppnRate.value / 100.0))
            }
            manualPpnAmount.value.isNotBlank() -> manualPpnAmount.value.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }

    val totalPayable: Double
        get() = maxOf(0.0, (cartSubtotal - discountAmount) + calculatedPpnAmount)

    val cashReceivedAmount: Double
        get() = cashReceivedInput.value.toDoubleOrNull() ?: 0.0

    val changeAmount: Double
        get() = if (selectedPaymentMethod.value == "Tunai") {
            maxOf(0.0, cashReceivedAmount - totalPayable)
        } else {
            0.0
        }

    fun setExactCash() {
        cashReceivedInput.value = totalPayable.toInt().toString()
    }

    fun addQuickCash(amount: Double) {
        val current = cashReceivedInput.value.toDoubleOrNull() ?: 0.0
        cashReceivedInput.value = (current + amount).toInt().toString()
    }

    // Checkout process
    fun processCheckout(onSuccess: (Transaction) -> Unit) {
        val items = _cart.value
        if (items.isEmpty()) return

        val now = System.currentTimeMillis()
        val codePrefix = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(now))
        val randomSuffix = (100..999).random()
        val code = "LK-$codePrefix-$randomSuffix"

        val transactionItems = items.map {
            TransactionItem(
                productId = it.product.id,
                productName = it.product.name,
                category = it.product.category,
                unit = it.product.unit,
                costPrice = it.product.costPrice,
                sellingPrice = it.product.sellingPrice,
                quantity = it.quantity,
                subtotal = it.subtotal
            )
        }

        val effectiveCashReceived = if (selectedPaymentMethod.value == "Tunai") {
            cashReceivedAmount
        } else {
            totalPayable
        }

        val isPaid = selectedPaymentMethod.value != "Kasbon"

        val transaction = Transaction(
            transactionCode = code,
            timestamp = now,
            subtotal = cartSubtotal,
            discount = discountAmount,
            discountNote = if (discountAmount > 0) "Diskon Langsung" else "",
            ppnRate = if (isPpnAuto.value) ppnRate.value else 0.0,
            ppnAmount = calculatedPpnAmount,
            total = totalPayable,
            paymentMethod = selectedPaymentMethod.value,
            cashReceived = effectiveCashReceived,
            changeAmount = changeAmount,
            customerName = customerName.value.trim(),
            customerPhone = customerPhone.value.trim(),
            isPaid = isPaid,
            itemsJson = TransactionItem.listToJsonString(transactionItems)
        )

        viewModelScope.launch {
            // Save transaction
            val id = transactionDao.insert(transaction)
            val savedTx = transaction.copy(id = id)

            // Deduct stock for products with finite stock
            items.forEach { item ->
                if (!item.product.isUnlimitedStock) {
                    val updatedStock = maxOf(0.0, item.product.stock - item.quantity)
                    productDao.updateStock(item.product.id, updatedStock)
                }
            }

            // Clear cart
            clearCart()

            // Open Receipt
            activeReceiptTransaction.value = savedTx
            _currentScreen.value = Screen.RECEIPT_VIEW
            onSuccess(savedTx)
        }
    }

    // Product CRUD
    fun saveProduct(
        id: Long = 0,
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
    ) {
        viewModelScope.launch {
            val product = Product(
                id = id,
                name = name.trim(),
                category = category.trim(),
                unit = unit.trim(),
                barcode = barcode.trim(),
                costPrice = costPrice,
                sellingPrice = sellingPrice,
                stock = if (isUnlimitedStock) 0.0 else stock,
                isUnlimitedStock = isUnlimitedStock,
                minStockAlert = minStockAlert,
                supplierName = supplierName.trim(),
                supplierPhone = supplierPhone.trim(),
                imageUri = imageUri
            )
            if (id == 0L) {
                productDao.insert(product)
            } else {
                productDao.update(product)
            }
            productToEdit.value = null
            _currentScreen.value = Screen.PRODUCTS
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            productDao.delete(product)
        }
    }

    fun quickUpdateStock(product: Product, delta: Double) {
        if (product.isUnlimitedStock) return
        val newStock = maxOf(0.0, product.stock + delta)
        viewModelScope.launch {
            productDao.updateStock(product.id, newStock)
        }
    }

    // Expense
    fun addExpense(title: String, category: String, amount: Double, note: String) {
        viewModelScope.launch {
            val expense = Expense(
                title = title.trim(),
                category = category.trim(),
                amount = amount,
                note = note.trim()
            )
            expenseDao.insert(expense)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseDao.delete(expense)
        }
    }

    // Mark Kasbon as Paid
    fun markTransactionAsPaid(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.updatePaymentStatus(transaction.id, true)
            if (activeReceiptTransaction.value?.id == transaction.id) {
                activeReceiptTransaction.value = transaction.copy(isPaid = true)
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.delete(transaction)
            if (activeReceiptTransaction.value?.id == transaction.id) {
                activeReceiptTransaction.value = null
                _currentScreen.value = Screen.HISTORY
            }
        }
    }

    // Settings
    fun updateStoreProfile(profile: StoreProfile) {
        prefsManager.saveStoreProfile(profile)
    }

    fun setThemeMode(mode: String) {
        prefsManager.setThemeMode(mode)
    }

    fun setLanguage(lang: String) {
        prefsManager.setLanguage(lang)
    }
}
