# CI Fix 2.3.5
- Mantiene i fix Ktor DEX e Supabase Experimental API.
- Workflow GitHub Actions senza `gradle/actions/setup-gradle`.
- Usa esclusivamente il Gradle Wrapper 8.13 incluso nel repository.
- Rimosso anche il cache Gradle da `actions/setup-java` per rendere il log inequivocabile.
- Marker CI: `LivasODV Android 2.3.5 CI - NO GRADLE ACTION`.
