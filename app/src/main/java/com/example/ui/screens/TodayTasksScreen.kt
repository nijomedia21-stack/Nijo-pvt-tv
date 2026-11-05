package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.TaskItem
import com.example.data.models.WorkspaceMode
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentIndigo
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

@Composable
fun TodayTasksScreen(
    currentWorkspace: WorkspaceMode,
    tasks: List<TaskItem>,
    onAddTask: (title: String, desc: String, category: String, priority: String, dueDate: String, voiceNote: String) -> Unit,
    onToggleTaskStatus: (taskId: String, newStatus: String) -> Unit,
    onDeleteTask: (taskId: String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filterTabs = listOf("All", "Today", "Upcoming", "Completed")
    var showCreateDialog by remember { mutableStateOf(false) }

    // Filter tasks based on selected tab and active workspace
    val filteredTasks = tasks.filter { task ->
        when (selectedFilter) {
            "Today" -> task.status != "COMPLETED" && (task.dueDate.contains("Today", ignoreCase = true) || task.dueDate.isBlank())
            "Upcoming" -> task.status != "COMPLETED" && !task.dueDate.contains("Today", ignoreCase = true)
            "Completed" -> task.status == "COMPLETED"
            else -> true
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(SlateBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Workspace Header Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PureWhite)
                    .border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Daily Priorities & Deadlines",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = NavyPrimary
                    )
                    Text(
                        text = if (currentWorkspace == WorkspaceMode.STUDY)
                            "Polytechnic AI & ML • Semester 1 Modules"
                        else
                            "Freelance Studio • Libra Brand Deliverables",
                        fontSize = 12.sp,
                        color = SlateSubtext
                    )
                }

                // Status Count Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(NavyPrimary)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${tasks.count { it.status == "COMPLETED" }}/${tasks.size} Done",
                        color = PureWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Tabs Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filterTabs) { tab ->
                    val isSelected = selectedFilter == tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) NavyPrimary else PureWhite)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) NavyPrimary else SlateBorder,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedFilter = tab }
                            .focusable()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("filter_chip_$tab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = if (isSelected) PureWhite else CharcoalText,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Task List
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PureWhite)
                        .border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No tasks in '$selectedFilter'",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NavyPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the + button to add study notes or client deliverables",
                            color = SlateSubtext,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredTasks, key = { it.id.ifBlank { it.title } }) { task ->
                        TaskCardItem(
                            task = task,
                            onToggleStatus = {
                                val nextStatus = if (task.status == "COMPLETED") "TODO" else "COMPLETED"
                                onToggleTaskStatus(task.id, nextStatus)
                            },
                            onDelete = { onDeleteTask(task.id) }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add Task
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = NavyPrimary,
            contentColor = PureWhite,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_task_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Task")
        }
    }

    if (showCreateDialog) {
        CreateTaskDialog(
            currentWorkspace = currentWorkspace,
            onDismiss = { showCreateDialog = false },
            onCreate = { title, desc, category, priority, due, voiceNote ->
                onAddTask(title, desc, category, priority, due, voiceNote)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun TaskCardItem(
    task: TaskItem,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    val isCompleted = task.status == "COMPLETED"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isCompleted) SlateBorder else SlateBorder, RoundedCornerShape(16.dp))
            .focusable()
            .testTag("task_item_${task.title.take(10)}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Checkbox
            IconButton(
                onClick = onToggleStatus,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle Status",
                    tint = if (isCompleted) ElectricCyan else NavyPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isCompleted) SlateSubtext else CharcoalText,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )

                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        fontSize = 12.sp,
                        color = SlateSubtext,
                        lineHeight = 16.sp
                    )
                }

                if (task.voiceNote.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateBackground)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Memo",
                            tint = ElectricCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Voice Memo: ${task.voiceNote}",
                            fontSize = 11.sp,
                            color = SlateSubtext
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tag Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NavySecondary.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = task.category,
                            color = NavyPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Priority Tag (Strict Zero-Red Rule: Amber, Teal, Navy)
                    val priorityColor = when (task.priority.lowercase()) {
                        "high" -> AccentAmber
                        "low" -> AccentTeal
                        else -> NavyLight
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(priorityColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = task.priority,
                            color = priorityColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (task.dueDate.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = SlateSubtext,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = task.dueDate,
                                color = SlateSubtext,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = SlateMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun CreateTaskDialog(
    currentWorkspace: WorkspaceMode,
    onDismiss: () -> Unit,
    onCreate: (title: String, desc: String, category: String, priority: String, due: String, voiceNote: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(if (currentWorkspace == WorkspaceMode.STUDY) "Study" else "Design") }
    var priority by remember { mutableStateOf("Medium") }
    var dueDate by remember { mutableStateOf("Today") }
    var voiceNote by remember { mutableStateOf("") }
    var isRecordingMemo by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SlateBorder, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "New Priority Task",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = NavyPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title (e.g. Maths I Matrix Assignment)") },
                    modifier = Modifier.fillMaxWidth().testTag("task_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Details & formulas") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Priority Selector Chips
                Text("Priority Level", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CharcoalText)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("High", "Medium", "Low").forEach { p ->
                        val isSel = priority == p
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) NavyPrimary else SlateBackground)
                                .clickable { priority = p }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(p, color = if (isSel) PureWhite else CharcoalText, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Voice Note Modal Memo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SlateBackground)
                        .padding(10.dp)
                ) {
                    IconButton(
                        onClick = {
                            isRecordingMemo = !isRecordingMemo
                            if (voiceNote.isBlank()) {
                                voiceNote = "Discussed with team: Submit matrix derivations before 5 PM."
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NavyPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Note Memo",
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (voiceNote.isNotBlank()) "Voice Memo Attached" else "Tap Mic for Voice Note Memo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NavyPrimary
                        )
                        if (voiceNote.isNotBlank()) {
                            Text(voiceNote, fontSize = 11.sp, color = SlateSubtext, maxLines = 1)
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
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onCreate(title, desc, category, priority, dueDate, voiceNote)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = PureWhite),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_task_button")
                    ) {
                        Text("Add Task")
                    }
                }
            }
        }
    }
}
