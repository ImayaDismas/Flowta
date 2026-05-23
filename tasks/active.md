# Active Tasks — Flowta

> This file is the cross-session task tracker. Claude reads it at the start of every session via `reorient.md` and updates it before ending each session. Do not summarise guidelines here — just task state.
>
> Reconstructed 2026-05-23 via `resume_project.md`. Scope = Phase 1 MVP.

---

## In Progress

<!-- Claude sets exactly one task here at a time -->

_None — Customer credit (deni) slice landed in `fcc7f2f` (2026-05-23). Pick next from Next Session._

> Open follow-up: interactive in-app verification still pending for the wallet-detail, insights, transaction-detail, and **deni** flows — no device/emulator/AVD has been available. Verify on a device before treating any of these as user-tested. Deni reminders (WorkManager notification) especially need on-device confirmation.

---

## Next Session

<!-- Claude writes the next task to pick up here before closing -->

**Pick up next:** open choice across the remaining areas below. Reconciliation is the remaining core differentiator (needs Stitch screens designed first). Smaller options: CSV export, or the Phase 1 paywall.

### Phase 1 remaining — by feature area

**Ledger** — core flows complete (record, history, wallet + transaction detail/edit/delete).

**Reconciliation** (mobile money — M-Pesa / Airtel / T-Kash)
- [ ] Design Stitch screens (MISSING).
- [ ] Pluggable SMS parser engine — one rule per provider (per concept).
- [ ] (1) SMS copy-paste parse → match to sales.
- [ ] (2) Camera OCR scan.
- [ ] (3) Statement import (PDF / CSV).
- [ ] (4) SMS inbox scan — lowest priority (Play Store risk).

**Credit (deni)** — core shipped in `fcc7f2f` (built in Compose, no Stitch).
- [ ] Fast-follow: integrate credit into the Record-Sale flow (mark a sale fully/partly on credit → auto-create a deni credit entry). Currently deni uses a standalone "record credit / add customer" path.

**Export**
- [ ] Design Stitch screen (MISSING).
- [ ] Basic CSV export (free — "your data is always yours").

**Security / licensing**
- [x] PIN lock + SQLCipher encryption — done.
- [ ] Phase 1 paywall: license state stored locally + encrypted, M-Pesa Paybill + SMS activation code (no full account).

**Cross-cutting guardrails (not tasks — verify as you go)**
- Multi-currency: every monetary record carries ISO 4217 `CurrencyCode` (KES launch).
- `business_id` FK on all records; `role` (OWNER/CASHIER) on profile from Phase 1.
- Offline-first: Room is the single source of truth; no login gate on offline features.

---

## Deferred — later phase

<!-- Recorded but intentionally not Phase 1 work-now; revisit in its actual phase -->

- [ ] WhatsApp receipt sharing (standard share intent) — deferred to its later phase. The Transaction Detail Share button (`f991449`) stays an inert placeholder until then.

---

## Blocked

<!-- Note blocker, what it depends on, and when it can unblock -->

_None._

> Note: Reconciliation, Credit (deni), and Export feature areas have **no Stitch screens yet** — design them before/at the start of those slices. Not a hard blocker for backend/domain work.

---

## Completed

<!-- Move tasks here when done. Format: - [x] Task name — brief outcome note -->

- [x] Project foundation — DI, Room + SQLCipher, theme, navigation, domain primitives.
- [x] Onboarding flow — Get Started → Add Business → Set PIN → Setup Complete.
- [x] Security — PIN unlock on launch.
- [x] Home shell — Wallets / History / Insights tabs.
- [x] Ledger — Add-wallet flow (Room v2 schema) + wallets list.
- [x] Ledger — Record-transaction flow + live history + computed wallet balances.
- [x] Wallets — Wallet Detail + Edit + transaction-aware Delete (`616c4f8`).
- [x] Summary — Insights dashboard: this-week revenue/expenses vs prior week, en+sw (`616c4f8`).
- [x] Ledger — Transaction Detail (receipt) + Edit + Delete; Share placeholder (`f991449`).
- [x] Credit (deni) — customer accounts, credit/partial-payment ledger, dashboard owed-card, WorkManager reminders, Room v4 (`fcc7f2f`).

---

## How to use this file

- **Start of session**: paste `reorient.md` prompt — Claude reads this file and loads task state.
- **During session**: Claude moves tasks between sections as work progresses.
- **End of session**: Claude updates In Progress, Blocked, and Next Session before closing.
- **New project**: paste `new_project.md` prompt — Claude initialises this file with Phase 1 tasks.
