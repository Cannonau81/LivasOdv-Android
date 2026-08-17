package it.livasodv.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.time.OffsetDateTime
import java.util.UUID

@Serializable
data class PresidioRecord(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String = "Altro",
    val quantity: Int = 1,
    val available: Boolean = true,
    val notes: String? = null
)

@Serializable
data class AuditEvent(
    val id: String = UUID.randomUUID().toString(),
    val date: String = OffsetDateTime.now().toString(),
    val area: String,
    val action: String,
    val detail: String,
    val actor: String = "App Android"
)

@Serializable
data class AppNotificationItem(
    val id: String = UUID.randomUUID().toString(),
    val date: String = OffsetDateTime.now().toString(),
    val title: String,
    val body: String,
    val level: String = "info",
    val isRead: Boolean = false
)

@Serializable
data class OperationalMission(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val location: String = "",
    val vehicle: String = "",
    val startDate: String = OffsetDateTime.now().toString(),
    val endDate: String? = null,
    val status: String = "Pianificato",
    val notes: String? = null
)

@Serializable
data class TrashRecord(
    val id: String = UUID.randomUUID().toString(),
    val deletedAt: String = OffsetDateTime.now().toString(),
    val expiresAt: String = OffsetDateTime.now().plusDays(30).toString(),
    val kind: String,
    val title: String,
    val payload: String
)


@Serializable
data class MemberCertificationRecord(
    val id: String = UUID.randomUUID().toString(),
    val memberId: String,
    val title: String,
    val issuedAt: String? = null,
    val expiresAt: String? = null,
    val issuer: String = "",
    val notes: String = ""
)

@Serializable
data class LocalBackupData(
    val presidi: List<PresidioRecord> = emptyList(),
    val auditEvents: List<AuditEvent> = emptyList(),
    val notifications: List<AppNotificationItem> = emptyList(),
    val missions: List<OperationalMission> = emptyList(),
    val trash: List<TrashRecord> = emptyList(),
    val certifications: List<MemberCertificationRecord> = emptyList()
)

object LocalManagementStore {
    private const val PREF = "livas_management_v2"
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private lateinit var context: Context

    private val _presidi = MutableStateFlow<List<PresidioRecord>>(emptyList()); val presidi = _presidi.asStateFlow()
    private val _audit = MutableStateFlow<List<AuditEvent>>(emptyList()); val audit = _audit.asStateFlow()
    private val _notifications = MutableStateFlow<List<AppNotificationItem>>(emptyList()); val notifications = _notifications.asStateFlow()
    private val _missions = MutableStateFlow<List<OperationalMission>>(emptyList()); val missions = _missions.asStateFlow()
    private val _trash = MutableStateFlow<List<TrashRecord>>(emptyList()); val trash = _trash.asStateFlow()
    private val _certifications = MutableStateFlow<List<MemberCertificationRecord>>(emptyList()); val certifications = _certifications.asStateFlow()

    fun init(ctx: Context) {
        if (::context.isInitialized) return
        context = ctx.applicationContext
        _presidi.value = read("presidi", PresidioRecord.serializer())
        _audit.value = read("audit", AuditEvent.serializer())
        _notifications.value = read("notifications", AppNotificationItem.serializer())
        _missions.value = read("missions", OperationalMission.serializer())
        _certifications.value = read("certifications", MemberCertificationRecord.serializer())
        _trash.value = read("trash", TrashRecord.serializer()).filter { record ->
            runCatching { OffsetDateTime.parse(record.expiresAt).isAfter(OffsetDateTime.now()) }.getOrDefault(true)
        }
        saveTrash()
    }

    private fun prefs() = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    private fun <T> read(key: String, serializer: kotlinx.serialization.KSerializer<T>): List<T> {
        val raw = prefs().getString(key, null) ?: return emptyList()
        return runCatching { json.decodeFromString(ListSerializer(serializer), raw) }.getOrDefault(emptyList())
    }

    private fun <T> write(key: String, serializer: kotlinx.serialization.KSerializer<T>, values: List<T>) {
        prefs().edit().putString(key, json.encodeToString(ListSerializer(serializer), values)).apply()
    }

    fun addPresidio(item: PresidioRecord) { _presidi.value = (_presidi.value.filterNot { it.id == item.id } + item).sortedBy { it.name.lowercase() }; savePresidi(); log("Presidi", "Salvataggio", item.name) }
    fun deletePresidio(item: PresidioRecord) { _presidi.value = _presidi.value.filterNot { it.id == item.id }; savePresidi(); log("Presidi", "Eliminazione", item.name) }

    fun log(area: String, action: String, detail: String, actor: String = "App Android") {
        _audit.value = listOf(AuditEvent(area = area, action = action, detail = detail, actor = actor)) + _audit.value.take(499)
        saveAudit()
    }

    fun notify(title: String, body: String, level: String = "info") {
        _notifications.value = listOf(AppNotificationItem(title = title, body = body, level = level)) + _notifications.value.take(249)
        saveNotifications()
    }
    fun markNotificationRead(id: String) { _notifications.value = _notifications.value.map { if (it.id == id) it.copy(isRead = true) else it }; saveNotifications() }
    fun markAllNotificationsRead() { _notifications.value = _notifications.value.map { it.copy(isRead = true) }; saveNotifications() }

    fun saveMission(item: OperationalMission) { _missions.value = listOf(item) + _missions.value.filterNot { it.id == item.id }; saveMissions(); log("Operativo", "Salvataggio", item.title) }
    fun deleteMission(item: OperationalMission) { _missions.value = _missions.value.filterNot { it.id == item.id }; saveMissions(); log("Operativo", "Eliminazione", item.title) }

    fun addTrash(record: TrashRecord) { _trash.value = listOf(record) + _trash.value; saveTrash() }
    fun removeTrash(id: String) { _trash.value = _trash.value.filterNot { it.id == id }; saveTrash() }
    fun clearTrash() { _trash.value = emptyList(); saveTrash() }

    fun saveCertification(item: MemberCertificationRecord) {
        _certifications.value = (_certifications.value.filterNot { it.id == item.id } + item).sortedBy { it.title.lowercase() }
        saveCertifications(); log("Soci", "Corso / abilitazione", item.title)
    }
    fun deleteCertification(item: MemberCertificationRecord) {
        _certifications.value = _certifications.value.filterNot { it.id == item.id }; saveCertifications(); log("Soci", "Eliminazione corso", item.title)
    }

    fun snapshot() = LocalBackupData(_presidi.value, _audit.value, _notifications.value, _missions.value, _trash.value, _certifications.value)
    fun restore(data: LocalBackupData) {
        _presidi.value = data.presidi
        _audit.value = data.auditEvents
        _notifications.value = data.notifications
        _missions.value = data.missions
        _trash.value = data.trash
        _certifications.value = data.certifications
        savePresidi(); saveAudit(); saveNotifications(); saveMissions(); saveTrash(); saveCertifications()
    }

    private fun savePresidi() = write("presidi", PresidioRecord.serializer(), _presidi.value)
    private fun saveAudit() = write("audit", AuditEvent.serializer(), _audit.value)
    private fun saveNotifications() = write("notifications", AppNotificationItem.serializer(), _notifications.value)
    private fun saveMissions() = write("missions", OperationalMission.serializer(), _missions.value)
    private fun saveTrash() = write("trash", TrashRecord.serializer(), _trash.value)
    private fun saveCertifications() = write("certifications", MemberCertificationRecord.serializer(), _certifications.value)

    val jsonCodec: Json get() = json
}
