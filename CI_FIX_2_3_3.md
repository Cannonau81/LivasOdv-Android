# CI / DEX Fix 2.3.3

- Aggiornata la famiglia Ktor da 3.2.0 a 3.2.3.
- Aggiunte esplicitamente `ktor-client-core` e `ktor-utils` alla stessa versione per evitare mix di artefatti Ktor.
- Corretto il fallimento D8 su `ktor-client-core-jvm:3.2.0` durante `mergeExtDexDebug`.
- Versione app: 2.3.3 / versionCode 233.
- Nessuna modifica allo schema Supabase o ai dati.
