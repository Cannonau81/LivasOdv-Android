-- Shared communications schema for iOS/Android parity. Applied to production on 2026-08-20.
alter table public.communications add column if not exists target_roles text[] not null default array['admin','direttivo','socio','magazzino','olp','servizio_civile','servizi_sociali']::text[];

create table if not exists public.communication_reads (
  communication_id uuid not null references public.communications(id) on delete cascade,
  user_id uuid not null references auth.users(id) on delete cascade,
  read_at timestamptz not null default now(),
  primary key (communication_id, user_id)
);
