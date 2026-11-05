package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSubtext

@Composable
fun SettingsDialog(
    currentPin: String,
    onSavePin: (String) -> Unit,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit
) {
    var pinInput by remember { mutableStateOf(currentPin) }
    var pinSavedFeedback by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SlateBorder, RoundedCornerShape(24.dp))
                .testTag("settings_system_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Dialog Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(NavyPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Security",
                            tint = PureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Settings & Security",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = NavyPrimary
                        )
                        Text(
                            text = "NIJO PVT TV System Hub",
                            fontSize = 12.sp,
                            color = SlateSubtext
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = SlateBorder)
                Spacer(modifier = Modifier.height(16.dp))

                // Target User Profile Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SlateBackground)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User",
                        tint = NavyPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Nijo (Polytechnic AI & ML)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = CharcoalText
                        )
                        Text(
                            text = "nijo.media21@gmail.com",
                            fontSize = 12.sp,
                            color = SlateSubtext
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4-Digit Security Transfer PIN Section
                Text(
                    text = "Security Transfer PIN (4 Digits)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = CharcoalText
                )
                Text(
                    text = "Required for authorization during cross-device clipboard & file transfers",
                    fontSize = 11.sp,
                    color = SlateSubtext
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                pinInput = it
                                pinSavedFeedback = false
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("security_pin_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Key, contentDescription = null, tint = NavyPrimary)
                        }
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            if (pinInput.length == 4) {
                                onSavePin(pinInput)
                                pinSavedFeedback = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NavyPrimary,
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_pin_button")
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }

                if (pinSavedFeedback) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "PIN updated to $pinInput successfully!",
                        color = ElectricCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Multi-Surface Device Mesh Details
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SlateBackground)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = "Mesh",
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Linked Surfaces (3/3 Online)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = CharcoalText
                        )
                        Text(
                            text = "Laptop (Win 11) • Mobile (Android) • Smart TV (Living Room)",
                            fontSize = 11.sp,
                            color = SlateSubtext
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // App version
                Text(
                    text = "NIJO PVT TV • Version 1.0.4-tv (Zero-Red Navy Protocol)",
                    fontSize = 11.sp,
                    color = SlateSubtext,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Google TV Auto-Sync Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NavyPrimary.copy(alpha = 0.08f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Google TV Auto-Link: Active. TV uses system Google account automatically without requiring manual sign-in.",
                        fontSize = 11.sp,
                        color = NavyPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Re-sync & Clear Local Cache Button
                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("resync_cache_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Re-sync",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Re-sync Google TV Account & Cache",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
