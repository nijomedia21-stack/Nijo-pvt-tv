package com.example.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class DeviceRole(val id: String, val label: String) {
    TV("tv", "TV (Receive Only)"),
    LAPTOP("laptop", "Laptop (Send & Recv)"),
    MOBILE("mobile", "Mobile (Send & Recv)")
}

enum class WorkspaceMode(val id: String, val displayName: String, val subtitle: String) {
    STUDY("study", "Polytechnic AI & ML", "Semester 1 • Maths, Chemistry, Python, Physics"),
    DESIGN("design", "Freelance Designer & SMM", "Client: Libra • Instagram Reels • Brand Assets")
}

@IgnoreExtraProperties
data class TaskItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "Study", // Study, Design, General
    val priority: String = "Medium", // High, Medium, Low
    val status: String = "TODO", // TODO, IN_PROGRESS, COMPLETED
    val dueDate: String = "",
    val workspace: String = "study",
    val voiceNote: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CalendarReminder(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val date: String = "", // YYYY-MM-DD
    val time: String = "09:00",
    val notes: String = "",
    val vibrationTrigger: String = "Pulse", // Gentle, Pulse, Strong, None
    val panchangam: String = "", // Nakshatra, Tithi
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class TransferItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val type: String = "FILE", // FILE, CLIPBOARD, CODE, MEDIA
    val fileName: String = "",
    val fileSize: String = "",
    val devices: List<String> = emptyList(), // "Laptop (Windows 11)", "Mobile (Android)", "Smart TV (Living Room)"
    val status: String = "SYNCED",
    val pinVerified: Boolean = false,
    val sender: String = "laptop", // "laptop", "mobile", "tv"
    val target: String = "all", // "tv", "mobile", "laptop", "all"
    val downloadUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserSettings(
    @DocumentId val id: String = "config",
    val userId: String = "",
    val securityPin: String = "8492",
    val currentWorkspace: String = "study",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
