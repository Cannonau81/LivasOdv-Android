@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package it.livasodv.app.feature

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
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
    // Stessa sequenza e stesse icone semantiche di WelcomeAccessView (iOS Build 31).
    val tiles = listOf(
        AppleAccessTile("DIRETTIVO", "Gestione", Icons.Default.Groups, protectedArea = AccessArea.DIRETTIVO),
        AppleAccessTile("SOCI", "Turni e servizi", Icons.Default.Group, protectedArea = AccessArea.SOCI),
        AppleAccessTile("MAGAZZINO", "Materiali e DPI", Icons.Default.Inventory2, protectedArea = AccessArea.MAGAZZINO),
        AppleAccessTile("SERVIZI SOCIALI", "Richieste", Icons.Default.MedicalServices, protectedArea = AccessArea.SERVIZI_SOCIALI),
        AppleAccessTile("S.C. • OLP", "Gestione", Icons.Default.School, protectedArea = AccessArea.OLP),
        AppleAccessTile("S.C. OPERATORI", "Turni e corsi", Icons.Default.Badge, protectedArea = AccessArea.SERVIZIO_CIVILE),
        AppleAccessTile("CITTADINI", "Richieste", Icons.Default.WavingHand, citizen = true),
        AppleAccessTile("EMERGENZE", "FAQ e primo soccorso", Icons.Default.Phone, publicRoute = "emergency"),
        AppleAccessTile("PASSATEMPO", "Rescue Run", Icons.Default.SportsEsports, publicRoute = "rescue_run"),
        AppleAccessTile("MONITOR PS 118", "Pronto Soccorso Sardegna", Icons.Default.MonitorHeart, publicRoute = "ps118"),
        AppleAccessTile("PROTEZIONE CIVILE", "Allerte meteo e incendi", Icons.Default.Shield, publicRoute = "civil_protection")
    )

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(
                    Color.Black,
                    Color(0xFF0E0203), // iOS: red 0.055 / green 0.006 / blue 0.012
                    Color.Black
                )
            )
        )
    ) {
        Column(
            Modifier.fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 14.dp)
                .padding(top = 12.dp, bottom = 10.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Benvenuto,", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp))
                    Text("Li.v.a.s.", color = Color.Red, fontSize = 23.sp, fontWeight = FontWeight.Black)
                }
            }
            Text(
                "Sempre pronti, sempre al servizio della comunità.",
                color = Color.White.copy(alpha = .56f),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(7.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.livas_3d_logo),
                    contentDescription = "Logo Lì.v.a.s.",
                    modifier = Modifier
                        .size(325.dp)
                        .shadow(18.dp, CircleShape)
                        .clip(CircleShape)
                        .border(1.2.dp, Brush.linearGradient(listOf(Color.Red.copy(alpha = .72f), Color(0xFFFF9500).copy(alpha = .22f), Color.Transparent)), CircleShape)
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 10.dp)
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
                border = BorderStroke(1.dp, Color.White.copy(alpha = .07f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.GridView, null, tint = Color.Red, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("ACCESSO RAPIDO", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.weight(1f))
                        Text("Beta 2.4.9", color = Color.White.copy(alpha = .40f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 116.dp)
            .shadow(9.dp, shape)
            .background(
                Brush.linearGradient(listOf(Color(0xFF161618), Color(0xFF030304))),
                shape
            )
            .clickable(onClick = onClick)
            .then(
                Modifier.background(Color.Transparent, shape)
            )
    ) {
        // bordo rosso sfumato della tile Apple
        Surface(
            modifier = Modifier.matchParentSize(),
            color = Color.Transparent,
            shape = shape,
            border = BorderStroke(
                1.25.dp,
                Brush.linearGradient(listOf(Color(0xFFFF382B), Color(0xFF8F0005)))
            )
        ) {}

        Column(
            Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(Modifier.height(45.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                // doppio livello: ombra scura + icona rossa, come il simbolo 3D della Build Apple 31
                Icon(
                    tile.icon,
                    null,
                    tint = Color(0xFF4D0004),
                    modifier = Modifier.size(34.dp).offset(x = 2.dp, y = 4.dp)
                )
                Icon(
                    tile.icon,
                    null,
                    tint = Color(0xFFF20A0A),
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(Modifier.height(7.dp))
            Text(
                tile.title,
                color = Color.White,
                fontSize = 10.5.sp,
                lineHeight = 11.5.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(Modifier.height(2.dp))
            Text(
                tile.subtitle,
                color = Color.White.copy(alpha = .55f),
                fontSize = 8.5.sp,
                lineHeight = 9.5.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}
