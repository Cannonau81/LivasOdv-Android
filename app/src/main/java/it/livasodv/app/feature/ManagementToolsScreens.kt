@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package it.livasodv.app.feature

import it.livasodv.app.BuildConfig
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import it.livasodv.app.data.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

private data class LiveAlert(val id: String, val title: String, val body: String, val level: String)

@Composable
fun NotificationCenterScreen(onBack: () -> Unit) {
    val values by LocalManagementStore.notifications.collectAsState()
    val requests by AppGraph.repo.requests.collectAsState()
    val warehouse by AppGraph.repo.warehouse.collectAsState()
    val vehicles by AppGraph.repo.vehicles.collectAsState()
    val communications by AppGraph.repo.communications.collectAsState()
    val today = LocalDate.now()
    val live = remember(requests, warehouse, vehicles, communications, today) {
        buildList {
            requests.filter { !it.isRead || it.status == "nuova" }.forEach { r -> add(LiveAlert("req-${r.id}", "Nuova richiesta cittadino", "${r.requestType} · ${r.firstName} ${r.lastName} · ${r.phone}", "urgent")) }
            warehouse.filter { it.quantity <= it.minimumStock }.forEach { w -> add(LiveAlert("stock-${w.id}", "Scorta sotto soglia", "${w.name}: ${w.quantity} disponibili, minimo ${w.minimumStock}", "warning")) }
            vehicles.forEach { v ->
                listOf("Assicurazione" to v.insuranceExpiry, "Revisione" to v.inspectionExpiry).forEach { (label, raw) ->
                    raw?.let { parseDate(it) }?.let { date ->
                        val days = java.time.temporal.ChronoUnit.DAYS.between(today, date)
                        if (days <= 30) add(LiveAlert("vehicle-${v.id}-$label", "$label · ${v.name}", if (days < 0) "Scaduta il $date" else "Scade il $date ($days giorni)", if (days <= 7) "urgent" else "warning"))
                    }
                }
            }
            communications.filter { it.urgent }.take(10).forEach { c -> add(LiveAlert("comm-${c.id}", "Comunicazione urgente", c.title, "urgent")) }
        }
    }
    Scaffold(topBar = { LivasTopAppBar(title = { Text("Notifiche") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } }, actions = { if (values.isNotEmpty()) TextButton({ LocalManagementStore.markAllNotificationsRead() }) { Text("Letto tutto") } }) }) { p ->
        LazyColumn(Modifier.padding(p), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (live.isEmpty() && values.isEmpty()) item { EmptyToolState("Nessuna notifica", "Richieste, scorte, scadenze e avvisi urgenti compariranno qui.", Icons.Default.NotificationsOff) }
            if (live.isNotEmpty()) {
                item { Text("Da controllare", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp)) }
                items(live, key = { it.id }) { item ->
                    ListItem(headlineContent = { Text(item.title, fontWeight = FontWeight.Bold) }, supportingContent = { Text(item.body) }, leadingContent = { Icon(if(item.level == "urgent") Icons.Default.Error else Icons.Default.Warning, null) }); HorizontalDivider()
                }
            }
            if (values.isNotEmpty()) {
                item { Text("Registro notifiche", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp)) }
                items(values, key = { it.id }) { item ->
                    ListItem(
                        modifier = Modifier.clickable { LocalManagementStore.markNotificationRead(item.id) },
                        headlineContent = { Text(item.title, fontWeight = if (item.isRead) FontWeight.Normal else FontWeight.Bold) },
                        supportingContent = { Text("${item.body}\n${formatDateTime(item.date)}") },
                        leadingContent = { Icon(when(item.level){"urgent"->Icons.Default.Error;"warning"->Icons.Default.Warning;"success"->Icons.Default.CheckCircle;else->Icons.Default.Info}, null) }
                    ); HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun AuditLogScreen(onBack: () -> Unit) {
    val values by LocalManagementStore.audit.collectAsState()
    Scaffold(topBar = { LivasTopAppBar(title = { Text("Registro attività") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } }) }) { p ->
        LazyColumn(Modifier.padding(p), contentPadding = PaddingValues(14.dp)) {
            if (values.isEmpty()) item { EmptyToolState("Registro vuoto", "Le operazioni gestionali verranno annotate qui.", Icons.Default.History) }
            items(values, key = { it.id }) { event ->
                ListItem(headlineContent = { Text(event.action, fontWeight = FontWeight.Bold) }, supportingContent = { Text("${event.area} · ${event.detail}\n${event.actor} · ${formatDateTime(event.date)}") }, leadingContent = { Icon(Icons.Default.History, null) }); HorizontalDivider()
            }
        }
    }
}

@Composable
fun OperationalMissionsScreen(onBack: () -> Unit) {
    val values by LocalManagementStore.missions.collectAsState()
    var editing by remember { mutableStateOf<OperationalMission?>(null) }
    var add by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<OperationalMission?>(null) }
    Scaffold(topBar = { LivasTopAppBar(title = { Text("Operativo") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } }, actions = { IconButton({ add = true }) { Icon(Icons.Default.Add, null) } }) }) { p ->
        LazyColumn(Modifier.padding(p), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            if (values.isEmpty()) item { EmptyToolState("Nessun servizio operativo", "Aggiungi interventi, presidi ed attività operative.", Icons.Default.Emergency) }
            items(values, key = { it.id }) { mission ->
                ListItem(
                    modifier = Modifier.clickable { editing = mission },
                    headlineContent = { Row { Text(mission.title, fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); Text(mission.status, style = MaterialTheme.typography.labelMedium) } },
                    supportingContent = { Text("${mission.location.ifBlank { "Luogo non indicato" }} · ${formatDateTime(mission.startDate)}${mission.vehicle.takeIf { it.isNotBlank() }?.let { "\nMezzo: $it" } ?: ""}") },
                    leadingContent = { Icon(Icons.Default.Emergency, null) },
                    trailingContent = { IconButton({ deleting = mission }) { Icon(Icons.Default.Delete, null) } }
                ); HorizontalDivider()
            }
        }
    }
    if (add) MissionEditor(null, { add = false }) { LocalManagementStore.saveMission(it); add = false }
    editing?.let { MissionEditor(it, { editing = null }) { v -> LocalManagementStore.saveMission(v); editing = null } }
    deleting?.let { m -> AlertDialog(onDismissRequest = { deleting = null }, title = { Text("Eliminare attività?") }, text = { Text(m.title) }, confirmButton = { TextButton({ LocalManagementStore.deleteMission(m); deleting = null }) { Text("Elimina") } }, dismissButton = { TextButton({ deleting = null }) { Text("Annulla") } }) }
}

