@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package it.livasodv.app.feature

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.livasodv.app.data.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftsScreen() {
    val r = AppGraph.repo
    val shifts by r.shifts.collectAsState()
    val members by r.members.collectAsState()
    val links by r.shiftMembers.collectAsState()
    val role by r.role.collectAsState()
    val canEdit = role == AppRole.DIRETTIVO
    var editing by remember { mutableStateOf<Shift?>(null) }
    var creating by remember { mutableStateOf(false) }
    var detail by remember { mutableStateOf<Shift?>(null) }
    var delete by remember { mutableStateOf<Shift?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(topBar = { LivasTopAppBar(title = { Text("Turni") }, actions = {
        if (canEdit) IconButton({ creating = true }) { Icon(Icons.Default.Add, "Nuovo turno") }
        IconButton({ scope.launch { r.refreshAll() } }) { Icon(Icons.Default.Refresh, "Aggiorna") }
    }) }) { p ->
        LazyColumn(Modifier.padding(p), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (shifts.isEmpty()) item { EmptyState("Nessun turno", "Crea un turno e assegna direttamente i soci dell'equipaggio.") }
            items(shifts.sortedByDescending { it.shiftDate }, key = { it.id }) { x ->
                val assigned = links.filter { it.shiftId == x.id }
                val driver = assigned.firstOrNull { it.status == "autista" }?.let { l -> members.firstOrNull { it.id == l.memberId } }
                Card(Modifier.fillMaxWidth().clickable { detail = x }) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(x.area, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            AssistChip(onClick = { detail = x }, label = { Text("${assigned.size} soci") })
                        }
                        Text("${x.shiftDate} · ${x.startTime ?: "—"}–${x.endTime ?: "—"}")
                        if (driver != null) Text("Autista: ${driver.firstName} ${driver.lastName}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        if (!x.notes.isNullOrBlank()) Text(x.notes, style = MaterialTheme.typography.bodySmall)
                        if (canEdit) Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton({ editing = x }) { Icon(Icons.Default.Edit, null); Text(" Modifica") }
                            TextButton({ detail = x }) { Icon(Icons.Default.Groups, null); Text(" Equipaggio") }
                            TextButton({ delete = x }) { Icon(Icons.Default.Delete, null); Text(" Elimina") }
                        }
                    }
                }
            }
        }
    }

    if (creating) ShiftEditorDialog(null, members, emptyList(), { creating = false }) { shift, assignments ->
        scope.launch {
            r.saveShift(shift)
            assignments.forEach { (memberId, status) -> r.assignMemberToShift(shift.id, memberId, status) }
            creating = false
        }
    }
    editing?.let { current ->
        val currentLinks = links.filter { it.shiftId == current.id }
        ShiftEditorDialog(current, members, currentLinks, { editing = null }) { shift, assignments ->
            scope.launch {
                r.saveShift(shift)
                val wanted = assignments.map { it.first }.toSet()
                currentLinks.filter { it.memberId !in wanted }.forEach { r.removeMemberFromShift(shift.id, it.memberId) }
                assignments.forEach { (memberId, status) -> r.assignMemberToShift(shift.id, memberId, status) }
                editing = null
            }
        }
    }
    detail?.let { shift ->
        AssignmentDetailDialog(
            title = "Equipaggio turno",
            subtitle = "${shift.area} · ${shift.shiftDate}",
            members = members,
            assignments = links.filter { it.shiftId == shift.id },
            canEdit = canEdit,
            onDismiss = { detail = null },
            onSetRole = { memberId, status -> scope.launch { r.assignMemberToShift(shift.id, memberId, status) } },
            onRemove = { memberId -> scope.launch { r.removeMemberFromShift(shift.id, memberId) } }
        )
    }
    delete?.let { x -> ConfirmDelete("Eliminare il turno del ${x.shiftDate}?", { delete = null }) { scope.launch { r.deleteShift(x.id); delete = null } } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(onBack: () -> Unit) {
    val r = AppGraph.repo
    val services by r.services.collectAsState()
    val members by r.members.collectAsState()
    val vehicles by r.vehicles.collectAsState()
    val links by r.serviceMembers.collectAsState()
    val role by r.role.collectAsState()
    val canEdit = role == AppRole.DIRETTIVO || role == AppRole.SERVIZI_SOCIALI
    var creating by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Service?>(null) }
    var detail by remember { mutableStateOf<Service?>(null) }
    var delete by remember { mutableStateOf<Service?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(topBar = { LivasTopAppBar(title = { Text("Servizi") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } }, actions = {
        if (canEdit) IconButton({ creating = true }) { Icon(Icons.Default.Add, "Nuovo servizio") }
        IconButton({ scope.launch { r.refreshAll() } }) { Icon(Icons.Default.Refresh, "Aggiorna") }
    }) }) { p ->
        LazyColumn(Modifier.padding(p), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (services.isEmpty()) item { EmptyState("Nessun servizio", "Crea accompagnamenti e servizi con mezzo ed equipaggio già assegnati.") }
            items(services.sortedByDescending { it.serviceDate }, key = { it.id }) { x ->
                val assigned = links.filter { it.serviceId == x.id }
                val vehicle = vehicles.firstOrNull { it.id == x.vehicleId }
                val driver = assigned.firstOrNull { it.status == "autista" }?.let { l -> members.firstOrNull { it.id == l.memberId } }
                Card(Modifier.fillMaxWidth().clickable { detail = x }) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(x.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            AssistChip(onClick = { detail = x }, label = { Text(x.status) })
                        }
                        Text("${x.serviceDate} · ${x.serviceType}")
                        Text("${x.fromPlace ?: "—"} → ${x.toPlace ?: "—"}")
                        Text("Mezzo: ${vehicle?.name ?: "Non assegnato"} · Equipaggio: ${assigned.size}", style = MaterialTheme.typography.bodySmall)
                        if (driver != null) Text("Autista: ${driver.firstName} ${driver.lastName}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        if (!x.notes.isNullOrBlank()) Text(x.notes, style = MaterialTheme.typography.bodySmall)
                        if (canEdit) Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton({ editing = x }) { Icon(Icons.Default.Edit, null); Text(" Modifica") }
                            TextButton({ detail = x }) { Icon(Icons.Default.Groups, null); Text(" Equipaggio") }
                            TextButton({ delete = x }) { Icon(Icons.Default.Delete, null); Text(" Elimina") }
                        }
                    }
                }
            }
        }
    }

    if (creating) ServiceEditorDialog(null, members, vehicles, emptyList(), { creating = false }) { service, assignments ->
        scope.launch {
            r.saveService(service)
            assignments.forEach { (memberId, status) -> r.assignMemberToService(service.id, memberId, status) }
            creating = false
        }
    }
    editing?.let { current ->
        val currentLinks = links.filter { it.serviceId == current.id }
        ServiceEditorDialog(current, members, vehicles, currentLinks, { editing = null }) { service, assignments ->
            scope.launch {
                r.saveService(service)
                val wanted = assignments.map { it.first }.toSet()
                currentLinks.filter { it.memberId !in wanted }.forEach { r.removeMemberFromService(service.id, it.memberId) }
                assignments.forEach { (memberId, status) -> r.assignMemberToService(service.id, memberId, status) }
                editing = null
            }
        }
    }
    detail?.let { service ->
        AssignmentDetailDialog(
            title = "Equipaggio servizio",
            subtitle = service.title,
            members = members,
            assignments = links.filter { it.serviceId == service.id },
            canEdit = canEdit,
            onDismiss = { detail = null },
            onSetRole = { memberId, status -> scope.launch { r.assignMemberToService(service.id, memberId, status) } },
            onRemove = { memberId -> scope.launch { r.removeMemberFromService(service.id, memberId) } }
        )
    }
    delete?.let { x -> ConfirmDelete("Eliminare il servizio ${x.title}?", { delete = null }) { scope.launch { r.deleteService(x.id); delete = null } } }
}

