@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package it.livasodv.app.feature

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.livasodv.app.data.Member
import it.livasodv.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivasTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = LivasBackgroundDeep,
            scrolledContainerColor = LivasSurfaceStrong,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}

@Composable
fun AppleCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LivasSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, LivasLine),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = content
    )
}

@Composable
fun MemberAvatar(member: Member, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(if (member.isActive) Color(0xFFE8F2FF) else Color(0xFFF0F1F3), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val initials = listOf(member.firstName.firstOrNull(), member.lastName.firstOrNull()).filterNotNull().joinToString("").uppercase()
        if (initials.isNotBlank()) Text(initials, color = LivasNavy, fontWeight = FontWeight.Bold)
        else Icon(Icons.Default.Person, null, tint = LivasNavy)
    }
}

@Composable
fun QualificationBadge(label: String, color: Color) {
    Surface(color = color, shape = RoundedCornerShape(6.dp)) {
        Text(label, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp))
    }
}

@Composable
fun MemberQualificationBadges(member: Member, compact: Boolean = false) {
    Row(horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 7.dp), verticalAlignment = Alignment.CenterVertically) {
        if (member.enabled118) QualificationBadge("118", LivasRed)
        if (member.enabledPc) QualificationBadge("PC", LivasBlue)
        if (member.enabledAib) QualificationBadge("AIB", LivasGreen)
        if (member.qualifications.contains("__SERVIZI_SOCIALI__")) QualificationBadge("SS", LivasPurple)
    }
}

@Composable
fun StatusPill(text: String, active: Boolean = true) {
    Surface(color = if (active) Color(0xFFE4F5EA) else Color(0xFFFFE8E8), shape = RoundedCornerShape(999.dp)) {
        Text(text, color = if (active) LivasGreen else LivasRed, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp))
    }
}