@Composable
private fun MissionEditor(existing: OperationalMission?, onDismiss: () -> Unit, onSave: (OperationalMission) -> Unit) {
    var title by remember(existing) { mutableStateOf(existing?.title ?: "") }
    var location by remember(existing) { mutableStateOf(existing?.location ?: "") }
    var vehicle by remember(existing) { mutableStateOf(existing?.vehicle ?: "") }
    var start by remember(existing) { mutableStateOf(existing?.startDate ?: OffsetDateTime.now().toString()) }
    var end by remember(existing) { mutableStateOf(existing?.endDate ?: "") }
    var status by remember(existing) { mutableStateOf(existing?.status ?: "Pianificato") }
    var notes by remember(existing) { mutableStateOf(existing?.notes ?: "") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if(existing == null) "Nuovo servizio operativo" else "Modifica servizio") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            OutlinedTextField(title, { title = it }, label = { Text("Titolo") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(location, { location = it }, label = { Text("Luogo") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(vehicle, { vehicle = it }, label = { Text("Mezzo") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(start, { start = it }, label = { Text("Inizio ISO") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(end, { end = it }, label = { Text("Fine ISO") }, modifier = Modifier.fillMaxWidth())
            ChoiceFieldLocal("Stato", status, listOf("Pianificato","Attivo","Chiuso")) { status = it }
            OutlinedTextField(notes, { notes = it }, label = { Text("Note") }, minLines = 2, modifier = Modifier.fillMaxWidth())
        }
    }, confirmButton = { TextButton({ onSave(OperationalMission(existing?.id ?: UUID.randomUUID().toString(), title.trim(), location.trim(), vehicle.trim(), start.trim(), end.trim().ifBlank { null }, status, notes.trim().ifBlank { null })) }, enabled = title.isNotBlank()) { Text("Salva") } }, dismissButton = { TextButton(onDismiss) { Text("Annulla") } })
}

@Composable
fun TrashBinScreen(onBack: () -> Unit) {
    val values by LocalManagementStore.trash.collectAsState()
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf<String?>(null) }
    Scaffold(topBar = { LivasTopAppBar(title = { Text("Cestino") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } }, actions = { if(values.isNotEmpty()) TextButton({ LocalManagementStore.clearTrash() }) { Text("Svuota") } }) }) { p ->
        LazyColumn(Modifier.padding(p), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            if (values.isEmpty()) item { EmptyToolState("Cestino vuoto", "Gli elementi eliminati vengono conservati localmente per 30 giorni.", Icons.Default.DeleteOutline) }
            items(values, key = { it.id }) { record ->
                ListItem(
                    headlineContent = { Text(record.title, fontWeight = FontWeight.Bold) },
                    supportingContent = { Text("${record.kind} · eliminato ${formatDateTime(record.deletedAt)}\nScade ${formatDateTime(record.expiresAt)}") },
                    leadingContent = { Icon(Icons.Default.Delete, null) },
                    trailingContent = { Row { IconButton({ scope.launch { val r = AppGraph.repo.restoreTrash(record); message = if(r.isSuccess) "Elemento ripristinato" else r.exceptionOrNull()?.message ?: "Ripristino non riuscito" } }) { Icon(Icons.Default.Restore, "Ripristina") }; IconButton({ LocalManagementStore.removeTrash(record.id) }) { Icon(Icons.Default.DeleteForever, "Elimina definitivamente") } } }
                ); HorizontalDivider()
            }
        }
    }
    message?.let { AlertDialog(onDismissRequest = { message = null }, title = { Text("Cestino") }, text = { Text(it) }, confirmButton = { TextButton({ message = null }) { Text("OK") } }) }
}

private data class ExpiryUi(val title: String, val subtitle: String, val date: LocalDate, val level: Int)

@Composable
fun ExpiryCenterScreen(onBack: () -> Unit) {
    val repo = AppGraph.repo
    val vehicles by repo.vehicles.collectAsState(); val maintenance by repo.vehicleMaintenance.collectAsState()
    val members by repo.members.collectAsState()
    val certifications by LocalManagementStore.certifications.collectAsState()
    val today = LocalDate.now()
    val entries = remember(vehicles, maintenance, members, certifications, today) {
        buildList {
            vehicles.forEach { v ->
                v.insuranceExpiry?.let { parseDate(it)?.let { d -> add(ExpiryUi("Assicurazione · ${v.name}", v.plate ?: "", d, expiryLevel(today,d))) } }
                v.inspectionExpiry?.let { parseDate(it)?.let { d -> add(ExpiryUi("Revisione · ${v.name}", v.plate ?: "", d, expiryLevel(today,d))) } }
            }
            maintenance.forEach { m -> m.nextDueDate?.let { parseDate(it)?.let { d -> add(ExpiryUi("${m.workType} · ${vehicles.firstOrNull { it.id == m.vehicleId }?.name ?: "Mezzo"}", m.description ?: "", d, expiryLevel(today,d))) } } }
            certifications.forEach { c ->
                c.expiresAt?.let { parseDate(it)?.let { d ->
                    val member = members.firstOrNull { it.id == c.memberId }
                    val who = member?.let { "${it.firstName} ${it.lastName}" } ?: "Socio"
                    add(ExpiryUi("${c.title} · $who", c.issuer.ifBlank { "Corso / abilitazione" }, d, expiryLevel(today,d)))
                } }
            }
        }.sortedBy { it.date }
    }
    Scaffold(topBar = { LivasTopAppBar(title = { Text("Scadenziario") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } }) }) { p ->
        LazyColumn(Modifier.padding(p), contentPadding = PaddingValues(14.dp)) {
            if(entries.isEmpty()) item { EmptyToolState("Nessuna scadenza", "Inserisci assicurazioni, revisioni, manutenzioni o scadenze dei corsi/abilitazioni soci.", Icons.Default.EventAvailable) }
            val groups = listOf("Scadute / entro 7 giorni" to entries.filter { it.level <= 1 }, "Entro 30 giorni" to entries.filter { it.level == 2 }, "Successive" to entries.filter { it.level == 3 })
            groups.forEach { (title, list) -> if(list.isNotEmpty()) { item { Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 10.dp)) }; items(list) { e -> ListItem(headlineContent = { Text(e.title, fontWeight = FontWeight.Bold) }, supportingContent = { Text(e.subtitle) }, trailingContent = { Text(e.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), fontWeight = FontWeight.SemiBold) }, leadingContent = { Icon(if(e.level==0) Icons.Default.Error else Icons.Default.Event, null) }); HorizontalDivider() } } }
        }
    }
}

