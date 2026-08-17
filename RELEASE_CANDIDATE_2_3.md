# Lì.v.a.s. O.d.V. Android 2.3.0 RC1

## Obiettivo
Release Candidate orientata alla massima parità con la build iPhone 31 e alla verifica automatica su GitHub.

## Migliorie RC1
- UI blu/bianca coerente con la versione iPhone approvata.
- Dashboard Direttivo con barra superiore, notifiche e 6 card riepilogo.
- Navigazione Direttivo allineata alla struttura iOS: Direttivo, Soci, Servizi, Mezzi, Altro.
- Badge richieste cittadino sul tab Direttivo.
- Target Android 16 / API 36.
- Supporto edge-to-edge e ridimensionamento IME.
- Login rifinito con tastiera email e mostra/nascondi password.
- Sincronizzazione equipaggi: riallineamento automatico delle tabelle ponte con chiave composta.
- Riallineamento automatico assegnazioni turni/corsi del Servizio Civile.
- GitHub Actions: test unitari, Android Lint e assembleDebug nello stesso workflow.
- Test automatici per mapping ruoli e autorizzazione aree protette.

## Verifica locale eseguita
- Bilanciamento sintattico di 23 file Kotlin.
- Parsing XML Manifest, styles e file_paths.
- Scansione del client per chiavi service_role / sb_secret.

## Verifica da eseguire in GitHub
Il workflow `.github/workflows/android-ci.yml` esegue:
1. testDebugUnitTest
2. lintDebug
3. assembleDebug
4. upload report
5. upload APK in caso di successo

La compilazione completa richiede accesso alle dipendenze Maven/Gradle e viene quindi demandata a GitHub Actions.
