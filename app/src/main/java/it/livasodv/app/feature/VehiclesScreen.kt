package it.livasodv.app.feature

import androidx.compose.foundation.clickable
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
fun VehiclesScreen(onBack: () -> Unit) {
    val repo = AppGraph.repo
    val list by repo.vehicles.collectAsState()
    val role by repo.role.collectAsState()
    val canEdit = role == AppRole.DIRETTIVO
    var selected by remember { mutableStateOf<Vehicle?>(null) }
    var edit by remember { mutableStateOf<Vehicle?>(null) }
    var adding by remember { mutableStateOf(false) }
    var delete by remember { mutableStateOf<Vehicle?>(null) }
    val scope = rememberCoroutineScope()

    if (selected != null) {
        VehicleDetailScreen(selected!!, onBack = { selected = null })
        return
    }

    Scaffold(topBar = { LivasTopAppBar(title = { Text("Mezzi") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } }, actions = {
        if (canEdit) IconButton({ adding = true }) { Icon(Icons.Default.Add, "Aggiungi") }
        IconButton({ scope.launch { repo.refreshAll() } }) { Icon(Icons.Default.Refresh, "Aggiorna") }
    }) }) { p ->
        LazyColumn(Modifier.padding(p), contentPadding = PaddingValues(12.dp)) {
            if (list.isEmpty()) item { EmptyState("Nessun mezzo registrato", "I mezzi inseriti qui sono condivisi con iPhone tramite Supabase.") }
            items(list.sortedBy { it.name.lowercase() }, key = { it.id }) { v ->
                AppleCard(Modifier.fillMaxWidth().padding(vertical = 5.dp).clickable { selected = v }) {
                    Row(Modifier.padding(14.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp), color = androidx.compose.ui.graphics.Color(0xFFEAF2FF)) {
                            Icon(if (v.operational) Icons.Default.DirectionsCar else Icons.Default.CarCrash, null, Modifier.padding(12.dp).size(30.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(v.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(v.makeModel ?: "—", style = MaterialTheme.typography.bodySmall)
                            Text(v.plate ?: "senza targa", style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(5.dp))
                            StatusPill(if (v.operational) "Operativo" else "Non operativo", v.operational)
                        }
                        if (canEdit) Row { IconButton({ edit = v }) { Icon(Icons.Default.Edit, "Modifica") }; IconButton({ delete = v }) { Icon(Icons.Default.Delete, "Elimina") } }
                        else Icon(Icons.Default.ChevronRight, null)
                    }
                }
            }
        }
    }
    if (adding || edit != null) VehicleDialog(edit, { adding = false; edit = null }) { value -> scope.launch { repo.saveVehicle(value); adding = false; edit = null } }
    delete?.let { v -> ConfirmDelete("Eliminare il mezzo ${v.name}?", { delete = null }) { scope.launch { repo.deleteVehicle(v.id); delete = null } } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehicleDetailScreen(vehicle: Vehicle, onBack: () -> Unit) {
    val repo = AppGraph.repo
    val maintenance by repo.vehicleMaintenance.collectAsState()
    val monthlyKm by repo.vehicleMonthlyKm.collectAsState()
    val role by repo.role.collectAsState()
    val canEdit = role == AppRole.DIRETTIVO
    val scope = rememberCoroutineScope()
    var section by remember { mutableStateOf(0) }
    var addMaintenance by remember { mutableStateOf(false) }
    var addKm by remember { mutableStateOf(false) }
    var deleteMaintenance by remember { mutableStateOf<VehicleMaintenance?>(null) }
    var deleteKm by remember { mutableStateOf<VehicleMonthlyKm?>(null) }
    val mList = maintenance.filter { it.vehicleId == vehicle.id }.sortedByDescending { it.workDate }
    val kList = monthlyKm.filter { it.vehicleId == vehicle.id }.sortedByDescending { it.month }

    Scaffold(topBar = { LivasTopAppBar(title = { Text(vehicle.name) }, navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } }, actions = { IconButton({ scope.launch { repo.refreshAll() } }) { Icon(Icons.Default.Refresh, null) } }) }) { p ->
        Column(Modifier.padding(p).fillMaxSize()) {
            Card(Modifier.padding(12.dp).fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(vehicle.makeModel ?: "—", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Targa: ${vehicle.plate ?: "—"} · Categoria: ${vehicle.category ?: "—"}")
                    Text("Km attuali: ${vehicle.currentKm}")
                    Text("Assicurazione: ${vehicle.insuranceCompany ?: "—"} · ${vehicle.insuranceExpiry ?: "—"}")
                    Text("Revisione: ${vehicle.inspectionExpiry ?: "—"}")
                    AssistChip(onClick = {}, label = { Text(if (vehicle.operational) "Operativo" else "Non operativo") }, leadingIcon = { Icon(if (vehicle.operational) Icons.Default.CheckCircle else Icons.Default.Warning, null) })
                    if (!vehicle.notes.isNullOrBlank()) Text(vehicle.notes, style = MaterialTheme.typography.bodySmall)
                }
            }
            SingleChoiceSegmentedButtonRow(Modifier.padding(horizontal = 12.dp).fillMaxWidth()) {
                listOf("Manutenzioni", "Km mensili").forEachIndexed { i, label -> SegmentedButton(selected = section == i, onClick = { section = i }, shape = SegmentedButtonDefaults.itemShape(i, 2), label = { Text(label) }) }
            }
            if (canEdit) Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (section == 0) Button({ addMaintenance = true }) { Icon(Icons.Default.Build, null); Text(" Nuova lavorazione") }
                else Button({ addKm = true }) { Icon(Icons.Default.Speed, null); Text(" Registra km") }
            }
            if (section == 0) LazyColumn(contentPadding = PaddingValues(12.dp)) {
                if (mList.isEmpty()) item { EmptyState("Nessuna manutenzione", "Tagliandi, riparazioni e prossime scadenze compariranno qui.") }
                items(mList, key = { it.id }) { m -> ListItem(headlineContent = { Text(m.workType, fontWeight = FontWeight.Bold) }, supportingContent = { Text("${m.workDate} · ${m.odometerKm?.let { "$it km" } ?: "km —"}${m.nextDueDate?.let { " · prossima $it" } ?: ""}\n${m.description ?: ""}${m.cost?.let { " · € $it" } ?: ""}") }, leadingContent = { Icon(Icons.Default.Build, null) }, trailingContent = if (canEdit) {{ IconButton({ deleteMaintenance = m }) { Icon(Icons.Default.Delete, null) } }} else null); HorizontalDivider() }
            } else LazyColumn(contentPadding = PaddingValues(12.dp)) {
                if (kList.isEmpty()) item { EmptyState("Nessun dato chilometrico", "Registra i km percorsi per mese.") }
                items(kList, key = { it.id }) { k -> ListItem(headlineContent = { Text(k.month, fontWeight = FontWeight.Bold) }, supportingContent = { Text("${k.km} km${k.notes?.let { " · $it" } ?: ""}") }, leadingContent = { Icon(Icons.Default.Speed, null) }, trailingContent = if (canEdit) {{ IconButton({ deleteKm = k }) { Icon(Icons.Default.Delete, null) } }} else null); HorizontalDivider() }
            }
        }
    }
    if (addMaintenance) SimpleAddDialog("Nuova manutenzione", listOf("Data YYYY-MM-DD", "Tipo lavoro", "Descrizione", "Km mezzo", "Costo", "Prossima scadenza YYYY-MM-DD"), { addMaintenance = false }) { v -> scope.launch { repo.saveVehicleMaintenance(VehicleMaintenance(repo.newId(), vehicle.id, v[0], v[1], v[2].ifBlank { null }, v[3].toIntOrNull(), v[4].replace(',', '.').toDoubleOrNull(), v[5].ifBlank { null })); addMaintenance = false } }
    if (addKm) SimpleAddDialog("Km mensili", listOf("Mese YYYY-MM-01", "Km percorsi", "Note"), { addKm = false }) { v -> scope.launch { repo.saveVehicleMonthlyKm(VehicleMonthlyKm(repo.newId(), vehicle.id, v[0], v[1].toIntOrNull() ?: 0, v[2].ifBlank { null })); addKm = false } }
    deleteMaintenance?.let { x -> ConfirmDelete("Eliminare la lavorazione ${x.workType}?", { deleteMaintenance = null }) { scope.launch { repo.deleteVehicleMaintenance(x.id); deleteMaintenance = null } } }
    deleteKm?.let { x -> ConfirmDelete("Eliminare i km del mese ${x.month}?", { deleteKm = null }) { scope.launch { repo.deleteVehicleMonthlyKm(x.id); deleteKm = null } } }
}

