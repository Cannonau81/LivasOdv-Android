@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package it.livasodv.app.feature

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import it.livasodv.app.data.AppRole
import it.livasodv.app.ui.theme.*
import it.livasodv.app.data.AppGraph
import kotlinx.coroutines.launch

data class ShellTab(val id: String, val label: String, val icon: ImageVector)

@Composable
fun LivasApp() {
    var loginArea by remember { mutableStateOf<AccessArea?>(null) }
    var activeArea by remember { mutableStateOf<AccessArea?>(null) }
    var citizen by remember { mutableStateOf(false) }
    var publicTool by remember { mutableStateOf<String?>(null) }

    when {
        publicTool != null -> PublicToolScreen(publicTool!!) { publicTool = null }
        citizen -> CitizenRootScreen { citizen = false }
        activeArea != null -> ProtectedAreaShell(activeArea!!) {
            activeArea = null
            loginArea = null
        }
        loginArea != null -> LoginScreen(loginArea!!, onBack = { loginArea = null }) {
            activeArea = loginArea
            loginArea = null
        }
        else -> AccessHomeScreen(
            onProtectedArea = { loginArea = it },
            onCitizenArea = { citizen = true },
            onPublicTool = { publicTool = it }
        )
    }
}

@Composable
private fun ProtectedAreaShell(area: AccessArea, onLoggedOut: () -> Unit) {
    val repo = AppGraph.repo
    val role by repo.role.collectAsState()
    val loading by repo.loading.collectAsState()
    val error by repo.error.collectAsState()
    val requests by repo.requests.collectAsState()
    val unreadRequests = requests.count { it.status.equals("nuova", true) || !it.isRead }
    val scope = rememberCoroutineScope()
    val effectiveArea = remember(area, role) {
        if (area != AccessArea.SOCI) area else when (role) {
            AppRole.MAGAZZINO -> AccessArea.MAGAZZINO
            AppRole.SERVIZI_SOCIALI -> AccessArea.SERVIZI_SOCIALI
            AppRole.OLP -> AccessArea.OLP
            AppRole.SERVIZIO_CIVILE -> AccessArea.SERVIZIO_CIVILE
            else -> AccessArea.SOCI
        }
    }
    val tabs = remember(effectiveArea) { tabsFor(effectiveArea) }
    var tab by remember(effectiveArea) { mutableStateOf(tabs.first().id) }
    var detail by remember(effectiveArea) { mutableStateOf<String?>(null) }

    fun navigate(route: String) {
        if (tabs.any { it.id == route }) { tab = route; detail = null } else detail = route
    }
    val current = detail ?: tab

    LaunchedEffect(effectiveArea) { repo.observeRealtime() }

    Scaffold(
        topBar = {
            if (error != null || loading) {
                Surface(tonalElevation = 2.dp) {
                    ListItem(
                        headlineContent = { Text(if (loading) "Sincronizzazione server…" else error ?: "") },
                        leadingContent = { if (loading) CircularProgressIndicator(strokeWidth = 2.dp) else Icon(Icons.Default.CloudOff, null) },
                        trailingContent = if (error != null) { { IconButton({ repo.clearError() }) { Icon(Icons.Default.Close, "Chiudi") } } } else null
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 4.dp) {
                tabs.forEach { t ->
                    NavigationBarItem(
                        selected = tab == t.id && detail == null,
                        onClick = { tab = t.id; detail = null },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LivasNavy,
                            selectedTextColor = LivasNavy,
                            indicatorColor = Color(0xFFEAF2FF),
                            unselectedIconColor = LivasMuted,
                            unselectedTextColor = LivasMuted
                        ),
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (effectiveArea == AccessArea.DIRETTIVO && t.id == "admin_home" && unreadRequests > 0) {
                                        Badge { Text(unreadRequests.coerceAtMost(99).toString()) }
                                    }
                                }
                            ) { Icon(t.icon, t.label) }
                        },
                        label = { Text(t.label) }
                    )
                }
            }
        }
    ) { p ->
        Box(Modifier.padding(p)) {
            when (current) {
                "admin_home" -> DashboardScreen(role, ::navigate)
                "member_home" -> MemberHomeScreen(::navigate)
                "warehouse_home" -> WarehouseHomeScreen(::navigate)
                "social_home" -> SocialServicesHomeScreen(::navigate)
                "civil_home" -> CivilDashboardHomeScreen(::navigate)
                "members" -> MembersScreen()
                "shifts" -> ShiftsScreen()
                "services" -> ServicesScreen { if (detail != null) detail = null else tab = tabs.first().id }
                "vehicles" -> VehiclesScreen { if (detail != null) detail = null else tab = tabs.first().id }
                "requests" -> RequestsScreen()
                "warehouse" -> WarehouseScreen { if (detail != null) detail = null else tab = tabs.first().id }
                "warehouse_movements" -> WarehouseMovementsScreen { if (detail != null) detail = null else tab = tabs.first().id }
                "communications" -> CommunicationsScreen { if (detail != null) detail = null else tab = tabs.first().id }
                "civil" -> CivilServiceFullScreen { if (detail != null) detail = null else tab = tabs.first().id }
                "clothing" -> ClothingScreen { detail = null }
                "management" -> ManagementCenterScreen({ detail = null }, ::navigate)
                "search" -> SearchScreen { detail = null }
                "backup" -> BackupScreen { detail = null }
                "server" -> ServerStatusScreen { detail = null }
                "privacy" -> PrivacyInfoScreen { detail = null }
                "presidi" -> PresidiGateScreen { detail = null }
                "notifications" -> NotificationCenterScreen { detail = null }
                "operational" -> OperationalMissionsScreen { detail = null }
                "audit" -> AuditLogScreen { detail = null }
                "trash" -> TrashBinScreen { detail = null }
                "expiry" -> ExpiryCenterScreen { detail = null }
                "report" -> ReportCenterScreen { detail = null }
                "app_info" -> AppInfoScreen { detail = null }
                "more" -> MoreScreen(role, ::navigate) { scope.launch { repo.signOut(); onLoggedOut() } }
                "profile" -> ProfileAreaScreen(effectiveArea, ::navigate) { scope.launch { repo.signOut(); onLoggedOut() } }
                else -> DashboardScreen(role, ::navigate)
            }
        }
    }
}

