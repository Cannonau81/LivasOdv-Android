package it.livasodv.app.feature

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.livasodv.app.data.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CivilServiceFullScreen(onBack: () -> Unit) {
    val repo = AppGraph.repo
    val volunteers by repo.civilVolunteers.collectAsState(); val shifts by repo.civilShifts.collectAsState(); val shiftLinks by repo.civilShiftVolunteers.collectAsState()
    val courses by repo.civilCourses.collectAsState(); val courseLinks by repo.civilCourseVolunteers.collectAsState(); val leaves by repo.civilLeave.collectAsState()
    val profile by repo.profile.collectAsState(); val role by repo.role.collectAsState(); val scope = rememberCoroutineScope()
    val canManage = role == AppRole.DIRETTIVO || role == AppRole.OLP
    val selfId = profile?.civilVolunteerId
    var section by remember { mutableIntStateOf(0) }
    var editVolunteer by remember { mutableStateOf<CivilVolunteer?>(null) }; var addVolunteer by remember { mutableStateOf(false) }
    var editShift by remember { mutableStateOf<CivilShift?>(null) }; var addShift by remember { mutableStateOf(false) }
    var editCourse by remember { mutableStateOf<CivilCourse?>(null) }; var addCourse by remember { mutableStateOf(false) }
    var addLeave by remember { mutableStateOf(false) }; var decide by remember { mutableStateOf<CivilLeaveRequest?>(null) }
    var deleteVolunteer by remember { mutableStateOf<CivilVolunteer?>(null) }; var deleteShift by remember { mutableStateOf<CivilShift?>(null) }; var deleteCourse by remember { mutableStateOf<CivilCourse?>(null) }
    val labels = listOf("Ragazzi","Turni","Corsi","Richieste","Calendario","Ore")

    Scaffold(topBar = { LivasTopAppBar(title = { Text("Servizio Civile") }, navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, null) } }, actions = { IconButton({ scope.launch { repo.refreshAll() } }) { Icon(Icons.Default.Refresh, null) } }) }) { p ->
        Column(Modifier.padding(p).fillMaxSize()) {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 10.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                labels.forEachIndexed { i, label -> FilterChip(section == i, { section = i }, { Text(label) }) }
            }
            Box(Modifier.weight(1f)) {
                when(section) {
                    0 -> LazyColumn(contentPadding = PaddingValues(12.dp)) {
                        if(volunteers.isEmpty()) item { Text("Nessun volontario registrato.") }
                        items(volunteers.sortedBy { it.lastName }, key={it.id}) { v ->
                            ListItem(headlineContent={Text("${v.lastName} ${v.firstName}",fontWeight=FontWeight.Bold)}, supportingContent={Text("${v.projectName ?: "—"} · ${v.startDate ?: "—"} → ${v.endDate ?: "—"}\n${v.phone ?: ""} ${v.email ?: ""}")}, leadingContent={Icon(Icons.Default.Badge,null)}, trailingContent=if(canManage) {{ Row { IconButton({editVolunteer=v}){Icon(Icons.Default.Edit,null)}; IconButton({deleteVolunteer=v}){Icon(Icons.Default.Delete,null)} } }} else null); HorizontalDivider()
                        }
                    }
                    1 -> LazyColumn(contentPadding = PaddingValues(12.dp)) {
                        if(shifts.isEmpty()) item { Text("Nessun turno registrato.") }
                        items(shifts.sortedByDescending { it.shiftDate }, key={it.id}) { s -> val count=shiftLinks.count{it.shiftId==s.id}; ListItem(headlineContent={Text(s.activity ?: "Turno",fontWeight=FontWeight.Bold)}, supportingContent={Text("${s.shiftDate} · ${s.startTime ?: "—"}–${s.endTime ?: "—"} · ${s.location ?: "—"}\n$count operatori")}, leadingContent={Icon(Icons.Default.Schedule,null)}, trailingContent=if(canManage) {{ Row { IconButton({editShift=s}){Icon(Icons.Default.Edit,null)}; IconButton({deleteShift=s}){Icon(Icons.Default.Delete,null)} } }} else null); HorizontalDivider() }
                    }
                    2 -> LazyColumn(contentPadding = PaddingValues(12.dp)) {
                        if(courses.isEmpty()) item { Text("Nessun corso registrato.") }
                        items(courses.sortedByDescending { it.courseDate }, key={it.id}) { c -> val count=courseLinks.count{it.courseId==c.id}; ListItem(headlineContent={Text(c.title,fontWeight=FontWeight.Bold)}, supportingContent={Text("${c.courseDate} · ${c.hours} ore · ${c.provider ?: "—"}\n$count partecipanti")}, leadingContent={Icon(Icons.Default.School,null)}, trailingContent=if(canManage) {{ Row { IconButton({editCourse=c}){Icon(Icons.Default.Edit,null)}; IconButton({deleteCourse=c}){Icon(Icons.Default.Delete,null)} } }} else null); HorizontalDivider() }
                    }
                    3 -> LazyColumn(contentPadding = PaddingValues(12.dp)) {
                        val visible=if(canManage) leaves else leaves.filter{it.civilVolunteerId==selfId}
                        if(visible.isEmpty()) item { Text("Nessuna richiesta.") }
                        items(visible.sortedByDescending{it.startDate},key={it.id}) { l -> val who=volunteers.firstOrNull{it.id==l.civilVolunteerId}; ListItem(headlineContent={Text(l.requestType,fontWeight=FontWeight.Bold)}, supportingContent={Text("${who?.firstName ?: ""} ${who?.lastName ?: ""} · ${l.startDate} → ${l.endDate} · ${l.status}\n${l.reason ?: ""}${l.decisionNote?.let{"\nEsito: $it"} ?: ""}")}, leadingContent={Icon(Icons.Default.EventBusy,null)}, trailingContent=if(canManage && l.status=="in_attesa") {{TextButton({decide=l}){Text("Valuta")}}} else null); HorizontalDivider() }
                    }
                    4 -> CivilCalendarContent(shifts,courses,leaves,volunteers)
                    5 -> CivilHoursContent(volunteers,shifts,shiftLinks,courses,courseLinks)
                }
            }
            if((section==0||section==1||section==2) && canManage) Button({ when(section){0->addVolunteer=true;1->addShift=true;2->addCourse=true} }, Modifier.fillMaxWidth().padding(12.dp)) { Icon(Icons.Default.Add,null); Text(" Aggiungi ${labels[section].lowercase()}") }
            if(section==3 && (canManage || role==AppRole.SERVIZIO_CIVILE)) Button({addLeave=true},Modifier.fillMaxWidth().padding(12.dp)){Icon(Icons.Default.Add,null);Text(" Nuova richiesta")}
        }
    }

    if(addVolunteer) CivilVolunteerEditorFull(null, {addVolunteer=false}) { v -> scope.launch { repo.saveCivilVolunteer(v); addVolunteer=false } }
    editVolunteer?.let { current -> CivilVolunteerEditorFull(current,{editVolunteer=null}) { v -> scope.launch { repo.saveCivilVolunteer(v); editVolunteer=null } } }
    if(addShift) CivilShiftEditorFull(null,volunteers,emptySet(),{addShift=false}) { s,ids -> scope.launch { repo.saveCivilShift(s); repo.setCivilShiftVolunteers(s.id,ids); addShift=false } }
    editShift?.let { current -> CivilShiftEditorFull(current,volunteers,shiftLinks.filter{it.shiftId==current.id}.map{it.civilVolunteerId}.toSet(),{editShift=null}) { s,ids -> scope.launch { repo.saveCivilShift(s); repo.setCivilShiftVolunteers(s.id,ids); editShift=null } } }
    if(addCourse) CivilCourseEditorFull(null,volunteers,emptySet(),{addCourse=false}) { c,ids -> scope.launch { repo.saveCivilCourse(c); repo.setCivilCourseVolunteers(c.id,ids); addCourse=false } }
    editCourse?.let { current -> CivilCourseEditorFull(current,volunteers,courseLinks.filter{it.courseId==current.id}.map{it.civilVolunteerId}.toSet(),{editCourse=null}) { c,ids -> scope.launch { repo.saveCivilCourse(c); repo.setCivilCourseVolunteers(c.id,ids); editCourse=null } } }
    if(addLeave) CivilLeaveEditorFull(volunteers, if(role==AppRole.SERVIZIO_CIVILE) selfId else null, {addLeave=false}) { l -> scope.launch { repo.saveCivilLeave(l); addLeave=false } }
    decide?.let { l -> CivilDecisionFull(l,{decide=null}) { status,note -> scope.launch { repo.decideCivilLeave(l.id,status,note); decide=null } } }
    deleteVolunteer?.let { x -> ConfirmDelete("Eliminare ${x.firstName} ${x.lastName}?",{deleteVolunteer=null}){scope.launch{repo.deleteCivilVolunteer(x.id);deleteVolunteer=null}} }
    deleteShift?.let { x -> ConfirmDelete("Eliminare il turno del ${x.shiftDate}?",{deleteShift=null}){scope.launch{repo.deleteCivilShift(x.id);deleteShift=null}} }
    deleteCourse?.let { x -> ConfirmDelete("Eliminare il corso ${x.title}?",{deleteCourse=null}){scope.launch{repo.deleteCivilCourse(x.id);deleteCourse=null}} }
}

