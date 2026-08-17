@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package it.livasodv.app.feature

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun PublicToolScreen(route: String, onClose: () -> Unit) {
    when (route) {
        "about" -> AboutPublicScreen(onClose)
        "contacts" -> ContactsPublicScreen(onClose)
        "emergency" -> EmergencyHelpScreen(onClose)
        "ps118" -> OfficialWebScreen("Monitor PS 118", "https://monitorps.sardegnasalute.it/monitorps/", onClose)
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
private fun OfficialWebScreen(title: String, url: String, onClose: () -> Unit) {
    Scaffold(topBar = { LivasTopAppBar(title = { Text(title) }, navigationIcon = { TextButton(onClose) { Text("Chiudi") } }) }) { p ->
        AndroidView(
            modifier = Modifier.padding(p).fillMaxSize(),
            factory = { context -> WebView(context).apply { settings.javaScriptEnabled = true; webViewClient = WebViewClient(); loadUrl(url) } },
            update = { if (it.url == null) it.loadUrl(url) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CivilProtectionScreen(onClose: () -> Unit) {
    var selected by remember { mutableIntStateOf(0) }
    val tabs = listOf("Allerte", "Meteo", "Incendi", "Cosa fare", "Numeri")
    val urls = listOf(
        "https://www.sardegnaambiente.it/protezionecivile/",
        "https://www.sardegnaambiente.it/index.php?c=7092&nodesc=1&s=20&v=9&xsl=2273",
        "https://www.sardegnaambiente.it/index.php?c=7093&nodesc=1&s=20&v=9&xsl=2273"
    )
    val context = LocalContext.current
    Scaffold(topBar = { LivasTopAppBar(title = { Text("Protezione Civile") }, navigationIcon = { TextButton(onClose) { Text("Chiudi") } }) }) { p ->
        Column(Modifier.padding(p).fillMaxSize()) {
            Row(Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                tabs.forEachIndexed { index, label -> FilterChip(selected == index, { selected = index }, { Text(label) }) }
            }
            when (selected) {
                0,1,2 -> AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { c -> WebView(c).apply { settings.javaScriptEnabled = true; webViewClient = WebViewClient(); loadUrl(urls[selected]) } },
                    update = { view -> if (view.url != urls[selected]) view.loadUrl(urls[selected]) }
                )
                3 -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item { HelpCard("In caso di allerta", Icons.Default.Warning) {
                        HelpStep("1", "Informati", "Consulta gli avvisi ufficiali di Protezione Civile e Comune.")
                        HelpStep("2", "Evita rischi", "Non attraversare sottopassi, guadi o strade allagate e limita gli spostamenti se non necessari.")
                        HelpStep("3", "Incendi", "Non avvicinarti al fronte di fuoco; lascia libere le vie ai mezzi di soccorso e segnala subito fumo o fiamme.")
                        HelpStep("4", "Emergenza", "Per pericolo immediato chiama il 112 e segui le indicazioni delle autorità.")
                    } }
                    item { Text("Le indicazioni in app sono un promemoria. Gli avvisi e le disposizioni ufficiali hanno sempre priorità.", style = MaterialTheme.typography.bodySmall) }
                }
                4 -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(14.dp)) {
                    val nums = listOf("Numero Unico Emergenze" to "112", "Vigili del Fuoco" to "115", "Emergenza sanitaria" to "118", "Li.v.a.s. O.D.V." to "0709798990")
                    items(nums) { n -> ListItem(modifier = Modifier.clickable { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${n.second}"))) }, headlineContent = { Text(n.first, fontWeight = FontWeight.Bold) }, supportingContent = { Text(n.second) }, leadingContent = { Icon(Icons.Default.Phone, null) }, trailingContent = { Icon(Icons.Default.ChevronRight, null) }); HorizontalDivider() }
                }
            }
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