private fun tabsFor(area: AccessArea): List<ShellTab> = when (area) {
    AccessArea.DIRETTIVO -> listOf(
        ShellTab("admin_home", "Direttivo", Icons.Default.Groups),
        ShellTab("members", "Soci", Icons.Default.Group),
        ShellTab("services", "Servizi", Icons.Default.MedicalServices),
        ShellTab("vehicles", "Mezzi", Icons.Default.DirectionsCar),
        ShellTab("more", "Altro", Icons.Default.MoreHoriz)
    )
    AccessArea.SOCI -> listOf(
        ShellTab("member_home", "Home", Icons.Default.Home),
        ShellTab("shifts", "Turni", Icons.Default.CalendarMonth),
        ShellTab("services", "Servizi", Icons.Default.MedicalServices),
        ShellTab("communications", "Avvisi", Icons.Default.Notifications),
        ShellTab("profile", "Profilo", Icons.Default.AccountCircle)
    )
    AccessArea.MAGAZZINO -> listOf(
        ShellTab("warehouse_home", "Magazzino", Icons.Default.Inventory2),
        ShellTab("warehouse", "Inventario", Icons.Default.ListAlt),
        ShellTab("warehouse_movements", "Movimenti", Icons.Default.SwapHoriz),
        ShellTab("members", "Soci", Icons.Default.Groups),
        ShellTab("profile", "Profilo", Icons.Default.AccountCircle)
    )
    AccessArea.SERVIZI_SOCIALI -> listOf(
        ShellTab("social_home", "Home", Icons.Default.MedicalServices),
        ShellTab("requests", "Richieste", Icons.Default.Inbox),
        ShellTab("services", "Servizi", Icons.Default.LocalHospital),
        ShellTab("profile", "Profilo", Icons.Default.AccountCircle)
    )
    AccessArea.OLP -> listOf(
        ShellTab("civil_home", "Servizio Civile", Icons.Default.School),
        ShellTab("civil", "Gestione", Icons.Default.CalendarMonth),
        ShellTab("profile", "Altro", Icons.Default.MoreHoriz)
    )
    AccessArea.SERVIZIO_CIVILE -> listOf(
        ShellTab("civil_home", "Home", Icons.Default.Home),
        ShellTab("civil", "Turni e corsi", Icons.Default.CalendarMonth),
        ShellTab("profile", "Profilo", Icons.Default.AccountCircle)
    )
}
