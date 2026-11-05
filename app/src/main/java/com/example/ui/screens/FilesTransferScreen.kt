package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.TransferItem
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.SlateSubtext

private const val DEVICE_LAPTOP = "Laptop (Windows 11)"
private const val DEVICE_MOBILE = "Mobile (Android)"
private const val DEVICE_TV = "Smart TV (Living Room)"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilesTransferScreen(
    transfers: List<TransferItem>,
    onPushTransfer: (title: String, content: String, type: String, devices: List<String>, fileName: String, fileSize: String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var clipboardText by remember { mutableStateOf("") }
    val selectedDevices = remember { mutableStateListOf(DEVICE_LAPTOP, DEVICE_MOBILE, DEVICE_TV) }
    var showSendFileDialog by remember { mutableStateOf(false) }

    fun setPreset(preset: String) {
        selectedDevices.clear()
        when (preset) {
            "Laptop Only" -> selectedDevices.add(DEVICE_LAPTOP)
            "Mobile Only" -> selectedDevices.add(DEVICE_MOBILE)
            "TV Only" -> selectedDevices.add(DEVICE_TV)
            "All 3 Devices" -> {
                selectedDevices.addAll(listOf(DEVICE_LAPTOP, DEVICE_MOBILE, DEVICE_TV))
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 1. Local Transfer Mesh Status Card (Wi-Fi Direct and QuickShare Ready)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mesh_status_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NavySecondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Hub,
                                    contentDescription = "Mesh",
                                    tint = PureWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Local Transfer Mesh",
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Wi-Fi Direct & QuickShare Ready",
                                    color = ElectricCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Connected indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(NavySecondary)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(AccentTeal)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mesh Active",
                                color = PureWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3 Device Nodes Indicator Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DeviceNodeItem(
                            icon = Icons.Default.Laptop,
                            name = "Laptop",
                            sub = "Win 11 Pro",
                            isOnline = true
                        )
                        DeviceNodeItem(
                            icon = Icons.Default.Smartphone,
                            name = "Mobile",
                            sub = "Android 14",
                            isOnline = true
                        )
                        DeviceNodeItem(
                            icon = Icons.Default.Tv,
                            name = "Smart TV",
                            sub = "Living Room",
                            isOnline = true
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showSendFileDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("open_send_file_dialog_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Send File / QuickShare Payload",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 2. Universal Clipboard Section
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorder, RoundedCornerShape(18.dp))
                    .testTag("universal_clipboard_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = "Clipboard",
                            tint = NavyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Universal Clipboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NavyPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Type, paste, or push code snippets, links, or text notes across linked surfaces",
                        fontSize = 12.sp,
                        color = SlateSubtext
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = clipboardText,
                        onValueChange = { clipboardText = it },
                        placeholder = {
                            Text(
                                text = "Enter or paste text, URLs, Python snippets, or client notes...",
                                fontSize = 13.sp,
                                color = SlateMuted
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("universal_clipboard_textfield"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = SlateBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Multi-Select Checkbox Group for Device Routing
                    Text(
                        text = "Device Routing (Multi-Select):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = CharcoalText
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Column {
                        listOf(DEVICE_LAPTOP, DEVICE_MOBILE, DEVICE_TV).forEach { deviceName ->
                            val isChecked = selectedDevices.contains(deviceName)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked) {
                                            if (selectedDevices.size > 1) selectedDevices.remove(deviceName)
                                        } else {
                                            selectedDevices.add(deviceName)
                                        }
                                    }
                                    .padding(vertical = 2.dp)
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = {
                                        if (isChecked) {
                                            if (selectedDevices.size > 1) selectedDevices.remove(deviceName)
                                        } else {
                                            selectedDevices.add(deviceName)
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = NavyPrimary,
                                        checkmarkColor = PureWhite
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = deviceName,
                                    fontSize = 13.sp,
                                    color = CharcoalText,
                                    fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Preset Chips: "Laptop Only", "Mobile Only", "TV Only", "All 3 Devices"
                    Text(
                        text = "Quick Presets:",
                        fontSize = 11.sp,
                        color = SlateSubtext,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Laptop Only", "Mobile Only", "TV Only", "All 3 Devices").forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SlateBackground)
                                    .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                                    .clickable { setPreset(preset) }
                                    .focusable()
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                                    .testTag("preset_$preset")
                            ) {
                                Text(preset, fontSize = 11.sp, color = NavyPrimary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons: "Paste from Clipboard", "Clear", "Push to Selected"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clip = clipboardManager.getText()?.text
                                if (!clip.isNullOrBlank()) {
                                    clipboardText = clip
                                    Toast.makeText(context, "Pasted from system clipboard", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("paste_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Paste", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { clipboardText = "" },
                            modifier = Modifier
                                .weight(0.7f)
                                .height(44.dp)
                                .testTag("clear_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (clipboardText.isNotBlank()) {
                                    clipboardManager.setText(AnnotatedString(clipboardText))
                                    onPushTransfer(
                                        clipboardText.take(40).replace("\n", " "),
                                        clipboardText,
                                        if (clipboardText.contains("{") || clipboardText.contains("def ")) "CODE" else "CLIPBOARD",
                                        selectedDevices.toList(),
                                        "",
                                        "${clipboardText.length} bytes"
                                    )
                                    Toast.makeText(context, "Pushed to ${selectedDevices.size} devices!", Toast.LENGTH_SHORT).show()
                                    clipboardText = ""
                                }
                            },
                            enabled = clipboardText.isNotBlank() && selectedDevices.isNotEmpty(),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(44.dp)
                                .testTag("push_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NavyPrimary,
                                contentColor = PureWhite
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Push to Selected", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 3. Unified Transfer History List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transfer History & Synced Payloads",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NavyPrimary
                )
                Text(
                    text = "${transfers.size} Items",
                    fontSize = 12.sp,
                    color = SlateSubtext
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (transfers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(PureWhite)
                        .border(1.dp, SlateBorder, RoundedCornerShape(14.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transfers yet. Push a clipboard snippet or send a file above.",
                        color = SlateSubtext,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            items(transfers, key = { it.id.ifBlank { it.title } }) { item ->
                TransferHistoryItemCard(
                    item = item,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(item.content))
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(90.dp))
        }
    }

    if (showSendFileDialog) {
        SendFileDialog(
            onDismiss = { showSendFileDialog = false },
            onSend = { title, content, type, devices, fileName, fileSize ->
                onPushTransfer(title, content, type, devices, fileName, fileSize)
                showSendFileDialog = false
                Toast.makeText(context, "File transmitted across mesh!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun DeviceNodeItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    name: String,
    sub: String,
    isOnline: Boolean
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(NavySecondary)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = name,
            tint = ElectricCyan,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(name, color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(sub, color = SlateBorder, fontSize = 9.sp)
        }
    }
}

@Composable
private fun TransferHistoryItemCard(
    item: TransferItem,
    onCopy: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, SlateBorder, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NavyPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (item.type) {
                        "CODE" -> Icons.Default.Code
                        "FILE" -> Icons.Default.FolderZip
                        "MEDIA" -> Icons.Default.Image
                        else -> Icons.Default.Assignment
                    },
                    contentDescription = item.type,
                    tint = PureWhite,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = CharcoalText,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.content,
                    fontSize = 11.sp,
                    color = SlateSubtext,
                    maxLines = 2,
                    fontFamily = if (item.type == "CODE") FontFamily.Monospace else FontFamily.Default
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item.devices.forEach { dev ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SlateBackground)
                                .border(1.dp, SlateBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = dev.split(" ").first(),
                                fontSize = 9.sp,
                                color = NavyPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    if (item.fileSize.isNotBlank()) {
                        Text("• ${item.fileSize}", fontSize = 10.sp, color = SlateMuted)
                    }
                }
            }

            IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Content",
                    tint = NavyPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SendFileDialog(
    onDismiss: () -> Unit,
    onSend: (title: String, content: String, type: String, devices: List<String>, fileName: String, fileSize: String) -> Unit
) {
    var fileName by remember { mutableStateOf("libra_brand_identity_kit.zip") }
    var fileSize by remember { mutableStateOf("4.2 MB") }
    var fileType by remember { mutableStateOf("FILE") }
    val selectedDevices = remember { mutableStateListOf(DEVICE_LAPTOP, DEVICE_MOBILE, DEVICE_TV) }

    fun setDialogPreset(preset: String) {
        selectedDevices.clear()
        when (preset) {
            "Laptop Only" -> selectedDevices.add(DEVICE_LAPTOP)
            "Mobile Only" -> selectedDevices.add(DEVICE_MOBILE)
            "TV Only" -> selectedDevices.add(DEVICE_TV)
            "All 3 Devices" -> selectedDevices.addAll(listOf(DEVICE_LAPTOP, DEVICE_MOBILE, DEVICE_TV))
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            modifier = Modifier.fillMaxWidth().border(1.dp, SlateBorder, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "QuickShare File Payload",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = NavyPrimary
                )
                Text(
                    text = "Select simulated or real files for high-speed local Wi-Fi Direct sync",
                    fontSize = 11.sp,
                    color = SlateSubtext
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("File Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = fileSize,
                    onValueChange = { fileSize = it },
                    label = { Text("Size") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Device Routing Checkboxes in Dialog
                Text("Target Device Routing:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = CharcoalText)
                Spacer(modifier = Modifier.height(4.dp))

                Column {
                    listOf(DEVICE_LAPTOP, DEVICE_MOBILE, DEVICE_TV).forEach { dev ->
                        val isChecked = selectedDevices.contains(dev)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) {
                                        if (selectedDevices.size > 1) selectedDevices.remove(dev)
                                    } else selectedDevices.add(dev)
                                }
                                .padding(vertical = 2.dp)
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    if (isChecked) {
                                        if (selectedDevices.size > 1) selectedDevices.remove(dev)
                                    } else selectedDevices.add(dev)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = NavyPrimary)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(dev, fontSize = 12.sp, color = CharcoalText)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Quick presets
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Laptop Only", "Mobile Only", "TV Only", "All 3 Devices").forEach { p ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SlateBackground)
                                .clickable { setDialogPreset(p) }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(p, fontSize = 10.sp, color = NavyPrimary)
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
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (fileName.isNotBlank()) {
                                onSend(fileName, "QuickShare transfer payload ready for sync", fileType, selectedDevices.toList(), fileName, fileSize)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = PureWhite),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Send to Selected")
                    }
                }
            }
        }
    }
}
