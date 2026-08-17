# Lì.v.a.s. O.D.V. – Android

Versione Android ricostruita per ottenere la **parità funzionale con l'app iPhone Build 31** e usare lo stesso backend Supabase.

## Build corrente

- Versione app: `2.1.0-ios-parity-github`
- Version code: `210`
- Package: `it.livasodv.app`
- Min SDK: 26
- Compile SDK: 36
- Target SDK: 35
- JDK CI: 17
- Gradle CI: 8.13
- Backend: Supabase condiviso iPhone ↔ Android

## Compilazione su GitHub (senza Android Studio)

1. Carica **il contenuto di questa cartella** in un repository GitHub.
2. Apri la scheda **Actions**.
3. Avvia `LivasODV Android CI` con **Run workflow**, oppure fai semplicemente un push.
4. La pipeline esegue `:app:assembleDebug`.
5. A build terminata apri il workflow e scarica l'artifact **LivasODV-Android-debug**.

Se la compilazione fallisce, GitHub conserva anche `LivasODV-build-reports`: usa quel log per correggere gli errori in un unico giro.

## Aree e funzioni allineate all'iPhone

### Accesso
- Login Supabase reale e recupero password via email
- Direttivo
- Soci
- Magazzino
- Servizi Sociali
- Servizio Civile / OLP
- Servizio Civile operatori
- Cittadini
- Emergenze
- Passatempo / Rescue Run
- Monitor PS 118
- Protezione Civile

### Direttivo
- Dashboard in stile iPhone con logo 3D e accessi rapidi
- Soci: ricerca, aggiunta, modifica, eliminazione, qualifiche 118/PC/AIB, autista
- Scheda socio: recapiti, qualifiche, **Servizi Sociali**, corsi/abilitazioni, dotazioni, turni e servizi assegnati
- Vestizione: profili 118/PC/AIB e dotazione standard Lì.v.a.s., con quantità/taglie/consegne
- Turni: CRUD, autista/equipaggio
- Servizi: CRUD, mezzo, stato, autista/equipaggio
- Mezzi: CRUD, assicurazione, revisione, km, manutenzioni e storico km
- Magazzino: articoli, quantità, soglie e movimenti
- Presidi: area separata con archivio dedicato
- Comunicazioni
- Richieste cittadini e assegnazione mezzo
- Ricerca globale
- Backup completo JSON e ripristino
- Gestione 2.1: notifiche, operativo, audit, cestino, scadenze, report PDF, informazioni

### Servizio Civile / OLP
- Operatori: aggiunta, modifica, eliminazione
- Turni e assegnazione operatori
- Corsi e partecipanti
- Ferie / permessi / malattia
- Approvazione o rifiuto OLP con nota
- Calendario
- Riepilogo ore turni e formazione

### Pubblico
- Richieste cittadino
- Numeri di emergenza
- Monitor PS 118
- Protezione Civile: Allerte, Meteo, Incendi, Cosa fare, Numeri
- Rescue Run

## Sincronizzazione

Le entità condivise (soci, mezzi, turni, servizi, vestizione, magazzino, comunicazioni, richieste e Servizio Civile) usano le tabelle Supabase già utilizzate dal progetto. Ruoli e autorizzazioni vengono letti da `profiles.role` e dalle policy RLS del server.

Ricerca e Backup sono accessibili dall'area Direttivo.

## Sicurezza

Nel client è presente soltanto la **publishable key** Supabase. Non deve essere inserita alcuna `service_role` key nell'app o nel repository. Le autorizzazioni devono continuare a essere applicate dal backend tramite RLS.

I file di firma Android (`.jks`, `.keystore`) sono esclusi dal repository. Questa pipeline produce volutamente un APK **debug**. La firma release può essere aggiunta in seguito con GitHub Secrets senza salvare credenziali nel codice.

## Struttura GitHub

- `.github/workflows/android-ci.yml` – build automatica APK
- `app/` – applicazione Android
- `PARITA_IPHONE_BUILD31.md` – mappa delle funzioni iPhone → Android
- `.gitignore` – esclude build, IDE e file di firma