@Composable private fun CivilCalendarContent(shifts:List<CivilShift>,courses:List<CivilCourse>,leaves:List<CivilLeaveRequest>,volunteers:List<CivilVolunteer>) {
    data class E(val date:String,val title:String,val sub:String)
    val rows=(shifts.map{E(it.shiftDate,it.activity?:"Turno","${it.startTime?:"—"}–${it.endTime?:"—"} · ${it.location?:"—"}")}+courses.map{E(it.courseDate,it.title,"Corso · ${it.hours} ore")}+leaves.map{l->val v=volunteers.firstOrNull{it.id==l.civilVolunteerId};E(l.startDate,"${l.requestType} · ${v?.firstName?:""} ${v?.lastName?:""}","${l.startDate} → ${l.endDate} · ${l.status}")}).sortedBy{it.date}
    LazyColumn(contentPadding=PaddingValues(12.dp)){if(rows.isEmpty())item{Text("Calendario vuoto.")};items(rows){e->ListItem(headlineContent={Text(e.title,fontWeight=FontWeight.Bold)},supportingContent={Text(e.sub)},leadingContent={Text(e.date.takeLast(2).ifBlank{e.date.take(10)})});HorizontalDivider()}}
}

@Composable private fun CivilHoursContent(volunteers:List<CivilVolunteer>,shifts:List<CivilShift>,links:List<CivilShiftVolunteer>,courses:List<CivilCourse>,courseLinks:List<CivilCourseVolunteer>) {
    LazyColumn(contentPadding=PaddingValues(12.dp)){if(volunteers.isEmpty())item{Text("Nessun operatore.")};items(volunteers.sortedBy{it.lastName}){v->val shiftHours=links.filter{it.civilVolunteerId==v.id}.sumOf{link->val s=shifts.firstOrNull{it.id==link.shiftId};durationHours(s?.startTime,s?.endTime)};val courseHours=courseLinks.filter{it.civilVolunteerId==v.id}.sumOf{link->courses.firstOrNull{it.id==link.courseId}?.hours?:0.0};ListItem(headlineContent={Text("${v.lastName} ${v.firstName}",fontWeight=FontWeight.Bold)},supportingContent={Text("Turni: ${"%.1f".format(shiftHours)} h · Formazione: ${"%.1f".format(courseHours)} h")},leadingContent={Icon(Icons.Default.Timer,null)});HorizontalDivider()}}
}
private fun durationHours(start:String?,end:String?):Double {
    if (start.isNullOrBlank() || end.isNullOrBlank()) return 0.0
    return runCatching { ChronoUnit.MINUTES.between(LocalTime.parse(start.take(5)), LocalTime.parse(end.take(5))).coerceAtLeast(0) / 60.0 }.getOrDefault(0.0)
}

