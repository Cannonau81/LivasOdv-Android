package it.livasodv.app.feature

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.livasodv.app.data.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersScreen() {
    val repo = AppGraph.repo
    val list by repo.members.collectAsState()
    val role by repo.role.collectAsState()
    val canEdit = role == AppRole.DIRETTIVO
    var selected by remember { mutableStateOf<Member?>(null) }
    var edit by remember { mutableStateOf<Member?>(null) }
    var adding by remember { mutableStateOf(false) }
    var delete by remember { mutableStateOf<Member?>(null) }
    var search by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val filtered = remember(list, search) {
        val q = search.trim()
        if (q.isBlank()) list else list.filter { "${it.firstName} ${it.lastName} ${it.roleLabel ?: ""} ${it.qualifications.joinToString(" ")}".contains(q, true) }
    }

    selected?.let { member ->
        MemberDetailScreen(member = member, onBack = { selected = null }, onEdit = if (canEdit) {{ edit = member }} else null)
        if (edit != null) MemberDialog(edit, onDismiss = { edit = null }) { value ->
            scope.launch { repo.saveMember(value); selected = value; edit = null }
        }
        return
    }

    Scaffold(topBar = {
        LivasTopAppBar(title = { Text("Elenco Soci", fontWeight = FontWeight.Bold) }, actions = {
            IconButton({ scope.launch { repo.refreshAll() } }) { Icon(Icons.Default.Search, "Cerca") }
            if (canEdit) IconButton({ adding = true }) { Icon(Icons.Default.Add, "Aggiungi") }
        })
    }) { p ->
        LazyColumn(Modifier.padding(p).fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
            item {
                Column(Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        placeholder = { Text("Cerca socio...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                    )
                    Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${filtered.size} soci trovati", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        Text("Filtri", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            if (filtered.isEmpty()) item { EmptyState(if (list.isEmpty()) "Nessun socio visibile" else "Nessun risultato", "I dati sono sincronizzati con il server Li.v.a.s.") }
            items(filtered.sortedBy { it.lastName.lowercase() }, key = { it.id }) { m ->
                Column(Modifier.fillMaxWidth().clickable { selected = m }) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                        MemberAvatar(m, Modifier.size(46.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("${m.firstName} ${m.lastName}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text(m.roleLabel ?: if (m.isDriver) "Autista Soccorritore" else "Volontario", style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(5.dp))
                            MemberQualificationBadges(m, compact = true)
                        }
                        if (!m.isActive) StatusPill("Non attivo", false)
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .65f))
                }
            }
            item {
                val completed = list.count { m -> repo.clothing.value.any { it.memberId == m.id && it.assigned } }
                AppleCard(Modifier.padding(14.dp).fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Checkroom, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Riepilogo vestizione", fontWeight = FontWeight.Bold)
                            Text("Con dotazioni: $completed   ·   Da verificare: ${list.size - completed}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }

    if (adding || edit != null) MemberDialog(edit, onDismiss = { adding = false; edit = null }) { value -> scope.launch { repo.saveMember(value); adding = false; edit = null } }
    delete?.let { m -> ConfirmDelete("Eliminare ${m.firstName} ${m.lastName}?", { delete = null }) { scope.launch { repo.deleteMember(m.id); delete = null } } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemberDetailScreen(member: Member, onBack: () -> Unit, onEdit: (() -> Unit)?) {
    val repo = AppGraph.repo
    val clothing by repo.clothing.collectAsState()
    val shifts by repo.shifts.collectAsState()
    val services by repo.services.collectAsState()
    val shiftLinks by repo.shiftMembers.collectAsState()
    val serviceLinks by repo.serviceMembers.collectAsState()
    val certifications by LocalManagementStore.certifications.collectAsState()
    val role by repo.role.collectAsState()
    val canManage = role == AppRole.DIRETTIVO
    val scope = rememberCoroutineScope()
    var assignShift by remember { mutableStateOf(false) }
    var assignService by remember { mutableStateOf(false) }
    var editCertification by remember { mutableStateOf<MemberCertificationRecord?>(null) }
    var addCertification by remember { mutableStateOf(false) }
    var section by remember { mutableIntStateOf(0) }

    val myClothing = clothing.filter { it.memberId == member.id }
    val myShiftIds = shiftLinks.filter { it.memberId == member.id }.map { it.shiftId }.toSet()
    val myServiceIds = serviceLinks.filter { it.memberId == member.id }.map { it.serviceId }.toSet()
    val myShifts = shifts.filter { it.id in myShiftIds }
    val myServices = services.filter { it.id in myServiceIds }
    val myCertifications = certifications.filter { it.memberId == member.id }

    Scaffold(topBar = {
        LivasTopAppBar(
            title = { Text("Dettaglio Socio", fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Indietro") } },
            actions = { if (onEdit != null) TextButton(onEdit) { Text("Modifica", color = androidx.compose.ui.graphics.Color.White) } }
        )
    }) { p ->
        LazyColumn(Modifier.padding(p).fillMaxSize(), contentPadding = PaddingValues(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Column(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    MemberAvatar(member, Modifier.size(104.dp))
                    Spacer(Modifier.height(10.dp))
                    Text("${member.firstName} ${member.lastName}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Text(member.roleLabel ?: "Volontario")
                    Text("Cod. ${member.id.take(8).uppercase()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(10.dp))
                    MemberQualificationBadges(member)
                }
            }
            item {
                AppleCard(Modifier.padding(horizontal = 14.dp).fillMaxWidth()) {
                    Column {
                        DetailLine(Icons.Default.Phone, "Telefono", member.phone ?: "—")
                        HorizontalDivider()
                        DetailLine(Icons.Default.Email, "Email", member.email ?: "—")
                        HorizontalDivider()
                        DetailLine(Icons.Default.Badge, "Stato", if (member.isActive) "Attivo" else "Non attivo")
                        if (!member.notes.isNullOrBlank()) { HorizontalDivider(); DetailLine(Icons.Default.Info, "Note", member.notes) }
                    }
                }
            }
            item {
                ScrollableTabRow(selectedTabIndex = section, edgePadding = 12.dp, containerColor = androidx.compose.ui.graphics.Color.Transparent) {
                    listOf("Dati", "Qualifiche", "Vestizione", "Turni", "Servizi").forEachIndexed { i, label ->
                        Tab(selected = section == i, onClick = { section = i }, text = { Text(label, fontWeight = if (section == i) FontWeight.Bold else FontWeight.Normal) })
                    }
                }
            }
            when (section) {
                0 -> item {
                    AppleCard(Modifier.padding(horizontal = 14.dp).fillMaxWidth()) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Profilo socio", fontWeight = FontWeight.Bold)
                            Text("Ruolo: ${member.roleLabel ?: "Volontario"}")
                            Text("Autista: ${if (member.isDriver) "Sì" else "No"}")
                            Text("Stato: ${if (member.isActive) "Attivo" else "Non attivo"}")
                        }
                    }
                }
                1 -> {
                    item {
                        AppleCard(Modifier.padding(horizontal = 14.dp).fillMaxWidth()) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Profilo Operativo", fontWeight = FontWeight.Bold)
                                if (member.enabled118) { Row(verticalAlignment = Alignment.CenterVertically) { QualificationBadge("118", it.livasodv.app.ui.theme.LivasRed); Spacer(Modifier.width(10.dp)); Text("Emergenza Sanitaria") } }
                                if (member.enabledPc) { Row(verticalAlignment = Alignment.CenterVertically) { QualificationBadge("PC", it.livasodv.app.ui.theme.LivasBlue); Spacer(Modifier.width(10.dp)); Text("Protezione Civile") } }
                                if (member.enabledAib) { Row(verticalAlignment = Alignment.CenterVertically) { QualificationBadge("AIB", it.livasodv.app.ui.theme.LivasGreen); Spacer(Modifier.width(10.dp)); Text("Antincendio Boschivo") } }
                                if (member.isDriver) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.DriveEta, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(10.dp)); Text("Autista") } }
                            }
                        }
                    }
                    if (myCertifications.isNotEmpty()) items(myCertifications) { c -> ListItem(headlineContent = { Text(c.title, fontWeight = FontWeight.Bold) }, supportingContent = { Text("${c.issuer ?: "—"} · ${c.expiresAt ?: "senza scadenza"}") }) }
                    if (canManage) item { OutlinedButton({ addCertification = true }, Modifier.padding(horizontal = 14.dp).fillMaxWidth()) { Icon(Icons.Default.Add, null); Text(" Aggiungi abilitazione/corso") } }
                }
                2 -> {
                    if (canManage) item { FilledTonalButton(onClick = { scope.launch { applyWardrobeTemplate(member, myClothing) } }, modifier = Modifier.padding(horizontal = 14.dp).fillMaxWidth()) { Icon(Icons.Default.AutoAwesome, null); Text(" Genera vestizione dalle qualifiche") } }
                    if (myClothing.isEmpty()) item { EmptyState("Nessuna dotazione", "Le dotazioni assegnate al socio compariranno qui.") }
                    items(myClothing.sortedWith(compareBy({ it.area }, { it.itemName })), key = { it.id }) { x ->
                        AppleCard(Modifier.padding(horizontal = 14.dp).fillMaxWidth()) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Checkroom, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(x.itemName, fontWeight = FontWeight.Bold)
                                    Text("${x.area} · Taglia ${x.size ?: "—"} · ${x.deliveredQuantity}/${x.targetQuantity}", style = MaterialTheme.typography.bodySmall)
                                    Text("Consegnato: ${x.deliveredAt ?: "—"}", style = MaterialTheme.typography.labelSmall)
                                }
                                StatusPill(if (x.assigned) "Assegnato" else "Non assegnato", x.assigned)
                            }
                        }
                    }
                }
                3 -> {
                    if (canManage) item { Button({ assignShift = true }, Modifier.padding(horizontal = 14.dp).fillMaxWidth()) { Icon(Icons.Default.Add, null); Text(" Assegna turno") } }
                    if (myShifts.isEmpty()) item { EmptyState("Nessun turno", "Nessun turno assegnato al socio.") }
                    items(myShifts, key = { it.id }) { x ->
                        AppleCard(Modifier.padding(horizontal = 14.dp).fillMaxWidth()) { Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(x.area, fontWeight = FontWeight.Bold); Text("${x.shiftDate} · ${x.startTime ?: "—"}–${x.endTime ?: "—"}", style = MaterialTheme.typography.bodySmall) }; if (canManage) IconButton({ scope.launch { repo.removeMemberFromShift(x.id, member.id) } }) { Icon(Icons.Default.Close, null) } } }
                    }
                }
                4 -> {
                    if (canManage) item { Button({ assignService = true }, Modifier.padding(horizontal = 14.dp).fillMaxWidth()) { Icon(Icons.Default.Add, null); Text(" Assegna servizio") } }
                    if (myServices.isEmpty()) item { EmptyState("Nessun servizio", "Nessun servizio assegnato al socio.") }
                    items(myServices, key = { it.id }) { x ->
                        AppleCard(Modifier.padding(horizontal = 14.dp).fillMaxWidth()) { Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.MedicalServices, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(x.title, fontWeight = FontWeight.Bold); Text("${x.serviceDate} · ${x.status}", style = MaterialTheme.typography.bodySmall) }; if (canManage) IconButton({ scope.launch { repo.removeMemberFromService(x.id, member.id) } }) { Icon(Icons.Default.Close, null) } } }
                    }
                }
            }
            if (section == 0) item {
                OutlinedButton({ section = 2 }, Modifier.padding(horizontal = 14.dp).fillMaxWidth()) { Icon(Icons.Default.Checkroom, null); Spacer(Modifier.width(8.dp)); Text("Vestizione e Dotazioni", fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); Icon(Icons.Default.ChevronRight, null) }
            }
        }
    }

    if (assignShift) AssignmentDialog("Assegna turno", shifts.filter { it.id !in myShiftIds }.map { Triple(it.id, it.area, "${it.shiftDate} · ${it.startTime ?: "—"}") }, { assignShift = false }) { id -> scope.launch { repo.assignMemberToShift(id, member.id, "equipaggio"); assignShift = false } }
    if (assignService) AssignmentDialog("Assegna servizio", services.filter { it.id !in myServiceIds }.map { Triple(it.id, it.title, it.serviceDate) }, { assignService = false }) { id -> scope.launch { repo.assignMemberToService(id, member.id, "equipaggio"); assignService = false } }
    if (addCertification) CertificationEditor(member.id, null, { addCertification = false }) { LocalManagementStore.saveCertification(it); addCertification = false }
    editCertification?.let { c -> CertificationEditor(member.id, c, { editCertification = null }) { LocalManagementStore.saveCertification(it); editCertification = null } }
}

