@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package it.livasodv.app.feature

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import it.livasodv.app.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

@Composable
fun MoreScreen(role: AppRole, onRoute: (String) -> Unit, onLogout: () -> Unit) {
    val profile by AppGraph.repo.profile.collectAsState()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        item { Text("Altro", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 10.dp)) }
        if (role == AppRole.DIRETTIVO) {
            item { MoreRow("Gestione 2.1", "Notifiche, operativo, registro, cestino, scadenze e report") { onRoute("management") } }
            item { MoreRow("Magazzino", "Inventario, movimenti e dotazioni") { onRoute("warehouse") } }
            item { MoreRow("Servizio Civile", "OLP, ragazzi, turni, corsi e richieste") { onRoute("civil") } }
            item { MoreRow("Comunicazioni", "Avvisi e news") { onRoute("communications") } }
            item { MoreRow("Richieste cittadini", "Visite, presidi e dimissioni") { onRoute("requests") } }
            item { MoreRow("Turni", "Programmazione operativa") { onRoute("shifts") } }
            item { MoreRow("Vestizione e dotazioni", "Gestione vestiario soci") { onRoute("clothing") } }
            item { MoreRow("Server Li.v.a.s.", "Stato connessione e account") { onRoute("server") } }
            item { MoreRow("Privacy", "Informativa privacy") { onRoute("privacy") } }
        }
        item { Spacer(Modifier.height(10.dp)) }
        item { Button(onLogout, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Logout, null); Text(" Esci dall'area") } }
        item { Text("Ruolo server: ${profile?.role ?: role.name.lowercase()}", Modifier.padding(top = 14.dp), style = MaterialTheme.typography.labelMedium) }
    }
}

@Composable
fun MoreRow(title: String, sub: String, onClick: (() -> Unit)? = null) {
    val m = if (onClick != null) Modifier.fillMaxWidth().clickable { onClick() } else Modifier.fillMaxWidth()
    ListItem(modifier = m, headlineContent = { Text(title, fontWeight = FontWeight.SemiBold) }, supportingContent = { Text(sub) }, trailingContent = { Icon(Icons.Default.ChevronRight, null) })
    HorizontalDivider()
}

