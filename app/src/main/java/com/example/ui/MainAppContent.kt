package com.example.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.models.TransferItem
import com.example.data.repository.FirestoreRepository
import com.example.data.service.StorageStatus
import com.example.data.service.TvFileManager
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.SlateSubtext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URLEncoder
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

private const val PREFS_NAME = "pvt_tv_prefs"
private const val KEY_HOST_IP = "key_laptop_server_ip"
private const val DEFAULT_HOST_IP = "192.168.1.2:4000"

@Composable
fun MainAppContent(
    repository: FirestoreRepository,
    onSignOutRequested: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val sharedPrefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val fileManager = remember { TvFileManager(context) }

    // Host IP configuration (default: 192.168.1.2:4000)
    var hostIpInput by remember {
        mutableStateOf(sharedPrefs.getString(KEY_HOST_IP, DEFAULT_HOST_IP) ?: DEFAULT_HOST_IP)
    }
    var showIpDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Normalize Host address & URL
    val cleanHost = remember(hostIpInput) {
        val raw = hostIpInput.trim().removePrefix("http://").removePrefix("https://").trimEnd('/')
        if (raw.contains(":")) raw else "$raw:4000"
    }
    val baseUrl = "http://$cleanHost"
    val eventsUrl = "$baseUrl/events"

    // Live Socket/SSE Connection State
    var isConnectedToHub by remember { mutableStateOf(false) }
    var connectionStatusText by remember { mutableStateOf("Listening") }

    // Real TV Storage Status
    var storageStatus by remember {
        mutableStateOf(
            StorageStatus(
                usedPercent = 22,
                availableBytes = 12L * 1024L * 1024L * 1024L,
                totalBytes = 16L * 1024L * 1024L * 1024L,
                usedBytes = 4L * 1024L * 1024L * 1024L,
                receivedFilesCount = 0,
                receivedFilesSizeBytes = 0L
            )
        )
    }

    // Refresh real TV storage status
    LaunchedEffect(Unit) {
        storageStatus = fileManager.getStorageStatusAndAutoClean()
        while (isActive) {
            delay(15000L)
            storageStatus = fileManager.getStorageStatusAndAutoClean()
        }
    }

    // Real-time transfer records from Repository
    val allTransfers by repository.observeTransfers().collectAsState(initial = emptyList())

    // De-duplicate transfers strictly by title/filename to prevent duplicate items
    val deduplicatedTransfers = remember(allTransfers) {
        allTransfers.distinctBy { (if (it.fileName.isNotBlank()) it.fileName else it.title).lowercase() }
    }

    // Active incoming notification banner state
    var activeNotification by remember { mutableStateOf<String?>(null) }
    var lastHandledCount by remember { mutableStateOf(deduplicatedTransfers.size) }
    var selectedItemForDetail by remember { mutableStateOf<TransferItem?>(null) }

    // Digital clock
    var timeString by remember { mutableStateOf("12:00 PM") }
    LaunchedEffect(Unit) {
        val fmt = DateTimeFormatter.ofPattern("hh:mm a")
        while (true) {
            timeString = LocalTime.now().format(fmt)
            delay(1000L)
        }
    }

    // ZERO-CLICK AUTO-DOWNLOAD & SSE LISTENER
    LaunchedEffect(eventsUrl) {
        withContext(Dispatchers.IO) {
            val client = OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .connectTimeout(6, TimeUnit.SECONDS)
                .build()

            while (isActive) {
                try {
                    connectionStatusText = "Connecting..."
                    val request = Request.Builder()
                        .url(eventsUrl)
                        .header("Accept", "text/event-stream")
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            isConnectedToHub = true
                            connectionStatusText = "Connected"
                            val stream = response.body?.byteStream()
                            if (stream != null) {
                                val reader = BufferedReader(InputStreamReader(stream))
                                var line: String? = null
                                while (isActive && reader.readLine().also { line = it } != null) {
                                    val currentLine = line?.trim() ?: continue
                                    if (currentLine.startsWith("data:")) {
                                        val jsonString = currentLine.removePrefix("data:").trim()
                                        if (jsonString.isNotBlank() && jsonString.startsWith("{")) {
                                            try {
                                                val json = JSONObject(jsonString)
                                                val sender = json.optString("sender", "phone")
                                                val target = json.optString("target", "all")
                                                val fileName = json.optString("filename", "received_file")
                                                val title = json.optString("title", fileName)
                                                val content = json.optString("content", "Transferred via Local Wi-Fi")
                                                val fileSize = json.optString("filesize", json.optString("size", "1.5 MB"))
                                                val rawUrl = json.optString("url", "")
                                                val downloadUrl = if (rawUrl.startsWith("http")) {
                                                    rawUrl
                                                } else if (rawUrl.isNotBlank()) {
                                                    "$baseUrl$rawUrl"
                                                } else {
                                                    "$baseUrl/download/$fileName"
                                                }

                                                // Auto-trigger download for TV
                                                if (target == "tv" || target == "all") {
                                                    coroutineScope.launch {
                                                        fileManager.downloadOrRedownload(downloadUrl, fileName)
                                                        storageStatus = fileManager.getStorageStatusAndAutoClean()
                                                    }

                                                    val fileType = when {
                                                        fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".jpeg") -> "IMAGE"
                                                        fileName.endsWith(".pdf") || fileName.endsWith(".doc") || fileName.endsWith(".docx") -> "FILE"
                                                        fileName.endsWith(".zip") || fileName.endsWith(".tar") || fileName.endsWith(".gz") -> "FILE"
                                                        fileName.endsWith(".kt") || fileName.endsWith(".py") || fileName.endsWith(".js") || fileName.endsWith(".json") -> "CODE"
                                                        else -> "FILE"
                                                    }

                                                    val destTags = if (target == "all") listOf("All 3 Devices") else listOf("TV")

                                                    repository.pushTransfer(
                                                        title = title,
                                                        content = content,
                                                        type = fileType,
                                                        fileName = fileName,
                                                        fileSize = fileSize,
                                                        devices = destTags,
                                                        pinVerified = true,
                                                        sender = sender,
                                                        target = target,
                                                        downloadUrl = downloadUrl
                                                    )
                                                }
                                            } catch (_: Exception) {}
                                        }
                                    }
                                }
                            }
                        } else {
                            isConnectedToHub = false
                            connectionStatusText = "Standby (HTTP ${response.code})"
                        }
                    }
                } catch (e: Exception) {
                    isConnectedToHub = false
                    connectionStatusText = "Standby (Retrying)"
                }
                delay(5000L)
            }
        }
    }

    // Auto-Alert and Auto-Download Notification Banner
    LaunchedEffect(deduplicatedTransfers.size) {
        if (deduplicatedTransfers.isNotEmpty() && deduplicatedTransfers.size > lastHandledCount) {
            val latest = deduplicatedTransfers.first()
            activeNotification = "Auto-Downloaded: ${latest.title}"
            triggerAlert(context)
            Toast.makeText(context, "Zero-Click Download: ${latest.title}", Toast.LENGTH_SHORT).show()
            storageStatus = fileManager.getStorageStatusAndAutoClean()
            delay(5000L)
            activeNotification = null
        }
        lastHandledCount = deduplicatedTransfers.size
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ULTRA-MINIMALIST TOP BAR (App Brand, Real TV Storage, Settings Button, Clock)
            UltraMinimalistHeader(
                storageStatus = storageStatus,
                timeString = timeString,
                onOpenSettings = { showSettingsDialog = true }
            )

            Divider(color = SlateBorder, thickness = 1.dp)

            // MAIN ULTRA-MINIMALIST TV SCREEN: REDUCED TO 3-4 CORE ELEMENTS
            // Left (approx 45%): Phone QR Scan & Laptop Wi-Fi Hub
            // Right (approx 55%): Latest Received File & Remote Test Simulators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(PureWhite)
                    .padding(18.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // LEFT COLUMN (45%): TRANSFER CHANNELS (PHONE QR & LAPTOP WI-FI)
                LeftTransferChannelsSection(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight(),
                    baseUrl = baseUrl,
                    cleanHost = cleanHost,
                    isConnected = isConnectedToHub,
                    connectionStatus = connectionStatusText,
                    onEditIp = { showIpDialog = true }
                )

                // RIGHT COLUMN (55%): LATEST RECEIVED FILE & REMOTE VERIFICATION
                RightReceivedHeroSection(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight(),
                    latestItem = deduplicatedTransfers.firstOrNull(),
                    onItemClick = { selectedItemForDetail = it },
                    onReDownload = { item ->
                        coroutineScope.launch {
                            Toast.makeText(context, "Re-downloading ${item.fileName}...", Toast.LENGTH_SHORT).show()
                            val success = fileManager.downloadOrRedownload(item.downloadUrl, item.fileName)
                            storageStatus = fileManager.getStorageStatusAndAutoClean()
                            if (success) {
                                Toast.makeText(context, "Re-downloaded ${item.fileName} successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Re-download failed. Check host connection.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onCopy = { text ->
                        clipboardManager.setText(AnnotatedString(text))
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    onSimulateLaptopSend = {
                        coroutineScope.launch {
                            val sampleName = "project_specs.pdf"
                            val dlUrl = "$baseUrl/download/$sampleName"
                            repository.pushTransfer(
                                title = sampleName,
                                content = "Design deliverables, vector tokens, and deployment bundle.",
                                type = "FILE",
                                fileName = sampleName,
                                fileSize = "2.8 MB",
                                devices = listOf("All 3 Devices"),
                                pinVerified = true,
                                sender = "laptop",
                                target = "tv",
                                downloadUrl = dlUrl
                            )
                            fileManager.downloadOrRedownload(dlUrl, sampleName)
                            storageStatus = fileManager.getStorageStatusAndAutoClean()
                        }
                    },
                    onSimulatePhoneSend = {
                        coroutineScope.launch {
                            val sampleName = "camera_snapshot.jpg"
                            val dlUrl = "$baseUrl/download/$sampleName"
                            repository.pushTransfer(
                                title = sampleName,
                                content = "High resolution photo captured on Phone sent to TV.",
                                type = "IMAGE",
                                fileName = sampleName,
                                fileSize = "3.2 MB",
                                devices = listOf("TV", "Phone"),
                                pinVerified = true,
                                sender = "phone",
                                target = "tv",
                                downloadUrl = dlUrl
                            )
                            fileManager.downloadOrRedownload(dlUrl, sampleName)
                            storageStatus = fileManager.getStorageStatusAndAutoClean()
                        }
                    }
                )
            }
        }

        // Notification Banner
        AnimatedVisibility(
            visible = activeNotification != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 20.dp)
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .width(400.dp)
                    .border(1.5.dp, ElectricCyan, RoundedCornerShape(14.dp))
                    .clickable { activeNotification = null }
                    .testTag("notification_banner")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NavySecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Downloaded Successfully",
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = activeNotification ?: "",
                            color = PureWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Host IP Configuration Dialog (Opened via Pencil icon)
        if (showIpDialog) {
            ServerIpDialog(
                currentIp = cleanHost,
                onDismiss = { showIpDialog = false },
                onSave = { newIp ->
                    val clean = newIp.trim()
                    if (clean.isNotBlank()) {
                        hostIpInput = clean
                        sharedPrefs.edit().putString(KEY_HOST_IP, clean).apply()
                        Toast.makeText(context, "Host updated: $clean", Toast.LENGTH_SHORT).show()
                    }
                    showIpDialog = false
                }
            )
        }

        // Unified TV Settings & Information Dialog (Houses History, Storage & Info)
        if (showSettingsDialog) {
            UnifiedSettingsAndHistoryDialog(
                cleanHost = cleanHost,
                storageStatus = storageStatus,
                transfers = deduplicatedTransfers,
                onDismiss = { showSettingsDialog = false },
                onEditIp = {
                    showSettingsDialog = false
                    showIpDialog = true
                },
                onManualClear = {
                    coroutineScope.launch {
                        val count = fileManager.clearAllFiles()
                        storageStatus = fileManager.getStorageStatusAndAutoClean()
                        Toast.makeText(context, "Cleared $count cached files", Toast.LENGTH_SHORT).show()
                    }
                },
                onItemClick = { item ->
                    showSettingsDialog = false
                    selectedItemForDetail = item
                },
                onReDownload = { item ->
                    coroutineScope.launch {
                        fileManager.downloadOrRedownload(item.downloadUrl, item.fileName)
                        storageStatus = fileManager.getStorageStatusAndAutoClean()
                        Toast.makeText(context, "Re-downloaded ${item.fileName}", Toast.LENGTH_SHORT).show()
                    }
                },
                onDelete = { item ->
                    coroutineScope.launch {
                        fileManager.deleteLocalFile(item.fileName)
                        repository.deleteTransfer(item.id)
                        storageStatus = fileManager.getStorageStatusAndAutoClean()
                        Toast.makeText(context, "Removed ${item.title}", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // Detail Dialog
        if (selectedItemForDetail != null) {
            PayloadDetailDialog(
                item = selectedItemForDetail!!,
                baseUrl = baseUrl,
                onDismiss = { selectedItemForDetail = null },
                onReDownload = { item ->
                    coroutineScope.launch {
                        fileManager.downloadOrRedownload(item.downloadUrl, item.fileName)
                        storageStatus = fileManager.getStorageStatusAndAutoClean()
                        Toast.makeText(context, "Re-downloaded ${item.fileName}", Toast.LENGTH_SHORT).show()
                    }
                },
                onCopy = { text ->
                    clipboardManager.setText(AnnotatedString(text))
                    Toast.makeText(context, "Copied to Clipboard", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

private fun triggerAlert(context: Context) {
    try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator?.vibrate(120)
        }
    } catch (_: Exception) {}
}

// ==================== ULTRA-MINIMALIST TOP HEADER ====================
@Composable
private fun UltraMinimalistHeader(
    storageStatus: StorageStatus,
    timeString: String,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PureWhite)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Title & Receiver Tag
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NavyPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "</>",
                    color = PureWhite,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "PVT TV",
                color = CharcoalText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(NavyPrimary)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "RECEIVER",
                    color = PureWhite,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Right side: Real TV Storage Badge + Settings Icon + Clock
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Real TV Storage badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SlateBackground)
                    .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                    .clickable { onOpenSettings() }
                    .focusable()
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("tv_storage_header_badge")
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = NavyPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = storageStatus.headerBadgeText,
                    color = CharcoalText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Settings Icon (Houses History, Storage & Configuration)
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SlateBackground)
                    .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                    .focusable()
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = NavyPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = timeString,
                color = CharcoalText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// ==================== LEFT COLUMN (45%): PHONE QR & LAPTOP WI-FI ====================
@Composable
private fun LeftTransferChannelsSection(
    modifier: Modifier = Modifier,
    baseUrl: String,
    cleanHost: String,
    isConnected: Boolean,
    connectionStatus: String,
    onEditIp: () -> Unit
) {
    val context = LocalContext.current
    val encodedBaseUrl = remember(baseUrl) {
        try {
            URLEncoder.encode(baseUrl, "UTF-8")
        } catch (_: Exception) {
            baseUrl
        }
    }
    val qrApiUrl = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&margin=12&data=$encodedBaseUrl"

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // CARD 1: SEND FROM PHONE (SCAN QR)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.3f)
                .border(1.dp, SlateBorder, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Smartphone,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Send from Phone",
                        color = CharcoalText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(NavySecondary)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Scan QR", color = PureWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // High-Contrast Pure White QR Code
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(PureWhite)
                        .border(1.5.dp, CharcoalText, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(qrApiUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "QR Code for $baseUrl",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(135.dp),
                        loading = {
                            Box(modifier = Modifier.size(135.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = NavyPrimary, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                            }
                        },
                        error = {
                            Box(modifier = Modifier.size(135.dp), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.QrCode, contentDescription = null, tint = CharcoalText, modifier = Modifier.size(54.dp))
                            }
                        }
                    )
                }

                Text(
                    text = "Point camera to send files directly over local Wi-Fi",
                    color = SlateSubtext,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // CARD 2: SEND FROM LAPTOP (WI-FI HUB + PENCIL EDIT)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f)
                .border(1.dp, SlateBorder, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Laptop,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Send from Laptop",
                        color = CharcoalText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) AccentTeal else SlateMuted)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isConnected) "Connected" else "Standby",
                            color = if (isConnected) AccentTeal else SlateSubtext,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = "Open this link in laptop browser to upload files to TV:",
                    color = SlateSubtext,
                    fontSize = 11.sp
                )

                // Host IP Selection with Pencil Icon (Clickable/Focusable)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateBackground)
                        .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                        .clickable { onEditIp() }
                        .focusable()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("host_ip_pencil_button")
                ) {
                    Text(
                        text = "http://$cleanHost",
                        color = CharcoalText,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Host IP",
                        tint = NavyPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

// ==================== RIGHT COLUMN (55%): LATEST RECEIVED FILE & ACTIONS ====================
@Composable
private fun RightReceivedHeroSection(
    modifier: Modifier = Modifier,
    latestItem: TransferItem?,
    onItemClick: (TransferItem) -> Unit,
    onReDownload: (TransferItem) -> Unit,
    onCopy: (String) -> Unit,
    onSimulateLaptopSend: () -> Unit,
    onSimulatePhoneSend: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        modifier = modifier.border(1.dp, SlateBorder, RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(AccentTeal)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RECEIVER ACTIVE",
                        color = NavyPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NavyPrimary)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("Auto-Download On", color = PureWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Main Received File Content Box
            if (latestItem != null) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateBackground),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SlateBorder, RoundedCornerShape(14.dp))
                        .clickable { onItemClick(latestItem) }
                        .focusable()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NavyPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getFileTypeIcon(latestItem.fileName, latestItem.type),
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = latestItem.title,
                                    color = CharcoalText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val senderText = if (latestItem.sender.contains("phone", true) || latestItem.sender.contains("mobile", true)) "From Phone" else "From Laptop"
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(NavyPrimary)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(senderText, color = PureWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = latestItem.fileSize,
                                        color = SlateSubtext,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "• Auto-Downloaded",
                                        color = AccentTeal,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = latestItem.content,
                            color = CharcoalText,
                            fontSize = 12.sp,
                            maxLines = 3,
                            fontFamily = if (latestItem.type == "CODE") FontFamily.Monospace else FontFamily.Default
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Large D-pad remote buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onCopy(latestItem.content) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary),
                                modifier = Modifier.focusable()
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Content", fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Button(
                                onClick = { onReDownload(latestItem) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = PureWhite),
                                modifier = Modifier.focusable()
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Re-Download", fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SlateBackground)
                        .border(1.dp, SlateBorder, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = SlateSubtext,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Waiting for files from Phone or Laptop...",
                            color = SlateSubtext,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Remote Test Simulators (Zero-Click Verified)
            Column {
                Text(
                    text = "One-Touch Remote Simulators (Zero-Click Test)",
                    color = SlateSubtext,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onSimulatePhoneSend,
                        modifier = Modifier
                            .weight(1f)
                            .focusable()
                            .testTag("simulate_phone_send"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary)
                    ) {
                        Icon(Icons.Default.Smartphone, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simulate Phone Send", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onSimulateLaptopSend,
                        modifier = Modifier
                            .weight(1f)
                            .focusable()
                            .testTag("simulate_laptop_send"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary)
                    ) {
                        Icon(Icons.Default.Laptop, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simulate Laptop Send", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ==================== UNIFIED SETTINGS & HISTORY DIALOG ====================
// Neatly houses Transfer History, Real TV Storage, and System Info so main TV screen is ultra-clean!
@Composable
private fun UnifiedSettingsAndHistoryDialog(
    cleanHost: String,
    storageStatus: StorageStatus,
    transfers: List<TransferItem>,
    onDismiss: () -> Unit,
    onEditIp: () -> Unit,
    onManualClear: () -> Unit,
    onItemClick: (TransferItem) -> Unit,
    onReDownload: (TransferItem) -> Unit,
    onDelete: (TransferItem) -> Unit
) {
    var selectedSection by remember { mutableStateOf("History") } // "History" or "Storage & Setup"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            modifier = Modifier
                .width(580.dp)
                .fillMaxHeight(0.85f)
                .border(1.dp, SlateBorder, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TV Settings & History",
                            color = CharcoalText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Section Switcher
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateBackground)
                            .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                            .padding(2.dp)
                    ) {
                        listOf("History", "Storage & Setup").forEach { tab ->
                            val isSel = selectedSection == tab
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) NavyPrimary else Color.Transparent)
                                    .clickable { selectedSection = tab }
                                    .focusable()
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = tab,
                                    color = if (isSel) PureWhite else CharcoalText,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = SlateBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                if (selectedSection == "History") {
                    // Full Transfer History list
                    if (transfers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No received files in history.", color = SlateSubtext, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(transfers, key = { it.id.ifBlank { it.title } }) { item ->
                                HistoryRowItem(
                                    item = item,
                                    onClick = { onItemClick(item) },
                                    onReDownload = { onReDownload(item) },
                                    onDelete = { onDelete(item) }
                                )
                            }
                        }
                    }
                } else {
                    // Storage & Setup Section
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Real Storage Box
                        Text(
                            text = "Real Android TV Storage",
                            color = CharcoalText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = SlateBackground),
                            modifier = Modifier.fillMaxWidth().border(1.dp, SlateBorder, RoundedCornerShape(10.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total TV Storage:", color = SlateSubtext, fontSize = 11.sp)
                                    Text(storageStatus.formattedTotal, color = CharcoalText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Available Free Space:", color = SlateSubtext, fontSize = 11.sp)
                                    Text(storageStatus.formattedAvailable, color = NavyPrimary, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Usage Capacity:", color = SlateSubtext, fontSize = 11.sp)
                                    Text("${storageStatus.usedPercent}% (Auto-Clean at 90%)", color = CharcoalText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            OutlinedButton(
                                onClick = onManualClear,
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary),
                                modifier = Modifier.focusable()
                            ) {
                                Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear Cache Now", fontSize = 11.sp)
                            }
                        }

                        // Host IP Config
                        Text(
                            text = "Host Hub IP Address",
                            color = CharcoalText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlateBackground)
                                .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                                .clickable { onEditIp() }
                                .focusable()
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "http://$cleanHost",
                                color = CharcoalText,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = NavyPrimary, modifier = Modifier.size(15.dp))
                        }

                        // Sideload info
                        Text(
                            text = "Wireless Sideload Guide",
                            color = CharcoalText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• Install LocalSend on TV via Google Play or Downloader app.\n" +
                                   "• Send any APK or files directly to TV over Wi-Fi without USB cables.",
                            color = SlateSubtext,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = PureWhite),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.focusable()
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRowItem(
    item: TransferItem,
    onClick: () -> Unit,
    onReDownload: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SlateBackground),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SlateBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .focusable()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(NavyPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getFileTypeIcon(item.fileName, item.type),
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = CharcoalText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val sText = if (item.sender.contains("phone", true) || item.sender.contains("mobile", true)) "Phone" else "Laptop"
                    Text(
                        text = "From $sText",
                        color = NavyPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${item.fileSize}",
                        color = SlateSubtext,
                        fontSize = 10.sp
                    )
                }
            }

            IconButton(
                onClick = onReDownload,
                modifier = Modifier.size(30.dp).focusable()
            ) {
                Icon(Icons.Default.Download, contentDescription = "Re-download", tint = NavyPrimary, modifier = Modifier.size(15.dp))
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(30.dp).focusable()
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SlateSubtext, modifier = Modifier.size(15.dp))
            }
        }
    }
}

private fun getFileTypeIcon(fileName: String, type: String): ImageVector {
    val lower = fileName.lowercase()
    return when {
        lower.endsWith(".pdf") -> Icons.Default.PictureAsPdf
        lower.endsWith(".jpg") || lower.endsWith(".png") || lower.endsWith(".jpeg") || type == "IMAGE" -> Icons.Default.Image
        lower.endsWith(".zip") || lower.endsWith(".tar") || lower.endsWith(".gz") -> Icons.Default.FolderZip
        lower.endsWith(".kt") || lower.endsWith(".py") || lower.endsWith(".js") || lower.endsWith(".json") || type == "CODE" -> Icons.Default.Code
        else -> Icons.Default.Folder
    }
}

// ==================== DETAIL DIALOG ====================
@Composable
private fun PayloadDetailDialog(
    item: TransferItem,
    baseUrl: String,
    onDismiss: () -> Unit,
    onReDownload: (TransferItem) -> Unit,
    onCopy: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            modifier = Modifier
                .width(480.dp)
                .border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        color = CharcoalText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NavyPrimary)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(item.type, color = PureWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val senderName = if (item.sender.contains("phone", true) || item.sender.contains("mobile", true)) "PHONE" else "LAPTOP"
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("From: $senderName", color = NavyPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("• Target: TV", color = SlateSubtext, fontSize = 11.sp)
                    if (item.fileSize.isNotBlank()) {
                        Text("• ${item.fileSize}", color = SlateMuted, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                val dlUrl = if (item.downloadUrl.isNotBlank()) item.downloadUrl else "$baseUrl/download/${item.fileName.ifBlank { "file" }}"
                Text(
                    text = "URL: $dlUrl",
                    color = SlateSubtext,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateBackground)
                        .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = item.content,
                        color = CharcoalText,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontFamily = if (item.type == "CODE") FontFamily.Monospace else FontFamily.Default
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SlateBackground, contentColor = SlateSubtext),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.focusable()
                    ) {
                        Text("Close")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onReDownload(item) },
                        colors = ButtonDefaults.buttonColors(containerColor = NavySecondary, contentColor = PureWhite),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.focusable()
                    ) {
                        Text("Re-Download")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onCopy(item.content)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = PureWhite),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.focusable()
                    ) {
                        Text("Copy Content")
                    }
                }
            }
        }
    }
}

// ==================== HOST IP DIALOG ====================
@Composable
private fun ServerIpDialog(
    currentIp: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var ipInput by remember { mutableStateOf(currentIp) }
    val presets = listOf("192.168.1.2:4000", "192.168.1.10:4000", "192.168.0.105:4000", "10.0.2.2:4000")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            modifier = Modifier
                .width(440.dp)
                .border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Configure Host IP & Port",
                    color = CharcoalText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Enter computer or phone IP on same Wi-Fi. QR code updates automatically.",
                    color = SlateSubtext,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = ipInput,
                    onValueChange = { ipInput = it },
                    label = { Text("Host IP & Port") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("host_ip_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NavyPrimary,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = CharcoalText,
                        unfocusedTextColor = CharcoalText
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Quick Presets:", color = SlateSubtext, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (ipInput == preset) NavyPrimary else SlateBackground)
                                .border(1.dp, SlateBorder, RoundedCornerShape(6.dp))
                                .clickable { ipInput = preset }
                                .focusable()
                                .padding(horizontal = 6.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = preset,
                                color = if (ipInput == preset) PureWhite else CharcoalText,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SlateBackground, contentColor = SlateSubtext),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.focusable()
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(ipInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = PureWhite),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.focusable()
                    ) {
                        Text("Save & Update")
                    }
                }
            }
        }
    }
}
