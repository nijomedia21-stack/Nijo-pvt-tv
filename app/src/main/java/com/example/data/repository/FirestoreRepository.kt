package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.models.CalendarReminder
import com.example.data.models.TaskItem
import com.example.data.models.TransferItem
import com.example.data.models.UserSettings
import com.example.data.models.WorkspaceMode
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

private const val TAG = "FirestoreRepo"

class FirestoreRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    // Local in-memory reactive state for instant Google TV workspace access
    private val localTasks = MutableStateFlow<List<TaskItem>>(emptyList())
    private val localEvents = MutableStateFlow<List<CalendarReminder>>(emptyList())
    private val localTransfers = MutableStateFlow<List<TransferItem>>(emptyList())
    private val localSettings = MutableStateFlow(UserSettings(userId = "nijo_tv_profile", securityPin = "8492"))

    init {
        seedInitialMemoryData()
    }

    private fun seedInitialMemoryData() {
        val defaultTasks = listOf(
            TaskItem(
                id = "task_maths_1",
                userId = "nijo_tv_profile",
                title = "Maths I: Complete Matrix Inversion & Rank Unit 1",
                description = "Formulas: Gauss-Jordan elimination, Eigenvalues and Cayley-Hamilton theorem.",
                category = "Study",
                priority = "High",
                status = "TODO",
                workspace = WorkspaceMode.STUDY.id,
                dueDate = "Today 5:00 PM"
            ),
            TaskItem(
                id = "task_chem_1",
                userId = "nijo_tv_profile",
                title = "Applied Chemistry: Review Water Hardness & EDTA titration",
                description = "Polytechnic Semester 1 lab module notes and numerical calculations.",
                category = "Study",
                priority = "Medium",
                status = "TODO",
                workspace = WorkspaceMode.STUDY.id,
                dueDate = "Tomorrow"
            ),
            TaskItem(
                id = "task_libra_1",
                userId = "nijo_tv_profile",
                title = "Libra: Finalize Brand Identity Kit & Color Palettes",
                description = "Deliver navy blue and pure white primary identity assets to client Libra.",
                category = "Design",
                priority = "High",
                status = "TODO",
                workspace = WorkspaceMode.DESIGN.id,
                dueDate = "Today 8:00 PM"
            ),
            TaskItem(
                id = "task_libra_2",
                userId = "nijo_tv_profile",
                title = "Libra: Schedule 3 Instagram Reels for Weekly Campaign",
                description = "Editing motion graphics and AI prompt designs for Libra fashion showcase.",
                category = "Design",
                priority = "Medium",
                status = "TODO",
                workspace = WorkspaceMode.DESIGN.id,
                dueDate = "Friday"
            )
        )
        localTasks.value = defaultTasks

        val defaultEvents = listOf(
            CalendarReminder(
                id = "event_maths_exam",
                userId = "nijo_tv_profile",
                title = "Maths I Model Exam (Unit 1 & 2)",
                date = "2026-11-05",
                time = "10:00 AM",
                notes = "Hall 4, Polytechnic Campus",
                vibrationTrigger = "Strong",
                panchangam = "അശ്വതി (Aswathi) • ശുക്ലപക്ഷ ഏകാദശി"
            ),
            CalendarReminder(
                id = "event_libra_meeting",
                userId = "nijo_tv_profile",
                title = "Client Libra: Brand Identity Review Meeting",
                date = "2026-11-07",
                time = "04:30 PM",
                notes = "Google Meet with Libra Creative Director",
                vibrationTrigger = "Pulse",
                panchangam = "Bharani • Dvitiya"
            )
        )
        localEvents.value = defaultEvents

        val defaultTransfers = listOf(
            TransferItem(
                id = "tr_libra_tokens",
                userId = "nijo_tv_profile",
                title = "brand_tokens.json",
                fileName = "brand_tokens.json",
                content = "PRIMARY_NAVY = #0F294A;\nSECONDARY_BLUE = #1E3A8A;\nACCENT_CYAN = #0284C7;\nBACKGROUND = #F8FAFC;\nTEXT_CHARCOAL = #0F172A;",
                type = "CODE",
                fileSize = "142 KB",
                devices = listOf("All 3 Devices"),
                status = "AUTO-DOWNLOADED",
                pinVerified = true,
                sender = "laptop",
                target = "all",
                downloadUrl = "http://192.168.1.2:4000/download/brand_tokens.json"
            ),
            TransferItem(
                id = "tr_maths_notes",
                userId = "nijo_tv_profile",
                title = "document_exam_notes.pdf",
                fileName = "document_exam_notes.pdf",
                content = "Comprehensive formula sheet for Semester 1 Polytechnic exams.",
                type = "FILE",
                fileSize = "2.4 MB",
                devices = listOf("TV", "Laptop"),
                status = "AUTO-DOWNLOADED",
                pinVerified = true,
                sender = "laptop",
                target = "tv",
                downloadUrl = "http://192.168.1.2:4000/download/document_exam_notes.pdf"
            ),
            TransferItem(
                id = "tr_camera_photo",
                userId = "nijo_tv_profile",
                title = "camera_capture.jpg",
                fileName = "camera_capture.jpg",
                content = "Ultra HD camera snapshot transferred to living room TV.",
                type = "IMAGE",
                fileSize = "3.1 MB",
                devices = listOf("TV", "Mobile"),
                status = "AUTO-DOWNLOADED",
                pinVerified = true,
                sender = "mobile",
                target = "tv",
                downloadUrl = "http://192.168.1.2:4000/download/camera_capture.jpg"
            )
        )
        localTransfers.value = defaultTransfers
    }

    fun getUserId(): String? = auth.currentUser?.uid

    // ==================== TASKS ====================
    fun observeTasks(): Flow<List<TaskItem>> {
        val uid = auth.currentUser?.uid
        return if (uid != null) {
            db.collection("users").document(uid).collection("tasks")
                .snapshots()
                .map { snapshot ->
                    val remote = snapshot.toObjects(TaskItem::class.java)
                    if (remote.isNotEmpty()) remote else localTasks.value
                }
                .catch { e ->
                    Log.w(TAG, "Falling back to local tasks: ${e.message}")
                    emit(localTasks.value)
                }
        } else {
            localTasks.asStateFlow()
        }
    }

    suspend fun addTask(
        title: String,
        description: String = "",
        category: String = "Study",
        priority: String = "Medium",
        workspace: String = "study",
        dueDate: String = "",
        voiceNote: String = ""
    ) {
        val uid = auth.currentUser?.uid
        val newTask = TaskItem(
            id = UUID.randomUUID().toString(),
            userId = uid ?: "nijo_tv_profile",
            title = title,
            description = description,
            category = category,
            priority = priority,
            status = "TODO",
            workspace = workspace,
            dueDate = dueDate,
            voiceNote = voiceNote
        )
        // Update local memory state immediately
        localTasks.value = listOf(newTask) + localTasks.value

        // If authenticated, also push to Firestore
        if (uid != null) {
            try {
                val docRef = db.collection("users").document(uid).collection("tasks").document(newTask.id)
                val payload = mapOf(
                    "userId" to uid,
                    "title" to title,
                    "description" to description,
                    "category" to category,
                    "priority" to priority,
                    "status" to "TODO",
                    "workspace" to workspace,
                    "dueDate" to dueDate,
                    "voiceNote" to voiceNote,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } catch (e: Exception) {
                Log.w(TAG, "Error pushing task to Firestore: ${e.message}")
            }
        }
    }

    suspend fun updateTaskStatus(taskId: String, newStatus: String) {
        localTasks.value = localTasks.value.map {
            if (it.id == taskId) it.copy(status = newStatus) else it
        }

        val uid = auth.currentUser?.uid
        if (uid != null) {
            try {
                val docRef = db.collection("users").document(uid).collection("tasks").document(taskId)
                docRef.update(
                    mapOf(
                        "status" to newStatus,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } catch (e: Exception) {
                Log.w(TAG, "Error updating task in Firestore: ${e.message}")
            }
        }
    }

    suspend fun deleteTask(taskId: String) {
        localTasks.value = localTasks.value.filter { it.id != taskId }

        val uid = auth.currentUser?.uid
        if (uid != null) {
            try {
                db.collection("users").document(uid).collection("tasks").document(taskId).delete().await()
            } catch (e: Exception) {
                Log.w(TAG, "Error deleting task in Firestore: ${e.message}")
            }
        }
    }

    // ==================== CALENDAR ====================
    fun observeEvents(): Flow<List<CalendarReminder>> {
        val uid = auth.currentUser?.uid
        return if (uid != null) {
            db.collection("users").document(uid).collection("events")
                .snapshots()
                .map { snapshot ->
                    val remote = snapshot.toObjects(CalendarReminder::class.java)
                    if (remote.isNotEmpty()) remote else localEvents.value
                }
                .catch { e ->
                    Log.w(TAG, "Falling back to local events: ${e.message}")
                    emit(localEvents.value)
                }
        } else {
            localEvents.asStateFlow()
        }
    }

    suspend fun addEvent(
        title: String,
        date: String,
        time: String = "10:00",
        notes: String = "",
        vibrationTrigger: String = "Pulse",
        panchangam: String = ""
    ) {
        val uid = auth.currentUser?.uid
        val newEvent = CalendarReminder(
            id = UUID.randomUUID().toString(),
            userId = uid ?: "nijo_tv_profile",
            title = title,
            date = date,
            time = time,
            notes = notes,
            vibrationTrigger = vibrationTrigger,
            panchangam = panchangam
        )
        localEvents.value = listOf(newEvent) + localEvents.value

        if (uid != null) {
            try {
                val docRef = db.collection("users").document(uid).collection("events").document(newEvent.id)
                val payload = mapOf(
                    "userId" to uid,
                    "title" to title,
                    "date" to date,
                    "time" to time,
                    "notes" to notes,
                    "vibrationTrigger" to vibrationTrigger,
                    "panchangam" to panchangam,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } catch (e: Exception) {
                Log.w(TAG, "Error adding event to Firestore: ${e.message}")
            }
        }
    }

    // ==================== TRANSFERS & CLIPBOARD ====================
    fun observeTransfers(): Flow<List<TransferItem>> {
        val uid = auth.currentUser?.uid
        return if (uid != null) {
            db.collection("users").document(uid).collection("transfers")
                .snapshots()
                .map { snapshot ->
                    val remote = snapshot.toObjects(TransferItem::class.java)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                    if (remote.isNotEmpty()) remote else localTransfers.value
                }
                .catch { e ->
                    Log.w(TAG, "Falling back to local transfers: ${e.message}")
                    emit(localTransfers.value)
                }
        } else {
            localTransfers.asStateFlow()
        }
    }

    suspend fun pushTransfer(
        title: String,
        content: String,
        type: String = "FILE",
        fileName: String = "",
        fileSize: String = "",
        devices: List<String> = emptyList(),
        pinVerified: Boolean = true,
        sender: String = "laptop",
        target: String = "all",
        downloadUrl: String = ""
    ) {
        val uid = auth.currentUser?.uid
        val newTransfer = TransferItem(
            id = UUID.randomUUID().toString(),
            userId = uid ?: "nijo_tv_profile",
            title = title,
            content = content,
            type = type,
            fileName = fileName,
            fileSize = fileSize,
            devices = devices,
            status = "SYNCED",
            pinVerified = pinVerified,
            sender = sender,
            target = target,
            downloadUrl = downloadUrl
        )
        localTransfers.value = listOf(newTransfer) + localTransfers.value

        if (uid != null) {
            try {
                val docRef = db.collection("users").document(uid).collection("transfers").document(newTransfer.id)
                val payload = mapOf(
                    "userId" to uid,
                    "title" to title,
                    "content" to content,
                    "type" to type,
                    "fileName" to fileName,
                    "fileSize" to fileSize,
                    "devices" to devices,
                    "status" to "SYNCED",
                    "pinVerified" to pinVerified,
                    "sender" to sender,
                    "target" to target,
                    "downloadUrl" to downloadUrl,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } catch (e: Exception) {
                Log.w(TAG, "Error adding transfer to Firestore: ${e.message}")
            }
        }
    }

    suspend fun deleteTransfer(transferId: String) {
        localTransfers.value = localTransfers.value.filter { it.id != transferId }
        val uid = auth.currentUser?.uid
        if (uid != null) {
            try {
                db.collection("users").document(uid).collection("transfers").document(transferId).delete().await()
            } catch (e: Exception) {
                Log.w(TAG, "Error deleting transfer from Firestore: ${e.message}")
            }
        }
    }

    suspend fun clearAllTransfers() {
        localTransfers.value = emptyList()
        val uid = auth.currentUser?.uid
        if (uid != null) {
            try {
                val snapshot = db.collection("users").document(uid).collection("transfers").get().await()
                for (doc in snapshot.documents) {
                    doc.reference.delete().await()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error clearing transfers from Firestore: ${e.message}")
            }
        }
    }

    // ==================== USER SETTINGS ====================
    fun observeSettings(): Flow<UserSettings?> {
        val uid = auth.currentUser?.uid
        return if (uid != null) {
            db.collection("users").document(uid).collection("settings").document("config")
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) snapshot.toObject(UserSettings::class.java) else localSettings.value
                }
                .catch { e ->
                    Log.w(TAG, "Falling back to local settings: ${e.message}")
                    emit(localSettings.value)
                }
        } else {
            localSettings.asStateFlow()
        }
    }

    suspend fun updateSecurityPin(newPin: String) {
        localSettings.value = localSettings.value.copy(securityPin = newPin)

        val uid = auth.currentUser?.uid
        if (uid != null) {
            try {
                val docRef = db.collection("users").document(uid).collection("settings").document("config")
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "securityPin" to newPin,
                        "currentWorkspace" to "study",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } catch (e: Exception) {
                Log.w(TAG, "Error updating PIN in Firestore: ${e.message}")
            }
        }
    }

    suspend fun seedDefaultsIfEmpty() {
        val uid = auth.currentUser?.uid ?: return
        try {
            val tasksSnapshot = db.collection("users").document(uid).collection("tasks").limit(1).get().await()
            if (tasksSnapshot.isEmpty) {
                localTasks.value.forEach { t ->
                    addTask(t.title, t.description, t.category, t.priority, t.workspace, t.dueDate, t.voiceNote)
                }
            }
            val settingsDoc = db.collection("users").document(uid).collection("settings").document("config").get().await()
            if (!settingsDoc.exists()) {
                updateSecurityPin("8492")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Non-fatal seed error: ${e.message}")
        }
    }
}
