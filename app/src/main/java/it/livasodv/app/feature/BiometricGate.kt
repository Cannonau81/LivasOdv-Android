@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package it.livasodv.app.feature

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.core.content.ContextCompat
import it.livasodv.app.R
import it.livasodv.app.ui.theme.LivasRed
import java.util.concurrent.Executor

/**
 * Secondo livello di protezione dopo il login, equivalente a Face ID / Touch ID della Build 31 iPhone.
 * Se sul dispositivo non è disponibile alcun metodo biometrico/credenziale sicura, il login già
 * completato resta sufficiente, come previsto anche dalla versione iOS per device/simulatori non compatibili.
 */
@Composable
fun BiometricProtectedArea(
    areaName: String,
    onExit: () -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var unlocked by remember(areaName) { mutableStateOf(false) }
    var checking by remember(areaName) { mutableStateOf(false) }
    var error by remember(areaName) { mutableStateOf<String?>(null) }

    fun authenticate() {
        if (unlocked || checking) return
        if (activity == null) {
            unlocked = true
            return
        }
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        val manager = BiometricManager.from(activity)
        val can = manager.canAuthenticate(authenticators)
        if (can != BiometricManager.BIOMETRIC_SUCCESS) {
            unlocked = true
            return
        }
        checking = true
        error = null
        val executor: Executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                checking = false
                unlocked = true
                error = null
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                checking = false
                error = if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    "Verifica annullata."
                } else {
                    "Verifica non completata: $errString"
                }
            }
            override fun onAuthenticationFailed() {
                error = "Identità non riconosciuta. Riprova."
            }
        })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Area $areaName protetta")
            .setSubtitle("Conferma la tua identità per continuare")
            .setAllowedAuthenticators(authenticators)
            .build()
        prompt.authenticate(info)
    }

    LaunchedEffect(areaName) { authenticate() }

    if (unlocked) {
        content()
    } else {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                Modifier.fillMaxSize().padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.livas_official_logo),
                    contentDescription = "Logo Lì.v.a.s.",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(104.dp)
                )
                Spacer(Modifier.height(18.dp))
                Icon(Icons.Default.Fingerprint, null, tint = LivasRed, modifier = Modifier.size(52.dp))
                Spacer(Modifier.height(14.dp))
                Text("Area $areaName protetta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Conferma la tua identità per continuare.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
                )
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 10.dp))
                }
                Button(onClick = { authenticate() }, enabled = !checking, modifier = Modifier.fillMaxWidth().padding(top = 18.dp)) {
                    if (checking) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                    else Icon(Icons.Default.LockOpen, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (checking) "Verifica…" else "Sblocca")
                }
                TextButton(onClick = onExit, modifier = Modifier.padding(top = 6.dp)) { Text("Esci") }
            }
        }
    }
}