@Composable
private fun ShiftEditorDialog(
    existing: Shift?,
    members: List<Member>,
    existingLinks: List<ShiftMember>,
    dismiss: () -> Unit,
    save: (Shift, List<Pair<String, String>>) -> Unit
) {
    var area by remember { mutableStateOf(existing?.area ?: "118") }
    var date by remember { mutableStateOf(existing?.shiftDate ?: "") }
    var start by remember { mutableStateOf(existing?.startTime ?: "") }
    var end by remember { mutableStateOf(existing?.endTime ?: "") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    val selected = remember(existing?.id, members.size) { mutableStateMapOf<String, String>().apply { existingLinks.forEach { put(it.memberId, it.status) } } }
    val repo = AppGraph.repo
    AlertDialog(onDismissRequest = dismiss, title = { Text(if (existing == null) "Nuovo turno" else "Modifica turno") }, text = {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { OutlinedTextField(area, { area = it }, label = { Text("Area 118 / PC / AIB") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(date, { date = it }, label = { Text("Data YYYY-MM-DD") }, modifier = Modifier.fillMaxWidth()) }
            item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(start, { start = it }, label = { Text("Inizio") }, modifier = Modifier.weight(1f)); OutlinedTextField(end, { end = it }, label = { Text("Fine") }, modifier = Modifier.weight(1f)) } }
            item { OutlinedTextField(notes, { notes = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth()) }
            item { Text("Equipaggio", fontWeight = FontWeight.Bold) }
            items(members.filter { it.isActive }.sortedBy { it.lastName.lowercase() }, key = { it.id }) { m -> CrewPickerRow(m, selected) }
        }
    }, confirmButton = {
        Button(onClick = {
            val id = existing?.id ?: repo.newId()
            save(Shift(id, date, area, start.ifBlank { null }, end.ifBlank { null }, notes.ifBlank { null }, existing?.createdBy ?: repo.currentUserId()), selected.map { it.key to it.value })
        }, enabled = area.isNotBlank() && date.isNotBlank()) { Text("Salva") }
    }, dismissButton = { TextButton(dismiss) { Text("Annulla") } })
}

@Composable
private fun ServiceEditorDialog(
    existing: Service?,
    members: List<Member>,
    vehicles: List<Vehicle>,
    existingLinks: List<ServiceMember>,
    dismiss: () -> Unit,
    save: (Service, List<Pair<String, String>>) -> Unit
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var date by remember { mutableStateOf(existing?.serviceDate ?: "") }
    var type by remember { mutableStateOf(existing?.serviceType ?: "visita") }
    var from by remember { mutableStateOf(existing?.fromPlace ?: "") }
    var to by remember { mutableStateOf(existing?.toPlace ?: "") }
    var status by remember { mutableStateOf(existing?.status ?: "programmato") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var vehicleId by remember { mutableStateOf(existing?.vehicleId) }
    var vehicleMenu by remember { mutableStateOf(false) }
    val selected = remember(existing?.id, members.size) { mutableStateMapOf<String, String>().apply { existingLinks.forEach { put(it.memberId, it.status) } } }
    val repo = AppGraph.repo
    AlertDialog(onDismissRequest = dismiss, title = { Text(if (existing == null) "Nuovo servizio" else "Modifica servizio") }, text = {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { OutlinedTextField(title, { title = it }, label = { Text("Titolo / paziente") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(date, { date = it }, label = { Text("Data e ora ISO") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(type, { type = it }, label = { Text("Tipo servizio") }, modifier = Modifier.fillMaxWidth()) }
            item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(from, { from = it }, label = { Text("Da") }, modifier = Modifier.weight(1f)); OutlinedTextField(to, { to = it }, label = { Text("A") }, modifier = Modifier.weight(1f)) } }
            item {
                Box {
                    OutlinedButton({ vehicleMenu = true }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.DirectionsCar, null); Text(" ${vehicles.firstOrNull { it.id == vehicleId }?.name ?: "Seleziona mezzo"}") }
                    DropdownMenu(expanded = vehicleMenu, onDismissRequest = { vehicleMenu = false }) {
                        DropdownMenuItem(text = { Text("Nessun mezzo") }, onClick = { vehicleId = null; vehicleMenu = false })
                        vehicles.filter { it.operational }.forEach { v -> DropdownMenuItem(text = { Text("${v.name} · ${v.plate ?: "—"}") }, onClick = { vehicleId = v.id; vehicleMenu = false }) }
                    }
                }
            }
            item { OutlinedTextField(status, { status = it }, label = { Text("Stato") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(notes, { notes = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth()) }
            item { Text("Equipaggio", fontWeight = FontWeight.Bold) }
            items(members.filter { it.isActive }.sortedBy { it.lastName.lowercase() }, key = { it.id }) { m -> CrewPickerRow(m, selected) }
        }
    }, confirmButton = {
        Button(onClick = {
            val id = existing?.id ?: repo.newId()
            save(Service(id, date, type, title, from.ifBlank { null }, to.ifBlank { null }, vehicleId, status.ifBlank { "programmato" }, notes.ifBlank { null }, existing?.createdBy ?: repo.currentUserId()), selected.map { it.key to it.value })
        }, enabled = title.isNotBlank() && date.isNotBlank() && type.isNotBlank()) { Text("Salva") }
    }, dismissButton = { TextButton(dismiss) { Text("Annulla") } })
}

@Composable
private fun CrewPickerRow(member: Member, selected: MutableMap<String, String>) {
    val current = selected[member.id]
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(10.dp)) {
            Text("${member.firstName} ${member.lastName}", fontWeight = FontWeight.SemiBold)
            if (member.isDriver) Text("Abilitato autista", style = MaterialTheme.typography.labelSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = current == "equipaggio", onClick = { if (current == "equipaggio") selected.remove(member.id) else selected[member.id] = "equipaggio" }, label = { Text("Equipaggio") })
                FilterChip(selected = current == "autista", enabled = member.isDriver, onClick = {
                    if (current == "autista") selected.remove(member.id) else {
                        selected.entries.filter { it.value == "autista" }.map { it.key }.forEach { selected[it] = "equipaggio" }
                        selected[member.id] = "autista"
                    }
                }, label = { Text("Autista") })
            }
        }
    }
}

@Composable
private fun <T> AssignmentDetailDialog(
    title: String,
    subtitle: String,
    members: List<Member>,
    assignments: List<T>,
    canEdit: Boolean,
    onDismiss: () -> Unit,
    onSetRole: (String, String) -> Unit,
    onRemove: (String) -> Unit
) {
    fun memberIdOf(item: T): String = when (item) { is ShiftMember -> item.memberId; is ServiceMember -> item.memberId; else -> "" }
    fun statusOf(item: T): String = when (item) { is ShiftMember -> item.status; is ServiceMember -> item.status; else -> "assegnato" }
    var addMenu by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Column { Text(title); Text(subtitle, style = MaterialTheme.typography.bodySmall) } }, text = {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (assignments.isEmpty()) item { Text("Nessun socio assegnato") }
            items(assignments, key = { memberIdOf(it) }) { a ->
                val m = members.firstOrNull { it.id == memberIdOf(a) }
                if (m != null) ListItem(
                    headlineContent = { Text("${m.firstName} ${m.lastName}") },
                    supportingContent = { Text(if (statusOf(a) == "autista") "Autista" else "Equipaggio") },
                    leadingContent = { Icon(if (statusOf(a) == "autista") Icons.Default.DriveEta else Icons.Default.Person, null) },
                    trailingContent = if (canEdit) {{ Row { if (m.isDriver && statusOf(a) != "autista") IconButton({ onSetRole(m.id, "autista") }) { Icon(Icons.Default.DriveEta, "Imposta autista") }; IconButton({ onRemove(m.id) }) { Icon(Icons.Default.Close, "Rimuovi") } } }} else null
                )
            }
            if (canEdit) item {
                Box {
                    FilledTonalButton({ addMenu = true }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.PersonAdd, null); Text(" Aggiungi socio") }
                    DropdownMenu(expanded = addMenu, onDismissRequest = { addMenu = false }) {
                        val already = assignments.map { memberIdOf(it) }.toSet()
                        members.filter { it.isActive && it.id !in already }.sortedBy { it.lastName.lowercase() }.forEach { m ->
                            DropdownMenuItem(text = { Text("${m.firstName} ${m.lastName}") }, onClick = { onSetRole(m.id, "equipaggio"); addMenu = false })
                        }
                    }
                }
            }
        }
    }, confirmButton = { TextButton(onDismiss) { Text("Chiudi") } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsScreen() {
    val r = AppGraph.repo
    val list by r.requests.collectAsState()
    val vehicles by r.vehicles.collectAsState()
    var assign by remember { mutableStateOf<CitizenRequest?>(null) }
    val scope = rememberCoroutineScope()
    Scaffold(topBar = { LivasTopAppBar(title = { Text("Richieste cittadini") }, actions = { IconButton({ scope.launch { r.refreshAll() } }) { Icon(Icons.Default.Refresh, null) } }) }) { p ->
        LazyColumn(Modifier.padding(p), contentPadding = PaddingValues(12.dp)) {
            if (list.isEmpty()) item { EmptyState("Nessuna richiesta", "Le richieste inviate dai cittadini compariranno qui.") }
            items(list.sortedByDescending { it.requestedAt ?: "" }, key = { it.id }) { x ->
                val assignedName = vehicles.firstOrNull { it.id == x.assignedVehicleId }?.name
                Card(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(x.requestType, fontWeight = FontWeight.Bold)
                        Text("${x.firstName} ${x.lastName} · ${x.phone}")
                        Text("${x.fromPlace ?: x.address ?: "—"} → ${x.toPlace ?: "—"}", style = MaterialTheme.typography.bodySmall)
                        if (!x.mobility.isNullOrBlank()) Text("Mobilità: ${x.mobility}${x.stairs?.let { " · Scale: $it" } ?: ""}", style = MaterialTheme.typography.bodySmall)
                        if (!x.equipment.isNullOrBlank()) Text("Presidio: ${x.equipment}", style = MaterialTheme.typography.bodySmall)
                        if (!x.notes.isNullOrBlank()) Text(x.notes, style = MaterialTheme.typography.bodySmall)
                        if (assignedName != null) Text("Mezzo assegnato: $assignedName", fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            AssistChip(onClick = {}, label = { Text(x.status) })
                            TextButton({ assign = x }) { Text(if (assignedName == null) "Assegna mezzo" else "Cambia mezzo") }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (x.status != "presa_in_carico") TextButton({ scope.launch { r.updateRequestStatus(x.id, "presa_in_carico") } }) { Text("Prendi in carico") }
                            if (x.status != "completata") TextButton({ scope.launch { r.updateRequestStatus(x.id, "completata") } }) { Text("Completa") }
                        }
                    }
                }
            }
        }
    }
    assign?.let { req -> VehicleAssignDialog(req, vehicles, { assign = null }) { id -> scope.launch { r.assignRequestVehicle(req.id, id); assign = null } } }
}