private data class SearchRow(val type: String, val title: String, val subtitle: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(onBack: () -> Unit) {
    var q by remember { mutableStateOf("") }
    val r = AppGraph.repo
    val members by r.members.collectAsState(); val vehicles by r.vehicles.collectAsState(); val wh by r.warehouse.collectAsState(); val services by r.services.collectAsState(); val communications by r.communications.collectAsState()
    val presidi by LocalManagementStore.presidi.collectAsState()
    val rows = remember(q, members, vehicles, wh, services, communications, presidi) {
        val query = q.trim()
        if (query.isBlank()) emptyList() else buildList {
            addAll(members.filter { "${it.firstName} ${it.lastName} ${it.email ?: ""} ${it.roleLabel ?: ""} ${it.qualifications.joinToString(" ")}".contains(query, true) }.map { SearchRow("Socio", "${it.firstName} ${it.lastName}", it.roleLabel ?: "Volontario") })
            addAll(vehicles.filter { "${it.name} ${it.plate ?: ""} ${it.makeModel ?: ""}".contains(query, true) }.map { SearchRow("Mezzo", it.name, it.plate ?: it.makeModel ?: "—") })
            addAll(wh.filter { "${it.name} ${it.category} ${it.size}".contains(query, true) }.map { SearchRow("Magazzino", it.name, "${it.quantity} disponibili") })
            addAll(services.filter { "${it.title} ${it.fromPlace ?: ""} ${it.toPlace ?: ""}".contains(query, true) }.map { SearchRow("Servizio", it.title, "${it.fromPlace ?: "—"} → ${it.toPlace ?: "—"}") })
            addAll(communications.filter { "${it.title} ${it.body}".contains(query, true) }.map { SearchRow("Comunicazione", it.title, it.body.take(90)) })
            addAll(presidi.filter { "${it.name} ${it.category} ${it.notes ?: ""}".contains(query, true) }.map { SearchRow("Presidio", it.name, "${it.category} · quantità ${it.quantity}") })
        }
    }
    Scaffold(topBar = { LivasTopAppBar(title = { Text("Ricerca globale") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } }) }) { p ->
        Column(Modifier.padding(p).padding(16.dp)) {
            OutlinedTextField(q, { q = it }, label = { Text("Cerca nell'app") }, leadingIcon = { Icon(Icons.Default.Search, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(8.dp))
            if (q.isBlank()) Text("Cerca un socio, una targa, un servizio, un materiale, una comunicazione o un presidio.", style = MaterialTheme.typography.bodySmall)
            else if (rows.isEmpty()) Text("Nessun risultato per “$q”.", style = MaterialTheme.typography.bodySmall)
            else LazyColumn { items(rows) { row -> ListItem(headlineContent = { Text(row.title, fontWeight = FontWeight.Bold) }, supportingContent = { Text("${row.type} · ${row.subtitle}") }, leadingContent = { Icon(when(row.type){"Socio"->Icons.Default.Person;"Mezzo"->Icons.Default.DirectionsCar;"Magazzino"->Icons.Default.Inventory2;"Servizio"->Icons.Default.MedicalServices;"Comunicazione"->Icons.Default.Campaign;else->Icons.Default.MedicalServices}, null) }); HorizontalDivider() } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = AppGraph.repo
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf<String?>(null) }
    var pendingBackup by remember { mutableStateOf<String?>(null) }

    val createDocument = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val text = pendingBackup
        if (uri != null && text != null) scope.launch {
            runCatching { withContext(Dispatchers.IO) { context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(text) } ?: error("Impossibile aprire il file") } }
                .onSuccess { status = "Backup salvato correttamente."; LocalManagementStore.log("Backup", "Esportazione", "Backup JSON completo") }
                .onFailure { status = "Errore salvataggio: ${it.message}" }
        }
    }
    val openDocument = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            val result = runCatching {
                val raw = withContext(Dispatchers.IO) { context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: error("File non leggibile") }
                val backup = LocalManagementStore.jsonCodec.decodeFromString<LivasFullBackup>(raw)
                val restore = repo.restoreServer(backup.server)
                if (restore.isFailure) throw (restore.exceptionOrNull() ?: IllegalStateException("Ripristino server non riuscito"))
                LocalManagementStore.restore(backup.local)
                backup
            }
            status = if(result.isSuccess) "Backup ripristinato. Dati server e locali aggiornati." else "Ripristino non riuscito: ${result.exceptionOrNull()?.message}"
        }
    }

    Scaffold(topBar = { LivasTopAppBar(title = { Text("Backup e Ripristino") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } }) }) { p ->
        LazyColumn(Modifier.padding(p), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Icon(Icons.Default.Backup, null, Modifier.size(50.dp), tint = MaterialTheme.colorScheme.primary) }
            item { Text("Centro Backup Direttivo", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
            item { Text("Include soci, turni, servizi, mezzi, manutenzioni, magazzino, vestizione, comunicazioni, richieste, Servizio Civile, registro attività, cestino, operativo e Presidi.") }
            item { Button({ val envelope = LivasFullBackup(server = repo.snapshotServer(), local = LocalManagementStore.snapshot()); pendingBackup = LocalManagementStore.jsonCodec.encodeToString(envelope); createDocument.launch("LIVAS_Backup_${System.currentTimeMillis()}.json") }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Download, null); Text(" Crea e salva backup JSON") } }
            item { OutlinedButton({ openDocument.launch(arrayOf("application/json","text/json","text/plain")) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Restore, null); Text(" Importa e ripristina backup") } }
            status?.let { item { Card { Text(it, Modifier.padding(14.dp)) } } }
            item { Text("Il ripristino richiede il ruolo Direttivo e rispetta le policy RLS del server. Il file di backup può contenere dati personali: conservarlo in modo sicuro.", style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerStatusScreen(onBack: () -> Unit) {
    val repo = AppGraph.repo
    val profile by repo.profile.collectAsState(); val role by repo.role.collectAsState(); val loading by repo.loading.collectAsState(); val error by repo.error.collectAsState()
    val scope = rememberCoroutineScope()
    Scaffold(topBar = { LivasTopAppBar(title = { Text("Server Li.v.a.s.") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } }) }) { p ->
        LazyColumn(Modifier.padding(p), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Text("Supabase condiviso iPhone ↔ Android", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            item { ListItem(headlineContent = { Text("Connessione") }, supportingContent = { Text(if (error == null) "Operativa" else error ?: "Errore") }, leadingContent = { Icon(if (error == null) Icons.Default.CloudDone else Icons.Default.CloudOff, null) }) }
            item { ListItem(headlineContent = { Text("Account") }, supportingContent = { Text(profile?.email ?: "—") }, leadingContent = { Icon(Icons.Default.AccountCircle, null) }) }
            item { ListItem(headlineContent = { Text("Ruolo") }, supportingContent = { Text(profile?.role ?: role.name) }, leadingContent = { Icon(Icons.Default.Security, null) }) }
            item { ListItem(headlineContent = { Text("Sincronizzazione") }, supportingContent = { Text(if (loading) "In corso" else "Realtime attivo") }, leadingContent = { Icon(Icons.Default.Sync, null) }) }
            item { OutlinedButton({ scope.launch { repo.refreshAll() } }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Refresh, null); Text(" Aggiorna dati") } }
        }
    }
}
