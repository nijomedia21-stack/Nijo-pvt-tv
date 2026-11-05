package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.WorkspaceMode
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSubtext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeaderTopBar(
    currentWorkspace: WorkspaceMode,
    onOpenWorkspaceSwitcher: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavyPrimary)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: </> Logo with Long Press support for Instagram-style workspace switcher
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .combinedClickable(
                    onClick = onOpenWorkspaceSwitcher,
                    onLongClick = onOpenWorkspaceSwitcher
                )
                .focusable()
                .padding(6.dp)
                .testTag("app_logo_switch_trigger")
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NavySecondary)
                    .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "</>",
                    color = PureWhite,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "NIJO PVT TV",
                        color = PureWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = "Smart TV Node",
                        tint = ElectricCyan,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Text(
                    text = "നിജ ടിവി ടിവി",
                    color = ElectricCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Right side: Active Workspace Badge + Settings Cog
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Workspace pill chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(NavySecondary)
                    .border(1.dp, ElectricCyan.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                    .combinedClickable(
                        onClick = onOpenWorkspaceSwitcher,
                        onLongClick = onOpenWorkspaceSwitcher
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .testTag("workspace_pill_chip"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (currentWorkspace == WorkspaceMode.STUDY) Icons.Default.School else Icons.Default.Palette,
                        contentDescription = currentWorkspace.displayName,
                        tint = PureWhite,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (currentWorkspace == WorkspaceMode.STUDY) "Study" else "Libra Studio",
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Settings Cog
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .testTag("settings_cog_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = PureWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