@Composable
private fun VehicleDialog(v: Vehicle?, dismiss: () -> Unit, save: (Vehicle) -> Unit) {
    var name by remember { mutableStateOf(v?.name ?: "") }
    var model by remember { mutableStateOf(v?.makeModel ?: "") }
    var plate by remember { mutableStateOf(v?.plate ?: "") }
    var category by remember { mutableStateOf(v?.category ?: "") }
    var km by remember { mutableStateOf(v?.currentKm?.toString() ?: "0") }
    var company by remember { mutableStateOf(v?.insuranceCompany ?: "") }
    var ins by remember { mutableStateOf(v?.insuranceExpiry ?: "") }
    var rev by remember { mutableStateOf(v?.inspectionExpiry ?: "") }
    var operational by remember { mutableStateOf(v?.operational ?: true) }
    var notes by remember { mutableStateOf(v?.notes ?: "") }

    AlertDialog(onDismissRequest = dismiss, title = { Text(if (v == null) "Nuovo mezzo" else "Modifica mezzo") }, text = {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item { OutlinedTextField(name, { name = it }, label = { Text("Nome mezzo") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(model, { model = it }, label = { Text("Marca / modello") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(plate, { plate = it.uppercase() }, label = { Text("Targa") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(category, { category = it }, label = { Text("Categoria (Ambulanza/AIB/Auto…)") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(km, { km = it.filter(Char::isDigit) }, label = { Text("Km attuali") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(company, { company = it }, label = { Text("Compagnia assicurativa") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(ins, { ins = it }, label = { Text("Scadenza assicurazione YYYY-MM-DD") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(rev, { rev = it }, label = { Text("Scadenza revisione YYYY-MM-DD") }, modifier = Modifier.fillMaxWidth()) }
            item { ToggleLine("Operativo", operational) { operational = it } }
            item { OutlinedTextField(notes, { notes = it }, label = { Text("Note / lavorazioni") }, modifier = Modifier.fillMaxWidth()) }
        }
    }, confirmButton = { Button({ save(Vehicle(v?.id ?: AppGraph.repo.newId(), name.trim(), model.ifBlank { null }, plate.ifBlank { null }, category.ifBlank { null }, operational, km.toIntOrNull() ?: 0, company.ifBlank { null }, ins.ifBlank { null }, rev.ifBlank { null }, notes.ifBlank { null })) }, enabled = name.isNotBlank()) { Text("Salva") } }, dismissButton = { TextButton(dismiss) { Text("Annulla") } })
}
