@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package it.livasodv.app.feature

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun PublicToolScreen(route: String, onClose: () -> Unit) {
    when (route) {
        "about" -> AboutPublicScreen(onClose)
        "contacts" -> ContactsPublicScreen(onClose)
        "emergency" -> EmergencyHelpScreen(onClose)
        "ps118" -> Ps118NativeScreen(onClose)
        "civil_protection" -> CivilProtectionScreen(onClose)
        "rescue_run" -> RescueRunScreen(onClose)
        else -> onClose()
    }
}


@Composable
private fun AboutPublicScreen(onClose: () -> Unit) {
    Scaffold(topBar = { LivasTopAppBar(title = { Text("Chi siamo") }, navigationIcon = { TextButton(onClose) { Text("Chiudi", color = androidx.compose.ui.graphics.Color.White) } }) }) { p ->
        LazyColumn(Modifier.padding(p).fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { Text("Lì.v.a.s. O.d.V. Gonnosfanadiga", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black) }
            item { Text("Libera Associazione Volontari del Soccorso. Volontariato sanitario, Protezione Civile, antincendio boschivo e servizi sociali al servizio della comunità.") }
        }
    }
}

@Composable
private fun ContactsPublicScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    Scaffold(topBar = { LivasTopAppBar(title = { Text("Contatti") }, navigationIcon = { TextButton(onClose) { Text("Chiudi", color = androidx.compose.ui.graphics.Color.White) } }) }) { p ->
        LazyColumn(Modifier.padding(p).fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { ListItem(headlineContent = { Text("Telefono") }, supportingContent = { Text("070 9798990") }, leadingContent = { Icon(Icons.Default.Phone, null) }, modifier = Modifier.clickable { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:0709798990"))) }) }
            item { ListItem(headlineContent = { Text("Email") }, supportingContent = { Text("Livas.gonnos@tiscali.it") }, leadingContent = { Icon(Icons.Default.Email, null) }, modifier = Modifier.clickable { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:Livas.gonnos@tiscali.it"))) }) }
            item { Text("Gonnosfanadiga · Sardegna", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmergencyHelpScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    val numbers = listOf(
        Triple("Numero Unico Emergenze", "112", Icons.Default.Sos),
        Triple("Emergenza sanitaria", "118", Icons.Default.MedicalServices),
        Triple("Vigili del Fuoco", "115", Icons.Default.LocalFireDepartment),
        Triple("Carabinieri Gonnosfanadiga", "0709799022", Icons.Default.Shield),
        Triple("Guardia Medica Gonnosfanadiga", "0709799019", Icons.Default.LocalHospital),
        Triple("Polizia Locale Gonnosfanadiga", "0709798733", Icons.Default.Security),
        Triple("Li.v.a.s. O.D.V. Gonnosfanadiga", "0709798990", Icons.Default.VolunteerActivism)
    )
    Scaffold(topBar = { LivasTopAppBar(title = { Text("Emergenze • Primo soccorso") }, navigationIcon = { TextButton(onClose) { Text("Chiudi") } }) }) { p ->
        LazyColumn(Modifier.padding(p).fillMaxSize(), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                HelpCard("Emergenza in corso?", Icons.Default.Sos) {
                    Text("Chiama subito il 112 o il 118. Rispondi con calma alle domande dell’operatore e segui le istruzioni della Centrale.")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EmergencyCallButton("112", Modifier.weight(1f)) { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))) }
                        EmergencyCallButton("118", Modifier.weight(1f)) { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:118"))) }
                    }
                }
            }
            item {
                HelpCard("Primo soccorso", Icons.Default.HealthAndSafety) {
                    HelpStep("1", "Sicurezza", "Controlla che la scena sia sicura per te, la persona e chi è vicino.")
                    HelpStep("2", "Valuta", "Controlla se la persona risponde e se respira normalmente.")
                    HelpStep("3", "Chiama", "Attiva il 112/118 e indica luogo preciso, cosa è successo e condizioni della persona.")
                    HelpStep("4", "Attendi e assisti", "Segui le istruzioni dell’operatore e non spostare un traumatizzato salvo pericolo immediato.")
                }
            }
            item {
                HelpCard("BLSD • arresto cardiaco e DAE", Icons.Default.MonitorHeart) {
                    HelpStep("1", "Allerta", "Fai chiamare 112/118 e chiedi che venga portato un DAE.")
                    HelpStep("2", "RCP", "Inizia compressioni al centro del torace; se sei formato, segui il protocollo appreso.")
                    HelpStep("3", "DAE", "Accendilo, applica le placche come illustrato e segui le istruzioni vocali.")
                    HelpStep("4", "Continua", "Prosegui fino alla ripresa di segni di vita o all’arrivo dei soccorsi.")
                }
            }
            item { Text("Numeri utili", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(numbers.size) { i ->
                val n = numbers[i]
                ListItem(
                    modifier = Modifier.clickable { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${n.second}"))) },
                    headlineContent = { Text(n.first, fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text(n.second) },
                    leadingContent = { Icon(n.third, null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = { Icon(Icons.Default.Phone, "Chiama") }
                )
            }
            item {
                Text(
                    "Questa guida è un promemoria informativo e non sostituisce un corso BLSD/primo soccorso, una valutazione sanitaria o le istruzioni della Centrale Operativa.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .60f)
                )
            }
        }
    }
}

@Composable
private fun HelpCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(8.dp)); Text(title, fontWeight = FontWeight.Bold) }
            content()
        }
    }
}

@Composable
private fun HelpStep(number: String, title: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primary) { Text(number, Modifier.padding(horizontal = 9.dp, vertical = 5.dp), fontWeight = FontWeight.Bold) }
        Spacer(Modifier.width(9.dp))
        Column { Text(title, fontWeight = FontWeight.SemiBold); Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .65f)) }
    }
}

@Composable
private fun EmergencyCallButton(number: String, modifier: Modifier, onClick: () -> Unit) {
    Button(onClick, modifier) { Icon(Icons.Default.Phone, null); Text(" Chiama $number") }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Ps118NativeScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    var search by remember { mutableStateOf("") }
    val hospitals = remember {
        listOf(
            Triple("OSP N.S. DI BONARIA S.GAVINO MONREALE", "San Gavino Monreale", true),
            Triple("A.O.U. SASSARI", "Sassari", false),
            Triple("OSPEDALE A. SEGNI OZIERI", "Ozieri", false),
            Triple("PRESIDIO G. BROTZU", "Cagliari", false),
            Triple("OSPEDALE SS. TRINITÀ", "Cagliari", false),
            Triple("POLICLINICO D. CASULA", "Monserrato", false),
            Triple("P.O. SAN MARTINO", "Oristano", false),
            Triple("OSPEDALE SAN FRANCESCO", "Nuoro", false),
            Triple("OSPEDALE GIOVANNI PAOLO II", "Olbia", false)
        )
    }
    val filtered = hospitals.filter { search.isBlank() || it.first.contains(search, true) || it.second.contains(search, true) }
    val officialUrl = "https://monitorps.sardegnasalute.it/monitorps/MonitorServlet"

    Scaffold(
        containerColor = Color(0xFF06111A),
        topBar = {
            TopAppBar(
                title = { Text("PS118 Live Open", fontWeight = FontWeight.Bold) },
                navigationIcon = { TextButton(onClose) { Icon(Icons.Default.Cancel, null); Text("  Chiudi") } },
                actions = { IconButton({ context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(officialUrl))) }) { Icon(Icons.Default.Refresh, "Aggiorna", tint = Color(0xFF55D8FF)) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF090D12), titleContentColor = Color.White)
            )
        }
    ) { p ->
        LazyColumn(
            Modifier.padding(p).fillMaxSize(),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2129)),
                    border = BorderStroke(1.dp, Color(0xFF245266)),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = Color(0xFF153D4D), modifier = Modifier.size(56.dp)) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Sensors, null, tint = Color(0xFF55D8FF), modifier = Modifier.size(30.dp)) }
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text("SARDEGNA LIVE", color = Color(0xFF55D8FF), fontWeight = FontWeight.Black)
                                Text("Situazione Pronto Soccorso", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                                Text("Dati informativi dalla fonte regionale ufficiale", color = Color.White.copy(alpha = .68f))
                            }
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = .18f))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("◉  Auto refresh 60 s", color = Color.White.copy(alpha = .65f))
                            Text(java.text.SimpleDateFormat("HH:mm", java.util.Locale.ITALY).format(java.util.Date()), color = Color.White.copy(alpha = .65f))
                        }
                    }
                }
            }
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF211B18)),
                    border = BorderStroke(1.dp, Color(0xFF70411C)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(Modifier.padding(16.dp)) {
                        Icon(Icons.Default.ReportProblem, null, tint = Color(0xFFFF8A2C))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Informazioni, non triage", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("I dati possono essere incompleti, ritardati o non disponibili e non devono essere usati per rinviare cure o scegliere autonomamente una destinazione in emergenza. Per un'urgenza chiama 112/118.", color = Color.White.copy(alpha = .70f))
                        }
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Cerca pronto soccorso") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF252A30),
                        unfocusedContainerColor = Color(0xFF252A30),
                        focusedBorderColor = Color.White.copy(alpha=.28f),
                        unfocusedBorderColor = Color.White.copy(alpha=.18f)
                    )
                )
            }
            items(filtered) { h ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2129)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = .09f)),
                    shape = RoundedCornerShape(26.dp),
                    modifier = Modifier.fillMaxWidth().clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(officialUrl))) }
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Column(Modifier.weight(1f)) {
                                Text(h.first, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                                Text(h.second, color = Color.White.copy(alpha = .64f))
                            }
                            if (h.third) Icon(Icons.Default.Star, null, tint = Color(0xFFFFD60A), modifier = Modifier.size(30.dp))
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("●  Affollamento N/D", color = Color.White.copy(alpha = .52f), fontWeight = FontWeight.SemiBold)
                            Text("LIVE", color = Color(0xFF55D8FF), fontWeight = FontWeight.Black)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PsMetric("—", "ATTESA", Modifier.weight(1f))
                            PsMetric("—", "IN GESTIONE", Modifier.weight(1f))
                            PsMetric("—", "ARRIVO 118", Modifier.weight(1f))
                        }
                        Text("Tocca la scheda per consultare i valori live ufficiali.", color = Color.White.copy(alpha=.48f), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun PsMetric(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = Color(0xFF242B34), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, color = Color.White.copy(alpha = .63f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CivilProtectionScreen(onClose: () -> Unit) {
    var selected by remember { mutableIntStateOf(0) }
    val tabs = listOf("Allerte", "Meteo", "Incendi", "Cosa fare", "Numeri")
    val context = LocalContext.current
    fun openOfficial(url: String) = context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

    Scaffold(topBar = { LivasTopAppBar(title = { Text("Protezione Civile") }, navigationIcon = { TextButton(onClose) { Text("Chiudi") } }) }) { p ->
        Column(Modifier.padding(p).fillMaxSize()) {
            Row(Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                tabs.forEachIndexed { index, label -> FilterChip(selected == index, { selected = index }, { Text(label) }) }
            }
            when (selected) {
                0 -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { OfficialSourceCard("Allerte regionali", "Avvisi di criticità, rischio idrogeologico e comunicazioni ufficiali della Protezione Civile Sardegna.", Icons.Default.Warning) { openOfficial("https://sardegnaambiente.it/protezionecivile/") } }
                    item { OfficialSourceCard("Bollettino di criticità regionale", "Consulta il bollettino del Centro Funzionale Decentrato.", Icons.Default.Water) { openOfficial("https://sardegnaambiente.it/index.php?c=12836&nodesc=1&s=20&v=9&xsl=2273") } }
                    item { Text("L'app non genera né modifica i livelli di allerta: fa sempre fede la fonte ufficiale regionale.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                1 -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { OfficialSourceCard("Meteo e rischio idrogeologico", "Bollettini di vigilanza meteo, criticità e avvisi di condizioni meteorologiche avverse.", Icons.Default.Thunderstorm) { openOfficial("https://sardegnaambiente.it/index.php?c=7092&nodesc=1&s=20&v=9&xsl=2273") } }
                }
                2 -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { OfficialSourceCard("Pericolo incendi", "Bollettino regionale giornaliero di previsione del pericolo incendio.", Icons.Default.LocalFireDepartment) { openOfficial("https://sardegnaambiente.it/index.php?c=7093&nodesc=1&s=20&v=9&xsl=2273") } }
                }
                3 -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item { HelpCard("In caso di allerta", Icons.Default.Warning) {
                        HelpStep("1", "Informati", "Consulta gli avvisi ufficiali di Protezione Civile e Comune.")
                        HelpStep("2", "Evita rischi", "Non attraversare sottopassi, guadi o strade allagate e limita gli spostamenti se non necessari.")
                        HelpStep("3", "Incendi", "Non avvicinarti al fronte di fuoco; lascia libere le vie ai mezzi di soccorso e segnala subito fumo o fiamme.")
                        HelpStep("4", "Emergenza", "Per pericolo immediato chiama il 112 e segui le indicazioni delle autorità.")
                    } }
                    item { Text("Le disposizioni ufficiali hanno sempre priorità sulle indicazioni presenti nell'app.", style = MaterialTheme.typography.bodySmall) }
                }
                4 -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(14.dp)) {
                    val nums = listOf("Numero Unico Emergenze" to "112", "Vigili del Fuoco" to "115", "Emergenza sanitaria" to "118", "Li.v.a.s. O.D.V." to "0709798990")
                    items(nums) { n -> ListItem(modifier = Modifier.clickable { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${n.second}"))) }, headlineContent = { Text(n.first, fontWeight = FontWeight.Bold) }, supportingContent = { Text(n.second) }, leadingContent = { Icon(Icons.Default.Phone, null) }, trailingContent = { Icon(Icons.Default.ChevronRight, null) }); HorizontalDivider() }
                }
            }
        }
    }
}

@Composable
private fun OfficialSourceCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onOpen: () -> Unit) {
    Card(shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(9.dp))
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onOpen, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.OpenInNew, null); Text(" Apri fonte ufficiale") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RescueRunScreen(onClose: () -> Unit) {
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }
    var prompt by remember { mutableStateOf("Premi AVVIA per iniziare") }
    var running by remember { mutableStateOf(false) }
    var correctSide by remember { mutableIntStateOf(0) }

    fun next() {
        correctSide = Random.nextInt(0, 3)
        prompt = listOf("DAE", "Zaino sanitario", "Radio")[correctSide]
    }

    Scaffold(topBar = { LivasTopAppBar(title = { Text("Rescue Run") }, navigationIcon = { TextButton(onClose) { Text("Chiudi") } }) }) { p ->
        Column(
            Modifier.padding(p).fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Emergency, null, Modifier.size(92.dp), tint = MaterialTheme.colorScheme.primary)
            Text("RESCUE RUN", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text("Punteggio: $score   •   Vite: $lives")
            Spacer(Modifier.height(28.dp))
            Card { Text(if (running) "Raccogli: $prompt" else prompt, Modifier.padding(24.dp), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(20.dp))
            if (!running) {
                Button({ score = 0; lives = 3; running = true; next() }) { Text("AVVIA") }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("DAE", "Zaino", "Radio").forEachIndexed { index, label ->
                        Button(onClick = {
                            if (index == correctSide) score += 10 else lives -= 1
                            if (lives <= 0) { running = false; prompt = "Fine partita • $score punti" } else next()
                        }) { Text(label) }
                    }
                }
                Text("Mini-gioco locale, nessun dato inviato.", Modifier.padding(top = 20.dp), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