@Composable private fun CivilVolunteerEditorFull(existing:CivilVolunteer?,dismiss:()->Unit,save:(CivilVolunteer)->Unit){var first by remember{mutableStateOf(existing?.firstName?:"")};var last by remember{mutableStateOf(existing?.lastName?:"")};var phone by remember{mutableStateOf(existing?.phone?:"")};var email by remember{mutableStateOf(existing?.email?:"")};var project by remember{mutableStateOf(existing?.projectName?:"")};var start by remember{mutableStateOf(existing?.startDate?:"")};var end by remember{mutableStateOf(existing?.endDate?:"")};var active by remember{mutableStateOf(existing?.isActive?:true)};var notes by remember{mutableStateOf(existing?.notes?:"")};AlertDialog(onDismissRequest=dismiss,title={Text(if(existing==null)"Nuovo operatore" else "Modifica operatore")},text={LazyColumn(verticalArrangement=Arrangement.spacedBy(6.dp)){item{OutlinedTextField(first,{first=it},label={Text("Nome")})};item{OutlinedTextField(last,{last=it},label={Text("Cognome")})};item{OutlinedTextField(phone,{phone=it},label={Text("Telefono")})};item{OutlinedTextField(email,{email=it},label={Text("Email")})};item{OutlinedTextField(project,{project=it},label={Text("Progetto")})};item{OutlinedTextField(start,{start=it},label={Text("Inizio YYYY-MM-DD")})};item{OutlinedTextField(end,{end=it},label={Text("Fine YYYY-MM-DD")})};item{Row(verticalAlignment=Alignment.CenterVertically){Switch(active,{active=it});Text(" Attivo")}};item{OutlinedTextField(notes,{notes=it},label={Text("Note")})}}},confirmButton={Button({save(CivilVolunteer(existing?.id?:AppGraph.repo.newId(),first.trim(),last.trim(),phone.ifBlank{null},email.ifBlank{null},project.ifBlank{null},start.ifBlank{null},end.ifBlank{null},active,notes.ifBlank{null}))},enabled=first.isNotBlank()&&last.isNotBlank()){Text("Salva")}},dismissButton={TextButton(dismiss){Text("Annulla")}})}