@Composable
fun ReportCenterScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = AppGraph.repo
    val members by repo.members.collectAsState(); val vehicles by repo.vehicles.collectAsState(); val warehouse by repo.warehouse.collectAsState(); val requests by repo.requests.collectAsState(); val missions by LocalManagementStore.missions.collectAsState()
    var message by remember { mutableStateOf<String?>(null) }
    Scaffold(topBar = { LivasTopAppBar(title = { Text("Report PDF") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } }) }) { p ->
        LazyColumn(Modifier.padding(p), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Text("Riepilogo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            item { ListItem(headlineContent = { Text("Soci attivi") }, trailingContent = { Text("${members.count { it.isActive }}") }) }
            item { ListItem(headlineContent = { Text("Mezzi") }, trailingContent = { Text("${vehicles.size}") }) }
            item { ListItem(headlineContent = { Text("Magazzino") }, trailingContent = { Text("${warehouse.size} articoli") }) }
            item { ListItem(headlineContent = { Text("Richieste aperte") }, trailingContent = { Text("${requests.count { it.status != "completata" && it.status != "annullata" }}") }) }
            item { ListItem(headlineContent = { Text("Servizi operativi") }, trailingContent = { Text("${missions.size}") }) }
            item { Button({ runCatching { val file = createReportPdf(context, repo.snapshotServer(), LocalManagementStore.snapshot()); shareFile(context, file, "application/pdf"); LocalManagementStore.log("Report", "PDF generato", file.name) }.onFailure { message = it.message } }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.PictureAsPdf, null); Text(" Genera e condividi PDF") } }
            item { Text("Il report contiene dati organizzativi dell'associazione. Condividilo solo con destinatari autorizzati.", style = MaterialTheme.typography.bodySmall) }
        }
    }
    message?.let { AlertDialog(onDismissRequest = { message = null }, title = { Text("Report") }, text = { Text(it) }, confirmButton = { TextButton({ message = null }) { Text("OK") } }) }
}

