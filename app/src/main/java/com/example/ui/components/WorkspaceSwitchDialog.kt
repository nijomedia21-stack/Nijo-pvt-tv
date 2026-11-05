package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.WorkspaceMode
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSubtext

@Composable
fun WorkspaceSwitchDialog(
    currentWorkspace: WorkspaceMode,
    onSelectWorkspace: (WorkspaceMode) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SlateBorder, RoundedCornerShape(24.dp))
                .testTag("workspace_switch_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NavyPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("</>", color = PureWhite, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Switch Workspace",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = NavyPrimary
                        )
                        Text(
                            text = "Instagram-style profile switcher",
                            fontSize = 12.sp,
                            color = SlateSubtext
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Divider(color = SlateBorder)
                Spacer(modifier = Modifier.height(18.dp))

                // Profile 1: Polytechnic AI & ML
                WorkspaceProfileCard(
                    title = "Polytechnic AI & ML",
                    subtitle = "Semester 1 Study: Maths I, Chemistry, PSPP/Python, Physics, Formulas & Countdown",
                    icon = Icons.Default.School,
                    isSelected = currentWorkspace == WorkspaceMode.STUDY,
                    onClick = {
                        onSelectWorkspace(WorkspaceMode.STUDY)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Profile 2: Freelance Designer & SMM Studio
                WorkspaceProfileCard(
                    title = "Freelance Designer & SMM Studio",
                    subtitle = "Client: Libra • Brand Identity Kit • Reels Calendar • Design Asset Hub",
                    icon = Icons.Default.Palette,
                    isSelected = currentWorkspace == WorkspaceMode.DESIGN,
                    onClick = {
                        onSelectWorkspace(WorkspaceMode.DESIGN)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Tip: Long-press the </> logo in header or the 4th bottom tab to quickly switch profiles.",
                    fontSize = 11.sp,
                    color = SlateSubtext,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun WorkspaceProfileCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) SlateBackground else PureWhite)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) NavyPrimary else SlateBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .focusable()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) NavyPrimary else NavySecondary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = PureWhite,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = CharcoalText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = SlateSubtext,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = NavyPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
