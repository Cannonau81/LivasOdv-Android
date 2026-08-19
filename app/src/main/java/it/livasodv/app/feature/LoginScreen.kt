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
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import it.livasodv.app.R
import it.livasodv.app.data.*
import it.livasodv.app.ui.theme.*
import kotlinx.coroutines.launch
import java.security.MessageDigest

fun roleAllowsArea(role: AppRole, area: AccessArea): Boolean = when (area) {
    AccessArea.DIRETTIVO -> role == AppRole.DIRETTIVO
    AccessArea.SOCI -> true
    AccessArea.MAGAZZINO -> role == AppRole.MAGAZZINO || role == AppRole.DIRETTIVO
    AccessArea.SERVIZI_SOCIALI -> role == AppRole.SERVIZI_SOCIALI || role == AppRole.DIRETTIVO
    AccessArea.OLP -> role == AppRole.OLP || role == AppRole.DIRETTIVO
    AccessArea.SERVIZIO_CIVILE -> role == AppRole.SERVIZIO_CIVILE || role == AppRole.OLP || role == AppRole.DIRETTIVO
}

private object AppleLocalAccess {
    const val ADMIN_HASH = "185d0db42de5af30d2bb69f9d9fed98b76592c9d884f714c5ac9f63bda5d3a35"
    const val SOCIO_HASH = "ef5da651dd6856012559bbb75f16f1cb6ec913806780d51d7288a9a149c33f40"
    const val MAGAZZINO_HASH = "8a64994067e1a3cc1937e2bb1c242c1c7f6c9c90b38005d05af3542e0e8218e9"
    const val OLP_HASH = "d9154165ae84f24c5ead0ba62649e1b2fb8ab433c49751b94c40cc5a2de76804"
    const val APPLE_REVIEW_HASH = "03678646920bc7ccb61248ae63529e6b8bcf8e32944d5f2c5ec4bdbece387c20"
    const val SERVIZI_SOCIALI_HASH = "41750d95d4467a1e0211299f1f46f965dc6794b80a7293cdbd639c69e64d7798"
    const val SERVIZI_SOCIALI_LEGACY_HASH = "03678646920bc7ccb61248ae63529e6b8bcf8e32944d5f2c5ec4bdbece387c20"
}

private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
    .digest(value.toByteArray())
    .joinToString("") { "%02x".format(it) }

private fun localRoleFor(area: AccessArea, username: String, password: String): AppRole? {
    val clean = username.trim().lowercase()
    val hash = sha256(password)
    if (clean == "appletest" && hash == AppleLocalAccess.APPLE_REVIEW_HASH) {
        return when (area) {
            AccessArea.DIRETTIVO -> AppRole.DIRETTIVO
            AccessArea.SOCI -> AppRole.SOCIO
            AccessArea.MAGAZZINO -> AppRole.MAGAZZINO
            AccessArea.SERVIZI_SOCIALI -> AppRole.SERVIZI_SOCIALI
            AccessArea.OLP -> AppRole.OLP
            AccessArea.SERVIZIO_CIVILE -> AppRole.SERVIZIO_CIVILE
        }
    }
    return when (area) {
        AccessArea.DIRETTIVO -> if (clean == "admin" && hash == AppleLocalAccess.ADMIN_HASH) AppRole.DIRETTIVO else null
        AccessArea.SOCI -> if (clean == "socio" && hash == AppleLocalAccess.SOCIO_HASH) AppRole.SOCIO else null
        AccessArea.MAGAZZINO -> if (clean == "magazzino" && hash == AppleLocalAccess.MAGAZZINO_HASH) AppRole.MAGAZZINO else null
        AccessArea.OLP -> if (clean == "olp" && hash == AppleLocalAccess.OLP_HASH) AppRole.OLP else null
        AccessArea.SERVIZI_SOCIALI -> if (clean == "servizisociali" && (hash == AppleLocalAccess.SERVIZI_SOCIALI_HASH || hash == AppleLocalAccess.SERVIZI_SOCIALI_LEGACY_HASH)) AppRole.SERVIZI_SOCIALI else null
        AccessArea.SERVIZIO_CIVILE -> null
    }
}

private fun loginEmailFor(area: AccessArea, username: String): String? {
    val clean = username.trim().lowercase()
    return when (area) {
        AccessArea.SERVIZI_SOCIALI -> if (clean == "servizisociali" || clean == "appletest") "sannavalerio@gmail.com" else null
        AccessArea.DIRETTIVO -> if (clean == "admin") "livas.gonnos@tiscali.it" else null
        AccessArea.SOCI -> if (clean == "socio") "sannav@libero.it" else null
        else -> null
    }
}

