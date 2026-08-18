@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package it.livasodv.app.feature

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.livasodv.app.R


enum class AccessArea(val title: String) {
    DIRETTIVO("Direttivo"),
    SOCI("Soci"),
    MAGAZZINO("Magazzino"),
    SERVIZI_SOCIALI("Servizi Sociali"),
    OLP("Servizio Civile · OLP"),
    SERVIZIO_CIVILE("Servizio Civile · Operatore")
}

private data class AppleAccessTile(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val protectedArea: AccessArea? = null,
    val publicRoute: String? = null,
    val citizen: Boolean = false
)

@Composable
fun AccessHomeScreen(
    onProtectedArea: (AccessArea) -> Unit,
    onCitizenArea: () -> Unit,
    onPublicTool: (String) -> Unit
) {
    val tiles = listOf(
        AppleAccessTile("DIRETTIVO", "Gestione", Icons.Default.Groups, protectedArea = AccessArea.DIRETTIVO),
        AppleAccessTile("SOCI", "Turni e servizi", Icons.Default.Group, protectedArea = AccessArea.SOCI),
        AppleAccessTile("MAGAZZINO", "Materiali e DPI", Icons.Default.Inventory2, protectedArea = AccessArea.MAGAZZINO),
        AppleAccessTile("SERVIZI SOCIALI", "Richieste", Icons.Default.MedicalServices, protectedArea = AccessArea.SERVIZI_SOCIALI),
        AppleAccessTile("S.C. • OLP", "Gestione", Icons.Default.School, protectedArea = AccessArea.OLP),
        AppleAccessTile("S.C. OPERATORI", "Turni e corsi", Icons.Default.Badge, protectedArea = AccessArea.SERVIZIO_CIVILE),
        AppleAccessTile("CITTADINI", "Richieste", Icons.Default.Person, citizen = true),
        AppleAccessTile("EMERGENZE", "FAQ e primo soccorso", Icons.Default.Phone, publicRoute = "emergency"),
        AppleAccessTile("PASSATEMPO", "Rescue Run", Icons.Default.SportsEsports, publicRoute = "rescue_run"),
        AppleAccessTile("MONITOR PS 118", "Pronto Soccorso Sardegna", Icons.Default.MonitorHeart, publicRoute = "ps118"),
        AppleAccessTile("PROTEZIONE CIVILE", "Allerte meteo e incendi", Icons.Default.Shield, publicRoute = "civil_protection")
    )

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(Color.Black, Color(0xFF0E0204), Color.Black)
            )
        )
    ) {
        Column(
            Modifier.fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Benvenuto, ", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                        Text("Li.v.a.s.", color = Color(0xFFE01B24), fontSize = 23.sp, fontWeight = FontWeight.Black)
                    }
                    Text(
                        "Sempre pronti, sempre al servizio della comunità.",
                        color = Color.White.copy(alpha = .56f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.livas_3d_logo),
                    contentDescription = "Logo Lì.v.a.s.",
                    modifier = Modifier.size(285.dp)
                )
            }
            Spacer(Modifier.height(6.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(tiles) { tile ->
                    MockupAccessTileAndroid(tile) {
                        when {
                            tile.protectedArea != null -> onProtectedArea(tile.protectedArea)
                            tile.citizen -> onCitizenArea()
                            tile.publicRoute != null -> onPublicTool(tile.publicRoute)
                        }
                    }
                }
            }

            Surface(
                color = Color.White.copy(alpha = .035f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = .07f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.GridView, null, tint = Color(0xFFE01B24), modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("ACCESSO RAPIDO", color = Color(0xFFE01B24), fontSize = 11.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.weight(1f))
                        Text("Beta 2.4.4", color = Color.White.copy(alpha = .40f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "Le aree riservate richiedono autenticazione. Le funzioni disponibili dipendono dal ruolo.",
                        color = Color.White.copy(alpha = .42f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MockupAccessTileAndroid(tile: AppleAccessTile, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(94.dp)
            .shadow(7.dp, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF171719),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = .09f))
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 5.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(Color(0xFFE01B24).copy(alpha = .14f), RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(tile.icon, null, tint = Color(0xFFE01B24), modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(
                tile.title,
                color = Color.White,
                fontSize = if (tile.title.length > 13) 8.sp else 9.sp,
                lineHeight = 10.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Text(
                tile.subtitle,
                color = Color.White.copy(alpha = .43f),
                fontSize = 7.5.sp,
                lineHeight = 9.sp,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}
