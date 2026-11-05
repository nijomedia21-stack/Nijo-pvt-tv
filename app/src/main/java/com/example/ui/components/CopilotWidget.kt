package com.example.ui.components

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.CalendarReminder
import com.example.data.models.TaskItem
import com.example.data.models.TransferItem
import com.example.data.models.WorkspaceMode
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
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TAG = "NijoCopilot"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CopilotWidget(
    currentWorkspace: WorkspaceMode,
    tasks: List<TaskItem>,
    events: List<CalendarReminder>,
    transfers: List<TransferItem>,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var promptInput by remember { mutableStateOf("") }
    var responseText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var streamingJob by remember { mutableStateOf<Job?>(null) }
    var statusMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    val model = remember {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    val fastChips = remember(currentWorkspace) {
        listOf(
            "Maths I Formulas",
            "Chemistry Concepts",
            "Check Calendar",
            "Libra SMM Plan",
            "Exam Countdown"
        )
    }

    fun executePrompt(promptText: String) {
        if (promptText.isBlank()) return
        streamingJob?.cancel()
        isLoading = true
        responseText = ""
        statusMessage = "Thinking with Gemini 3.8 Flash..."

        // Build context payload from active workspace, tasks, calendar events, and transfer items
        val contextSummary = buildString {
            append("Target User: Nijo (nijo.media21@gmail.com)\n")
            append("Active Workspace: ${currentWorkspace.displayName} (${currentWorkspace.id})\n")
            append("Upcoming Tasks (${tasks.size}):\n")
            tasks.take(4).forEach { t -> append("- [${t.status}] ${t.title} (${t.priority} priority, ${t.dueDate})\n") }
            append("Calendar Schedule (${events.size}):\n")
            events.take(3).forEach { e -> append("- ${e.date} ${e.time}: ${e.title} [Panchangam: ${e.panchangam}]\n") }
            append("Recent Cross-Device Transfers (${transfers.size}):\n")
            transfers.take(2).forEach { tr -> append("- ${tr.type}: ${tr.title} -> ${tr.devices.joinToString()}\n") }
        }

        streamingJob = coroutineScope.launch {
            try {
                withTimeout(60_000L) {
                    val fullPrompt = """
                        You are "Nijo Copilot", the private intelligent assistant for Nijo's NIJO PVT TV multi-surface workspace.
                        Nijo is a Polytechnic AI & ML student (Semester 1) and freelance graphic designer/SMM manager for client brand "Libra".
                        
                        Current Workspace & Device Context:
                        $contextSummary
                        
                        User Request: $promptText
                        
                        Instructions:
                        - Give clear, concise, actionable responses formatted cleanly.
                        - For Maths I: provide exact formulas (matrices, rank, derivatives).
                        - For Applied Chemistry: explain engineering chemistry concepts clearly.
                        - For Libra: provide Instagram Reel hooks, captions, and brand color guidelines.
                        - For Calendar/Tasks: analyze user deadlines.
                        - Do not recommend red colors or themes (strictly observe the zero-red navy/white rule).
                    """.trimIndent()

                    val stream = model.generateContentStream(fullPrompt)
                    stream.collect { chunk ->
                        responseText += (chunk.text ?: "")
                        statusMessage = "Streaming response..."
                    }
                    statusMessage = ""
                }
            } catch (e: TimeoutCancellationException) {
                responseText = "Request timed out after 60 seconds. Please check your connection."
                statusMessage = ""
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Copilot streaming error: ${e.message}", e)
                responseText = "Copilot inference note: ${e.localizedMessage ?: "Unable to complete request. Please verify connection."}"
                statusMessage = ""
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.End
    ) {
        AnimatedVisibility(visible = isExpanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorder, RoundedCornerShape(20.dp))
                    .testTag("copilot_expanded_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(NavyPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Copilot",
                                    tint = PureWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Nijo Copilot",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = NavyPrimary
                                )
                                Text(
                                    text = "Firebase AI Logic • Gemini 3.8 Flash",
                                    fontSize = 10.sp,
                                    color = ElectricCyan,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        IconButton(
                            onClick = { isExpanded = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Copilot",
                                tint = SlateSubtext,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Fast Prompt Chips
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        fastChips.forEach { chipLabel ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(SlateBackground)
                                    .border(1.dp, SlateBorder, RoundedCornerShape(14.dp))
                                    .clickable {
                                        promptInput = chipLabel
                                        executePrompt(chipLabel)
                                    }
                                    .focusable()
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = chipLabel,
                                    color = NavyPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Text Input Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = promptInput,
                            onValueChange = { promptInput = it },
                            placeholder = { Text("Ask Copilot about notes, formulas, SMM...", fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("copilot_prompt_input"),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NavyPrimary,
                                unfocusedBorderColor = SlateBorder
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                val p = promptInput
                                promptInput = ""
                                executePrompt(p)
                            },
                            enabled = !isLoading && promptInput.isNotBlank(),
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (!isLoading && promptInput.isNotBlank()) NavyPrimary else SlateBorder)
                                .testTag("copilot_send_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = PureWhite,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send Prompt",
                                    tint = PureWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    if (statusMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = statusMessage,
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Response Box
                    if (responseText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SlateBackground)
                                .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Copilot Response",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = SlateSubtext
                                    )
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(responseText))
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Response",
                                            tint = SlateSubtext,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = responseText,
                                    color = CharcoalText,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Floating Action Pill (Collapsed state)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(NavyPrimary)
                .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .clickable { isExpanded = !isExpanded }
                .focusable()
                .padding(horizontal = 14.dp, vertical = 9.dp)
                .testTag("copilot_floating_pill"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Nijo Copilot",
                    tint = PureWhite,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isExpanded) "Hide Copilot" else "Nijo Copilot",
                    color = PureWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
