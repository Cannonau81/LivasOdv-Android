package it.livasodv.app.data
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import it.livasodv.app.BuildConfig
object SupabaseProvider { val client = createSupabaseClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_PUBLISHABLE_KEY) { install(Auth); install(Postgrest); install(Realtime) } }