@Composable private fun CivilShiftEditorFull(existing:CivilShift?,volunteers:List<CivilVolunteer>,initial:Set<String>,dismiss:()->Unit,save:(CivilShift,Set<String>)->Unit){var date by remember{mutableStateOf(existing?.shiftDate?:LocalDate.now().toString())};var start by remember{mutableStateOf(existing?.startTime?:"")};var end by remember{mutableStateOf(existing?.endTime?:"")};var activity by remember{mutableStateOf(existing?.activity?:"")};var location by remember{mutableStateOf(existing?.location?:"")};var notes by remember{mutableStateOf(existing?.notes?:"")};var selected by remember{mutableStateOf(initial)};AlertDialog(onDismissRequest=dismiss,title={Text(if(existing==null)"Nuovo turno" else "Modifica turno")},text={LazyColumn(verticalArrangement=Arrangement.spacedBy(6.dp)){item{OutlinedTextField(date,{date=it},label={Text("Data YYYY-MM-DD")})};item{Row{OutlinedTextField(start,{start=it},label={Text("Inizio")},modifier=Modifier.weight(1f));Spacer(Modifier.width(6.dp));OutlinedTextField(end,{end=it},label={Text("Fine")},modifier=Modifier.weight(1f))}};item{OutlinedTextField(activity,{activity=it},label={Text("Attività")})};item{OutlinedTextField(location,{location=it},label={Text("Luogo")})};item{OutlinedTextField(notes,{notes=it},label={Text("Note")})};item{Text("Operatori",fontWeight=FontWeight.Bold)};items(volunteers,key={it.id}){v->Row(verticalAlignment=Alignment.CenterVertically){Checkbox(v.id in selected,{checked->selected=if(checked)selected+v.id else selected-v.id});Text("${v.firstName} ${v.lastName}")}}}},confirmButton={Button({save(CivilShift(existing?.id?:AppGraph.repo.newId(),date,start.ifBlank{null},end.ifBlank{null},activity.ifBlank{null},location.ifBlank{null},notes.ifBlank{null},AppGraph.repo.currentUserId()),selected)},enabled=date.isNotBlank()){Text("Salva")}},dismissButton={TextButton(dismiss){Text("Annulla")}})}

