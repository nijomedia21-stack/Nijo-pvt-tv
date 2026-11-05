package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun AdaptiveStudyDesignScreen(
    currentWorkspace: WorkspaceMode,
    onPushSnippetToTransfer: (title: String, content: String, type: String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    if (currentWorkspace == WorkspaceMode.STUDY) {
        StudyModeView(
            onCopy = { text ->
                clipboardManager.setText(AnnotatedString(text))
                Toast.makeText(context, "Copied formula to clipboard", Toast.LENGTH_SHORT).show()
            },
            onPushToTransfer = onPushSnippetToTransfer
        )
    } else {
        DesignModeView(
            onCopy = { text ->
                clipboardManager.setText(AnnotatedString(text))
                Toast.makeText(context, "Copied prompt to clipboard", Toast.LENGTH_SHORT).show()
            },
            onPushToTransfer = onPushSnippetToTransfer
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StudyModeView(
    onCopy: (String) -> Unit,
    onPushToTransfer: (title: String, content: String, type: String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Exam Countdown Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth().testTag("exam_countdown_card")
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
                                    imageVector = Icons.Default.HourglassTop,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Semester 1 Board Exams",
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Polytechnic AI & ML Curriculum",
                                    color = ElectricCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Countdown Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(NavySecondary)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "18 Days Left",
                                color = PureWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Syllabus Progress: 78% completed • Focus: Matrix Inversion & EDTA titrations",
                        color = PureWhite.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Subject Units & Formulas
        item {
            Text(
                text = "Semester 1 Core Modules & Formula Notes",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = NavyPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 1. Maths I
        item {
            SubjectCard(
                subject = "Mathematics I",
                code = "2001",
                icon = Icons.Default.School,
                color = NavyPrimary,
                units = listOf("Unit 1: Matrices & Rank", "Unit 2: Differential Calculus", "Unit 3: Integral Calculus", "Unit 4: Vector Algebra"),
                formulaPreview = "A · A⁻¹ = I; |A - λI| = 0 (Cayley-Hamilton Theorem); rank(A) = num(non-zero pivots)",
                onCopyFormula = { onCopy("A · A⁻¹ = I; |A - λI| = 0; rank(A) = pivot columns; d/dx(uv) = u'v + uv'") },
                onPushTransfer = { onPushToTransfer("Maths I Formulas", "A · A⁻¹ = I\n|A - λI| = 0\nrank(A) = pivot columns", "CODE") }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 2. Applied Chemistry
        item {
            SubjectCard(
                subject = "Applied Chemistry",
                code = "2002",
                icon = Icons.Default.Science,
                color = AccentTeal,
                units = listOf("Unit 1: Chemical Bonding", "Unit 2: Water Technology", "Unit 3: Engineering Polymers", "Unit 4: Electrochemistry"),
                formulaPreview = "Hardness (ppm CaCO₃) = (V · N · 50 · 1000) / V_sample; EDTA Chelation ratio 1:1",
                onCopyFormula = { onCopy("Hardness (ppm) = (V * N * 50 * 1000) / V_sample; pH = -log[H+]") },
                onPushTransfer = { onPushToTransfer("Applied Chemistry Notes", "Hardness (ppm) = (V * N * 50 * 1000) / V_sample", "CODE") }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 3. Problem Solving & Python (PSPP)
        item {
            SubjectCard(
                subject = "PSPP (Python & AI/ML Intro)",
                code = "2003",
                icon = Icons.Default.Code,
                color = AccentIndigo,
                units = listOf("Unit 1: Algorithms & Flowcharts", "Unit 2: Python Data Types", "Unit 3: Functions & Recursion", "Unit 4: NumPy & OOP"),
                formulaPreview = "def matrix_rank(mat): return np.linalg.matrix_rank(mat) # O(n³) SVD",
                onCopyFormula = { onCopy("import numpy as np\ndef rank(a): return np.linalg.matrix_rank(a)") },
                onPushTransfer = { onPushToTransfer("Python Matrix SVD Snippet", "import numpy as np\ndef rank(a): return np.linalg.matrix_rank(a)", "CODE") }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 4. Applied Physics
        item {
            SubjectCard(
                subject = "Applied Physics",
                code = "2004",
                icon = Icons.Default.Biotech,
                color = NavySecondary,
                units = listOf("Unit 1: Optics & Fiber Cables", "Unit 2: Laser Physics", "Unit 3: Quantum Mechanics", "Unit 4: Nanomaterials"),
                formulaPreview = "Snell's Law: n₁ sin θ₁ = n₂ sin θ₂; Numerical Aperture NA = √(n₁² - n₂²)",
                onCopyFormula = { onCopy("n1 * sin(theta1) = n2 * sin(theta2); NA = sqrt(n1^2 - n2^2)") },
                onPushTransfer = { onPushToTransfer("Physics Optics Formulas", "NA = sqrt(n1^2 - n2^2); E = h*nu", "CODE") }
            )
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SubjectCard(
    subject: String,
    code: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    units: List<String>,
    formulaPreview: String,
    onCopyFormula: () -> Unit,
    onPushTransfer: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
            .testTag("subject_card_${subject.take(5)}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = PureWhite, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(subject, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CharcoalText)
                        Text("Course Code $code • Semester 1", fontSize = 11.sp, color = SlateSubtext)
                    }
                }

                Row {
                    IconButton(onClick = onCopyFormula, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Formulas", tint = NavyPrimary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onPushTransfer, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Download, contentDescription = "Push to Mesh", tint = ElectricCyan, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Units list chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                units.take(2).forEach { u ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SlateBackground)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(u, fontSize = 10.sp, color = SlateSubtext)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Formula Preview snippet box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SlateBackground)
                    .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = formulaPreview,
                    fontSize = 11.sp,
                    color = CharcoalText,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 2
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DesignModeView(
    onCopy: (String) -> Unit,
    onPushToTransfer: (title: String, content: String, type: String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // "Libra" Brand Identity Kit Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth().testTag("libra_brand_card")
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
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(NavySecondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Client: Libra",
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Brand Identity Kit & Design Guidelines",
                                    color = ElectricCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(NavySecondary)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text("Active Client", color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Official Palette Tokens (Strict Zero-Red Protocol):",
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Brand color swatches
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ColorSwatchItem(color = Color(0xFF0F294A), label = "Navy", hex = "#0F294A")
                        ColorSwatchItem(color = Color(0xFF1E3A8A), label = "Royal", hex = "#1E3A8A")
                        ColorSwatchItem(color = Color(0xFF0284C7), label = "Cyan", hex = "#0284C7")
                        ColorSwatchItem(color = Color(0xFFF8FAFC), label = "Slate", hex = "#F8FAFC", border = true)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Instagram Reels Content Calendar
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                modifier = Modifier.fillMaxWidth().border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Movie, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Instagram Reels Calendar (Libra)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NavyPrimary
                            )
                        }

                        Text("3 Scheduled", fontSize = 11.sp, color = ElectricCyan, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    ReelScheduleItem(
                        day = "Tuesday",
                        hook = "3 Branding mistakes killing your brand vibe",
                        asset = "libra_reels_ep1.mp4",
                        status = "Editing 90%"
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ReelScheduleItem(
                        day = "Thursday",
                        hook = "Why zero-red aesthetics build royal trust",
                        asset = "libra_reels_ep2.mp4",
                        status = "Script Ready"
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ReelScheduleItem(
                        day = "Saturday",
                        hook = "Behind the scenes: Libra Identity Design",
                        asset = "libra_reels_ep3.mp4",
                        status = "Planned"
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // AI Image Generation & Creative Prompt Library
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                modifier = Modifier.fillMaxWidth().border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Creative AI Prompt Library",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NavyPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    PromptCardItem(
                        title = "Libra Minimalist Product Billboard",
                        prompt = "Minimalist luxury product showcase on navy podium, soft cyan volumetric lighting, pure white typography, ultra-clean studio render, 8k octane.",
                        onCopy = { onCopy("Minimalist luxury product showcase on navy podium, soft cyan volumetric lighting, pure white typography, ultra-clean studio render, 8k octane.") },
                        onPush = { onPushToTransfer("Libra Billboard Prompt", "Minimalist luxury product showcase on navy podium, soft cyan volumetric lighting...", "CODE") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PromptCardItem(
                        title = "Cyber TV Workspace Concept",
                        prompt = "Futuristic personal command station with clean navy blue dashboards, geometric code glyphs, soft glowing white displays, ergonomic modern architecture.",
                        onCopy = { onCopy("Futuristic personal command station with clean navy blue dashboards, geometric code glyphs, soft glowing white displays, ergonomic modern architecture.") },
                        onPush = { onPushToTransfer("Cyber TV Prompt", "Futuristic personal command station with clean navy blue dashboards...", "CODE") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ColorSwatchItem(
    color: Color,
    label: String,
    hex: String,
    border: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color)
                .border(if (border) 1.dp else 0.dp, SlateBorder, RoundedCornerShape(10.dp))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = PureWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(hex, color = PureWhite.copy(alpha = 0.8f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun ReelScheduleItem(
    day: String,
    hook: String,
    asset: String,
    status: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SlateBackground)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(NavyPrimary),
            contentAlignment = Alignment.Center
        ) {
            Text(day.take(3), color = PureWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(hook, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = CharcoalText)
            Text("$asset • Status: $status", fontSize = 10.sp, color = SlateSubtext)
        }
    }
}

@Composable
private fun PromptCardItem(
    title: String,
    prompt: String,
    onCopy: () -> Unit,
    onPush: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SlateBackground)
            .border(1.dp, SlateBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)
                Row {
                    IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Prompt", tint = NavyPrimary, modifier = Modifier.size(14.dp))
                    }
                    IconButton(onClick = onPush, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Download, contentDescription = "Push", tint = ElectricCyan, modifier = Modifier.size(14.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(prompt, fontSize = 11.sp, color = SlateSubtext, lineHeight = 15.sp)
        }
    }
}
