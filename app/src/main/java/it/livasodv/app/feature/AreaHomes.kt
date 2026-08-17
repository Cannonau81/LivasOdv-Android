package it.livasodv.app.feature

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.livasodv.app.data.*
import it.livasodv.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun MemberHomeScreen(onRoute: (String) -> Unit) {
    val r = AppGraph.repo
    val profile by r.profile.collectAsState()
    val shifts by r.shifts.collectAsState()
    val services by r.services.collectAsState()
    val shiftLinks by r.shiftMembers.collectAsState()
    val serviceLinks by r.serviceMembers.collectAsState()
    val communications by r.communications.collectAsState()
    val memberId = profile?.memberId
    val myShiftIds = if (memberId == null) emptySet() else shiftLinks.filter { it.memberId == memberId }.map { it.shiftId }.toSet()
    val myServiceIds = if (memberId == null) emptySet() else serviceLinks.filter { it.memberId == memberId }.map { it.serviceId }.toSet()
    val nextShift = shifts.filter { it.id in myShiftIds }.sortedBy { it.shiftDate }.firstOrNull()
    val nextService = services.filter { it.id in myServiceIds }.sortedBy { it.serviceDate }.firstOrNull()
    val unread = communications.count { !it.isRead }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Groups, null, Modifier.size(54.dp), tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(12.dp))
                Column { Text("Area Soci", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("Attività operative e turni", color = LivasMuted) }
            }
        }
        nextShift?.let { s -> item { HighlightCard("Prossimo turno", s.area, "${s.shiftDate} · ${s.startTime ?: "—"}–${s.endTime ?: "—"}", Icons.Default.CalendarMonth) } }
        nextService?.let { s -> item { HighlightCard("Prossimo servizio", s.title, "${s.fromPlace ?: "—"} → ${s.toPlace ?: "—"}", Icons.Default.MedicalServices) } }
        item { AreaAction(Icons.Default.CalendarMonth, "Turni", "Consulta i turni dell'associazione") { onRoute("shifts") } }
        item { AreaAction(Icons.Default.DirectionsWalk, "Servizi sociali", "Accompagnamenti, visite, ricoveri e dimissioni") { onRoute("services") } }
        item { AreaAction(Icons.Default.Notifications, "Comunicazioni", if (unread > 0) "$unread comunicazioni da leggere" else "Comunicazioni del Direttivo") { onRoute("communications") } }
        item { AreaAction(Icons.Default.Event, "Assistenza eventi", "Manifestazioni, sagre e feste") { onRoute("services") } }
    }
}

@Composable
fun WarehouseHomeScreen(onRoute: (String) -> Unit) {
    val r = AppGraph.repo
    val items by r.warehouse.collectAsState()
    val movements by r.warehouseMovements.collectAsState()
    val members by r.members.collectAsState()
    val total = items.sumOf { it.quantity }
    val low = items.count { it.quantity <= it.minimumStock }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { HeaderBlock(Icons.Default.Inventory2, "Area Magazzino", "Inventario e dotazioni soci") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniMetric("Disponibili", total, "Pezzi", Modifier.weight(1f))
                MiniMetric("Sotto soglia", low, "Da reintegrare", Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniMetric("Movimenti", movements.size, "Registrati", Modifier.weight(1f))
                MiniMetric("Soci", members.size, "Vestizione", Modifier.weight(1f))
            }
        }
        item { AreaAction(Icons.Default.Inventory2, "Inventario", "Giacenze, taglie e soglie minime") { onRoute("warehouse") } }
        item { AreaAction(Icons.Default.SwapHoriz, "Storico movimenti", "Entrate, uscite e consegne") { onRoute("warehouse_movements") } }
        item { AreaAction(Icons.Default.Checkroom, "Consegna a un socio", "Vestizione e dotazioni") { onRoute("clothing") } }
        item { AreaAction(Icons.Default.MedicalServices, "Presidi", "Archivio separato dal magazzino") { onRoute("presidi") } }
        if (low > 0) item {
            Card { Column(Modifier.padding(14.dp)) { Text("Scorte da reintegrare", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold); items.filter { it.quantity <= it.minimumStock }.take(6).forEach { Text("${it.name} · ${it.quantity}/${it.minimumStock}") } } }
        }
    }
}

@Composable
fun SocialServicesHomeScreen(onRoute: (String) -> Unit) {
    val r = AppGraph.repo
    val req by r.requests.collectAsState()
    val services by r.services.collectAsState()
    val pending = req.count { it.status == "nuova" || it.status == "presa_in_carico" }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { HeaderBlock(Icons.Default.MedicalServices, "Servizi Sociali", "Richieste cittadini e servizi programmati") }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { MiniMetric("Richieste", pending, "Da gestire", Modifier.weight(1f)); MiniMetric("Servizi", services.size, "Registrati", Modifier.weight(1f)) } }
        item { AreaAction(Icons.Default.Inbox, "Richieste cittadini", "Visite, presidi e dimissioni") { onRoute("requests") } }
        item { AreaAction(Icons.Default.MedicalServices, "Servizi", "Gestione trasporti e accompagnamenti") { onRoute("services") } }
    }
}

