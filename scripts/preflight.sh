#!/usr/bin/env bash
set -euo pipefail
fail(){ echo "PREFLIGHT ERROR: $*" >&2; exit 1; }
test -f app/src/main/java/it/livasodv/app/data/AppRepository.kt || fail "AppRepository missing"
test -f app/src/main/java/it/livasodv/app/feature/DashboardScreen.kt || fail "DashboardScreen missing"
grep -q 'val recentRequests = requests.sortedByDescending { it.requestedAt' app/src/main/java/it/livasodv/app/feature/DashboardScreen.kt || fail "Dashboard timestamp regression"
grep -q 'import androidx.compose.ui.unit.dp' app/src/main/java/it/livasodv/app/feature/LivasApp.kt || fail "dp import missing"
grep -q 'suspend fun applyWardrobeTemplate' app/src/main/java/it/livasodv/app/data/AppRepository.kt || fail "wardrobe function missing"
grep -q '@file:OptIn(io.github.jan.supabase.annotations.SupabaseExperimental::class)' app/src/main/java/it/livasodv/app/data/AppRepository.kt || fail "Supabase opt-in missing"
for f in app/src/main/java/it/livasodv/app/feature/*.kt; do
  if grep -q 'androidx.compose.material3' "$f"; then
    head -n 1 "$f" | grep -q '@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)' || fail "Material3 opt-in missing: $f"
  fi
done
echo 'PREFLIGHT OK'