@Composable
private fun VehicleAssignDialog(req: CitizenRequest, vehicles: List<Vehicle>, dismiss: () -> Unit, assign: (String?) -> Unit) {
    AlertDialog(onDismissRequest = dismiss, title = { Text("Assegna mezzo") }, text = {
        LazyColumn {
            item { Text("${req.firstName} ${req.lastName}", fontWeight = FontWeight.Bold) }
            item { ListItem(modifier = Modifier.clickable { assign(null) }, headlineContent = { Text("Nessun mezzo") }, leadingContent = { Icon(Icons.Default.DoNotDisturb, null) }) }
            items(vehicles.filter { it.operational }, key = { it.id }) { v -> ListItem(modifier = Modifier.clickable { assign(v.id) }, headlineContent = { Text(v.name) }, supportingContent = { Text("${v.makeModel ?: "—"} · ${v.plate ?: "—"}") }, leadingContent = { Icon(Icons.Default.DirectionsCar, null) }) }
        }
    }, confirmButton = {}, dismissButton = { TextButton(dismiss) { Text("Chiudi") } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehouseScreen(onBack: () -> Unit) {
    val r = AppGraph.repo
    val list by r.warehouse.collectAsState()
    val role by r.role.collectAsState()
    val canEdit = role == AppRole.DIRETTIVO || role == AppRole.MAGAZZINO
    var editing by remember { mutableStateOf<WarehouseItem?>(null) }
    var adding by remember { mutableStateOf(false) }
    var delete by remember { mutableStateOf<WarehouseItem?>(null) }
    var search by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Tutti") }
    val categories = remember(list) { listOf("Tutti") + list.map { it.category.ifBlank { "Altro" } }.distinct().sorted() }
    val filtered = remember(list, search, category) {
        list.filter { item ->
            (category == "Tutti" || item.category.equals(category, true)) &&
                (search.isBlank() || "${item.name} ${item.category} ${item.size} ${item.notes ?: ""}".contains(search.trim(), true))
        }
    }
    val scope = rememberCoroutineScope()

    Scaffold(topBar = { LivasTopAppBar(title = { Text("Magazzino") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } }, actions = {
        if (canEdit) IconButton({ adding = true }) { Icon(Icons.Default.Add, null) }
        IconButton({ scope.launch { r.refreshAll() } }) { Icon(Icons.Default.Refresh, null) }
    }) }) { p ->
        LazyColumn(Modifier.padding(p), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFEAF2FF))) {
                        Column(Modifier.padding(12.dp)) { Text("Sotto scorta", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium); Text("${list.count { it.quantity <= it.minimumStock }}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black); Text("prodotti", style = MaterialTheme.typography.bodySmall) }
                    }
                    Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFFFF2DF))) {
                        Column(Modifier.padding(12.dp)) { Text("Articoli totali", color = androidx.compose.ui.graphics.Color(0xFFB85C00), style = MaterialTheme.typography.labelMedium); Text("${list.size}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black); Text("prodotti", style = MaterialTheme.typography.bodySmall) }
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(search, { search = it }, placeholder = { Text("Cerca materiale...") }, leadingIcon = { Icon(Icons.Default.Search, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.forEach { c -> FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c) }) }
                }
            }
            if (filtered.isEmpty()) item { EmptyState(if (list.isEmpty()) "Magazzino vuoto" else "Nessun risultato", if (list.isEmpty()) "Registra DPI, vestiario e materiali." else "Modifica ricerca o categoria.") }
            items(filtered.sortedBy { it.name.lowercase() }, key = { it.id }) { x ->
                Card(Modifier.fillMaxWidth().clickable(enabled = canEdit) { editing = x }) {
                    ListItem(
                        headlineContent = { Text(x.name, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("${x.category} · Taglia ${x.size}\nMinimo ${x.minimumStock}${x.notes?.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""}") },
                        leadingContent = { Icon(if (x.quantity <= x.minimumStock) Icons.Default.Warning else Icons.Default.Inventory2, null) },
                        trailingContent = { Column(horizontalAlignment = androidx.compose.ui.Alignment.End) { Text("${x.quantity} pz", fontWeight = FontWeight.Black); if (canEdit) Row { IconButton({ editing = x }) { Icon(Icons.Default.Edit, "Modifica") }; IconButton({ delete = x }) { Icon(Icons.Default.Delete, "Elimina") } } } }
                    )
                }
            }
        }
    }
    if (adding) WarehouseItemEditor(null, { adding = false }) { value -> scope.launch { r.saveWarehouse(value); adding = false } }
    editing?.let { current -> WarehouseItemEditor(current, { editing = null }) { value -> scope.launch { r.saveWarehouse(value); editing = null } } }
    delete?.let { x -> ConfirmDelete("Eliminare ${x.name} dal magazzino?", { delete = null }) { scope.launch { r.deleteWarehouse(x.id); delete = null } } }
}