@Composable
fun CivilDashboardHomeScreen(onRoute: (String) -> Unit) {
    val r = AppGraph.repo
    val volunteers by r.civilVolunteers.collectAsState()
    val shifts by r.civilShifts.collectAsState()
    val courses by r.civilCourses.collectAsState()
    val leave by r.civilLeave.collectAsState()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { HeaderBlock(Icons.Default.School, "Servizio Civile", "Gestione OLP e operatori") }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { MiniMetric("Ragazzi", volunteers.count { it.isActive }, "Attivi", Modifier.weight(1f)); MiniMetric("Turni", shifts.size, "Registrati", Modifier.weight(1f)) } }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { MiniMetric("Corsi", courses.size, "Formazione", Modifier.weight(1f)); MiniMetric("Richieste", leave.count { it.status == "in_attesa" }, "Da gestire", Modifier.weight(1f)) } }
        item { AreaAction(Icons.Default.ManageAccounts, "Gestione completa", "Ragazzi, turni, corsi, ferie e permessi") { onRoute("civil") } }
    }
}

@Composable
fun ProfileAreaScreen(area: AccessArea, onRoute: (String) -> Unit, onLogout: () -> Unit) {
    val p by AppGraph.repo.profile.collectAsState()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { HeaderBlock(Icons.Default.AccountCircle, "Profilo", area.title) }
        item { Card { Column(Modifier.padding(16.dp)) { Text(p?.displayName?.ifBlank { p?.email ?: "Account" } ?: "Account", fontWeight = FontWeight.Bold); Text(p?.email ?: "—"); Text("Ruolo server: ${p?.role ?: "—"}") } } }
        if (area == AccessArea.SOCI) item { AreaAction(Icons.Default.Checkroom, "Vestizione e dotazioni", "La mia scheda") { onRoute("clothing") } }
        item { AreaAction(Icons.Default.Lock, "Privacy", "Informativa privacy") { onRoute("privacy") } }
        item { Button(onLogout, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Logout, null); Text(" Esci dall'area") } }
    }
}

@Composable
fun WarehouseMovementsScreen(onBack: () -> Unit) {
    val r = AppGraph.repo
    val movements by r.warehouseMovements.collectAsState()
    val items by r.warehouse.collectAsState()
    val members by r.members.collectAsState()
    var add by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Scaffold(topBar = { LivasTopAppBar(title = { Text("Movimenti magazzino") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } }, actions = { IconButton({ add = true }) { Icon(Icons.Default.Add, "Nuovo") } }) }) { p ->
        LazyColumn(Modifier.padding(p), contentPadding = PaddingValues(12.dp)) {
            if (movements.isEmpty()) item { EmptyState("Nessun movimento", "Carichi e scarichi compariranno qui.") }
            items(movements.sortedByDescending { it.createdAt ?: "" }, key = { it.id }) { m ->
                val item = items.firstOrNull { it.id == m.warehouseItemId }
                val member = members.firstOrNull { it.id == m.memberId }
                ListItem(
                    headlineContent = { Text(item?.name ?: "Articolo", fontWeight = FontWeight.Bold) },
                    supportingContent = { Text("${m.movementType} · ${m.quantity} pz${member?.let { " · ${it.firstName} ${it.lastName}" } ?: ""}${m.note?.let { "\n$it" } ?: ""}") },
                    leadingContent = { Icon(if (m.movementType.lowercase().contains("carico")) Icons.Default.AddBox else Icons.Default.Outbox, null) }
                )
                HorizontalDivider()
            }
        }
    }
    if (add) WarehouseMovementDialog(items, members, { add = false }) { itemId, delta, type, memberId, note ->
        scope.launch { r.recordWarehouseMovement(itemId, delta, type, memberId, note); add = false }
    }
}

@Composable
private fun WarehouseMovementDialog(items: List<WarehouseItem>, members: List<Member>, dismiss: () -> Unit, save: (String, Int, String, String?, String?) -> Unit) {
    var itemId by remember { mutableStateOf(items.firstOrNull()?.id ?: "") }
    var memberId by remember { mutableStateOf<String?>(null) }
    var qty by remember { mutableStateOf("1") }
    var type by remember { mutableStateOf("Carico") }
    var note by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = dismiss, title = { Text("Nuovo movimento") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ChoiceButton("Articolo", items.firstOrNull { it.id == itemId }?.name ?: "Seleziona", items.map { it.id to it.name }) { itemId = it }
            ChoiceButton("Tipo", type, listOf("Carico" to "Carico", "Consegna socio" to "Consegna socio", "Rientro" to "Rientro", "Scarico manuale" to "Scarico manuale")) { type = it }
            OutlinedTextField(qty, { qty = it }, label = { Text("Quantità") })
            if (type == "Consegna socio") ChoiceButton("Socio", members.firstOrNull { it.id == memberId }?.let { "${it.firstName} ${it.lastName}" } ?: "Seleziona", members.map { it.id to "${it.firstName} ${it.lastName}" }) { memberId = it }
            OutlinedTextField(note, { note = it }, label = { Text("Note") })
        }
    }, confirmButton = {
        Button({
            val q = qty.toIntOrNull()?.coerceAtLeast(1) ?: 1
            val delta = if (type == "Carico" || type == "Rientro") q else -q
            save(itemId, delta, type, memberId, note.ifBlank { null })
        }, enabled = itemId.isNotBlank()) { Text("Salva") }
    }, dismissButton = { TextButton(dismiss) { Text("Annulla") } })
}