@Composable
private fun DetailLine(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String) {
    ListItem(headlineContent = { Text(title) }, supportingContent = { Text(value) }, leadingContent = { Icon(icon, null) })
}

@Composable
private fun AssignmentDialog(title: String, rows: List<Triple<String, String, String>>, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn {
                if (rows.isEmpty()) item { Text("Non ci sono elementi disponibili da assegnare.") }
                items(rows, key = { it.first }) { row ->
                    ListItem(
                        modifier = Modifier.clickable { onSelect(row.first) },
                        headlineContent = { Text(row.second, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text(row.third) },
                        trailingContent = { Icon(Icons.Default.AddCircle, null) }
                    )
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onDismiss) { Text("Chiudi") } }
    )
}

@Composable
private fun MemberDialog(existing: Member?, onDismiss: () -> Unit, onSave: (Member) -> Unit) {
    var first by remember { mutableStateOf(existing?.firstName ?: "") }
    var last by remember { mutableStateOf(existing?.lastName ?: "") }
    var email by remember { mutableStateOf(existing?.email ?: "") }
    var phone by remember { mutableStateOf(existing?.phone ?: "") }
    var roleLabel by remember { mutableStateOf(existing?.roleLabel ?: "Socio") }
    var q118 by remember { mutableStateOf(existing?.enabled118 ?: false) }
    var qPc by remember { mutableStateOf(existing?.enabledPc ?: false) }
    var qAib by remember { mutableStateOf(existing?.enabledAib ?: false) }
    var qSoc by remember { mutableStateOf(existing?.qualifications?.contains("__SERVIZI_SOCIALI__") ?: false) }
    var driver by remember { mutableStateOf(existing?.isDriver ?: false) }
    var active by remember { mutableStateOf(existing?.isActive ?: true) }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Nuovo socio" else "Modifica socio") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                item { OutlinedTextField(first, { first = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(last, { last = it }, label = { Text("Cognome") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(phone, { phone = it }, label = { Text("Telefono") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(roleLabel, { roleLabel = it }, label = { Text("Ruolo / incarico") }, modifier = Modifier.fillMaxWidth()) }
                item { Text("Qualifiche", fontWeight = FontWeight.Bold) }
                item { ToggleLine("118", q118) { q118 = it } }
                item { ToggleLine("Protezione Civile", qPc) { qPc = it } }
                item { ToggleLine("AIB", qAib) { qAib = it } }
                item { ToggleLine("Servizi sociali", qSoc) { qSoc = it } }
                item { ToggleLine("Autista", driver) { driver = it } }
                item { ToggleLine("Socio attivo", active) { active = it } }
                item { OutlinedTextField(notes, { notes = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth()) }
            }
        },
        confirmButton = {
            Button({
                val preserved = existing?.qualifications?.filterNot { it in setOf("118", "PC", "AIB", "__SERVIZI_SOCIALI__") }.orEmpty()
                val quals = buildList { addAll(preserved); if (q118) add("118"); if (qPc) add("PC"); if (qAib) add("AIB"); if (qSoc) add("__SERVIZI_SOCIALI__") }
                onSave(Member(existing?.id ?: AppGraph.repo.newId(), first.trim(), last.trim(), phone.ifBlank { null }, email.ifBlank { null }, roleLabel.ifBlank { null }, quals, q118, qPc, qAib, driver, active, notes.ifBlank { null }))
            }, enabled = first.isNotBlank() && last.isNotBlank()) { Text("Salva") }
        },
        dismissButton = { TextButton(onDismiss) { Text("Annulla") } }
    )
}