@Composable private fun CivilCourseEditorFull(existing:CivilCourse?,volunteers:List<CivilVolunteer>,initial:Set<String>,dismiss:()->Unit,save:(CivilCourse,Set<String>)->Unit){var title by remember{mutableStateOf(existing?.title?:"")};var date by remember{mutableStateOf(existing?.courseDate?:LocalDate.now().toString())};var hours by remember{mutableStateOf((existing?.hours?:0.0).toString())};var provider by remember{mutableStateOf(existing?.provider?:"")};var notes by remember{mutableStateOf(existing?.notes?:"")};var selected by remember{mutableStateOf(initial)};AlertDialog(onDismissRequest=dismiss,title={Text(if(existing==null)"Nuovo corso" else "Modifica corso")},text={LazyColumn(verticalArrangement=Arrangement.spacedBy(6.dp)){item{OutlinedTextField(title,{title=it},label={Text("Titolo")})};item{OutlinedTextField(date,{date=it},label={Text("Data YYYY-MM-DD")})};item{OutlinedTextField(hours,{hours=it},label={Text("Ore")})};item{OutlinedTextField(provider,{provider=it},label={Text("Ente / formatore")})};item{OutlinedTextField(notes,{notes=it},label={Text("Note")})};item{Text("Partecipanti",fontWeight=FontWeight.Bold)};items(volunteers,key={it.id}){v->Row(verticalAlignment=Alignment.CenterVertically){Checkbox(v.id in selected,{checked->selected=if(checked)selected+v.id else selected-v.id});Text("${v.firstName} ${v.lastName}")}}}},confirmButton={Button({save(CivilCourse(existing?.id?:AppGraph.repo.newId(),title.trim(),date,hours.replace(',','.').toDoubleOrNull()?:0.0,provider.ifBlank{null},notes.ifBlank{null},AppGraph.repo.currentUserId()),selected)},enabled=title.isNotBlank()&&date.isNotBlank()){Text("Salva")}},dismissButton={TextButton(dismiss){Text("Annulla")}})}

@Composable private fun CivilLeaveEditorFull(volunteers:List<CivilVolunteer>,forcedId:String?,dismiss:()->Unit,save:(CivilLeaveRequest)->Unit){var id by remember{mutableStateOf(forcedId?:volunteers.firstOrNull()?.id.orEmpty())};var type by remember{mutableStateOf("ferie")};var start by remember{mutableStateOf(LocalDate.now().toString())};var end by remember{mutableStateOf(LocalDate.now().toString())};var reason by remember{mutableStateOf("")};AlertDialog(onDismissRequest=dismiss,title={Text("Nuova richiesta")},text={Column(verticalArrangement=Arrangement.spacedBy(7.dp)){if(forcedId==null)ChoicePairField("Operatore",id,volunteers.map{it.id to "${it.firstName} ${it.lastName}"}){id=it};ChoicePairField("Tipo",type,listOf("ferie" to "Ferie","permesso" to "Permesso","malattia" to "Malattia","altro" to "Altro")){type=it};OutlinedTextField(start,{start=it},label={Text("Dal YYYY-MM-DD")});OutlinedTextField(end,{end=it},label={Text("Al YYYY-MM-DD")});OutlinedTextField(reason,{reason=it},label={Text("Motivo")})}},confirmButton={Button({save(CivilLeaveRequest(AppGraph.repo.newId(),id,type,start,end,reason.ifBlank{null}))},enabled=id.isNotBlank()){Text("Invia")}},dismissButton={TextButton(dismiss){Text("Annulla")}})}

@Composable private fun CivilDecisionFull(req:CivilLeaveRequest,dismiss:()->Unit,decide:(String,String?)->Unit){var note by remember{mutableStateOf("")};AlertDialog(onDismissRequest=dismiss,title={Text("Valuta richiesta")},text={Column{Text("${req.requestType}: ${req.startDate} → ${req.endDate}");OutlinedTextField(note,{note=it},label={Text("Nota esito")})}},confirmButton={Row{TextButton({decide("rifiutata",note.ifBlank{null})}){Text("Rifiuta")};Button({decide("approvata",note.ifBlank{null})}){Text("Approva")}}},dismissButton={TextButton(dismiss){Text("Annulla")}})}

@Composable private fun ChoicePairField(label:String,value:String,options:List<Pair<String,String>>,onChange:(String)->Unit){var open by remember{mutableStateOf(false)};Box{OutlinedButton({open=true},Modifier.fillMaxWidth()){Text("$label: ${options.firstOrNull{it.first==value}?.second?:value}",Modifier.weight(1f));Icon(Icons.Default.ArrowDropDown,null)};DropdownMenu(open,{open=false}){options.forEach{o->DropdownMenuItem({Text(o.second)},{onChange(o.first);open=false})}}}}