@Composable
private fun ChoiceButton(label: String, value: String, options: List<Pair<String, String>>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box { OutlinedButton({ expanded = true }, modifier = Modifier.fillMaxWidth()) { Text("$label: $value", Modifier.weight(1f)); Icon(Icons.Default.ArrowDropDown, null) }; DropdownMenu(expanded, { expanded = false }) { options.forEach { o -> DropdownMenuItem({ Text(o.second) }, { onSelect(o.first); expanded = false }) } } }
}

@Composable
fun ManagementCenterScreen(onBack: () -> Unit, onRoute: (String) -> Unit) {
    val r = AppGraph.repo
    val vehicles by r.vehicles.collectAsState(); val warehouse by r.warehouse.collectAsState(); val requests by r.requests.collectAsState()
    val expiringVehicles = vehicles.count { !it.insuranceExpiry.isNullOrBlank() || !it.inspectionExpiry.isNullOrBlank() }
    val low = warehouse.count { it.quantity <= it.minimumStock }
    Scaffold(topBar = { LivasTopAppBar(title = { Text("Centro gestione") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } }) }) { p ->
        LazyColumn(Modifier.padding(p), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { HeaderBlock(Icons.Default.DashboardCustomize, "Gestione Direttivo", "Controllo operativo e strumenti") }
            item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { MiniMetric("Scadenze mezzi", expiringVehicles, "Da controllare", Modifier.weight(1f)); MiniMetric("Sotto scorta", low, "Magazzino", Modifier.weight(1f)) } }
            item { MiniMetric("Richieste", requests.count { it.status != "completata" }, "Aperte", Modifier.fillMaxWidth()) }
            item { AreaAction(Icons.Default.Notifications, "Notifiche", "Centro notifiche gestionale") { onRoute("notifications") } }
            item { AreaAction(Icons.Default.Emergency, "Operativo", "Servizi e interventi") { onRoute("operational") } }
            item { AreaAction(Icons.Default.History, "Registro attività", "Audit delle operazioni") { onRoute("audit") } }
            item { AreaAction(Icons.Default.Delete, "Cestino", "Ripristino elementi eliminati") { onRoute("trash") } }
            item { AreaAction(Icons.Default.Event, "Scadenze", "Assicurazioni, revisioni e manutenzioni") { onRoute("expiry") } }
            item { AreaAction(Icons.Default.PictureAsPdf, "Report PDF", "Genera ed esporta") { onRoute("report") } }
            item { AreaAction(Icons.Default.Search, "Ricerca globale", "Soci, mezzi, servizi, magazzino, comunicazioni e presidi") { onRoute("search") } }
            item { AreaAction(Icons.Default.Backup, "Backup", "Esporta e ripristina") { onRoute("backup") } }
            item { AreaAction(Icons.Default.Info, "Informazioni", "Versione, privacy e supporto") { onRoute("app_info") } }
            item { AreaAction(Icons.Default.CalendarMonth, "Turni", "Programmazione operativa") { onRoute("shifts") } }
            item { AreaAction(Icons.Default.Inventory2, "Magazzino", "Scorte e movimenti") { onRoute("warehouse") } }
        }
    }
}

@Composable
private fun HeaderBlock(icon: ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(12.dp)); Column { Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text(subtitle, color = LivasMuted, style = MaterialTheme.typography.bodySmall) } }
}

@Composable
private fun MiniMetric(title: String, value: Int, subtitle: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(14.dp)) { Text("$value", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black); Text(title, fontWeight = FontWeight.Bold); Text(subtitle, style = MaterialTheme.typography.labelSmall, color = LivasMuted) } }
}

@Composable
private fun HighlightCard(section: String, title: String, subtitle: String, icon: ImageVector) {
    Card { Column(Modifier.padding(14.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(8.dp)); Text(section, fontWeight = FontWeight.Bold) }; Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text(subtitle, color = LivasMuted) } }
}

@Composable
private fun AreaAction(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = MaterialTheme.colorScheme.secondary); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = LivasMuted) }; Icon(Icons.Default.ChevronRight, null) } }
}
