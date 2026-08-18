@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package it.livasodv.app.feature

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.livasodv.app.data.*
import it.livasodv.app.ui.theme.*
import java.time.LocalDate

@Composable
fun DashboardScreen(role: AppRole, onRoute: (String) -> Unit) {
    val repo = AppGraph.repo
    val members by repo.members.collectAsState()
    val vehicles by repo.vehicles.collectAsState()
    val shifts by repo.shifts.collectAsState()
    val services by repo.services.collectAsState()
    val requests by repo.requests.collectAsState()
    val today = remember { LocalDate.now() }
    val weekEnd = remember(today) { today.plusDays(7) }

    val activeMembers = members.count { it.isActive }
    val nextShifts = shifts.count { s -> runCatching { LocalDate.parse(s.shiftDate) }.getOrNull()?.let { !it.isBefore(today) && !it.isAfter(weekEnd) } == true }
    val nextServices = services.count { s -> runCatching { LocalDate.parse(s.serviceDate.take(10)) }.getOrNull()?.let { !it.isBefore(today) && !it.isAfter(weekEnd) } == true }
    val nextEvents = services.count { s ->
        val isEvent = s.serviceType.contains("event", true) || s.serviceType.contains("manifest", true) || s.serviceType.contains("assistenza", true)
        isEvent && runCatching { LocalDate.parse(s.serviceDate.take(10)) }.getOrNull()?.let { !it.isBefore(today) && !it.isAfter(weekEnd) } == true
    }
    val newRequests = requests.count { it.status.equals("nuova", true) || !it.isRead }
    val operationalVehicles = vehicles.count { it.operational }
    val recentRequests = requests.sortedByDescending { it.requestedAt ?: "" }.take(3)

    Scaffold(
        containerColor = LivasBackground,
        topBar = {
            LivasTopAppBar(
                title = { Text("Dashboard Direttivo", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton({ onRoute("more") }) { Icon(Icons.Default.Menu, "Menu") } },
                actions = {
                    Box {
                        IconButton({ onRoute("notifications") }) { Icon(Icons.Default.Notifications, "Notifiche") }
                        if (newRequests > 0) {
                            Surface(shape = CircleShape, color = LivasRed, modifier = Modifier.align(Alignment.TopEnd)) {
                                Text("$newRequests", Modifier.padding(horizontal = 5.dp, vertical = 1.dp), color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            )
        }
    ) { inner ->
        LazyColumn(
            Modifier.fillMaxSize().padding(inner),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("Buongiorno Direttivo!", style = MaterialTheme.typography.titleLarge, color = LivasText, fontWeight = FontWeight.Black)
                Text("Ecco la situazione aggiornata.", style = MaterialTheme.typography.bodyMedium, color = LivasMuted)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppleMetricCard("$activeMembers", "Soci Attivi", "Totali soci iscritti", Icons.Default.Groups, LivasBlue, Modifier.weight(1f)) { onRoute("members") }
                        AppleMetricCard("$nextShifts", "Turni", "Prossimi 7 giorni", Icons.Default.CalendarMonth, LivasRed, Modifier.weight(1f)) { onRoute("shifts") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppleMetricCard("$nextServices", "Servizi Sociali", "Prossimi 7 giorni", Icons.Default.MedicalServices, LivasGreen, Modifier.weight(1f)) { onRoute("services") }
                        AppleMetricCard("$nextEvents", "Eventi", "Prossimi 7 giorni", Icons.Default.Event, LivasOrange, Modifier.weight(1f)) { onRoute("services") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppleMetricCard("$newRequests", "Richieste Cittadini", "Nuove", Icons.Default.Mail, LivasPurple, Modifier.weight(1f)) { onRoute("requests") }
                        AppleMetricCard("$operationalVehicles", "Mezzi", "Operativi", Icons.Default.DirectionsCar, LivasBlue, Modifier.weight(1f)) { onRoute("vehicles") }
                    }
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Richieste recenti", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Spacer(Modifier.weight(1f))
                    TextButton({ onRoute("requests") }) { Text("Vedi tutte") }
                }
            }

            if (recentRequests.isEmpty()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = LivasSurface), border = BorderStroke(1.dp, LivasLine), shape = RoundedCornerShape(16.dp)) {
                        Text("Nessuna nuova richiesta.", Modifier.padding(18.dp), color = LivasMuted)
                    }
                }
            } else {
                items(recentRequests.size) { index ->
                    val r = recentRequests[index]
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onRoute("requests") },
                        colors = CardDefaults.cardColors(containerColor = LivasSurface),
                        border = BorderStroke(1.dp, LivasLine),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = LivasRed.copy(alpha = 0.15f), modifier = Modifier.size(36.dp)) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.MedicalServices, null, tint = LivasRed, modifier = Modifier.size(20.dp)) }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(r.requestType.replace('_', ' ').replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Bold)
                                Text("${r.firstName} ${r.lastName}", style = MaterialTheme.typography.bodySmall, color = LivasMuted)
                                val route = listOfNotNull(r.fromPlace, r.toPlace).joinToString(" → ")
                                if (route.isNotBlank()) Text(route, style = MaterialTheme.typography.bodySmall, color = LivasMuted)
                            }
                            Surface(shape = RoundedCornerShape(8.dp), color = LivasWarmAccent.copy(alpha = 0.88f)) {
                                Text(r.status.uppercase(), Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickLink("Ricerca", Icons.Default.Search, Modifier.weight(1f)) { onRoute("search") }
                    QuickLink("Backup", Icons.Default.Backup, Modifier.weight(1f)) { onRoute("backup") }
                    QuickLink("Gestione", Icons.Default.DashboardCustomize, Modifier.weight(1f)) { onRoute("management") }
                }
            }
        }
    }
}

@Composable
private fun AppleMetricCard(
    value: String,
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(128.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = LivasSurface),
        border = BorderStroke(1.dp, LivasLine),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(13.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(29.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = LivasText)
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = LivasMuted, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun QuickLink(title: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier.height(54.dp), border = BorderStroke(1.dp, LivasLine), shape = RoundedCornerShape(13.dp)) {
        Icon(icon, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(5.dp)); Text(title, style = MaterialTheme.typography.labelMedium)
    }
}
