@file:OptIn(io.github.jan.supabase.annotations.SupabaseExperimental::class)

package it.livasodv.app.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.selectAsFlow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.util.UUID

class AppRepository {
    private val client get() = SupabaseProvider.client

    private val _role = MutableStateFlow(AppRole.SOCIO); val role = _role.asStateFlow()
    private val _profile = MutableStateFlow<Profile?>(null); val profile = _profile.asStateFlow()
    private val _loading = MutableStateFlow(false); val loading = _loading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null); val error = _error.asStateFlow()

    private val _members = MutableStateFlow<List<Member>>(emptyList()); val members = _members.asStateFlow()
    private val _clothing = MutableStateFlow<List<MemberClothing>>(emptyList()); val clothing = _clothing.asStateFlow()
    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList()); val vehicles = _vehicles.asStateFlow()
    private val _shifts = MutableStateFlow<List<Shift>>(emptyList()); val shifts = _shifts.asStateFlow()
    private val _services = MutableStateFlow<List<Service>>(emptyList()); val services = _services.asStateFlow()
    private val _shiftMembers = MutableStateFlow<List<ShiftMember>>(emptyList()); val shiftMembers = _shiftMembers.asStateFlow()
    private val _serviceMembers = MutableStateFlow<List<ServiceMember>>(emptyList()); val serviceMembers = _serviceMembers.asStateFlow()
    private val _warehouse = MutableStateFlow<List<WarehouseItem>>(emptyList()); val warehouse = _warehouse.asStateFlow()
    private val _warehouseMovements = MutableStateFlow<List<WarehouseMovement>>(emptyList()); val warehouseMovements = _warehouseMovements.asStateFlow()
    private val _requests = MutableStateFlow<List<CitizenRequest>>(emptyList()); val requests = _requests.asStateFlow()
    private val _communications = MutableStateFlow<List<Communication>>(emptyList()); val communications = _communications.asStateFlow()
    private val _civilVolunteers = MutableStateFlow<List<CivilVolunteer>>(emptyList()); val civilVolunteers = _civilVolunteers.asStateFlow()
    private val _civilShifts = MutableStateFlow<List<CivilShift>>(emptyList()); val civilShifts = _civilShifts.asStateFlow()
    private val _civilShiftVolunteers = MutableStateFlow<List<CivilShiftVolunteer>>(emptyList()); val civilShiftVolunteers = _civilShiftVolunteers.asStateFlow()
    private val _civilCourses = MutableStateFlow<List<CivilCourse>>(emptyList()); val civilCourses = _civilCourses.asStateFlow()
    private val _civilCourseVolunteers = MutableStateFlow<List<CivilCourseVolunteer>>(emptyList()); val civilCourseVolunteers = _civilCourseVolunteers.asStateFlow()
    private val _civilLeave = MutableStateFlow<List<CivilLeaveRequest>>(emptyList()); val civilLeave = _civilLeave.asStateFlow()
    private val _vehicleMaintenance = MutableStateFlow<List<VehicleMaintenance>>(emptyList()); val vehicleMaintenance = _vehicleMaintenance.asStateFlow()
    private val _vehicleMonthlyKm = MutableStateFlow<List<VehicleMonthlyKm>>(emptyList()); val vehicleMonthlyKm = _vehicleMonthlyKm.asStateFlow()

    fun clearError() { _error.value = null }
    fun enterLocalMode(role: AppRole) {
        _role.value = role
        _profile.value = null
        _error.value = null
        _loading.value = false
    }
    fun newId() = UUID.randomUUID().toString()
    fun currentUserId(): String? = client.auth.currentUserOrNull()?.id

    suspend fun bootstrap(): Result<Unit> = runCatching {
        _loading.value = true
        _error.value = null
        val uid = currentUserId() ?: error("Sessione non valida")
        val p = client.from("profiles").select {
            filter { eq("id", uid) }
        }.decodeSingle<Profile>()
        if (!p.active) error("Account disattivato")
        _profile.value = p
        _role.value = AppRole.fromServer(p.role)
        refreshAll()
    }.onFailure { _error.value = it.message ?: "Errore di sincronizzazione" }
      .also { _loading.value = false }

    suspend fun refreshAll() {
        val r = _role.value
        fetch("Soci") { _members.value = client.from("members").select().decodeList<Member>() }
        fetch("Mezzi") { _vehicles.value = client.from("vehicles").select().decodeList<Vehicle>() }
        if (r == AppRole.DIRETTIVO) {
            fetch("Manutenzioni mezzi") { _vehicleMaintenance.value = client.from("vehicle_maintenance").select().decodeList<VehicleMaintenance>() }
            fetch("Km mezzi") { _vehicleMonthlyKm.value = client.from("vehicle_monthly_km").select().decodeList<VehicleMonthlyKm>() }
        }
        fetch("Turni") { _shifts.value = client.from("shifts").select().decodeList<Shift>() }
        fetch("Assegnazioni turni") { _shiftMembers.value = client.from("shift_members").select().decodeList<ShiftMember>() }
        fetch("Servizi") { _services.value = client.from("services").select().decodeList<Service>() }
        fetch("Assegnazioni servizi") { _serviceMembers.value = client.from("service_members").select().decodeList<ServiceMember>() }
        fetch("Comunicazioni") { _communications.value = client.from("communications").select().decodeList<Communication>() }

        fetch("Vestizione") { _clothing.value = client.from("member_clothing").select().decodeList<MemberClothing>() }
        if (r == AppRole.DIRETTIVO || r == AppRole.MAGAZZINO) {
            fetch("Magazzino") { _warehouse.value = client.from("warehouse_items").select().decodeList<WarehouseItem>() }
            fetch("Movimenti magazzino") { _warehouseMovements.value = client.from("warehouse_movements").select().decodeList<WarehouseMovement>() }
        }
        if (r == AppRole.DIRETTIVO || r == AppRole.SERVIZI_SOCIALI) {
            fetch("Richieste") { _requests.value = client.from("citizen_requests").select().decodeList<CitizenRequest>() }
        }
        if (r == AppRole.DIRETTIVO || r == AppRole.OLP || r == AppRole.SERVIZIO_CIVILE) {
            fetch("Servizio Civile") { _civilVolunteers.value = client.from("civil_volunteers").select().decodeList<CivilVolunteer>() }
            fetch("Turni Servizio Civile") { _civilShifts.value = client.from("civil_shifts").select().decodeList<CivilShift>() }
            fetch("Assegnazioni turni Servizio Civile") { _civilShiftVolunteers.value = client.from("civil_shift_volunteers").select().decodeList<CivilShiftVolunteer>() }
            fetch("Corsi Servizio Civile") { _civilCourses.value = client.from("civil_courses").select().decodeList<CivilCourse>() }
            fetch("Assegnazioni corsi Servizio Civile") { _civilCourseVolunteers.value = client.from("civil_course_volunteers").select().decodeList<CivilCourseVolunteer>() }
            fetch("Ferie Servizio Civile") { _civilLeave.value = client.from("civil_leave_requests").select().decodeList<CivilLeaveRequest>() }
        }
    }

    private suspend fun fetch(label: String, block: suspend () -> Unit) {
        runCatching { block() }.onFailure {
            // RLS può rendere volutamente invisibile una sezione al ruolo corrente.
            if (_error.value == null) _error.value = "$label: ${it.message ?: "accesso non disponibile"}"
        }
    }

    suspend fun saveMember(value: Member) = mutate {
        client.from("members").upsert(
            MemberWrite(value.id, value.firstName, value.lastName, value.phone, value.email, value.roleLabel,
                value.qualifications, value.enabled118, value.enabledPc, value.enabledAib, value.isDriver, value.isActive, value.notes)
        )
        _members.value = client.from("members").select().decodeList()
        LocalManagementStore.log("Soci", "Salvataggio", "${value.firstName} ${value.lastName}")
    }

    suspend fun deleteMember(id: String) = mutate {
        _members.value.firstOrNull { it.id == id }?.let { value ->
            LocalManagementStore.addTrash(TrashRecord(kind = "member", title = "${value.firstName} ${value.lastName}", payload = LocalManagementStore.jsonCodec.encodeToString(value)))
        }
        client.from("members").delete { filter { eq("id", id) } }
        _members.value = client.from("members").select().decodeList()
        LocalManagementStore.log("Soci", "Eliminazione", id)
    }

    suspend fun saveVehicle(value: Vehicle) = mutate {
        client.from("vehicles").upsert(
            VehicleWrite(value.id, value.name, value.makeModel, value.plate, value.category, value.operational,
                value.currentKm, value.insuranceCompany, value.insuranceExpiry, value.inspectionExpiry, value.notes)
        )
        _vehicles.value = client.from("vehicles").select().decodeList()
        LocalManagementStore.log("Mezzi", "Salvataggio", value.name)
    }

    suspend fun deleteVehicle(id: String) = mutate {
        _vehicles.value.firstOrNull { it.id == id }?.let { value ->
            LocalManagementStore.addTrash(TrashRecord(kind = "vehicle", title = value.name, payload = LocalManagementStore.jsonCodec.encodeToString(value)))
        }
        client.from("vehicles").delete { filter { eq("id", id) } }
        _vehicles.value = client.from("vehicles").select().decodeList()
        LocalManagementStore.log("Mezzi", "Eliminazione", id)
    }

    suspend fun saveVehicleMaintenance(value: VehicleMaintenance) = mutate {
        client.from("vehicle_maintenance").upsert(value)
        _vehicleMaintenance.value = client.from("vehicle_maintenance").select().decodeList()
    }

    suspend fun deleteVehicleMaintenance(id: String) = mutate {
        client.from("vehicle_maintenance").delete { filter { eq("id", id) } }
        _vehicleMaintenance.value = client.from("vehicle_maintenance").select().decodeList()
    }

    suspend fun saveVehicleMonthlyKm(value: VehicleMonthlyKm) = mutate {
        client.from("vehicle_monthly_km").upsert(value)
        _vehicleMonthlyKm.value = client.from("vehicle_monthly_km").select().decodeList()
    }

    suspend fun deleteVehicleMonthlyKm(id: String) = mutate {
        client.from("vehicle_monthly_km").delete { filter { eq("id", id) } }
        _vehicleMonthlyKm.value = client.from("vehicle_monthly_km").select().decodeList()
    }

    suspend fun saveShift(value: Shift) = mutate {
        client.from("shifts").upsert(value)
        _shifts.value = client.from("shifts").select().decodeList()
    }

    suspend fun deleteShift(id: String) = mutate {
        client.from("shifts").delete { filter { eq("id", id) } }
        _shifts.value = client.from("shifts").select().decodeList()
    }

    suspend fun assignMemberToShift(shiftId: String, memberId: String, status: String = "assegnato") = mutate {
        client.from("shift_members").upsert(ShiftMember(shiftId, memberId, status))
        _shiftMembers.value = client.from("shift_members").select().decodeList()
    }

    suspend fun removeMemberFromShift(shiftId: String, memberId: String) = mutate {
        client.from("shift_members").delete { filter { eq("shift_id", shiftId); eq("member_id", memberId) } }
        _shiftMembers.value = client.from("shift_members").select().decodeList()
    }

    suspend fun saveService(value: Service) = mutate {
        client.from("services").upsert(value)
        _services.value = client.from("services").select().decodeList()
    }

    suspend fun deleteService(id: String) = mutate {
        _services.value.firstOrNull { it.id == id }?.let { value ->
            LocalManagementStore.addTrash(TrashRecord(kind = "service", title = value.title, payload = LocalManagementStore.jsonCodec.encodeToString(value)))
        }
        client.from("services").delete { filter { eq("id", id) } }
        _services.value = client.from("services").select().decodeList()
        LocalManagementStore.log("Servizi", "Eliminazione", id)
    }

    suspend fun assignMemberToService(serviceId: String, memberId: String, status: String = "assegnato") = mutate {
        client.from("service_members").upsert(ServiceMember(serviceId, memberId, status))
        _serviceMembers.value = client.from("service_members").select().decodeList()
    }

    suspend fun removeMemberFromService(serviceId: String, memberId: String) = mutate {
        client.from("service_members").delete { filter { eq("service_id", serviceId); eq("member_id", memberId) } }
        _serviceMembers.value = client.from("service_members").select().decodeList()
    }

    suspend fun saveWarehouse(value: WarehouseItem) = mutate {
        client.from("warehouse_items").upsert(value)
        _warehouse.value = client.from("warehouse_items").select().decodeList()
    }

    suspend fun deleteWarehouse(id: String) = mutate {
        _warehouse.value.firstOrNull { it.id == id }?.let { value ->
            LocalManagementStore.addTrash(TrashRecord(kind = "warehouse", title = value.name, payload = LocalManagementStore.jsonCodec.encodeToString(value)))
        }
        client.from("warehouse_items").delete { filter { eq("id", id) } }
        _warehouse.value = client.from("warehouse_items").select().decodeList()
        LocalManagementStore.log("Magazzino", "Eliminazione", id)
    }

    suspend fun recordWarehouseMovement(itemId: String, delta: Int, type: String, memberId: String? = null, note: String? = null) = mutate {
        val item = _warehouse.value.firstOrNull { it.id == itemId } ?: error("Articolo non trovato")
        val newQuantity = (item.quantity + delta).coerceAtLeast(0)
        client.from("warehouse_items").update({ set("quantity", newQuantity) }) { filter { eq("id", itemId) } }
        client.from("warehouse_movements").insert(
            WarehouseMovement(newId(), itemId, type, kotlin.math.abs(delta), memberId = memberId, note = note, performedBy = currentUserId())
        )
        _warehouse.value = client.from("warehouse_items").select().decodeList()
        _warehouseMovements.value = client.from("warehouse_movements").select().decodeList()
    }

    suspend fun saveCommunication(value: Communication) = mutate {
        client.from("communications").upsert(value)
        _communications.value = client.from("communications").select().decodeList()
    }

    suspend fun deleteCommunication(id: String) = mutate {
        _communications.value.firstOrNull { it.id == id }?.let { value ->
            LocalManagementStore.addTrash(TrashRecord(kind = "communication", title = value.title, payload = LocalManagementStore.jsonCodec.encodeToString(value)))
        }
        client.from("communications").delete { filter { eq("id", id) } }
        _communications.value = client.from("communications").select().decodeList()
        LocalManagementStore.log("Comunicazioni", "Eliminazione", id)
    }

    suspend fun submitCitizenRequest(value: CitizenRequest): Result<Unit> = runCatching {
        // Usa un DTO senza valori di default: privacy_accepted/status/is_read devono essere
        // inviati esplicitamente, altrimenti kotlinx.serialization può ometterli e la RLS
        // del backend rifiuta correttamente la richiesta pubblica.
        client.from("citizen_requests").insert(
            CitizenRequestInsert(
                id = value.id,
                requestType = value.requestType,
                firstName = value.firstName,
                lastName = value.lastName,
                phone = value.phone,
                email = value.email,
                address = value.address,
                fromPlace = value.fromPlace,
                toPlace = value.toPlace,
                requestedAt = value.requestedAt,
                mobility = value.mobility,
                stairs = value.stairs,
                equipment = value.equipment,
                notes = value.notes,
                privacyAccepted = value.privacyAccepted,
                status = value.status,
                assignedVehicleId = value.assignedVehicleId,
                isRead = value.isRead
            )
        )
    }

    suspend fun updateRequestStatus(id: String, status: String) = mutate {
        client.from("citizen_requests").update({ set("status", status); set("is_read", true) }) {
            filter { eq("id", id) }
        }
        _requests.value = client.from("citizen_requests").select().decodeList()
        LocalManagementStore.log("Richieste", "Stato", "$id → $status")
    }

    suspend fun assignRequestVehicle(id: String, vehicleId: String?) = mutate {
        client.from("citizen_requests").update({ set("assigned_vehicle_id", vehicleId); set("is_read", true) }) { filter { eq("id", id) } }
        _requests.value = client.from("citizen_requests").select().decodeList()
    }

    suspend fun saveClothing(value: MemberClothing) = mutate {
        val previous = _clothing.value.firstOrNull { it.id == value.id }
        val deliveryDelta = value.deliveredQuantity - (previous?.deliveredQuantity ?: 0)
        client.from("member_clothing").upsert(value)
        if (deliveryDelta != 0 && (_role.value == AppRole.DIRETTIVO || _role.value == AppRole.MAGAZZINO)) {
            val item = _warehouse.value.firstOrNull { stock ->
                stock.name.trim().equals(value.itemName.trim(), true) &&
                    (value.size.isNullOrBlank() || stock.size == "—" || stock.size.equals(value.size, true))
            }
            if (item != null) {
                val newQuantity = (item.quantity - deliveryDelta).coerceAtLeast(0)
                client.from("warehouse_items").update({ set("quantity", newQuantity) }) { filter { eq("id", item.id) } }
                client.from("warehouse_movements").insert(
                    WarehouseMovement(newId(), item.id, if (deliveryDelta > 0) "issue" else "returnToStock", kotlin.math.abs(deliveryDelta), memberId = value.memberId, memberClothingId = value.id, note = "Vestizione: ${value.itemName}", performedBy = currentUserId())
                )
                _warehouse.value = client.from("warehouse_items").select().decodeList()
                _warehouseMovements.value = client.from("warehouse_movements").select().decodeList()
            }
        }
        _clothing.value = client.from("member_clothing").select().decodeList()
        LocalManagementStore.log("Vestizione", "Salvataggio", value.itemName)
    }

    suspend fun applyWardrobeTemplate(member: Member, existing: List<MemberClothing>) = mutate {
        data class WardrobeBase(val name: String, val quantity: Int, val areas: Set<String>)
        val template = listOf(
            WardrobeBase("Maglietta", 2, setOf("118", "PC")),
            WardrobeBase("Casacca 118", 1, setOf("118")),
            WardrobeBase("Pantalone 118", 1, setOf("118")),
            WardrobeBase("Felpa", 1, setOf("118", "PC")),
            WardrobeBase("Scarpe", 1, setOf("118", "PC")),
            WardrobeBase("Casco AIB", 1, setOf("AIB")),
            WardrobeBase("Pantalone AIB", 1, setOf("AIB")),
            WardrobeBase("Casacca AIB", 1, setOf("AIB")),
            WardrobeBase("Scarpe AIB", 1, setOf("AIB")),
            WardrobeBase("Occhiali AIB", 1, setOf("AIB")),
            WardrobeBase("Fazzoletto", 1, setOf("118", "PC", "AIB")),
            WardrobeBase("Maglietta AIB", 1, setOf("AIB")),
            WardrobeBase("Maschera antifumo", 1, setOf("AIB")),
            WardrobeBase("Chiavi della sede", 1, setOf("118", "PC", "AIB")),
            WardrobeBase("Pantalone Prot. Civ.", 1, setOf("PC")),
            WardrobeBase("Casacca Prot. Civ.", 1, setOf("PC")),
            WardrobeBase("Maglietta Prot. Civ.", 1, setOf("PC")),
            WardrobeBase("Felpa Prot. Civ.", 1, setOf("PC")),
            WardrobeBase("Giubbotto Prot. Civ.", 1, setOf("PC")),
            WardrobeBase("Scarpe Prot. Civ.", 1, setOf("PC")),
            WardrobeBase("Smanicato tattico Prot. Civ.", 1, setOf("PC"))
        )
        fun eligible(areas: Set<String>): Boolean =
            (member.enabled118 && "118" in areas) ||
            (member.enabledPc && "PC" in areas) ||
            (member.enabledAib && "AIB" in areas)

        val byName = existing.associateBy { it.itemName.trim().lowercase() }
        template.forEach { base ->
            val old = byName[base.name.lowercase()]
            val value = if (old == null) {
                MemberClothing(
                    id = newId(), memberId = member.id, itemName = base.name,
                    area = base.areas.joinToString(","), targetQuantity = base.quantity,
                    assigned = eligible(base.areas)
                )
            } else {
                old.copy(
                    area = base.areas.joinToString(","),
                    targetQuantity = base.quantity,
                    assigned = eligible(base.areas) || old.deliveredQuantity > 0
                )
            }
            client.from("member_clothing").upsert(value)
        }
        _clothing.value = client.from("member_clothing").select().decodeList()
        LocalManagementStore.log("Vestizione", "Template qualifiche", "${member.firstName} ${member.lastName}")
    }

    suspend fun deleteClothing(id: String) = mutate {
        client.from("member_clothing").delete { filter { eq("id", id) } }
        _clothing.value = client.from("member_clothing").select().decodeList()
    }

    suspend fun saveCivilVolunteer(value: CivilVolunteer) = mutate {
        client.from("civil_volunteers").upsert(value)
        _civilVolunteers.value = client.from("civil_volunteers").select().decodeList()
    }

    suspend fun deleteCivilVolunteer(id: String) = mutate {
        client.from("civil_volunteers").delete { filter { eq("id", id) } }
        _civilVolunteers.value = client.from("civil_volunteers").select().decodeList()
    }

    suspend fun saveCivilShift(value: CivilShift) = mutate {
        client.from("civil_shifts").upsert(value)
        _civilShifts.value = client.from("civil_shifts").select().decodeList()
    }

    suspend fun deleteCivilShift(id: String) = mutate {
        client.from("civil_shifts").delete { filter { eq("id", id) } }
        _civilShifts.value = client.from("civil_shifts").select().decodeList()
    }

    suspend fun setCivilShiftVolunteers(shiftId: String, volunteerIds: Set<String>) = mutate {
        client.from("civil_shift_volunteers").delete { filter { eq("shift_id", shiftId) } }
        volunteerIds.forEach { client.from("civil_shift_volunteers").insert(CivilShiftVolunteer(shiftId, it)) }
        _civilShiftVolunteers.value = client.from("civil_shift_volunteers").select().decodeList()
    }

    suspend fun saveCivilCourse(value: CivilCourse) = mutate {
        client.from("civil_courses").upsert(value)
        _civilCourses.value = client.from("civil_courses").select().decodeList()
    }

    suspend fun deleteCivilCourse(id: String) = mutate {
        client.from("civil_courses").delete { filter { eq("id", id) } }
        _civilCourses.value = client.from("civil_courses").select().decodeList()
    }

    suspend fun setCivilCourseVolunteers(courseId: String, volunteerIds: Set<String>) = mutate {
        client.from("civil_course_volunteers").delete { filter { eq("course_id", courseId) } }
        volunteerIds.forEach { client.from("civil_course_volunteers").insert(CivilCourseVolunteer(courseId, it, false)) }
        _civilCourseVolunteers.value = client.from("civil_course_volunteers").select().decodeList()
    }

    suspend fun saveCivilLeave(value: CivilLeaveRequest) = mutate {
        client.from("civil_leave_requests").upsert(value)
        _civilLeave.value = client.from("civil_leave_requests").select().decodeList()
    }

    suspend fun decideCivilLeave(id: String, status: String, note: String? = null) = mutate {
        client.from("civil_leave_requests").update({
            set("status", status); set("decision_note", note); set("decided_by", currentUserId())
        }) { filter { eq("id", id) } }
        _civilLeave.value = client.from("civil_leave_requests").select().decodeList()
    }

    @OptIn(SupabaseExperimental::class)
    suspend fun observeRealtime() = coroutineScope {
        val r = _role.value
        launch { runCatching { client.from("members").selectAsFlow(Member::id).collect { _members.value = it } } }
        launch { runCatching { client.from("vehicles").selectAsFlow(Vehicle::id).collect { _vehicles.value = it } } }
        if (r == AppRole.DIRETTIVO) {
            launch { runCatching { client.from("vehicle_maintenance").selectAsFlow(VehicleMaintenance::id).collect { _vehicleMaintenance.value = it } } }
            launch { runCatching { client.from("vehicle_monthly_km").selectAsFlow(VehicleMonthlyKm::id).collect { _vehicleMonthlyKm.value = it } } }
        }
        launch { runCatching { client.from("shifts").selectAsFlow(Shift::id).collect { _shifts.value = it } } }
        // Le tabelle ponte usano chiavi primarie composte: le riallineiamo periodicamente
        // per evitare cache Realtime ambigue e mantenere iOS/Android coerenti.
        launch { while (true) { delay(4_000); runCatching { _shiftMembers.value = client.from("shift_members").select().decodeList<ShiftMember>() } } }
        launch { runCatching { client.from("services").selectAsFlow(Service::id).collect { _services.value = it } } }
        launch { while (true) { delay(4_000); runCatching { _serviceMembers.value = client.from("service_members").select().decodeList<ServiceMember>() } } }
        launch { runCatching { client.from("communications").selectAsFlow(Communication::id).collect { _communications.value = it } } }
        launch { runCatching { client.from("member_clothing").selectAsFlow(MemberClothing::id).collect { _clothing.value = it } } }
        if (r == AppRole.DIRETTIVO || r == AppRole.MAGAZZINO) {
            launch { runCatching { client.from("warehouse_items").selectAsFlow(WarehouseItem::id).collect { _warehouse.value = it } } }
            launch { runCatching { client.from("warehouse_movements").selectAsFlow(WarehouseMovement::id).collect { _warehouseMovements.value = it } } }
        }
        if (r == AppRole.DIRETTIVO || r == AppRole.SERVIZI_SOCIALI) {
            launch { runCatching { client.from("citizen_requests").selectAsFlow(CitizenRequest::id).collect { _requests.value = it } } }
        }
        if (r == AppRole.DIRETTIVO || r == AppRole.OLP || r == AppRole.SERVIZIO_CIVILE) {
            launch { runCatching { client.from("civil_volunteers").selectAsFlow(CivilVolunteer::id).collect { _civilVolunteers.value = it } } }
            launch { runCatching { client.from("civil_shifts").selectAsFlow(CivilShift::id).collect { _civilShifts.value = it } } }
            launch { while (true) { delay(4_000); runCatching { _civilShiftVolunteers.value = client.from("civil_shift_volunteers").select().decodeList<CivilShiftVolunteer>() } } }
            launch { runCatching { client.from("civil_courses").selectAsFlow(CivilCourse::id).collect { _civilCourses.value = it } } }
            launch { while (true) { delay(4_000); runCatching { _civilCourseVolunteers.value = client.from("civil_course_volunteers").select().decodeList<CivilCourseVolunteer>() } } }
            launch { runCatching { client.from("civil_leave_requests").selectAsFlow(CivilLeaveRequest::id).collect { _civilLeave.value = it } } }
        }
    }

    fun snapshotServer(): ServerBackupData = ServerBackupData(
        members = _members.value, clothing = _clothing.value, vehicles = _vehicles.value,
        vehicleMaintenance = _vehicleMaintenance.value, vehicleMonthlyKm = _vehicleMonthlyKm.value,
        shifts = _shifts.value, shiftMembers = _shiftMembers.value, services = _services.value, serviceMembers = _serviceMembers.value,
        warehouse = _warehouse.value, warehouseMovements = _warehouseMovements.value, communications = _communications.value,
        requests = _requests.value, civilVolunteers = _civilVolunteers.value, civilShifts = _civilShifts.value, civilShiftVolunteers = _civilShiftVolunteers.value,
        civilCourses = _civilCourses.value, civilCourseVolunteers = _civilCourseVolunteers.value, civilLeave = _civilLeave.value
    )

    suspend fun restoreServer(data: ServerBackupData): Result<Unit> = mutate {
        data.members.forEach { client.from("members").upsert(MemberWrite(it.id, it.firstName, it.lastName, it.phone, it.email, it.roleLabel, it.qualifications, it.enabled118, it.enabledPc, it.enabledAib, it.isDriver, it.isActive, it.notes)) }
        data.clothing.forEach { client.from("member_clothing").upsert(it) }
        data.vehicles.forEach { client.from("vehicles").upsert(VehicleWrite(it.id, it.name, it.makeModel, it.plate, it.category, it.operational, it.currentKm, it.insuranceCompany, it.insuranceExpiry, it.inspectionExpiry, it.notes)) }
        data.vehicleMaintenance.forEach { client.from("vehicle_maintenance").upsert(it) }
        data.vehicleMonthlyKm.forEach { client.from("vehicle_monthly_km").upsert(it) }
        data.shifts.forEach { client.from("shifts").upsert(it) }
        data.shiftMembers.forEach { client.from("shift_members").upsert(it) }
        data.services.forEach { client.from("services").upsert(it) }
        data.serviceMembers.forEach { client.from("service_members").upsert(it) }
        data.warehouse.forEach { client.from("warehouse_items").upsert(it) }
        data.warehouseMovements.forEach { client.from("warehouse_movements").upsert(it) }
        data.communications.forEach { client.from("communications").upsert(it) }
        data.requests.forEach { client.from("citizen_requests").upsert(it) }
        data.civilVolunteers.forEach { client.from("civil_volunteers").upsert(it) }
        data.civilShifts.forEach { client.from("civil_shifts").upsert(it) }
        data.civilShiftVolunteers.forEach { client.from("civil_shift_volunteers").upsert(it) }
        data.civilCourses.forEach { client.from("civil_courses").upsert(it) }
        data.civilCourseVolunteers.forEach { client.from("civil_course_volunteers").upsert(it) }
        data.civilLeave.forEach { client.from("civil_leave_requests").upsert(it) }
        refreshAll()
        LocalManagementStore.log("Backup", "Ripristino", "Backup server ripristinato")
    }

    suspend fun restoreTrash(record: TrashRecord): Result<Unit> {
        val codec = LocalManagementStore.jsonCodec
        val result = when (record.kind) {
            "member" -> saveMember(codec.decodeFromString<Member>(record.payload))
            "vehicle" -> saveVehicle(codec.decodeFromString<Vehicle>(record.payload))
            "warehouse" -> saveWarehouse(codec.decodeFromString<WarehouseItem>(record.payload))
            "communication" -> saveCommunication(codec.decodeFromString<Communication>(record.payload))
            "service" -> saveService(codec.decodeFromString<Service>(record.payload))
            else -> Result.failure(IllegalArgumentException("Tipo non ripristinabile"))
        }
        if (result.isSuccess) LocalManagementStore.removeTrash(record.id)
        return result
    }

    suspend fun signOut() {
        runCatching { client.auth.signOut() }
        _profile.value = null
        _role.value = AppRole.SOCIO
        _members.value = emptyList(); _clothing.value = emptyList(); _vehicles.value = emptyList(); _vehicleMaintenance.value = emptyList(); _vehicleMonthlyKm.value = emptyList()
        _shifts.value = emptyList(); _shiftMembers.value = emptyList(); _services.value = emptyList(); _serviceMembers.value = emptyList()
        _warehouse.value = emptyList(); _warehouseMovements.value = emptyList(); _requests.value = emptyList(); _communications.value = emptyList()
        _civilVolunteers.value = emptyList(); _civilShifts.value = emptyList(); _civilShiftVolunteers.value = emptyList(); _civilCourses.value = emptyList(); _civilCourseVolunteers.value = emptyList(); _civilLeave.value = emptyList()
    }

    private suspend fun mutate(block: suspend () -> Unit): Result<Unit> = runCatching {
        _loading.value = true; _error.value = null; block()
    }.onFailure { _error.value = it.message ?: "Operazione non riuscita" }
     .also { _loading.value = false }
}

object AppGraph { val repo = AppRepository() }
