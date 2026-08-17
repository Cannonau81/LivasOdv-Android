# CI Fix 2.3.7

- Removed dependence on the Gradle version preinstalled on GitHub runners.
- CI downloads and executes Gradle 8.13 explicitly.
- Replaced the previous bootstrap script with a version-pinned 8.13 bootstrap.
- Preserves Ktor DEX and Supabase Realtime opt-in fixes.
