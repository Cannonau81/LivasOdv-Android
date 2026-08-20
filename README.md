# LivasODV Android 2.5.0 Beta Soci

Pacchetto Android allineato alla versione iPhone Build 31 e predisposto per una prima distribuzione beta a un gruppo ristretto di soci.

## Versione
- versionName: `2.5.0-beta.1`
- versionCode: `2500`
- package: `it.livasodv.app`
- minSdk: 26
- targetSdk: 36

## Allineamento iPhone Build 31
- Home/accessi con grafica e palette Apple (nero/rosso, logo 3D, griglia 4 colonne).
- Aree Direttivo, Soci, Magazzino, Servizi Sociali, OLP e Servizio Civile.
- Richieste cittadini con badge non lette e sincronizzazione Supabase.
- Servizi Sociali autenticato realmente tramite Supabase e ruolo `servizi_sociali`.
- Protezione aggiuntiva delle aree riservate con biometria/credenziale dispositivo dopo il login.
- Logout automatico dopo 3 minuti in background.
- Ricerca e Backup presenti solo nel Direttivo, come Build 31.
- Gestione 2.1: notifiche, operativo, audit, cestino 30 giorni, scadenziario, report PDF e info.
- Scadenziario esteso anche a corsi/abilitazioni soci.
- Promemoria locali in-app per scadenze entro 7 giorni.
- Registro attività esteso a Soci, Mezzi, Turni, Servizi, Magazzino, Comunicazioni e Vestizione.
- Backup JSON completo e ripristino.
- Monitor PS118 con interfaccia nativa in stile iPhone e accesso alla fonte ufficiale.
- Protezione Civile con sezioni Allerte, Meteo, Incendi, Cosa fare e Numeri.
- Splash Android 12+ coerente con logo/app e palette scura.
- Stato offline esplicito: gli ultimi dati già caricati restano consultabili quando il server non risponde.

## CI GitHub
Il workflow è già incluso in `.github/workflows/android-ci.yml`.
Esegue:
1. Gradle 8.13 esatto
2. JDK 17
3. Android SDK 36 / Build Tools 35.0.0
4. preflight
5. assembleDebug
6. testDebugUnitTest
7. lintDebug
8. upload APK e report

Artifact atteso dopo run verde:
`LivasODV-Android-2.5.0-beta1-debug`

## Nota
Il pacchetto è stato sottoposto a controlli statici e preflight in questo ambiente. La compilazione Gradle completa deve essere confermata dal run GitHub Actions perché l'ambiente di preparazione non può raggiungere `services.gradle.org`.