@Composable
private fun WarehouseItemEditor(existing: WarehouseItem?, dismiss: () -> Unit, save: (WarehouseItem) -> Unit) {
    var name by remember(existing) { mutableStateOf(existing?.name ?: "") }
    var category by remember(existing) { mutableStateOf(existing?.category ?: "DPI") }
    var size by remember(existing) { mutableStateOf(existing?.size ?: "—") }
    var quantity by remember(existing) { mutableStateOf((existing?.quantity ?: 0).toString()) }
    var minimum by remember(existing) { mutableStateOf((existing?.minimumStock ?: 0).toString()) }
    var notes by remember(existing) { mutableStateOf(existing?.notes ?: "") }
    AlertDialog(onDismissRequest = dismiss, title = { Text(if (existing == null) "Nuovo articolo" else "Modifica articolo") }, text = {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item { OutlinedTextField(name, { name = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(category, { category = it }, label = { Text("Categoria") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(size, { size = it }, label = { Text("Taglia / formato") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(quantity, { quantity = it.filter(Char::isDigit) }, label = { Text("Quantità") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(minimum, { minimum = it.filter(Char::isDigit) }, label = { Text("Scorta minima") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(notes, { notes = it }, label = { Text("Note") }, minLines = 2, modifier = Modifier.fillMaxWidth()) }
        }
    }, confirmButton = { Button({ save(WarehouseItem(existing?.id ?: AppGraph.repo.newId(), name.trim(), category.trim().ifBlank { "Altro" }, size.trim().ifBlank { "—" }, quantity.toIntOrNull() ?: 0, minimum.toIntOrNull() ?: 0, notes.trim().ifBlank { null })) }, enabled = name.isNotBlank()) { Text("Salva") } }, dismissButton = { TextButton(dismiss) { Text("Annulla") } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunicationsScreen(onBack: () -> Unit) {
    val r = AppGraph.repo
    val list by r.communications.collectAsState()
    val role by r.role.collectAsState()
    val canEdit = role == AppRole.DIRETTIVO
    var add by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Communication?>(null) }
    var delete by remember { mutableStateOf<Communication?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(topBar = { LivasTopAppBar(title = { Text("Comunicazioni") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } }, actions = {
        if (canEdit) IconButton({ add = true }) { Icon(Icons.Default.Add, null) }
        IconButton({ scope.launch { r.refreshAll() } }) { Icon(Icons.Default.Refresh, null) }
    }) }) { p ->
        LazyColumn(Modifier.padding(p), contentPadding = PaddingValues(12.dp)) {
            if (list.isEmpty()) item { EmptyState("Nessuna comunicazione", "Gli avvisi pubblicati dal Direttivo compariranno qui.") }
            items(list.sortedByDescending { it.communicationDate }, key = { it.id }) { x ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                    scope.launch { r.markCommunicationRead(x.id) }
                    if (canEdit) editing = x
                }) {
                    ListItem(
                        headlineContent = { Text(if (x.urgent) "⚠ ${x.title}" else x.title, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("${x.communicationDate}\n${x.body}") },
                        leadingContent = { Icon(Icons.Default.Campaign, null) },
                        trailingContent = if (canEdit) { { Row { IconButton({ editing = x }) { Icon(Icons.Default.Edit, "Modifica") }; IconButton({ delete = x }) { Icon(Icons.Default.Delete, "Elimina") } } } } else null
                    )
                }
            }
        }
    }
    if (add) CommunicationEditor(null, { add = false }) { value -> scope.launch { r.saveCommunication(value); add = false } }
    editing?.let { current -> CommunicationEditor(current, { editing = null }) { value -> scope.launch { r.saveCommunication(value); editing = null } } }
    delete?.let { x -> ConfirmDelete("Eliminare la comunicazione ${x.title}?", { delete = null }) { scope.launch { r.deleteCommunication(x.id); delete = null } } }
}

@Composable
private fun CommunicationEditor(existing: Communication?, dismiss: () -> Unit, save: (Communication) -> Unit) {
    var title by remember(existing) { mutableStateOf(existing?.title ?: "") }
    var date by remember(existing) { mutableStateOf(existing?.communicationDate ?: java.time.OffsetDateTime.now().toString()) }
    var body by remember(existing) { mutableStateOf(existing?.body ?: "") }
    var urgent by remember(existing) { mutableStateOf(existing?.urgent ?: false) }
    val initialRoles = existing?.targetRoles?.toSet() ?: setOf("admin", "direttivo", "socio", "magazzino", "olp", "servizio_civile", "servizi_sociali")
    var roles by remember(existing) { mutableStateOf(initialRoles) }
    fun roleToggle(role: String, checked: Boolean) { roles = if (checked) roles + role else roles - role }
    AlertDialog(onDismissRequest = dismiss, title = { Text(if (existing == null) "Nuova comunicazione" else "Modifica comunicazione") }, text = {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item { OutlinedTextField(title, { title = it }, label = { Text("Titolo") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(date, { date = it }, label = { Text("Data/ora ISO") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(body, { body = it }, label = { Text("Testo") }, minLines = 4, modifier = Modifier.fillMaxWidth()) }
            item { ToggleLine("Urgente", urgent) { urgent = it } }
            item { Text("Destinatari", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold) }
            item { ToggleLine("Direttivo", roles.contains("direttivo") || roles.contains("admin")) { checked -> roleToggle("direttivo", checked); roleToggle("admin", checked) } }
            item { ToggleLine("Soci", roles.contains("socio")) { roleToggle("socio", it) } }
            item { ToggleLine("Magazzino", roles.contains("magazzino")) { roleToggle("magazzino", it) } }
            item { ToggleLine("Servizi Sociali", roles.contains("servizi_sociali")) { roleToggle("servizi_sociali", it) } }
            item { ToggleLine("OLP", roles.contains("olp")) { roleToggle("olp", it) } }
            item { ToggleLine("Servizio Civile", roles.contains("servizio_civile")) { roleToggle("servizio_civile", it) } }
        }
    }, confirmButton = {
        Button({
            save(Communication(
                existing?.id ?: AppGraph.repo.newId(), date.trim(), title.trim(), body.trim(), urgent, false,
                existing?.createdBy ?: AppGraph.repo.currentUserId(), roles.toList().sorted()
            ))
        }, enabled = title.isNotBlank() && body.isNotBlank() && roles.isNotEmpty()) { Text("Salva") }
    }, dismissButton = { TextButton(dismiss) { Text("Annulla") } })
}

@Composable
fun EmptyState(title: String, body: String) {
    Column(Modifier.fillMaxWidth().padding(28.dp)) { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text(body, style = MaterialTheme.typography.bodyMedium) }
}

@Composable
fun SimpleAddDialog(title: String, labels: List<String>, dismiss: () -> Unit, save: (List<String>) -> Unit) {
    val values = remember { labels.map { mutableStateOf("") } }
    AlertDialog(onDismissRequest = dismiss, title = { Text(title) }, text = {
        LazyColumn { items(labels.size) { i -> OutlinedTextField(values[i].value, { values[i].value = it }, label = { Text(labels[i]) }, modifier = Modifier.fillMaxWidth()) } }
    }, confirmButton = { Button({ save(values.map { it.value }) }, enabled = values.first().value.isNotBlank()) { Text("Salva") } }, dismissButton = { TextButton(dismiss) { Text("Annulla") } })
}

@Composable
fun ConfirmDelete(title: String, dismiss: () -> Unit, confirm: () -> Unit) {
    AlertDialog(onDismissRequest = dismiss, title = { Text("Conferma eliminazione") }, text = { Text(title) }, confirmButton = { Button(confirm) { Text("Elimina") } }, dismissButton = { TextButton(dismiss) { Text("Annulla") } })
}
