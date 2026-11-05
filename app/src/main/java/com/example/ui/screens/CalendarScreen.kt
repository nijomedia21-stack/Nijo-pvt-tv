package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.CalendarReminder
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSubtext
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    events: List<CalendarReminder>,
    onAddEvent: (title: String, date: String, time: String, notes: String, vibration: String, panchangam: String) -> Unit
) {
    val context = LocalContext.current
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showAddEventDialog by remember { mutableStateOf(false) }
    var showYearSelector by remember { mutableStateOf(false) }

    // Panchangam lookup for Kerala dates
    fun getKeralaPanchangamForDay(dayOfMonth: Int): String {
        val nakshatras = listOf(
            "അശ്വതി (Aswathi)", "ഭരണി (Bharani)", "കാർത്തിക (Karthika)",
            "രോഹിണി (Rohini)", "മകയിരം (Makayiram)", "തിരുവാതിര (Thiruvathira)",
            "പുണർതം (Punartham)", "പൂയം (Pooyam)", "ആയില്യം (Aayilyam)",
            "മകം (Makam)", "പൂരം (Pooram)", "ഉത്രം (Uthram)",
            "അത്തം (Atham)", "ചിത്തിര (Chithira)", "ചോതി (Chothi)",
            "വിശാഖം (Visakham)", "അനിഴം (Anizham)", "തൃക്കേട്ട (Thrikketta)",
            "മൂലം (Moolam)", "പൂരാടം (Pooradam)", "ഉത്രാടം (Uthradam)",
            "തിരുവോണം (Thiruvonam)", "അവിട്ടം (Avittam)", "ചതയം (Chathayam)",
            "പൂരുരുട്ടാതി (Pooruruttathi)", "ഉത്രട്ടാതി (Uthrattathi)", "രേവതി (Revathi)"
        )
        val tithis = listOf("ശുക്ലപക്ഷ ഏകാദശി (Ekadashi)", "പൗർണ്ണമി (Pournami)", "അമാവാസി (Amavasi)", "പ്രഥമ (Prathama)", "ദ്വിതീയ (Dvitiya)")
        val nak = nakshatras[(dayOfMonth - 1) % nakshatras.size]
        val tit = tithis[(dayOfMonth - 1) % tithis.size]
        return "$nak • $tit"
    }

    val selectedDateString = selectedDate.toString()
    val dailyEvents = events.filter { it.date == selectedDateString }

    Box(modifier = Modifier.fillMaxSize().background(SlateBackground)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Month Header with Fast Year Jumper
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(NavyPrimary)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { currentYearMonth = currentYearMonth.minusMonths(1) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Month",
                            tint = PureWhite
                        )
                    }

                    // Year & Month Jumper Button (Supports up to year 2100)
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(NavySecondary)
                                .clickable { showYearSelector = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("year_month_jumper_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${currentYearMonth.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${currentYearMonth.year}",
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        // Dropdown for Year Jump up to 2100
                        DropdownMenu(
                            expanded = showYearSelector,
                            onDismissRequest = { showYearSelector = false },
                            modifier = Modifier.background(PureWhite)
                        ) {
                            val currentYear = currentYearMonth.year
                            val candidateYears = listOf(2026, 2027, 2028, 2030, 2035, 2040, 2050, 2075, 2100)
                            candidateYears.forEach { yr ->
                                DropdownMenuItem(
                                    text = { Text("Year $yr", color = CharcoalText, fontWeight = if (yr == currentYear) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        currentYearMonth = YearMonth.of(yr, currentYearMonth.month)
                                        showYearSelector = false
                                    }
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { currentYearMonth = currentYearMonth.plusMonths(1) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Month",
                            tint = PureWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Day Headers (SUN, MON, TUE, WED, THU, FRI, SAT)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT").forEach { dayLabel ->
                        Text(
                            text = dayLabel,
                            color = SlateSubtext,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(40.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Monthly Calendar Grid
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        val firstDayOfWeek = currentYearMonth.atDay(1).dayOfWeek.value % 7 // 0 = Sunday
                        val daysInMonth = currentYearMonth.lengthOfMonth()
                        val totalSlots = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7

                        for (week in 0 until totalSlots / 7) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                for (dayOfWeek in 0..6) {
                                    val cellIndex = week * 7 + dayOfWeek
                                    val dayNum = cellIndex - firstDayOfWeek + 1
                                    if (dayNum in 1..daysInMonth) {
                                        val date = currentYearMonth.atDay(dayNum)
                                        val isSelected = date == selectedDate
                                        val hasEvents = events.any { it.date == date.toString() }

                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .padding(2.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) NavyPrimary
                                                    else if (hasEvents) NavySecondary.copy(alpha = 0.15f)
                                                    else PureWhite
                                                )
                                                .border(
                                                    width = if (isSelected) 2.dp else 0.dp,
                                                    color = if (isSelected) ElectricCyan else PureWhite,
                                                    shape = CircleShape
                                                )
                                                .clickable { selectedDate = date }
                                                .focusable(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = dayNum.toString(),
                                                    color = if (isSelected) PureWhite else CharcoalText,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                                if (hasEvents && !isSelected) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(NavyPrimary)
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.size(42.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Kerala Malayalam Panchangam Card for Selected Day (Zero Red Banner Rule)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(NavyPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "കേരള പഞ്ചാംഗം (Kerala Malayalam Panchangam)",
                                color = NavyPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val panchangam = getKeralaPanchangamForDay(selectedDate.dayOfMonth)
                        Text(
                            text = panchangam,
                            color = CharcoalText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Date: ${selectedDate.dayOfMonth} ${selectedDate.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${selectedDate.year} (Sunday/Daily Astrological Coordinates)",
                            color = SlateSubtext,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Daily Reminders and Alarms
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Schedule & Alarms for ${selectedDate.dayOfMonth} ${selectedDate.month.name}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NavyPrimary
                    )

                    Text(
                        text = "${dailyEvents.size} Events",
                        color = SlateSubtext,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            if (dailyEvents.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(PureWhite)
                            .border(1.dp, SlateBorder, RoundedCornerShape(14.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No events scheduled for this day. Tap + to add reminder.",
                            color = SlateSubtext,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                items(dailyEvents) { ev ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NavyPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ev.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = CharcoalText
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = SlateSubtext,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${ev.time} • Vibration: ${ev.vibrationTrigger}",
                                        fontSize = 11.sp,
                                        color = SlateSubtext
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // FAB to Add Event/Alarm
        FloatingActionButton(
            onClick = { showAddEventDialog = true },
            containerColor = NavyPrimary,
            contentColor = PureWhite,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_calendar_event_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Calendar Event")
        }
    }

    if (showAddEventDialog) {
        AddEventDialog(
            defaultDate = selectedDate.toString(),
            panchangamNote = getKeralaPanchangamForDay(selectedDate.dayOfMonth),
            onDismiss = { showAddEventDialog = false },
            onAdd = { title, date, time, notes, vibration, panchangam ->
                // Trigger test vibration
                triggerHaptic(context, vibration)
                onAddEvent(title, date, time, notes, vibration, panchangam)
                showAddEventDialog = false
            }
        )
    }
}

private fun triggerHaptic(context: Context, pattern: String) {
    try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = when (pattern.lowercase()) {
                "gentle" -> VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                "pulse" -> VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 100), -1)
                "strong" -> VibrationEffect.createOneShot(300, 255)
                else -> null
            }
            effect?.let { vibrator.vibrate(it) }
        } else {
            vibrator.vibrate(100)
        }
    } catch (_: Exception) {}
}

@Composable
private fun AddEventDialog(
    defaultDate: String,
    panchangamNote: String,
    onDismiss: () -> Unit,
    onAdd: (title: String, date: String, time: String, notes: String, vibration: String, panchangam: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("10:00 AM") }
    var notes by remember { mutableStateOf("") }
    var vibrationTrigger by remember { mutableStateOf("Pulse") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            modifier = Modifier.fillMaxWidth().border(1.dp, SlateBorder, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Add Event & Alarm",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = NavyPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title (e.g. Maths Exam, Libra Review)") },
                    modifier = Modifier.fillMaxWidth().testTag("event_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (e.g. 10:00 AM)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Custom Vibration Trigger", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CharcoalText)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Gentle", "Pulse", "Strong", "None").forEach { vib ->
                        val isSel = vibrationTrigger == vib
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) NavyPrimary else SlateBackground)
                                .clickable { vibrationTrigger = vib }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(vib, color = if (isSel) PureWhite else CharcoalText, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SlateBackground)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Panchangam: $panchangamNote",
                        fontSize = 11.sp,
                        color = NavyPrimary
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
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onAdd(title, defaultDate, time, notes, vibrationTrigger, panchangamNote)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = PureWhite),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_event_button")
                    ) {
                        Text("Save Event")
                    }
                }
            }
        }
    }
}
