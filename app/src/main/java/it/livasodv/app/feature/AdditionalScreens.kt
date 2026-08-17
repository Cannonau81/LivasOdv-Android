package it.livasodv.app.feature

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
fun ClothingScreen(onBack: () -> Unit) {
    val repo = AppGraph.repo
    val clothing by repo.clothing.collectAsState()
    val members by repo.members.collectAsState()
    val role by repo.role.collectAsState()
    val canEdit = role == AppRole.DIRETTIVO || role == AppRole.MAGAZZINO
    var selectedMember by remember { mutableStateOf<String?>(null) }
    var selectedArea by remember { mutableStateOf("Tutte") }
    var memberMenu by remember { mutableStateOf(false) }
    var areaMenu by remember { mutableStateOf(false) }
    var addFor by remember { mutableStateOf<Member?>(null) }
    var edit by remember { mutableStateOf<MemberClothing?>(null) }
    var delete by remember { mutableStateOf<MemberClothing?>(null) }
    val scope = rememberCoroutineScope()
    val selected = members.firstOrNull { it.id == selectedMember }
    val shown = remember(clothing, selectedMember, selectedArea) {
        clothing.filter { (selectedMember == null || it.memberId == selectedMember) && (selectedArea == "Tutte" || it.area.equals(selectedArea, true)) }
            .sortedWith(compareBy({ it.area }, { it.itemName }))
    }

    Scaffold(topBar = { LivasTopAppBar(
        title = { Text("Vestizione e Dotazioni", fontWeight = FontWeight.Bold) },
        navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } },
        actions = { IconButton({ scope.launch { repo.refreshAll() } }) { Icon(Icons.Default.Refresh, "Aggiorna") } }
    ) }) { p ->
        LazyColumn(Modifier.padding(p).fillMaxSize(), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Text("Socio", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Box {
                    OutlinedButton({ memberMenu = true }, Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Person, null); Spacer(Modifier.width(8.dp)); Text(selected?.let { "${it.firstName} ${it.lastName}" } ?: "Tutti i soci", Modifier.weight(1f)); Icon(Icons.Default.ArrowDropDown, null)
                    }
                    DropdownMenu(memberMenu, { memberMenu = false }) {
                        DropdownMenuItem(text = { Text("Tutti i soci") }, onClick = { selectedMember = null; memberMenu = false })
                        members.sortedBy { it.lastName }.forEach { m -> DropdownMenuItem(text = { Text("${m.firstName} ${m.lastName}") }, onClick = { selectedMember = m.id; memberMenu = false }) }
                    }
                }
            }
            item {
                Text("Qualifica / Profilo", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Box {
                    OutlinedButton({ areaMenu = true }, Modifier.fillMaxWidth()) {
                        Text(selectedArea, Modifier.weight(1f)); Icon(Icons.Default.ArrowDropDown, null)
                    }
                    DropdownMenu(areaMenu, { areaMenu = false }) {
                        listOf("Tutte", "118", "PC", "AIB").forEach { a -> DropdownMenuItem(text = { Text(when(a){"118"->"118 – Emergenza Sanitaria";"PC"->"Protezione Civile";"AIB"->"AIB – Antincendio Boschivo";else->"Tutte"}) }, onClick = { selectedArea = a; areaMenu = false }) }
                    }
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFEAF3FF))) {
                    Row(Modifier.padding(12.dp), verticalAlignment = androidx.compose.ui.Alignment.Top) {
                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Gli articoli mostrati sono quelli previsti per la qualifica selezionata. Puoi modificarne l’assegnazione singolarmente.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            if (canEdit && selected != null) item {
                Button(onClick = { addFor = selected }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Add, null); Text(" Aggiungi dotazione") }
            }
            if (shown.isEmpty()) item { EmptyState("Nessuna dotazione registrata", "Seleziona un socio oppure genera la vestizione dalla sua scheda.") }
            items(shown, key = { it.id }) { x ->
                val m = members.firstOrNull { it.id == x.memberId }
                AppleCard(Modifier.fillMaxWidth().clickable(enabled = canEdit) { edit = x }) {
                    Row(Modifier.padding(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(Icons.Default.Checkroom, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(x.itemName, fontWeight = FontWeight.Bold)
                            if (selectedMember == null) Text(m?.let { "${it.firstName} ${it.lastName}" } ?: x.memberId, style = MaterialTheme.typography.labelSmall)
                            Text("${x.area} · Taglia ${x.size ?: "—"}", style = MaterialTheme.typography.bodySmall)
                            Text("Consegnato: ${x.deliveredAt ?: "—"}", style = MaterialTheme.typography.labelSmall)
                        }
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                            StatusPill(if (x.assigned) "Assegna" else "Non assegnare", x.assigned)
                            Spacer(Modifier.height(5.dp))
                            Text("${x.deliveredQuantity}/${x.targetQuantity}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            if (selectedMember != null) item {
                val all = clothing.filter { it.memberId == selectedMember }
                val assigned = all.count { it.assigned }
                val delivered = all.sumOf { it.deliveredQuantity }
                val target = all.sumOf { it.targetQuantity }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFE9F7ED))) { Column(Modifier.padding(12.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) { Text("Voci assegnate", style = MaterialTheme.typography.labelSmall); Text("$assigned / ${all.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) } }
                    Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFE9F7ED))) { Column(Modifier.padding(12.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) { Text("Pezzi consegnati", style = MaterialTheme.typography.labelSmall); Text("$delivered / $target", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) } }
                }
            }
        }
    }
    addFor?.let { m -> ClothingDialog(null, m.id, { addFor = null }) { v -> scope.launch { repo.saveClothing(v); addFor = null } } }
    edit?.let { x -> ClothingDialog(x, x.memberId, { edit = null }) { v -> scope.launch { repo.saveClothing(v); edit = null } } }
    delete?.let { x -> ConfirmDelete("Eliminare ${x.itemName} dalla vestizione?", { delete = null }) { scope.launch { repo.deleteClothing(x.id); delete = null } } }
}

@Composable
private fun ClothingDialog(existing: MemberClothing?, memberId: String, dismiss: () -> Unit, save: (MemberClothing) -> Unit) {
    var item by remember { mutableStateOf(existing?.itemName ?: "") }
    var area by remember { mutableStateOf(existing?.area ?: "118") }
    var size by remember { mutableStateOf(existing?.size ?: "") }
    var target by remember { mutableStateOf((existing?.targetQuantity ?: 1).toString()) }
    var delivered by remember { mutableStateOf((existing?.deliveredQuantity ?: 0).toString()) }
    var assigned by remember { mutableStateOf(existing?.assigned ?: true) }
    var date by remember { mutableStateOf(existing?.deliveredAt ?: "") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    AlertDialog(onDismissRequest = dismiss, title = { Text(if (existing == null) "Nuova dotazione" else "Modifica dotazione") }, text = {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item { OutlinedTextField(item, { item = it }, label = { Text("Articolo") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(area, { area = it.uppercase() }, label = { Text("Area 118 / PC / AIB") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(size, { size = it.uppercase() }, label = { Text("Taglia") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(target, { target = it.filter(Char::isDigit) }, label = { Text("Quantità prevista") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(delivered, { delivered = it.filter(Char::isDigit) }, label = { Text("Quantità consegnata") }, modifier = Modifier.fillMaxWidth()) }
            item { ToggleLine("Assegnato", assigned) { assigned = it } }
            item { OutlinedTextField(date, { date = it }, label = { Text("Data consegna YYYY-MM-DD") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(notes, { notes = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth()) }
        }
    }, confirmButton = { Button({ save(MemberClothing(existing?.id ?: AppGraph.repo.newId(), memberId, item.trim(), area.ifBlank { "118" }, size.ifBlank { null }, target.toIntOrNull() ?: 1, delivered.toIntOrNull() ?: 0, assigned, date.ifBlank { null }, notes.ifBlank { null })) }, enabled = item.isNotBlank()) { Text("Salva") } }, dismissButton = { TextButton(dismiss) { Text("Annulla") } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CivilServiceScreen(onBack: () -> Unit) {
    val repo = AppGraph.repo
    val volunteers by repo.civilVolunteers.collectAsState()
    val shifts by repo.civilShifts.collectAsState()
    val courses by repo.civilCourses.collectAsState()
    val leave by repo.civilLeave.collectAsState()
    val profile by repo.profile.collectAsState()
    val role by repo.role.collectAsState()
    val canManage = role == AppRole.DIRETTIVO || role == AppRole.OLP
    val selfCivilId = profile?.civilVolunteerId
    var section by remember { mutableStateOf(0) }
    var add by remember { mutableStateOf(false) }
    var deleteVolunteer by remember { mutableStateOf<CivilVolunteer?>(null) }
    var deleteShift by remember { mutableStateOf<CivilShift?>(null) }
    var deleteCourse by remember { mutableStateOf<CivilCourse?>(null) }
    var decide by remember { mutableStateOf<CivilLeaveRequest?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(topBar = { LivasTopAppBar(title = { Text("Servizio Civile") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } }, actions = {
        if (canManage || (section == 3 && role == AppRole.SERVIZIO_CIVILE)) IconButton({ add = true }) { Icon(Icons.Default.Add, "Aggiungi") }
        IconButton({ scope.launch { repo.refreshAll() } }) { Icon(Icons.Default.Refresh, "Aggiorna") }
    }) }) { p ->
        Column(Modifier.padding(p).fillMaxSize()) {
            SingleChoiceSegmentedButtonRow(Modifier.padding(12.dp).fillMaxWidth()) {
                listOf("Ragazzi", "Turni", "Corsi", "Richieste").forEachIndexed { index, label ->
                    SegmentedButton(selected = section == index, onClick = { section = index; add = false }, shape = SegmentedButtonDefaults.itemShape(index, 4), label = { Text(label) })
                }
            }
            when (section) {
                0 -> LazyColumn(contentPadding = PaddingValues(12.dp)) {
                    if (volunteers.isEmpty()) item { EmptyState("Nessun volontario", "Aggiungi i ragazzi del Servizio Civile e i dati del progetto.") }
                    items(volunteers.sortedBy { it.lastName }, key = { it.id }) { v -> ListItem(headlineContent = { Text("${v.lastName} ${v.firstName}", fontWeight = FontWeight.Bold) }, supportingContent = { Text("${v.projectName ?: "—"} · ${v.startDate ?: "—"} → ${v.endDate ?: "—"}\n${v.phone ?: ""} ${v.email ?: ""}") }, leadingContent = { Icon(Icons.Default.Badge, null) }, trailingContent = if (canManage) {{ IconButton({ deleteVolunteer = v }) { Icon(Icons.Default.Delete, null) } }} else null); HorizontalDivider() }
                }
                1 -> LazyColumn(contentPadding = PaddingValues(12.dp)) {
                    if (shifts.isEmpty()) item { EmptyState("Nessun turno", "Registra i turni del Servizio Civile.") }
                    items(shifts.sortedByDescending { it.shiftDate }, key = { it.id }) { s -> ListItem(headlineContent = { Text(s.activity ?: "Turno", fontWeight = FontWeight.Bold) }, supportingContent = { Text("${s.shiftDate} · ${s.startTime ?: "—"}–${s.endTime ?: "—"} · ${s.location ?: "—"}") }, leadingContent = { Icon(Icons.Default.Schedule, null) }, trailingContent = if (canManage) {{ IconButton({ deleteShift = s }) { Icon(Icons.Default.Delete, null) } }} else null); HorizontalDivider() }
                }
                2 -> LazyColumn(contentPadding = PaddingValues(12.dp)) {
                    if (courses.isEmpty()) item { EmptyState("Nessun corso", "Registra corsi, ore e ente formatore.") }
                    items(courses.sortedByDescending { it.courseDate }, key = { it.id }) { c -> ListItem(headlineContent = { Text(c.title, fontWeight = FontWeight.Bold) }, supportingContent = { Text("${c.courseDate} · ${c.hours} ore · ${c.provider ?: "—"}") }, leadingContent = { Icon(Icons.Default.School, null) }, trailingContent = if (canManage) {{ IconButton({ deleteCourse = c }) { Icon(Icons.Default.Delete, null) } }} else null); HorizontalDivider() }
                }
                3 -> LazyColumn(contentPadding = PaddingValues(12.dp)) {
                    val visibleLeave = if (canManage) leave else leave.filter { it.civilVolunteerId == selfCivilId }
                    if (visibleLeave.isEmpty()) item { EmptyState("Nessuna richiesta", "Ferie, permessi e malattia compariranno qui.") }
                    items(visibleLeave.sortedByDescending { it.startDate }, key = { it.id }) { l ->
                        ListItem(headlineContent = { Text(l.requestType, fontWeight = FontWeight.Bold) }, supportingContent = { Text("${l.startDate} → ${l.endDate} · ${l.status}\n${l.reason ?: ""}${l.decisionNote?.let { "\nDecisione: $it" } ?: ""}") }, leadingContent = { Icon(Icons.Default.EventBusy, null) }, trailingContent = if (canManage && l.status == "in_attesa") {{ TextButton({ decide = l }) { Text("Valuta") } }} else null)
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (add) when (section) {
        0 -> if (canManage) SimpleAddDialog("Nuovo volontario", listOf("Nome", "Cognome", "Telefono", "Email", "Progetto", "Inizio YYYY-MM-DD", "Fine YYYY-MM-DD", "Note"), { add = false }) { v -> scope.launch { repo.saveCivilVolunteer(CivilVolunteer(repo.newId(), v[0], v[1], v[2].ifBlank { null }, v[3].ifBlank { null }, v[4].ifBlank { null }, v[5].ifBlank { null }, v[6].ifBlank { null }, true, v[7].ifBlank { null })); add = false } }
        1 -> if (canManage) SimpleAddDialog("Nuovo turno SC", listOf("Data YYYY-MM-DD", "Inizio HH:MM", "Fine HH:MM", "Attività", "Luogo", "Note"), { add = false }) { v -> scope.launch { repo.saveCivilShift(CivilShift(repo.newId(), v[0], v[1].ifBlank { null }, v[2].ifBlank { null }, v[3].ifBlank { null }, v[4].ifBlank { null }, v[5].ifBlank { null }, repo.currentUserId())); add = false } }
        2 -> if (canManage) SimpleAddDialog("Nuovo corso", listOf("Titolo", "Data YYYY-MM-DD", "Ore", "Ente", "Note"), { add = false }) { v -> scope.launch { repo.saveCivilCourse(CivilCourse(repo.newId(), v[0], v[1], v[2].toDoubleOrNull() ?: 0.0, v[3].ifBlank { null }, v[4].ifBlank { null }, repo.currentUserId())); add = false } }
        3 -> {
            val civilId = if (role == AppRole.SERVIZIO_CIVILE) selfCivilId else volunteers.firstOrNull()?.id
            if (civilId != null) SimpleAddDialog("Nuova richiesta", listOf("Tipo (ferie/permesso/malattia)", "Dal YYYY-MM-DD", "Al YYYY-MM-DD", "Motivo"), { add = false }) { v -> scope.launch { repo.saveCivilLeave(CivilLeaveRequest(repo.newId(), civilId, v[0], v[1], v[2], v[3].ifBlank { null })); add = false } }
            else add = false
        }
    }
    deleteVolunteer?.let { x -> ConfirmDelete("Eliminare ${x.firstName} ${x.lastName}?", { deleteVolunteer = null }) { scope.launch { repo.deleteCivilVolunteer(x.id); deleteVolunteer = null } } }
    deleteShift?.let { x -> ConfirmDelete("Eliminare il turno del ${x.shiftDate}?", { deleteShift = null }) { scope.launch { repo.deleteCivilShift(x.id); deleteShift = null } } }
    deleteCourse?.let { x -> ConfirmDelete("Eliminare il corso ${x.title}?", { deleteCourse = null }) { scope.launch { repo.deleteCivilCourse(x.id); deleteCourse = null } } }
    decide?.let { x -> CivilDecisionDialog(x, { decide = null }) { status, note -> scope.launch { repo.decideCivilLeave(x.id, status, note); decide = null } } }
}

@Composable
private fun CivilDecisionDialog(req: CivilLeaveRequest, dismiss: () -> Unit, decide: (String, String?) -> Unit) {
    var note by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = dismiss, title = { Text("Valuta richiesta") }, text = { Column { Text("${req.requestType}: ${req.startDate} → ${req.endDate}"); Spacer(Modifier.height(8.dp)); OutlinedTextField(note, { note = it }, label = { Text("Nota decisione") }) } }, confirmButton = { Row { TextButton({ decide("rifiutata", note.ifBlank { null }) }) { Text("Rifiuta") }; Button({ decide("approvata", note.ifBlank { null }) }) { Text("Approva") } } }, dismissButton = { TextButton(dismiss) { Text("Annulla") } })
}
