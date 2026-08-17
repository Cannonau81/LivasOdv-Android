# LivasODV Android 2.3.4 RC1 — Supabase Experimental Opt-In Fix

- Corretto `compileDebugKotlin` su `AppRepository.kt`.
- Aggiunto `@OptIn(SupabaseExperimental::class)` al metodo `observeRealtime()`.
- Aggiunto import `io.github.jan.supabase.annotations.SupabaseExperimental`.
- Nessuna modifica alle tabelle Supabase, ai dati o alla logica Realtime.
- Mantiene il precedente Ktor DEX fix.
