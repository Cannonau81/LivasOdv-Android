package it.livasodv.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class AppRole {
    DIRETTIVO, SOCIO, MAGAZZINO, OLP, SERVIZIO_CIVILE, SERVIZI_SOCIALI;
    companion object {
        fun fromServer(value: String): AppRole = when (value.lowercase()) {
            "admin", "direttivo" -> DIRETTIVO
            "magazzino" -> MAGAZZINO
            "olp" -> OLP
            "servizio_civile" -> SERVIZIO_CIVILE
            "servizi_sociali", "social_services" -> SERVIZI_SOCIALI
            else -> SOCIO
        }
    }
}

@Serializable
data class Profile(
    val id: String,
    val email: String? = null,
    @SerialName("display_name") val displayName: String = "",
    val role: String = "socio",
    @SerialName("member_id") val memberId: String? = null,
    @SerialName("civil_volunteer_id") val civilVolunteerId: String? = null,
    val active: Boolean = true
)

@Serializable
data class Member(
    val id: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val phone: String? = null,
    val email: String? = null,
    @SerialName("role_label") val roleLabel: String? = null,
    val qualifications: List<String> = emptyList(),
    @SerialName("enabled_118") val enabled118: Boolean = false,
    @SerialName("enabled_pc") val enabledPc: Boolean = false,
    @SerialName("enabled_aib") val enabledAib: Boolean = false,
    @SerialName("is_driver") val isDriver: Boolean = false,
    @SerialName("is_active") val isActive: Boolean = true,
    val notes: String? = null
)

@Serializable
data class MemberWrite(
    val id: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val phone: String? = null,
    val email: String? = null,
    @SerialName("role_label") val roleLabel: String? = null,
    val qualifications: List<String> = emptyList(),
    @SerialName("enabled_118") val enabled118: Boolean = false,
    @SerialName("enabled_pc") val enabledPc: Boolean = false,
    @SerialName("enabled_aib") val enabledAib: Boolean = false,
    @SerialName("is_driver") val isDriver: Boolean = false,
    @SerialName("is_active") val isActive: Boolean = true,
    val notes: String? = null
)

@Serializable
data class MemberClothing(
    val id: String,
    @SerialName("member_id") val memberId: String,
    @SerialName("item_name") val itemName: String,
    val area: String,
    val size: String? = null,
    @SerialName("target_quantity") val targetQuantity: Int = 1,
    @SerialName("delivered_quantity") val deliveredQuantity: Int = 0,
    val assigned: Boolean = false,
    @SerialName("delivered_at") val deliveredAt: String? = null,
    val notes: String? = null
)

@Serializable
data class Vehicle(
    val id: String,
    val name: String,
    @SerialName("make_model") val makeModel: String? = null,
    val plate: String? = null,
    val category: String? = null,
    val operational: Boolean = true,
    @SerialName("current_km") val currentKm: Int = 0,
    @SerialName("insurance_company") val insuranceCompany: String? = null,
    @SerialName("insurance_expiry") val insuranceExpiry: String? = null,
    @SerialName("inspection_expiry") val inspectionExpiry: String? = null,
    val notes: String? = null
)

@Serializable
data class VehicleWrite(
    val id: String,
    val name: String,
    @SerialName("make_model") val makeModel: String? = null,
    val plate: String? = null,
    val category: String? = null,
    val operational: Boolean = true,
    @SerialName("current_km") val currentKm: Int = 0,
    @SerialName("insurance_company") val insuranceCompany: String? = null,
    @SerialName("insurance_expiry") val insuranceExpiry: String? = null,
    @SerialName("inspection_expiry") val inspectionExpiry: String? = null,
    val notes: String? = null
)

@Serializable
data class Shift(
    val id: String,
    @SerialName("shift_date") val shiftDate: String,
    val area: String,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    val notes: String? = null,
    @SerialName("created_by") val createdBy: String? = null
)


@Serializable
data class ShiftMember(
    @SerialName("shift_id") val shiftId: String,
    @SerialName("member_id") val memberId: String,
    val status: String = "assegnato"
)

@Serializable
data class ServiceMember(
    @SerialName("service_id") val serviceId: String,
    @SerialName("member_id") val memberId: String,
    val status: String = "assegnato"
)

@Serializable
data class Service(
    val id: String,
    @SerialName("service_date") val serviceDate: String,
    @SerialName("service_type") val serviceType: String,
    val title: String,
    @SerialName("from_place") val fromPlace: String? = null,
    @SerialName("to_place") val toPlace: String? = null,
    @SerialName("vehicle_id") val vehicleId: String? = null,
    val status: String = "programmato",
    val notes: String? = null,
    @SerialName("created_by") val createdBy: String? = null
)

