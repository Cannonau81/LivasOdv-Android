package it.livasodv.app.feature

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import it.livasodv.app.data.LocalManagementStore
import it.livasodv.app.data.PresidioRecord
import java.security.MessageDigest

private const val PRESIDI_HASH = "93473afb4c54dd40f6641b5ab343fa924185793997cb8b7527e8b15e12373697"

private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
    .digest(value.toByteArray())
    .joinToString("") { "%02x".format(it) }

@Composable
fun PresidiGateScreen(onBack: () -> Unit) {
    var authenticated by remember { mutableStateOf(false) }
    if (authenticated) {
        PresidiRootScreen(onBack) { authenticated = false }
        return
    }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    Scaffold(topBar = { LivasTopAppBar(title = { Text("Presidi") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } }) }) { p ->
        Column(Modifier.padding(p).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.MedicalServices, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
            Text("Archivio Presidi", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Area separata dal magazzino, come nella versione iPhone.")
            OutlinedTextField(username, { username = it }, label = { Text("Nome utente") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(password, { password = it }, label = { Text("Password") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button({
                if (username.trim().equals("presidi", true) && sha256(password) == PRESIDI_HASH) {
                    authenticated = true; error = null
                } else error = "Credenziali Presidi non valide."
            }, enabled = username.isNotBlank() && password.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("ACCEDI A PRESIDI") }
        }
    }
}

@Composable
private fun PresidiRootScreen(onBack: () -> Unit, onLogout: () -> Unit) {
    val values by LocalManagementStore.presidi.collectAsState()
    var editing by remember { mutableStateOf<PresidioRecord?>(null) }
    var add by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<PresidioRecord?>(null) }
    Scaffold(
        topBar = { LivasTopAppBar(title = { Text("Presidi") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } }, actions = { IconButton({ add = true }) { Icon(Icons.Default.Add, "Aggiungi") } }) }
    ) { p ->
        LazyColumn(Modifier.padding(p), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item { Text("Tipi di presidio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            if (values.isEmpty()) item { Text("Nessun presidio. Aggiungi manualmente il primo con +.") }
            items(values, key = { it.id }) { item ->
                ListItem(
                    modifier = Modifier.fillMaxWidth().clickable { editing = item },
                    headlineContent = { Text(item.name, fontWeight = FontWeight.Bold) },
                    supportingContent = { Text("${item.category} · Quantità ${item.quantity}${item.notes?.takeIf { it.isNotBlank() }?.let { "\n$it" } ?: ""}") },
                    leadingContent = { Icon(Icons.Default.MedicalServices, null) },
                    trailingContent = { Row { if (item.available) Icon(Icons.Default.CheckCircle, "Disponibile", tint = MaterialTheme.colorScheme.tertiary); IconButton({ deleting = item }) { Icon(Icons.Default.Delete, "Elimina") } } }
                )
                HorizontalDivider()
            }
            item { OutlinedButton(onLogout, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) { Icon(Icons.Default.Logout, null); Text(" Esci da Presidi") } }
        }
    }
    if (add) PresidioEditor(null, { add = false }) { LocalManagementStore.addPresidio(it); add = false }
    editing?.let { current -> PresidioEditor(current, { editing = null }) { LocalManagementStore.addPresidio(it); editing = null } }
    deleting?.let { current -> AlertDialog(onDismissRequest = { deleting = null }, title = { Text("Eliminare presidio?") }, text = { Text(current.name) }, confirmButton = { TextButton({ LocalManagementStore.deletePresidio(current); deleting = null }) { Text("Elimina") } }, dismissButton = { TextButton({ deleting = null }) { Text("Annulla") } }) }
}

@Composable
private fun PresidioEditor(existing: PresidioRecord?, onDismiss: () -> Unit, onSave: (PresidioRecord) -> Unit) {
    var name by remember(existing) { mutableStateOf(existing?.name ?: "") }
    var category by remember(existing) { mutableStateOf(existing?.category ?: "Altro") }
    var quantity by remember(existing) { mutableStateOf((existing?.quantity ?: 1).toString()) }
    var available by remember(existing) { mutableStateOf(existing?.available ?: true) }
    var notes by remember(existing) { mutableStateOf(existing?.notes ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Nuovo presidio" else "Modifica presidio") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(category, { category = it }, label = { Text("Categoria") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(quantity, { quantity = it.filter(Char::isDigit) }, label = { Text("Quantità") }, modifier = Modifier.fillMaxWidth())
            Row { Switch(available, { available = it }); Spacer(Modifier.width(8.dp)); Text("Disponibile") }
            OutlinedTextField(notes, { notes = it }, label = { Text("Note") }, minLines = 2, modifier = Modifier.fillMaxWidth())
        } },
        confirmButton = { TextButton({ onSave(PresidioRecord(existing?.id ?: java.util.UUID.randomUUID().toString(), name.trim(), category.trim().ifBlank { "Altro" }, quantity.toIntOrNull() ?: 0, available, notes.trim().ifBlank { null })) }, enabled = name.isNotBlank()) { Text("Salva") } },
        dismissButton = { TextButton(onDismiss) { Text("Annulla") } }
    )
}
