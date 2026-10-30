package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Send
import com.example.util.BackupRestoreHelper
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.StoreProfile
import com.example.ui.LakuKasirViewModel
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.util.LakuStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: LakuKasirViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentProfile by viewModel.storeProfile.collectAsState()

    var storeName by remember(currentProfile) { mutableStateOf(currentProfile.storeName) }
    var ownerName by remember(currentProfile) { mutableStateOf(currentProfile.ownerName) }
    var address by remember(currentProfile) { mutableStateOf(currentProfile.address) }
    var phone by remember(currentProfile) { mutableStateOf(currentProfile.phone) }
    var receiptNotes by remember(currentProfile) { mutableStateOf(currentProfile.receiptNotes) }
    var logoUri by remember(currentProfile) { mutableStateOf(currentProfile.logoUri) }
    var selectedTheme by remember(currentProfile) { mutableStateOf(currentProfile.themeMode) }
    var selectedLanguage by remember(currentProfile) { mutableStateOf(currentProfile.language) }

    var saveSuccessMessage by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val products by viewModel.allProducts.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val expenses by viewModel.allExpenses.collectAsState()

    val restoreFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                BackupRestoreHelper.restoreBackupFromJsonUri(
                    context = context,
                    uri = uri,
                    onSuccess = { pCount, tCount ->
                        Toast.makeText(context, "Berhasil memulihkan $pCount produk & $tCount transaksi!", Toast.LENGTH_LONG).show()
                    },
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val permanentPath = com.example.util.ImageUtils.saveImageToInternalStorage(context, uri, "store_logo")
            if (permanentPath != null) {
                logoUri = permanentPath
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(LakuStrings.get("settings", selectedLanguage), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
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
            // Edit Profil Toko & Upload Logo
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = LakuStrings.get("edit_profile", selectedLanguage),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = EmeraldGreenDark
                    )

                    // Logo Uploader Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(EmeraldGreenPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (logoUri != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(logoUri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Logo Toko",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.logo_laku_kasir),
                                    contentDescription = "Default Logo",
                                    modifier = Modifier.size(56.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Logo / Foto Toko",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Ketuk untuk ganti foto logo toko yang akan tercetak di struk",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Ganti Logo",
                            tint = EmeraldGreenPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Nama Toko
                    OutlinedTextField(
                        value = storeName,
                        onValueChange = { storeName = it },
                        label = { Text(LakuStrings.get("store_name", selectedLanguage)) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Business, contentDescription = null, tint = EmeraldGreenPrimary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("store_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Owner / Pemilik
                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text(LakuStrings.get("owner_name", selectedLanguage)) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = EmeraldGreenPrimary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("owner_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Alamat Toko
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text(LakuStrings.get("store_address", selectedLanguage)) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Place, contentDescription = null, tint = EmeraldGreenPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Nomor WhatsApp Toko
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(LakuStrings.get("whatsapp_no", selectedLanguage)) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = EmeraldGreenPrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("store_phone_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Catatan di bagian struk (Pesan ucapan terima kasih)
                    OutlinedTextField(
                        value = receiptNotes,
                        onValueChange = { receiptNotes = it },
                        label = { Text(LakuStrings.get("receipt_notes", selectedLanguage)) },
                        placeholder = { Text("Contoh: Terima kasih telah berbelanja! Semoga berkah.") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Receipt, contentDescription = null, tint = EmeraldGreenPrimary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("receipt_notes_input"),
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Ganti Tema (Terang, Gelap, Sesuai Sistem)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = EmeraldGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = LakuStrings.get("theme", selectedLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val themes = listOf(
                            "system" to LakuStrings.get("theme_system", selectedLanguage),
                            "light" to LakuStrings.get("theme_light", selectedLanguage),
                            "dark" to LakuStrings.get("theme_dark", selectedLanguage)
                        )

                        themes.forEach { (mode, label) ->
                            val isSelected = selectedTheme == mode
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedTheme = mode
                                    viewModel.setThemeMode(mode)
                                },
                                label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldGreenPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }

            // Bahasa (Bahasa Indonesia & English)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = EmeraldGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = LakuStrings.get("language", selectedLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = selectedLanguage == "id",
                            onClick = {
                                selectedLanguage = "id"
                                viewModel.setLanguage("id")
                            },
                            label = { Text("🇮🇩 Bahasa Indonesia", fontSize = 13.sp, fontWeight = if (selectedLanguage == "id") FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldGreenPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        FilterChip(
                            selected = selectedLanguage == "en",
                            onClick = {
                                selectedLanguage = "en"
                                viewModel.setLanguage("en")
                            },
                            label = { Text("🇬🇧 English", fontSize = 13.sp, fontWeight = if (selectedLanguage == "en") FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldGreenPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // Cadangkan Data ke Google Drive atau Kirim ke WhatsApp
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Backup, contentDescription = null, tint = EmeraldGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cadangkan & Pulihkan Data Toko",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = EmeraldGreenDark
                        )
                    }

                    Text(
                        text = "Simpan seluruh katalog produk, stok, riwayat transaksi, dan biaya operasional toko ke file cadangan yang aman.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Tombol Cadangkan ke Google Drive
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val file = BackupRestoreHelper.createBackupJson(
                                    context, currentProfile, products, transactions, expenses
                                )
                                if (file != null) {
                                    BackupRestoreHelper.backupToGoogleDrive(context, file)
                                } else {
                                    Toast.makeText(context, "Gagal membuat file cadangan", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("backup_drive_button")
                    ) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cadangkan ke Google Drive",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }

                    // Tombol Kirim File ke WhatsApp
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val file = BackupRestoreHelper.createBackupJson(
                                    context, currentProfile, products, transactions, expenses
                                )
                                if (file != null) {
                                    BackupRestoreHelper.shareBackupToWhatsApp(context, file)
                                } else {
                                    Toast.makeText(context, "Gagal membuat file cadangan", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("backup_whatsapp_button")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kirim File Cadangan ke WhatsApp",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }

                    // Tombol Pulihkan Data dari File
                    OutlinedButton(
                        onClick = {
                            restoreFileLauncher.launch("application/json")
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("restore_backup_button")
                    ) {
                        Icon(imageVector = Icons.Default.Restore, contentDescription = null, tint = EmeraldGreenDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pulihkan Data dari File Cadangan (JSON)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = EmeraldGreenDark
                        )
                    }
                }
            }

            // Keterangan APK Versi 1.1
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreenPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = EmeraldGreenDark)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Laku Kasir Pro",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Aplikasi Kasir & POS by Rafline Dev",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Keterangan APK: Versi 1.1",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreenDark
                        )
                    }
                }
            }

            // Tombol Simpan Pengaturan
            Button(
                onClick = {
                    val updated = currentProfile.copy(
                        storeName = storeName.trim(),
                        ownerName = ownerName.trim(),
                        address = address.trim(),
                        phone = phone.trim(),
                        receiptNotes = receiptNotes.trim(),
                        logoUri = logoUri,
                        themeMode = selectedTheme,
                        language = selectedLanguage
                    )
                    viewModel.updateStoreProfile(updated)
                    saveSuccessMessage = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_settings_button")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = LakuStrings.get("save", selectedLanguage) + " Pengaturan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            if (saveSuccessMessage) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = EmeraldGreenPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✓ Pengaturan toko berhasil disimpan!",
                        color = EmeraldGreenDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