@Serializable
data class WarehouseItem(
    val id: String,
    val name: String,
    val category: String,
    val size: String = "—",
    val quantity: Int = 0,
    @SerialName("minimum_stock") val minimumStock: Int = 0,
    val notes: String? = null
)

@Serializable
data class Communication(
    val id: String,
    @SerialName("communication_date") val communicationDate: String,
    val title: String,
    val body: String,
    val urgent: Boolean = false,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_by") val createdBy: String? = null,
    @SerialName("target_roles") val targetRoles: List<String> = listOf("admin", "direttivo", "socio", "magazzino", "olp", "servizio_civile", "servizi_sociali")
)

@Serializable
data class CommunicationRead(
    @SerialName("communication_id") val communicationId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("read_at") val readAt: String? = null
)

@Serializable
data class CitizenRequest(
    val id: String,
    @SerialName("request_type") val requestType: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val phone: String,
    val email: String? = null,
    val address: String? = null,
    @SerialName("from_place") val fromPlace: String? = null,
    @SerialName("to_place") val toPlace: String? = null,
    @SerialName("requested_at") val requestedAt: String? = null,
    val mobility: String? = null,
    val stairs: String? = null,
    val equipment: String? = null,
    val notes: String? = null,
    @SerialName("privacy_accepted") val privacyAccepted: Boolean = true,
    val status: String = "nuova",
    @SerialName("assigned_vehicle_id") val assignedVehicleId: String? = null,
    @SerialName("is_read") val isRead: Boolean = false
)

@Serializable
data class CitizenRequestInsert(
    val id: String,
    @SerialName("request_type") val requestType: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val phone: String,
    val email: String?,
    val address: String?,
    @SerialName("from_place") val fromPlace: String?,
    @SerialName("to_place") val toPlace: String?,
    @SerialName("requested_at") val requestedAt: String?,
    val mobility: String?,
    val stairs: String?,
    val equipment: String?,
    val notes: String?,
    @SerialName("privacy_accepted") val privacyAccepted: Boolean,
    val status: String,
    @SerialName("assigned_vehicle_id") val assignedVehicleId: String?,
    @SerialName("is_read") val isRead: Boolean
)

@Serializable
data class CivilVolunteer(
    val id: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val phone: String? = null,
    val email: String? = null,
    @SerialName("project_name") val projectName: String? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    val notes: String? = null
)

@Serializable
data class CivilShift(
    val id: String,
    @SerialName("shift_date") val shiftDate: String,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    val activity: String? = null,
    val location: String? = null,
    val notes: String? = null,
    @SerialName("created_by") val createdBy: String? = null
)

@Serializable
data class CivilCourse(
    val id: String,
    val title: String,
    @SerialName("course_date") val courseDate: String,
    val hours: Double,
    val provider: String? = null,
    val notes: String? = null,
    @SerialName("created_by") val createdBy: String? = null
)

@Serializable
data class CivilLeaveRequest(
    val id: String,
    @SerialName("civil_volunteer_id") val civilVolunteerId: String,
    @SerialName("request_type") val requestType: String,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String,
    val reason: String? = null,
    val status: String = "in_attesa",
    @SerialName("decision_note") val decisionNote: String? = null
)


@Serializable
data class CivilShiftVolunteer(
    @SerialName("shift_id") val shiftId: String,
    @SerialName("civil_volunteer_id") val civilVolunteerId: String
)

@Serializable
data class CivilCourseVolunteer(
    @SerialName("course_id") val courseId: String,
    @SerialName("civil_volunteer_id") val civilVolunteerId: String,
    val attended: Boolean = false
)

@Serializable
data class VehicleMaintenance(
    val id: String,
    @SerialName("vehicle_id") val vehicleId: String,
    @SerialName("work_date") val workDate: String,
    @SerialName("work_type") val workType: String,
    val description: String? = null,
    @SerialName("odometer_km") val odometerKm: Int? = null,
    val cost: Double? = null,
    @SerialName("next_due_date") val nextDueDate: String? = null
)

@Serializable
data class VehicleMonthlyKm(
    val id: String,
    @SerialName("vehicle_id") val vehicleId: String,
    val month: String,
    val km: Int,
    val notes: String? = null
)

@Serializable
data class WarehouseMovement(
    val id: String,
    @SerialName("warehouse_item_id") val warehouseItemId: String,
    @SerialName("movement_type") val movementType: String,
    val quantity: Int,
    @SerialName("member_id") val memberId: String? = null,
    @SerialName("member_clothing_id") val memberClothingId: String? = null,
    val note: String? = null,
    @SerialName("performed_by") val performedBy: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