@Composable
private fun CertificationEditor(memberId: String, existing: MemberCertificationRecord?, dismiss: () -> Unit, save: (MemberCertificationRecord) -> Unit) {
    var title by remember(existing) { mutableStateOf(existing?.title ?: "") }
    var issuer by remember(existing) { mutableStateOf(existing?.issuer ?: "") }
    var issued by remember(existing) { mutableStateOf(existing?.issuedAt ?: "") }
    var expires by remember(existing) { mutableStateOf(existing?.expiresAt ?: "") }
    var notes by remember(existing) { mutableStateOf(existing?.notes ?: "") }
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text(if (existing == null) "Nuovo corso / abilitazione" else "Modifica corso / abilitazione") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                item { OutlinedTextField(title, { title = it }, label = { Text("Nome (es. BLSD)") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(issuer, { issuer = it }, label = { Text("Ente / istruttore") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(issued, { issued = it }, label = { Text("Conseguito YYYY-MM-DD") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(expires, { expires = it }, label = { Text("Scadenza YYYY-MM-DD") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(notes, { notes = it }, label = { Text("Note") }, minLines = 2, modifier = Modifier.fillMaxWidth()) }
                if (existing != null) item { TextButton({ LocalManagementStore.deleteCertification(existing); dismiss() }) { Icon(Icons.Default.Delete, null); Text(" Elimina corso") } }
            }
        },
        confirmButton = { Button({ save(MemberCertificationRecord(existing?.id ?: AppGraph.repo.newId(), memberId, title.trim(), issued.trim().ifBlank { null }, expires.trim().ifBlank { null }, issuer.trim(), notes.trim())) }, enabled = title.isNotBlank()) { Text("Salva") } },
        dismissButton = { TextButton(dismiss) { Text("Annulla") } }
    )
}

@Composable fun ToggleLine(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(label); Switch(value, onChange) }
}
