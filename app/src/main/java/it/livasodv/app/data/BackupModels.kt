package it.livasodv.app.data

import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class ServerBackupData(
    val members: List<Member> = emptyList(),
    val clothing: List<MemberClothing> = emptyList(),
    val vehicles: List<Vehicle> = emptyList(),
    val vehicleMaintenance: List<VehicleMaintenance> = emptyList(),
    val vehicleMonthlyKm: List<VehicleMonthlyKm> = emptyList(),
    val shifts: List<Shift> = emptyList(),
    val shiftMembers: List<ShiftMember> = emptyList(),
    val services: List<Service> = emptyList(),
    val serviceMembers: List<ServiceMember> = emptyList(),
    val warehouse: List<WarehouseItem> = emptyList(),
    val warehouseMovements: List<WarehouseMovement> = emptyList(),
    val communications: List<Communication> = emptyList(),
    val requests: List<CitizenRequest> = emptyList(),
    val civilVolunteers: List<CivilVolunteer> = emptyList(),
    val civilShifts: List<CivilShift> = emptyList(),
    val civilShiftVolunteers: List<CivilShiftVolunteer> = emptyList(),
    val civilCourses: List<CivilCourse> = emptyList(),
    val civilCourseVolunteers: List<CivilCourseVolunteer> = emptyList(),
    val civilLeave: List<CivilLeaveRequest> = emptyList()
)

@Serializable
data class LivasFullBackup(
    val generatedAt: String = OffsetDateTime.now().toString(),
    val version: String = "android-ios-parity-final",
    val server: ServerBackupData,
    val local: LocalBackupData
)
