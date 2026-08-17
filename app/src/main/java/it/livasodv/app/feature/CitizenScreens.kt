package it.livasodv.app.feature

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.livasodv.app.data.AppGraph
import it.livasodv.app.data.CitizenRequest
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenRootScreen(onExit: () -> Unit) {
    var route by remember { mutableStateOf<String?>(null) }
    when (route) {
        "visit" -> VisitRequestScreen { route = null }
        "aid" -> AidRequestScreen { route = null }
        "discharge" -> DischargeRequestScreen { route = null }
        "privacy" -> PrivacyInfoScreen { route = null }
        else -> Scaffold(
            topBar = { LivasTopAppBar(title = { Text("Cittadini") }, navigationIcon = { TextButton(onExit) { Text("Esci") } }) }
        ) { p ->
            LazyColumn(
                Modifier.padding(p).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.VolunteerActivism, null, Modifier.size(68.dp), tint = MaterialTheme.colorScheme.primary)
                        Text("Servizi per i cittadini", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("Richieste programmate Li.v.a.s. O.D.V.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .62f))
                    }
                }
                item { CitizenServiceCard(Icons.Default.MedicalServices, "Prenota accompagnamento", "Visite mediche e commissioni sanitarie") { route = "visit" } }
                item { CitizenServiceCard(Icons.Default.Accessible, "Richiedi un presidio", "Carrozzina, letto, materassino o stampelle") { route = "aid" } }
                item { CitizenServiceCard(Icons.Default.Bed, "Richiedi una dimissione", "Trasporto programmato da ospedale a domicilio o altra struttura") { route = "discharge" } }
                item { CitizenServiceCard(Icons.Default.Lock, "Privacy", "Come vengono trattati e protetti i dati") { route = "privacy" } }
                item {
                    Card {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text("Contatti", fontWeight = FontWeight.Bold)
                            Text("070 9798990")
                            Text("livas.gonnos@tiscali.it")
                        }
                    }
                }
                item {
                    Text(
                        "Le richieste vengono registrate sul server Li.v.a.s. Se il server non è raggiungibile, l'app prepara una email come procedura alternativa.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .60f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CitizenServiceCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(20.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(34.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .60f))
            }
            Icon(Icons.Default.ChevronRight, null)
        }
    }
}

private data class PersonForm(
    var firstName: String = "",
    var lastName: String = "",
    var phone: String = "",
    var email: String = "",
    var address: String = ""
)

