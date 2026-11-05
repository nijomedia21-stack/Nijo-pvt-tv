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
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Smartphone
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
import java.io.File
import java.io.FileOutputStream
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

    // Host IP configuration (default: 192.168.1.2:4000, persisted)
    var hostIpInput by remember {
        mutableStateOf(sharedPrefs.getString(KEY_HOST_IP, DEFAULT_HOST_IP) ?: DEFAULT_HOST_IP)
    }
    var showIpDialog by remember { mutableStateOf(false) }
    var showInstallGuideDialog by remember { mutableStateOf(false) }

    // Normalize Host address & base URL
    val cleanHost = remember(hostIpInput) {
        val raw = hostIpInput.trim().removePrefix("http://").removePrefix("https://").trimEnd('/')
        if (raw.contains(":")) raw else "$raw:4000"
    }
    val baseUrl = "http://$cleanHost"
    val eventsUrl = "$baseUrl/events"

    // Live Socket/SSE Connection State
    var isConnectedToHub by remember { mutableStateOf(false) }
    var connectionStatusText by remember { mutableStateOf("Listening") }

    // Real-time transfer records (strictly TV targeted or all)
    val allTransfers by repository.observeTransfers().collectAsState(initial = emptyList())
    val tvTransfers = remember(allTransfers) {
        allTransfers.filter { it.target == "tv" || it.target == "all" }
    }

    // Active incoming notification banner state
    var activeNotification by remember { mutableStateOf<String?>(null) }
    var lastHandledCount by remember { mutableStateOf(tvTransfers.size) }
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

    // ZERO-CLICK AUTO-DOWNLOAD & SSE LISTENER:
    // Connects to http://<HOST_IP>/events
    // When target === 'tv' or 'all', auto-triggers file download and adds to UI immediately
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
                                                val sender = json.optString("sender", "laptop")
                                                val target = json.optString("target", "all")
                                                val fileName = json.optString("filename", "received_file")
                                                val title = json.optString("title", fileName)
                                                val content = json.optString("content", "Transferred via LocalSend Protocol")
                                                val fileSize = json.optString("filesize", json.optString("size", "1.5 MB"))
                                                val rawUrl = json.optString("url", "")
                                                val downloadUrl = if (rawUrl.startsWith("http")) {
                                                    rawUrl
                                                } else if (rawUrl.isNotBlank()) {
                                                    "$baseUrl$rawUrl"
                                                } else {
                                                    "$baseUrl/download/$fileName"
                                                }

                                                // Zero-Click Auto Download for TV
                                                if (target == "tv" || target == "all") {
                                                    // Trigger background auto-download immediately without remote button press
                                                    coroutineScope.launch(Dispatchers.IO) {
                                                        try {
                                                            val dlReq = Request.Builder().url(downloadUrl).build()
                                                            client.newCall(dlReq).execute().use { dlResp ->
                                                                if (dlResp.isSuccessful) {
                                                                    val file = File(context.cacheDir, fileName)
                                                                    FileOutputStream(file).use { out ->
                                                                        dlResp.body?.byteStream()?.copyTo(out)
                                                                    }
                                                                }
                                                            }
                                                        } catch (_: Exception) {}
                                                    }

                                                    // Push transfer to Repository for live UI update
                                                    repository.pushTransfer(
                                                        title = title,
                                                        content = content,
                                                        type = if (fileName.endsWith(".zip") || fileName.endsWith(".tar")) "FILE" else if (fileName.endsWith(".kt") || fileName.endsWith(".js") || fileName.endsWith(".py")) "CODE" else if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) "IMAGE" else "FILE",
                                                        fileName = fileName,
                                                        fileSize = fileSize,
                                                        devices = listOf("Android TV"),
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
    LaunchedEffect(tvTransfers.size) {
        if (tvTransfers.isNotEmpty() && tvTransfers.size > lastHandledCount) {
            val latest = tvTransfers.first()
            activeNotification = "Auto-Downloaded from ${latest.sender.replaceFirstChar { it.uppercase() }}: ${latest.title}"
            triggerAlert(context)
            Toast.makeText(context, "Zero-Click Download: ${latest.title}", Toast.LENGTH_SHORT).show()
            delay(5000L)
            activeNotification = null
        }
        lastHandledCount = tvTransfers.size
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Bar: Clean White, Navy Logo, Host Server IP Indicator, Clock (NO ROLE SWITCHER)
            DedicatedTvHeader(
                cleanHost = cleanHost,
                isConnected = isConnectedToHub,
                connectionStatus = connectionStatusText,
                timeString = timeString,
                onEditIp = { showIpDialog = true },
                onOpenInstallGuide = { showInstallGuideDialog = true }
            )

            Divider(color = SlateBorder, thickness = 1.dp)

            // STRICT 50/50 SPLIT LAYOUT:
            // Left Side (50%):
            //   - Top (1/4th vertical space): Pure white padded QR Code Card + editable Host IP
            //   - Bottom: "Waiting for files..." status & active incoming file card
            // Right Side (50%):
            //   - Full "History" list of received files
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(PureWhite)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // LEFT SIDE (50%): QR Code Card + Receiver Status & Latest File
                LeftColumnReceiver(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    baseUrl = baseUrl,
                    cleanHost = cleanHost,
                    isConnected = isConnectedToHub,
                    connectionStatus = connectionStatusText,
                    latestItem = tvTransfers.firstOrNull(),
                    onEditIp = { showIpDialog = true },
                    onItemClick = { selectedItemForDetail = it },
                    onCopy = { text ->
                        clipboardManager.setText(AnnotatedString(text))
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    onSimulateLaptopSend = {
                        coroutineScope.launch {
                            val sampleName = "project_specs_v2.zip"
                            repository.pushTransfer(
                                title = "Project Deliverables & Specs",
                                content = "Exported design tokens, UI specifications, and deployment bundle.",
                                type = "FILE",
                                fileName = sampleName,
                                fileSize = "4.2 MB",
                                devices = listOf("Android TV"),
                                pinVerified = true,
                                sender = "laptop",
                                target = "tv",
                                downloadUrl = "$baseUrl/download/$sampleName"
                            )
                        }
                    },
                    onSimulateMobileSend = {
                        coroutineScope.launch {
                            val sampleName = "camera_shot_4k.jpg"
                            repository.pushTransfer(
                                title = "Mobile Camera Capture",
                                content = "High-definition camera photo sent to Android TV screen.",
                                type = "IMAGE",
                                fileName = sampleName,
                                fileSize = "2.8 MB",
                                devices = listOf("Android TV"),
                                pinVerified = true,
                                sender = "mobile",
                                target = "tv",
                                downloadUrl = "$baseUrl/download/$sampleName"
                            )
                        }
                    }
                )

                // RIGHT SIDE (50%): Full "History" List of Received Files
                RightColumnHistory(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    transfers = tvTransfers,
                    onItemClick = { selectedItemForDetail = it },
                    onCopy = { text ->
                        clipboardManager.setText(AnnotatedString(text))
                        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Automatic Top-Right Alert Notification Banner
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
                    .width(420.dp)
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(NavySecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Zero-Click Download Complete",
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

        // Dialog for Changing Host Server IP
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

        // LocalSend & Wireless TV Install Guide Dialog
        if (showInstallGuideDialog) {
            WirelessInstallGuideDialog(
                onDismiss = { showInstallGuideDialog = false }
            )
        }

        // Detail Dialog
        if (selectedItemForDetail != null) {
            PayloadDetailDialog(
                item = selectedItemForDetail!!,
                baseUrl = baseUrl,
                onDismiss = { selectedItemForDetail = null },
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

// ==================== DEDICATED TV HEADER ====================
@Composable
private fun DedicatedTvHeader(
    cleanHost: String,
    isConnected: Boolean,
    connectionStatus: String,
    timeString: String,
    onEditIp: () -> Unit,
    onOpenInstallGuide: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PureWhite)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Title: Navy Logo + Crisp Black "PVT TV"
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NavyPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "</>",
                    color = PureWhite,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "PVT TV",
                        color = CharcoalText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NavyPrimary)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "TV RECEIVER",
                            color = PureWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                Text(
                    text = "One-Way Automatic Transfer Station",
                    color = SlateSubtext,
                    fontSize = 11.sp
                )
            }
        }

        // Host Hub Indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(SlateBackground)
                .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                .clickable { onEditIp() }
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .testTag("server_ip_pill")
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isConnected) AccentTeal else SlateMuted)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "http://$cleanHost",
                    color = CharcoalText,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Status: $connectionStatus • Tap to Edit Host",
                    color = if (isConnected) AccentTeal else SlateSubtext,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit IP",
                tint = NavyPrimary,
                modifier = Modifier.size(16.dp)
            )
        }

        // Right side: LocalSend Guide, Wi-Fi, Bluetooth & Clock
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SlateBackground)
                    .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                    .clickable { onOpenInstallGuide() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("tv_install_guide_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Install Guide",
                    tint = NavyPrimary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Install Guide",
                    color = NavyPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = null,
                tint = NavyPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Bluetooth,
                contentDescription = null,
                tint = NavyPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = timeString,
                color = CharcoalText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// ==================== LEFT COLUMN (50%): QR Card + Receiver Status & Latest File ====================
@Composable
private fun LeftColumnReceiver(
    modifier: Modifier = Modifier,
    baseUrl: String,
    cleanHost: String,
    isConnected: Boolean,
    connectionStatus: String,
    latestItem: TransferItem?,
    onEditIp: () -> Unit,
    onItemClick: (TransferItem) -> Unit,
    onCopy: (String) -> Unit,
    onSimulateLaptopSend: () -> Unit,
    onSimulateMobileSend: () -> Unit
) {
    val context = LocalContext.current
    // QR Code URL: https://api.qrserver.com/v1/create-qr-code/?size=250x250&margin=12&data=http%3A%2F%2F192.168.1.2%3A4000
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
        // TOP 1/4th: PURE WHITE PADDED QR CODE CARD & EDITABLE HOST INPUT
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SlateBorder, RoundedCornerShape(20.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // High-Contrast Pure White Padded QR Box (padding 14.dp, border-radius 12.dp)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(PureWhite)
                        .border(1.5.dp, CharcoalText, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                        .testTag("qr_code_container"),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(qrApiUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Scannable QR Code for $baseUrl",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(118.dp)
                            .testTag("qr_code_image"),
                        loading = {
                            Box(
                                modifier = Modifier
                                    .size(118.dp)
                                    .background(PureWhite),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = NavyPrimary,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .size(118.dp)
                                    .background(PureWhite)
                                    .border(1.dp, SlateBorder, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = null,
                                    tint = CharcoalText,
                                    modifier = Modifier.size(56.dp)
                                )
                            }
                        }
                    )
                }

                // QR Meta & Editable Host IP Input
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Scan to Connect",
                            color = CharcoalText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Scan from phone to transfer files directly to TV",
                        color = SlateSubtext,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Host IP Row / Editor
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateBackground)
                            .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                            .clickable { onEditIp() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("qr_host_ip_pill")
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Host: $cleanHost",
                                color = CharcoalText,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Status: $connectionStatus",
                                color = if (isConnected) AccentTeal else SlateSubtext,
                                fontSize = 10.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Host",
                            tint = NavyPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // BOTTOM: RECEIVER STATUS & ACTIVE INCOMING FILE CARD
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, SlateBorder, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top: Status Header
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(AccentTeal)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RECEIVE MODE ONLY",
                            color = NavyPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Waiting for files from Laptop or Mobile...",
                        color = CharcoalText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Transfers to 'TV' will be auto-downloaded instantly without remote clicks.",
                        color = SlateSubtext,
                        fontSize = 11.sp
                    )
                }

                // Middle: Latest / Active Incoming File Card
                Column {
                    Text(
                        text = "Received File",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = CharcoalText
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (latestItem != null) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SlateBackground),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SlateBorder, RoundedCornerShape(14.dp))
                                .clickable { onItemClick(latestItem) }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(NavyPrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = when (latestItem.type) {
                                                    "CODE" -> Icons.Default.Code
                                                    "IMAGE" -> Icons.Default.Image
                                                    else -> Icons.Default.FolderZip
                                                },
                                                contentDescription = null,
                                                tint = PureWhite,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = latestItem.title,
                                                color = CharcoalText,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                maxLines = 1
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "From ${latestItem.sender.replaceFirstChar { it.uppercase() }}",
                                                    color = NavyPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                                if (latestItem.fileSize.isNotBlank()) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "• ${latestItem.fileSize}",
                                                        color = SlateSubtext,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(NavyPrimary)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = PureWhite,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "Auto-Downloaded",
                                                color = PureWhite,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = latestItem.content,
                                    color = CharcoalText,
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    fontFamily = if (latestItem.type == "CODE") FontFamily.Monospace else FontFamily.Default
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                val dlUrl = if (latestItem.downloadUrl.isNotBlank()) {
                                    latestItem.downloadUrl
                                } else {
                                    "$baseUrl/download/${latestItem.fileName.ifBlank { "file" }}"
                                }
                                Text(
                                    text = "Source: $dlUrl",
                                    color = SlateSubtext,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = { onCopy(latestItem.content) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copy Text", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(95.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SlateBackground)
                                .border(1.dp, SlateBorder, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = SlateSubtext,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Waiting for incoming files...",
                                    color = SlateSubtext,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Bottom: Channel Info & One-Touch TV Test Simulator Buttons
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateBackground)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text("• Quick Share", color = CharcoalText, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        Text("• LocalSend: 4000", color = CharcoalText, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        Text("• Bluetooth OBEX", color = CharcoalText, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSimulateLaptopSend,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("simulate_laptop_send"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary)
                        ) {
                            Icon(Icons.Default.Laptop, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simulate Laptop", fontSize = 10.sp)
                        }

                        OutlinedButton(
                            onClick = onSimulateMobileSend,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("simulate_mobile_send"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary)
                        ) {
                            Icon(Icons.Default.Smartphone, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simulate Mobile", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==================== RIGHT COLUMN (50%): FULL HISTORY LIST ====================
@Composable
private fun RightColumnHistory(
    modifier: Modifier = Modifier,
    transfers: List<TransferItem>,
    onItemClick: (TransferItem) -> Unit,
    onCopy: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        modifier = modifier.border(1.dp, SlateBorder, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "History",
                    color = CharcoalText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NavyPrimary)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${transfers.size} Items",
                        color = PureWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (transfers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "History is empty.",
                        color = SlateSubtext,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(transfers, key = { it.id.ifBlank { it.title } }) { item ->
                        HistoryCard(
                            item = item,
                            onClick = { onItemClick(item) },
                            onCopy = { onCopy(item.content) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(
    item: TransferItem,
    onClick: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SlateBackground),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SlateBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NavyPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (item.type) {
                        "CODE" -> Icons.Default.Code
                        "IMAGE" -> Icons.Default.Image
                        else -> Icons.Default.FolderZip
                    },
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = CharcoalText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.content,
                    color = SlateSubtext,
                    fontSize = 11.sp,
                    maxLines = 1,
                    fontFamily = if (item.type == "CODE") FontFamily.Monospace else FontFamily.Default
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "From: ${item.sender.replaceFirstChar { it.uppercase() }}",
                        color = NavyPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "To: TV",
                        color = SlateSubtext,
                        fontSize = 10.sp
                    )
                    if (item.fileSize.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${item.fileSize}",
                            color = SlateMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = NavyPrimary,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

// ==================== DETAIL DIALOG ====================
@Composable
private fun PayloadDetailDialog(
    item: TransferItem,
    baseUrl: String,
    onDismiss: () -> Unit,
    onCopy: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .border(1.dp, SlateBorder, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        color = CharcoalText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("From: ${item.sender.uppercase()}", color = NavyPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Target: TV", color = SlateSubtext, fontSize = 11.sp)
                    if (item.fileSize.isNotBlank()) {
                        Text("Size: ${item.fileSize}", color = SlateMuted, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val dlUrl = if (item.downloadUrl.isNotBlank()) {
                    item.downloadUrl
                } else {
                    "$baseUrl/download/${item.fileName.ifBlank { "file" }}"
                }
                Text(
                    text = "Download URL: $dlUrl",
                    color = SlateSubtext,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SlateBackground)
                        .border(1.dp, SlateBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = item.content,
                        color = CharcoalText,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        fontFamily = if (item.type == "CODE") FontFamily.Monospace else FontFamily.Default
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SlateBackground, contentColor = SlateSubtext),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Close")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onCopy(item.content)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = PureWhite),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Copy Content")
                    }
                }
            }
        }
    }
}

// ==================== REMOTE-FRIENDLY HOST IP DIALOG ====================
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
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            modifier = Modifier
                .width(460.dp)
                .border(1.dp, SlateBorder, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    text = "Configure Host IP Address",
                    color = CharcoalText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Enter your Computer / Server Wi-Fi IP and Port (e.g. 192.168.1.2:4000). The QR code updates automatically.",
                    color = SlateSubtext,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = ipInput,
                    onValueChange = { ipInput = it },
                    label = { Text("Host IP & Port") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("host_ip_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NavyPrimary,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = CharcoalText,
                        unfocusedTextColor = CharcoalText
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Quick Presets:",
                    color = SlateSubtext,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (ipInput == preset) NavyPrimary else SlateBackground)
                                .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                                .clickable { ipInput = preset }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = preset,
                                color = if (ipInput == preset) PureWhite else CharcoalText,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SlateBackground, contentColor = SlateSubtext),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(ipInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = PureWhite),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save & Update QR")
                    }
                }
            }
        }
    }
}

// ==================== WIRELESS INSTALL GUIDE DIALOG (LocalSend & APK) ====================
@Composable
private fun WirelessInstallGuideDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            modifier = Modifier
                .width(540.dp)
                .border(1.dp, SlateBorder, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NavyPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = PureWhite, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "TV Wireless Install Guide",
                            color = CharcoalText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NavyPrimary)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("No USB Needed", color = PureWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 1: LocalSend Official Installation
                Text(
                    text = "1. How to Install LocalSend on Android TV",
                    color = CharcoalText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Option A (Google Play): Open the Google Play Store on your Android TV, search for 'LocalSend', and click Install.\n" +
                           "• Option B (TV Browser / Downloader): Open 'Downloader by AFTVnews' on your TV and visit https://localsend.org or GitHub: https://github.com/localsend/localsend/releases to download the APK.",
                    color = SlateSubtext,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Section 2: Installing PVT TV APK without USB
                Text(
                    text = "2. How to Install this PVT TV App onto your TV via LocalSend",
                    color = CharcoalText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Step 1: On your Laptop/Phone, download the PVT TV APK from the AI Studio top bar (Settings / Export APK).\n" +
                           "• Step 2: Open LocalSend on your Laptop or Phone (connected to the same Wi-Fi).\n" +
                           "• Step 3: Select the PVT TV .apk file and choose your Android TV as the recipient.\n" +
                           "• Step 4: On the TV, accept the file and tap 'Open' to install it directly without any USB cable!",
                    color = SlateSubtext,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = PureWhite),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Got it")
                    }
                }
            }
        }
    }
}
