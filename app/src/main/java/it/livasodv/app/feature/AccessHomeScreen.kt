@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package it.livasodv.app.feature

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.livasodv.app.R
import it.livasodv.app.ui.theme.*

enum class AccessArea(val title: String) {
    DIRETTIVO("Direttivo"),
    SOCI("Soci"),
    MAGAZZINO("Magazzino"),
    SERVIZI_SOCIALI("Servizi Sociali"),
    OLP("Servizio Civile · OLP"),
    SERVIZIO_CIVILE("Servizio Civile · Operatore")
}

@Composable
fun AccessHomeScreen(
    onProtectedArea: (AccessArea) -> Unit,
    onCitizenArea: () -> Unit,
    onPublicTool: (String) -> Unit
) {
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color.White, Color(0xFFF8FAFD), Color.White))
        )
    ) {
        Column(
            Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(horizontal = 26.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(18.dp))
            Image(
                painter = painterResource(R.drawable.livas_official_logo),
                contentDescription = "Logo Lì.v.a.s.",
                modifier = Modifier.size(190.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text("Benvenuto in", style = MaterialTheme.typography.titleLarge, color = LivasNavy, fontWeight = FontWeight.Bold)
            Text("Lì.v.a.s. O.D.V.", style = MaterialTheme.typography.headlineMedium, color = LivasNavy, fontWeight = FontWeight.Black)
            Text("Gonnosfanadiga", style = MaterialTheme.typography.titleMedium, color = LivasMuted)
            Spacer(Modifier.height(26.dp))

            AccessButton(
                title = "Area Direttivo",
                subtitle = "Accesso riservato",
                icon = Icons.Default.Shield,
                start = LivasNavy,
                end = Color(0xFF0D59C6)
            ) { onProtectedArea(AccessArea.DIRETTIVO) }
            Spacer(Modifier.height(12.dp))
            AccessButton(
                title = "Area Soci",
                subtitle = "Accesso riservato",
                icon = Icons.Default.Groups,
                start = Color(0xFF158B44),
                end = Color(0xFF22A653)
            ) { onProtectedArea(AccessArea.SOCI) }
            Spacer(Modifier.height(12.dp))
            AccessButton(
                title = "Area Cittadini",
                subtitle = "Accesso libero",
                icon = Icons.Default.Person,
                start = Color(0xFFF28B15),
                end = Color(0xFFFFA21B)
            ) { onCitizenArea() }

            Spacer(Modifier.weight(1f))
            Text(
                "Insieme per aiutare,\nsempre.",
                style = MaterialTheme.typography.titleMedium,
                color = LivasNavy,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PublicMiniButton("Chi siamo", Modifier.weight(1f)) { onPublicTool("about") }
                PublicMiniButton("Contatti", Modifier.weight(1f)) { onPublicTool("contacts") }
                PublicMiniButton("Emergenze", Modifier.weight(1f), danger = true) { onPublicTool("emergency") }
            }
            Spacer(Modifier.height(8.dp))
            Text("Release Candidate · Parità iPhone", style = MaterialTheme.typography.labelSmall, color = LivasMuted)
        }
    }
}

@Composable
private fun AccessButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    start: Color,
    end: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(78.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(15.dp),
        shadowElevation = 3.dp,
        color = Color.Transparent
    ) {
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(start, end))).padding(horizontal = 18.dp)) {
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = .16f), modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(29.dp)) }
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Text(subtitle, color = Color.White.copy(alpha = .82f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun PublicMiniButton(title: String, modifier: Modifier = Modifier, danger: Boolean = false, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = if (danger) LivasRed else LivasNavy),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (danger) LivasRed.copy(alpha = .5f) else LivasLine)
    ) { Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
}
