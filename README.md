# LivasODV Android 2.4.9

Allineamento login Servizi Sociali e richieste cittadini.

- nickname visibile: `servizisociali`
- autenticazione server: account Supabase dedicato
- ruolo richiesto: `servizi_sociali`
- nessun fallback locale per Servizi Sociali
- richieste cittadini caricate dal backend con badge e realtime già presenti nella base 2.4.7.x

# Lì.v.a.s. O.d.V. — Android 2.4.0 RC1

Release Candidate Android consolidata dopo audit completo della precedente serie 2.3.x.

## Base tecnica
- Android API 36
- AGP 8.11.1
- Gradle 8.13
- JDK 17
- Kotlin 2.2.0
- Jetpack Compose
- Supabase condiviso con l'app iPhone

## Correzioni consolidate
- Ktor 3.2.3: rimosso il precedente errore D8 di ktor-client-core-jvm 3.2.0.
- Supabase Realtime: opt-in sperimentale esplicito.
- Material3: opt-in sperimentale globale e per file.
- Dashboard: usa requestedAt, campo realmente presente nel modello e nello schema server.
- LivasApp: import `dp` presente.
- Vestizione: `applyWardrobeTemplate` implementato e coerente con qualifiche 118/PC/AIB.
- CI: Gradle 8.13 esatto, build APK prima di test/lint.

Il workflow GitHub è in `.github/workflows/android-ci.yml`.


## 2.4.9 — controllo generale + parità Apple Build 31
- Accesso Servizi Sociali confermato su sessione Supabase reale e ruolo server `servizi_sociali`.
- Recupero password automatico Servizi Sociali disattivato: il flusso email del backend ha mostrato timeout; l'app ora evita messaggi fuorvianti.
- Logout automatico dopo 180 secondi in background per le aree protette, come Apple Build 31.
- Home rifinita con logo, bordo e glow derivati dalla Build 31; palette, superfici e navigazione restano allineate al tema Apple.
- Badge richieste cittadini mantenuto su Direttivo e Servizi Sociali.
- Versione: 2.4.9 / versionCode 2490.
