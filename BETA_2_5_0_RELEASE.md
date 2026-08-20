# Lì.v.a.s. O.D.V. Android — 2.5.0-beta.2

Candidata alla prima beta ufficiale Android per i soci.

## Controlli eseguiti
- separazione ruoli: le allerte richieste cittadini restano a Direttivo/Servizi Sociali; i soci vedono solo le aree loro consentite;
- autenticazione Servizi Sociali mantenuta su Supabase con ruolo server;
- logout di sicurezza dopo 3 minuti in background;
- backup Android disabilitato a livello manifest; traffico HTTP in chiaro disabilitato;
- preflight progetto e controllo assenza di chiavi `service_role`/`sb_secret_` nel client;
- UI finale riallineata alla Apple Build 31: palette scura, rosso Lì.v.a.s., card arrotondate, top/bottom bar, badge, icone e gerarchia visiva;
- Home accessi resa adattiva a telefoni/tablet (3/4/5 colonne) e logo ridimensionato per evitare compressione su schermi piccoli;
- feedback aptico sui pulsanti accesso rapido.

## Build CI attesa
Artifact APK: `LivasODV-Android-2.5.0-beta2-debug`

La pubblicazione beta va considerata pronta solo dopo run GitHub Actions verde e test installazione/login su dispositivo reale.