@Composable
private fun PersonFields(person: PersonForm, onChange: (PersonForm) -> Unit) {
    SectionTitle("Dati richiedente / assistito")
    OutlinedTextField(person.firstName, { onChange(person.copy(firstName = it)) }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(person.lastName, { onChange(person.copy(lastName = it)) }, label = { Text("Cognome") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(person.phone, { onChange(person.copy(phone = it)) }, label = { Text("Telefono") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(person.email, { onChange(person.copy(email = it)) }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(person.address, { onChange(person.copy(address = it)) }, label = { Text("Indirizzo") }, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
}

private fun isoDateTime(date: String, time: String): String? = runCatching {
    LocalDateTime.parse("${date}T${time}").atZone(ZoneId.systemDefault()).toOffsetDateTime().toString()
}.getOrNull()

@Composable
private fun PrivacyConsent(accepted: Boolean, onAccepted: (Boolean) -> Unit) {
    Card {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(accepted, onAccepted)
                Spacer(Modifier.width(10.dp))
                Text("Acconsento al trattamento dei dati inseriti per la gestione della richiesta", style = MaterialTheme.typography.bodySmall)
            }
            Text("I dati sono usati esclusivamente per la gestione della richiesta.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .55f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VisitRequestScreen(onBack: () -> Unit) {
    var person by remember { mutableStateOf(PersonForm()) }
    var departure by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var mobility by remember { mutableStateOf("Deambulante") }
    var companion by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var privacy by remember { mutableStateOf(false) }
    RequestScaffold("Prenota accompagnamento", onBack) { sending, submit ->
        PersonFields(person) { person = it }
        SectionTitle("Servizio")
        OutlinedTextField(departure, { departure = it }, label = { Text("Partenza") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(destination, { destination = it }, label = { Text("Destinazione / struttura") }, modifier = Modifier.fillMaxWidth())
        DateTimeFields(date, { date = it }, time, { time = it })
        ChoiceField("Mobilità", mobility, listOf("Deambulante", "Carrozzina", "Allettato", "Cardiopatico / scale")) { mobility = it }
        Row(verticalAlignment = Alignment.CenterVertically) { Switch(companion, { companion = it }); Spacer(Modifier.width(8.dp)); Text("Accompagnatore presente") }
        OutlinedTextField(notes, { notes = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        PrivacyConsent(privacy) { privacy = it }
        Button(
            onClick = {
                val req = CitizenRequest(
                    id = AppGraph.repo.newId(), requestType = "visita", firstName = person.firstName, lastName = person.lastName,
                    phone = person.phone, email = person.email.ifBlank { null }, address = person.address.ifBlank { null },
                    fromPlace = departure, toPlace = destination, requestedAt = isoDateTime(date, time), mobility = mobility,
                    notes = (if (companion) "Accompagnatore presente. " else "") + notes, privacyAccepted = true, status = "nuova", isRead = false
                )
                submit(req, "Richiesta visita - ${person.firstName} ${person.lastName}")
            },
            enabled = !sending && person.firstName.isNotBlank() && person.lastName.isNotBlank() && person.phone.isNotBlank() && departure.isNotBlank() && destination.isNotBlank() && date.isNotBlank() && time.isNotBlank() && privacy,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (sending) "Invio in corso…" else "Invia richiesta") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AidRequestScreen(onBack: () -> Unit) {
    var person by remember { mutableStateOf(PersonForm()) }
    var aid by remember { mutableStateOf("Carrozzina") }
    var date by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("Da concordare") }
    var notes by remember { mutableStateOf("") }
    var privacy by remember { mutableStateOf(false) }
    RequestScaffold("Richiedi presidio", onBack) { sending, submit ->
        PersonFields(person) { person = it }
        SectionTitle("Presidio richiesto")
        ChoiceField("Presidio", aid, listOf("Carrozzina", "Letto", "Materassino", "Stampelle")) { aid = it }
        OutlinedTextField(date, { date = it }, label = { Text("Necessario dal (AAAA-MM-GG)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(duration, { duration = it }, label = { Text("Durata prevista") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(notes, { notes = it }, label = { Text("Note / esigenze particolari") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        PrivacyConsent(privacy) { privacy = it }
        Button(
            onClick = {
                val req = CitizenRequest(
                    id = AppGraph.repo.newId(), requestType = "presidio", firstName = person.firstName, lastName = person.lastName,
                    phone = person.phone, email = person.email.ifBlank { null }, address = person.address, requestedAt = date.takeIf { it.isNotBlank() }?.let { "${it}T12:00:00" },
                    equipment = aid, notes = "Durata: $duration. $notes", privacyAccepted = true, status = "nuova", isRead = false
                )
                submit(req, "Richiesta presidio $aid - ${person.firstName} ${person.lastName}")
            },
            enabled = !sending && person.firstName.isNotBlank() && person.lastName.isNotBlank() && person.phone.isNotBlank() && person.address.isNotBlank() && privacy,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (sending) "Invio in corso…" else "Invia richiesta") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DischargeRequestScreen(onBack: () -> Unit) {
    var person by remember { mutableStateOf(PersonForm()) }
    var hospital by remember { mutableStateOf("") }
    var ward by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var mobility by remember { mutableStateOf("Allettato") }
    var floor by remember { mutableStateOf("") }
    var stairs by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var privacy by remember { mutableStateOf(false) }
    RequestScaffold("Richiedi dimissione", onBack) { sending, submit ->
        PersonFields(person) { person = it }
        SectionTitle("Dimissione")
        OutlinedTextField(hospital, { hospital = it }, label = { Text("Ospedale / struttura") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(ward, { ward = it }, label = { Text("Reparto") }, modifier = Modifier.fillMaxWidth())
        DateTimeFields(date, { date = it }, time, { time = it })
        OutlinedTextField(destination, { destination = it }, label = { Text("Destinazione") }, modifier = Modifier.fillMaxWidth())
        ChoiceField("Condizione trasporto", mobility, listOf("Deambulante", "Carrozzina", "Allettato", "Cardiopatico / scale")) { mobility = it }
        OutlinedTextField(floor, { floor = it }, label = { Text("Piano abitazione") }, modifier = Modifier.fillMaxWidth())
        Row(verticalAlignment = Alignment.CenterVertically) { Switch(stairs, { stairs = it }); Spacer(Modifier.width(8.dp)); Text("Sono presenti scale") }
        OutlinedTextField(notes, { notes = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        PrivacyConsent(privacy) { privacy = it }
        Button(
            onClick = {
                val req = CitizenRequest(
                    id = AppGraph.repo.newId(), requestType = "dimissione", firstName = person.firstName, lastName = person.lastName,
                    phone = person.phone, email = person.email.ifBlank { null }, address = person.address.ifBlank { null },
                    fromPlace = listOf(hospital, ward).filter { it.isNotBlank() }.joinToString(" · "), toPlace = destination,
                    requestedAt = isoDateTime(date, time), mobility = mobility,
                    stairs = "Piano ${floor.ifBlank { "n.d." }} · Scale: ${if (stairs) "Sì" else "No"}", notes = notes,
                    privacyAccepted = true, status = "nuova", isRead = false
                )
                submit(req, "Richiesta dimissione - ${person.firstName} ${person.lastName}")
            },
            enabled = !sending && person.firstName.isNotBlank() && person.lastName.isNotBlank() && person.phone.isNotBlank() && hospital.isNotBlank() && destination.isNotBlank() && date.isNotBlank() && time.isNotBlank() && privacy,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (sending) "Invio in corso…" else "Invia richiesta") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RequestScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.(Boolean, (CitizenRequest, String) -> Unit) -> Unit
) {
    var sending by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    Scaffold(topBar = { LivasTopAppBar(title = { Text(title) }, navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Indietro") } }) }) { p ->
        LazyColumn(Modifier.padding(p).fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    content(sending) { request, mailSubject ->
                        scope.launch {
                            sending = true
                            val result = AppGraph.repo.submitCitizenRequest(request)
                            if (result.isSuccess) {
                                message = "Richiesta registrata correttamente sul server Li.v.a.s."
                            } else {
                                val body = "Richiesta Li.v.a.s.\n\n${request.requestType}\n${request.firstName} ${request.lastName}\nTelefono: ${request.phone}\nDa: ${request.fromPlace ?: request.address ?: "—"}\nA: ${request.toPlace ?: "—"}\nNote: ${request.notes ?: "—"}"
                                val uri = Uri.parse("mailto:livas.gonnos@tiscali.it?subject=${Uri.encode(mailSubject)}&body=${Uri.encode(body)}")
                                runCatching { context.startActivity(Intent(Intent.ACTION_SENDTO, uri)) }
                                message = "Server non raggiungibile. È stata preparata una email come procedura alternativa."
                            }
                            sending = false
                        }
                    }
                }
            }
        }
    }
    message?.let { msg -> AlertDialog(onDismissRequest = { message = null }, title = { Text("Li.v.a.s.") }, text = { Text(msg) }, confirmButton = { TextButton({ message = null }) { Text("OK") } }) }
}

@Composable
private fun DateTimeFields(date: String, onDate: (String) -> Unit, time: String, onTime: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(date, onDate, label = { Text("Data AAAA-MM-GG") }, modifier = Modifier.weight(1.4f))
        OutlinedTextField(time, onTime, label = { Text("Ora HH:mm") }, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ChoiceField(label: String, value: String, options: List<String>, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton({ expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$label: $value", modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowDropDown, null)
        }
        DropdownMenu(expanded, { expanded = false }) {
            options.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { onChange(option); expanded = false }) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyInfoScreen(onBack: () -> Unit) {
    Scaffold(topBar = { LivasTopAppBar(title = { Text("Privacy") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } }) }) { p ->
        LazyColumn(Modifier.padding(p), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text("Informativa privacy app Li.v.a.s.", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
            item { Text("I dati inseriti nelle richieste dei cittadini vengono utilizzati per gestire accompagnamenti, presidi e dimissioni programmate. L'accesso gestionale è limitato ai ruoli autorizzati dal server.") }
            item { Text("Per richieste relative ai dati: livas.gonnos@tiscali.it · 070 9798990") }
        }
    }
}
