# LivasODV Android 2.4.0 RC1 — audit completo

Questa RC deriva dalla 2.3.8 e consolida i fix già verificati nei log GitHub:
- Gradle 8.13 esatto con AGP 8.11.1 / JDK 17 / API 36.
- Ktor 3.2.3 per evitare il precedente errore D8 su ktor-client-core-jvm 3.2.0.
- Opt-in Supabase Realtime dichiarato.
- Opt-in Material3 dichiarato sia a livello file sia a livello compiler, così nuove schermate non ricadono nello stesso errore.
- Dashboard usa requestedAt (campo realmente presente nel modello e nello schema Supabase) e non createdAt inesistente nel modello.
- import dp presente in LivasApp.
- applyWardrobeTemplate presente in AppRepository e mantiene le consegne già effettuate.
- Modelli principali confrontati con lo schema pubblico Supabase reale.

La CI compila prima l'APK, poi esegue test e lint: in questo modo eventuali errori Kotlin sono visibili subito e non vengono mascherati da controlli secondari.
