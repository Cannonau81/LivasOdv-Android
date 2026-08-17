# Parità iPhone Build 31 → Android

Questa tabella serve come controllo funzionale della build Android finale.

| Area iPhone | Android | Backend / stato |
|---|---|---|
| Home accessi multipli | Sì | UI Android equivalente |
| Login / recupero password | Sì | Supabase Auth |
| Dashboard Direttivo | Sì | dati reali Supabase |
| Soci | Sì | `members` |
| Scheda socio | Sì | `members`, `member_clothing`, `shift_members`, `service_members` |
| Qualifiche 118 / PC / AIB / Servizi Sociali | Sì | `members` (`__SERVIZI_SOCIALI__` come marker compatibile iOS) |
| Corsi / abilitazioni socio | Sì | archivio locale, come iOS Build 31 |
| Vestizione e dotazioni | Sì | `member_clothing` + magazzino |
| Turni | Sì | `shifts`, `shift_members` |
| Servizi | Sì | `services`, `service_members` |
| Mezzi | Sì | `vehicles` |
| Manutenzioni | Sì | `vehicle_maintenance` |
| Km mensili | Sì | `vehicle_monthly_km` |
| Magazzino | Sì | `warehouse_items`, `warehouse_movements` |
| Presidi | Sì | archivio locale, come area gestionale separata |
| Comunicazioni | Sì | `communications` |
| Richieste cittadini | Sì | `citizen_requests` |
| Servizio Civile | Sì | tabelle `civil_*` |
| OLP approva/rifiuta | Sì | `civil_leave_requests` |
| Calendario / ore SC | Sì | calcolo da turni/corsi |
| Gestione 2.1 | Sì | strumenti Android |
| Notifiche gestionali | Sì | archivio locale |
| Registro attività | Sì | audit locale |
| Operativo | Sì | archivio locale |
| Cestino 30 giorni | Sì | locale + ripristino entità supportate |
| Centro scadenze | Sì | dati mezzi/manutenzioni Supabase |
| Report PDF | Sì | PDF generato e condivisibile |
| Ricerca Direttivo | Sì | server + Presidi locali |
| Backup / Ripristino | Sì | JSON server + dati locali |
| Emergenze | Sì | chiamata telefonica |
| Monitor PS 118 | Sì | fonte ufficiale via WebView |
| Protezione Civile | Sì | Allerte/Meteo/Incendi/Cosa fare/Numeri |
| Rescue Run | Sì | area passatempo |

## Nota tecnica

Alcune funzioni dell'iPhone Build 31 sono archiviate localmente nell'app iOS e non possiedono tabelle Supabase dedicate. Per non alterare il database di produzione, Android mantiene locali le corrispondenti aree (Presidi, audit, notifiche gestionali, Operativo e Cestino), includendole comunque nel backup completo.