@Composable
fun AppInfoScreen(onBack: () -> Unit) {
    Scaffold(topBar = { LivasTopAppBar(title = { Text("Informazioni") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } }) }) { p ->
        LazyColumn(Modifier.padding(p), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { ListItem(headlineContent = { Text("Lì.v.a.s. O.D.V.") }, supportingContent = { Text("Gonnosfanadiga") }, leadingContent = { Icon(Icons.Default.Info, null) }) }
            item { ListItem(headlineContent = { Text("Versione Android") }, supportingContent = { Text("${BuildConfig.VERSION_NAME} · Beta soci · parità iPhone Build 31") }) }
            item { ListItem(headlineContent = { Text("Backend") }, supportingContent = { Text("Supabase condiviso iPhone ↔ Android") }) }
            item { ListItem(headlineContent = { Text("Privacy") }, supportingContent = { Text("Accesso e dati protetti tramite autenticazione e policy RLS") }) }
            item { Text("Supporto: livas.gonnos@tiscali.it · 070 9798990") }
        }
    }
}

@Composable
private fun EmptyToolState(title: String, text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(Modifier.fillMaxWidth().padding(28.dp)) { Icon(icon, null, Modifier.size(46.dp)); Spacer(Modifier.height(10.dp)); Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(text) }
}

