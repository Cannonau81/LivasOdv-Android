# LivasODV Android 2.2.0 – iOS UI Parity

- Elenco Soci riallineato alla Build 31 iPhone: ricerca, elenco compatto, badge 118/PC/AIB/SS e riepilogo vestizione.
- Scheda Socio riallineata: profilo centrale, codice, badge, recapiti, tab Dati/Qualifiche/Vestizione/Turni/Servizi.
- Vestizione e Dotazioni: selezione socio, filtro profilo 118/PC/AIB, messaggio informativo, contatori assegnati/consegnati.
- Mezzi: card operative coerenti con la UI iOS.
- Magazzino: riepilogo sotto-scorta e totale articoli, ricerca e categorie.
- Backend Supabase, ruoli, CRUD e Realtime invariati.
- GitHub Actions invariata per compilazione APK Debug.

# Visual parity iPhone – Android 2.1.1

Questa revisione porta la UI Android verso la stessa identità della Build 31 iPhone, senza cambiare database o schema Supabase.

- Accesso pubblico allineato a iPhone: Area Direttivo, Area Soci, Area Cittadini.
- Area Soci instrada automaticamente i ruoli server (socio, magazzino, servizi sociali, OLP, servizio civile).
- Logo e icona recuperati direttamente dagli asset iOS Build 31.
- Tema chiaro iOS-style: sfondo chiaro, card bianche, blu istituzionale, badge 118/PC/AIB.
- Top bar blu uniforme in tutte le schermate Compose.
- Bottom navigation bianca con selezione blu.
- Dashboard Direttivo riallineata al layout Apple a 6 metriche.
- Richieste recenti e scorciatoie Direttivo integrate nella dashboard.
- Chi siamo, Contatti ed Emergenze disponibili dalla schermata iniziale.
- Backend, RLS e sincronizzazione Supabase invariati.

Versione Android: 2.1.1-ios-visual-parity (versionCode 211)