private fun usernameHintFor(area: AccessArea): String = when (area) {
    AccessArea.DIRETTIVO -> "Nome utente amministratore"
    AccessArea.SOCI -> "Nome utente socio"
    AccessArea.MAGAZZINO -> "Nome utente magazzino"
    AccessArea.SERVIZI_SOCIALI -> "Nome utente Servizi Sociali"
    AccessArea.OLP -> "Nome utente OLP"
    AccessArea.SERVIZIO_CIVILE -> "Nome utente operatore"
}

@Composable
fun LoginScreen(area: AccessArea, onBack: () -> Unit, onSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black, Color(0xFF0E0203), Color.Black))).windowInsetsPadding(WindowInsets.safeDrawing)) {
        IconButton(onBack, Modifier.padding(8.dp).align(Alignment.TopStart)) {
            Icon(Icons.Default.ArrowBack, "Indietro", tint = Color.White)
        }
        Column(
            Modifier.fillMaxSize().padding(horizontal = 26.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(painterResource(R.drawable.livas_official_logo), "Logo Lì.v.a.s.", Modifier.size(190.dp))
            Spacer(Modifier.height(14.dp))
            Text("Accesso ${area.title}", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text("Lì.v.a.s. O.d.V. · Gonnosfanadiga", color = Color.White.copy(alpha = .72f), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(22.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A3038).copy(alpha = .92f)),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.24f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Area protetta", fontWeight = FontWeight.Bold)
                    }
                    OutlinedTextField(
                        username,
                        { username = it },
                        label = { Text(usernameHintFor(area)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
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
                            if (area != AccessArea.SERVIZI_SOCIALI) {
                                error = "La password di questa area è gestita dall'associazione. Contatta il Direttivo per il ripristino."
                            } else {
                                val serverEmail = loginEmailFor(area, username)
                                if (serverEmail == null) {
                                    error = "Nome utente non riconosciuto per questa area."
                                } else scope.launch {
                                    loading = true; error = null
                                    runCatching { SupabaseProvider.client.auth.resetPasswordForEmail(serverEmail) }
                                        .onSuccess { error = "Richiesta di recupero inviata all'indirizzo associato a questo account." }
                                        .onFailure { error = "Recupero password non riuscito. Riprova più tardi." }
                                    loading = false
                                }
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
                                    if (area == AccessArea.SERVIZI_SOCIALI) {
                                        val serverEmail = loginEmailFor(area, username)
                                            ?: throw IllegalArgumentException("Nome utente o password non corretti.")
                                        // Servizi Sociali deve avere una sessione Supabase reale: niente fallback locale.
                                        // Il nickname resta "servizisociali", ma l'identità tecnica è l'account Auth
                                        // dedicato creato sul backend e già marcato con role=servizi_sociali.
                                        if (localRoleFor(area, username, pass) == null) {
                                            throw IllegalArgumentException("Nome utente o password non corretti.")
                                        }
                                        SupabaseProvider.client.auth.signInWith(Email) {
                                            this.email = serverEmail
                                            this.password = pass
                                        }
                                        AppGraph.repo.bootstrap().getOrThrow()
                                        if (!roleAllowsArea(AppGraph.repo.role.value, area)) {
                                            runCatching { AppGraph.repo.signOut() }
                                            throw IllegalArgumentException("Account non autorizzato per Servizi Sociali.")
                                        }
                                        onSuccess()
                                    } else {
                                        val localRole = localRoleFor(area, username, pass)
                                            ?: throw IllegalArgumentException("Nome utente o password non corretti.")
                                        AppGraph.repo.enterLocalMode(localRole)

                                        // Se esiste anche un account server equivalente, tentiamo la sincronizzazione
                                        // senza bloccare l'accesso locale, come nella Build Apple 31.
                                        val serverEmail = loginEmailFor(area, username)
                                        if (serverEmail != null) {
                                            runCatching {
                                                SupabaseProvider.client.auth.signInWith(Email) { this.email = serverEmail; this.password = pass }
                                                AppGraph.repo.bootstrap().getOrThrow()
                                            }.onFailure { AppGraph.repo.enterLocalMode(localRole) }
                                        }
                                        onSuccess()
                                    }
                                } catch (e: Exception) {
                                    error = if (e is IllegalArgumentException) {
                                        "Nome utente o password non corretti."
                                    } else {
                                        val msg = e.message.orEmpty().lowercase()
                                        if ("invalid_credentials" in msg || "invalid login credentials" in msg) {
                                            "Nome utente o password non corretti."
                                        } else {
                                            "Accesso non riuscito. Controlla credenziali e connessione e riprova."
                                        }
                                    }
                                } finally {
                                    loading = false
                                }
                            }
                        },
                        enabled = !loading && username.isNotBlank() && pass.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LivasRed)
                    ) { Text(if (loading) "ACCESSO…" else "ACCEDI", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