@Composable
private fun ChoiceFieldLocal(label: String, value: String, options: List<String>, onChange: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box { OutlinedButton({ open = true }, modifier = Modifier.fillMaxWidth()) { Text("$label: $value", Modifier.weight(1f)); Icon(Icons.Default.ArrowDropDown, null) }; DropdownMenu(open, { open = false }) { options.forEach { DropdownMenuItem({ Text(it) }, { onChange(it); open = false }) } } }
}

private fun parseDate(value: String): LocalDate? = runCatching { LocalDate.parse(value.take(10)) }.getOrNull()
private fun expiryLevel(today: LocalDate, date: LocalDate): Int { val days = java.time.temporal.ChronoUnit.DAYS.between(today, date); return when { days < 0 -> 0; days <= 7 -> 1; days <= 30 -> 2; else -> 3 } }
private fun formatDateTime(value: String): String = runCatching { OffsetDateTime.parse(value).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) }.getOrElse { value.take(16).replace('T',' ') }

private fun createReportPdf(context: Context, server: ServerBackupData, local: LocalBackupData): File {
    val file = File(context.cacheDir, "LIVAS_Report_${System.currentTimeMillis()}.pdf")
    val document = PdfDocument(); val paint = Paint().apply { textSize = 11f; isAntiAlias = true }
    var pageNo = 1; var page = document.startPage(PdfDocument.PageInfo.Builder(595,842,pageNo).create()); var canvas = page.canvas; var y = 46f
    fun line(text: String, size: Float = 11f, bold: Boolean = false) {
        if(y > 800) { document.finishPage(page); pageNo++; page = document.startPage(PdfDocument.PageInfo.Builder(595,842,pageNo).create()); canvas = page.canvas; y = 46f }
        paint.textSize = size; paint.isFakeBoldText = bold; canvas.drawText(text.take(95), 42f, y, paint); y += size + 8f
    }
    line("LÌ.V.A.S. O.D.V. GONNOSFANADIGA", 19f, true); line("Report gestionale Android", 14f, true); line(OffsetDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), 10f); y += 8
    line("RIEPILOGO", 13f, true); line("Soci attivi: ${server.members.count { it.isActive }}"); line("Mezzi registrati: ${server.vehicles.size}"); line("Articoli magazzino: ${server.warehouse.size}"); line("Richieste aperte: ${server.requests.count { it.status != "completata" && it.status != "annullata" }}"); line("Servizi operativi: ${local.missions.size}"); y += 8
    line("SCADENZE MEZZI", 13f, true); server.vehicles.take(20).forEach { v -> v.insuranceExpiry?.let { line("• Assicurazione ${v.name}: $it", 10f) }; v.inspectionExpiry?.let { line("• Revisione ${v.name}: $it", 10f) } }
    y += 8; line("OPERATIVO", 13f, true); if(local.missions.isEmpty()) line("Nessun servizio operativo registrato.",10f) else local.missions.take(20).forEach { line("• ${it.title} — ${it.status} — ${it.location}",10f) }
    document.finishPage(page); FileOutputStream(file).use { document.writeTo(it) }; document.close(); return file
}

private fun shareFile(context: Context, file: File, mime: String) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    val intent = Intent(Intent.ACTION_SEND).apply { type = mime; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
    context.startActivity(Intent.createChooser(intent, "Condividi"))
}
