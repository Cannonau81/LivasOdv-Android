# CI Fix 2.3.6

- Forza in runtime un'unica `distributionUrl` Gradle 8.13.
- Elimina eventuali righe `distributionUrl` duplicate residue nel repository.
- Verifica che `./gradlew --version` restituisca esattamente 8.13 prima della build.
- Mantiene i fix Ktor DEX e Supabase Realtime opt-in.
