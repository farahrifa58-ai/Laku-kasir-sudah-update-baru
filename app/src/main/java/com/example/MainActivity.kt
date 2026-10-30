package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.LakuKasirViewModel
import com.example.ui.Screen
import com.example.ui.screens.AddEditProductScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.PosScreen
import com.example.ui.screens.ProductListScreen
import com.example.ui.screens.ReceiptScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.LakuKasirTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: LakuKasirViewModel = viewModel()
            val storeProfile by viewModel.storeProfile.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val productToEdit by viewModel.productToEdit.collectAsState()
            val activeReceipt by viewModel.activeReceiptTransaction.collectAsState()

            LakuKasirTheme(themeSetting = if (currentScreen == Screen.SPLASH) "light" else storeProfile.themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (currentScreen == Screen.SPLASH) androidx.compose.ui.graphics.Color(0xFFE8F5E9) else androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    // Back button handling
                    BackHandler(enabled = currentScreen != Screen.POS && currentScreen != Screen.SPLASH) {
                        when (currentScreen) {
                            Screen.ADD_EDIT_PRODUCT -> viewModel.navigateTo(Screen.PRODUCTS)
                            Screen.SETTINGS -> viewModel.navigateTo(Screen.POS)
                            Screen.RECEIPT_VIEW -> viewModel.navigateTo(Screen.POS)
                            else -> viewModel.navigateTo(Screen.POS)
                        }
                    }

                    when (currentScreen) {
                        Screen.SPLASH -> {
                            SplashScreen(
                                onContinue = { viewModel.finishSplash() }
                            )
                        }

                        Screen.POS -> {
                            PosScreen(
                                viewModel = viewModel,
                                storeProfile = storeProfile,
                                onNavigate = { viewModel.navigateTo(it) },
                                onOpenSettings = { viewModel.navigateTo(Screen.SETTINGS) },
                                onOpenCheckout = {
                                    viewModel.navigateTo(Screen.CHECKOUT)
                                }
                            )
                        }

                        Screen.CHECKOUT -> {
                            CheckoutScreen(
                                viewModel = viewModel,
                                lang = storeProfile.language,
                                onBack = { viewModel.navigateTo(Screen.POS) },
                                onSuccess = { savedTx ->
                                    viewModel.activeReceiptTransaction.value = savedTx
                                    viewModel.navigateTo(Screen.RECEIPT_VIEW)
                                }
                            )
                        }

                        Screen.PRODUCTS -> {
                            ProductListScreen(
                                viewModel = viewModel,
                                storeProfile = storeProfile,
                                onNavigate = { viewModel.navigateTo(it) },
                                onOpenSettings = { viewModel.navigateTo(Screen.SETTINGS) }
                            )
                        }

                        Screen.ADD_EDIT_PRODUCT -> {
                            AddEditProductScreen(
                                productToEdit = productToEdit,
                                lang = storeProfile.language,
                                onBack = { viewModel.navigateTo(Screen.PRODUCTS) },
                                onSave = { id, name, category, unit, barcode, costPrice, sellingPrice, stock, isUnlimited, minAlert, supplierName, supplierPhone, imageUri ->
                                    viewModel.saveProduct(
                                        id = id,
                                        name = name,
                                        category = category,
                                        unit = unit,
                                        barcode = barcode,
                                        costPrice = costPrice,
                                        sellingPrice = sellingPrice,
                                        stock = stock,
                                        isUnlimitedStock = isUnlimited,
                                        minStockAlert = minAlert,
                                        supplierName = supplierName,
                                        supplierPhone = supplierPhone,
                                        imageUri = imageUri
                                    )
                                }
                            )
                        }

                        Screen.HISTORY -> {
                            HistoryScreen(
                                viewModel = viewModel,
                                storeProfile = storeProfile,
                                onNavigate = { viewModel.navigateTo(it) },
                                onOpenSettings = { viewModel.navigateTo(Screen.SETTINGS) },
                                onSelectTransaction = { tx ->
                                    viewModel.activeReceiptTransaction.value = tx
                                    viewModel.navigateTo(Screen.RECEIPT_VIEW)
                                }
                            )
                        }

                        Screen.DASHBOARD -> {
                            DashboardScreen(
                                viewModel = viewModel,
                                storeProfile = storeProfile,
                                onNavigate = { viewModel.navigateTo(it) },
                                onOpenSettings = { viewModel.navigateTo(Screen.SETTINGS) }
                            )
                        }

                        Screen.SETTINGS -> {
                            SettingsScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.navigateTo(Screen.POS) }
                            )
                        }

                        Screen.RECEIPT_VIEW -> {
                            if (activeReceipt != null) {
                                ReceiptScreen(
                                    transaction = activeReceipt!!,
                                    storeProfile = storeProfile,
                                    lang = storeProfile.language,
                                    onBack = { viewModel.navigateTo(Screen.POS) },
                                    onNewTransaction = {
                                        viewModel.clearCart()
                                        viewModel.activeReceiptTransaction.value = null
                                        viewModel.navigateTo(Screen.POS)
                                    }
                                )
                            } else {
                                viewModel.navigateTo(Screen.POS)
                            }
                        }
                    }
                }
            }
        }
    }
}
