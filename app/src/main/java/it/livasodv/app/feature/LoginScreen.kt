@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package it.livasodv.app.feature

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import it.livasodv.app.R
import it.livasodv.app.data.*
import it.livasodv.app.ui.theme.*
import kotlinx.coroutines.launch

fun roleAllowsArea(role: AppRole, area: AccessArea): Boolean = when (area) {
    AccessArea.DIRETTIVO -> role == AppRole.DIRETTIVO
    AccessArea.SOCI -> true
    AccessArea.MAGAZZINO -> role == AppRole.MAGAZZINO || role == AppRole.DIRETTIVO
    AccessArea.SERVIZI_SOCIALI -> role == AppRole.SERVIZI_SOCIALI || role == AppRole.DIRETTIVO
    AccessArea.OLP -> role == AppRole.OLP || role == AppRole.DIRETTIVO
    AccessArea.SERVIZIO_CIVILE -> role == AppRole.SERVIZIO_CIVILE || role == AppRole.OLP || role == AppRole.DIRETTIVO
}

@Composable
fun LoginScreen(area: AccessArea, onBack: () -> Unit, onSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize().background(Color(0xFFF7F9FC)).windowInsetsPadding(WindowInsets.safeDrawing)) {
        IconButton(onBack, Modifier.padding(8.dp).align(Alignment.TopStart)) {
            Icon(Icons.Default.ArrowBack, "Indietro", tint = LivasNavy)
        }
        Column(
            Modifier.fillMaxSize().padding(horizontal = 26.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(painterResource(R.drawable.livas_official_logo), "Logo Lì.v.a.s.", Modifier.size(175.dp))
            Spacer(Modifier.height(14.dp))
            Text("Accesso ${area.title}", color = LivasNavy, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text("Lì.v.a.s. O.d.V. · Gonnosfanadiga", color = LivasMuted, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(22.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LivasLine),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, null, tint = LivasNavy)
                        Spacer(Modifier.width(8.dp))
                        Text("Area protetta", fontWeight = FontWeight.Bold)
                    }
                    OutlinedTextField(email, { email = it.trim() }, label = { Text("Email") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        pass, { pass = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton({ showPassword = !showPassword }) {
                                Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, if (showPassword) "Nascondi password" else "Mostra password")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                    TextButton(
                        onClick = {
                            if (email.isBlank()) { error = "Inserisci prima l'indirizzo email." } else scope.launch {
                                loading = true; error = null
                                runCatching { SupabaseProvider.client.auth.resetPasswordForEmail(email) }
                                    .onSuccess { error = "Email di recupero inviata. Controlla la posta e segui il link ricevuto." }
                                    .onFailure { error = "Recupero password non riuscito: ${it.message ?: "errore server"}" }
                                loading = false
                            }
                        },
                        enabled = !loading,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Password dimenticata?") }
                    Button(
                        onClick = {
                            scope.launch {
                                loading = true
                                error = null
                                try {
                                    SupabaseProvider.client.auth.signInWith(Email) { this.email = email; this.password = pass }
                                    val result = AppGraph.repo.bootstrap()
                                    if (result.isFailure) {
                                        error = AppGraph.repo.error.value ?: "Accesso non autorizzato"
                                    } else if (!roleAllowsArea(AppGraph.repo.role.value, area)) {
                                        AppGraph.repo.signOut()
                                        error = "Questo account non è abilitato all'area ${area.title}."
                                    } else {
                                        onSuccess()
                                    }
                                } catch (e: Exception) {
                                    error = "Accesso non riuscito: ${e.message ?: "controlla credenziali e connessione"}"
                                } finally {
                                    loading = false
                                }
                            }
                        },
                        enabled = !loading && email.isNotBlank() && pass.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LivasNavy)
                    ) { Text(if (loading) "ACCESSO…" else "ACCEDI", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
