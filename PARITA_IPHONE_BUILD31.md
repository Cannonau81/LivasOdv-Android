# Parità iPhone Build 31 → Android 2.5.0 Beta Soci

| Area iPhone | Android 2.5.0 | Stato |
|---|---|---|
| Home accessi multipli | Sì | grafica/palette Apple |
| Login per ruolo | Sì | locale + Supabase secondo area |
| Protezione biometrica dopo login | Sì | biometria/credenziale dispositivo |
| Logout dopo 3 min background | Sì | attivo |
| Dashboard Direttivo | Sì | Supabase |
| Soci / scheda socio | Sì | Supabase |
| Qualifiche 118 / PC / AIB / SS | Sì | badge e vestizione |
| Corsi / abilitazioni | Sì | locale + backup |
| Scadenze corsi/abilitazioni | Sì | incluse nello scadenziario |
| Turni | Sì | Supabase |
| Servizi | Sì | Supabase |
| Mezzi / manutenzioni / km | Sì | Supabase |
| Magazzino / movimenti | Sì | Supabase |
| Presidi | Sì | archivio locale separato |
| Comunicazioni | Sì | Supabase |
| Richieste cittadini | Sì | Supabase + badge non lette |
| Servizi Sociali | Sì | account/ruolo Supabase reale |
| Servizio Civile / OLP | Sì | tabelle civil_* |
| Gestione 2.1 | Sì | strumenti Android |
| Registro attività | Sì | locale |
| Notifiche gestionali | Sì | locale |
| Promemoria scadenze entro 7 gg | Sì | centro notifiche locale |
| Cestino 30 giorni | Sì | locale + ripristino supportato |
| Report PDF | Sì | generazione/condivisione |
| Ricerca globale | Sì | solo Direttivo |
| Backup / Ripristino | Sì | solo Direttivo |
| Stato offline esplicito | Sì | mantiene dati già caricati |
| Emergenze / Primo Soccorso | Sì | pubblico |
| Monitor PS118 | Sì | UI nativa + fonte ufficiale |
| Protezione Civile | Sì | fonti ufficiali e autoprotezione |
| Rescue Run | Sì | locale |

## Sicurezza beta
- Nessuna `service_role` o chiave segreta server nel client.
- `allowBackup=false` nel manifest.
- HTTPS obbligatorio (`usesCleartextTraffic=false`).
- Password non salvate in chiaro nel codice Android.
- Autorizzazioni operative derivate dal ruolo server/RLS quando applicabile.
