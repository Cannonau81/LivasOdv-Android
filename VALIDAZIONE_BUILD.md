# Validazione build

Questa repository è preparata per compilazione automatica tramite GitHub Actions.

Controlli eseguiti prima della consegna:

- parsing/struttura Kotlin: nessun errore sintattico rilevato;
- XML Android: parsing valido;
- workflow GitHub Actions: YAML valido;
- scansione `TODO` / placeholder / dati demo: nessuna voce residua;
- scansione segreti: nessuna chiave Supabase `service_role` / secret key nel client;
- Supabase: RLS mantenuta e policy di lettura aggiunte per la parità dei ruoli Servizi Sociali, Magazzino e Servizio Civile;
- ZIP finale: test di integrità eseguito.

## Compilazione reale

Il container usato per preparare il progetto non include Android SDK/Gradle con accesso alle dipendenze remote. Per questo la compilazione Android completa viene eseguita dal workflow `.github/workflows/android-ci.yml` al primo push su GitHub.

Il workflow usa:

- Java 17
- Gradle 8.13
- Android SDK 36
- task `:app:assembleDebug`

A build riuscita, scaricare l'artifact `LivasODV-Android-debug` dalla pagina del workflow.
