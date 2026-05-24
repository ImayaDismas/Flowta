# Active Tasks — Flowta

> This file is the cross-session task tracker. Claude reads it at the start of every session via `reorient.md` and updates it before ending each session. Do not summarise guidelines here — just task state.
>
> Reconstructed 2026-05-23 via `resume_project.md`. Scope = Phase 1 MVP.

---

## In Progress

<!-- Claude sets exactly one task here at a time -->

_None — Client rename (`1323c2e`) + wallet-linked deni (`0b5f666`) landed (2026-05-24). Pick next from Next Session._

> Verification status:
> - **Wallet-linked deni FULLY VERIFIED on-device (emulator API 36, 2026-05-24):** Room **v4→v5 migration** ran cleanly on the prior encrypted DB (no crash, data intact). All three picker paths confirmed, each leaving revenue/expenses insights **unchanged at 2,020 / 10,000** (P&L isolation holds):
>   1. **Payment + wallet** (M-Pesa): owed 3,000→2,000, M-Pesa 26,995→27,995 (cash in, label "Add to account").
>   2. **Credit + wallet** (Cash Drawer): owed 2,000→2,500, Cash Drawer 870→370 (cash out, label "Deduct from account").
>   3. **Add-client + wallet** (M-Pesa, initial credit 800): new client "Juma Test"; total owed 2,500→3,300, M-Pesa 27,995→27,195. Picker correctly appears only once an amount is entered.
>   Pickers show live balances; "Add client"/"Client name" rename live in UI. The cash ledger and receivable ledger stay independent as designed.
> - **Emulator test data now:** Mama Achieng owes KES 2,500, Juma Test owes KES 800 (total 3,300); Cash Drawer = KES 370, M-Pesa Till = KES 27,195. No delete-client flow in v1.
> - **Design note (intentional):** deni cash-movements adjust the selected wallet's *balance* but do NOT appear as line items in that wallet's transaction history (they're not sales/expenses, and excluded from the P&L). Surfacing them in wallet history is a possible follow-up.
> - **From 2026-05-23 (still standing):** reminder notification render still unconfirmed; wallet-detail/insights/transaction-detail flows still pending on-device.

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

**Credit (deni)** — core shipped in `fcc7f2f`; wallet-linked in `0b5f666` (built in Compose, no Stitch).
- [ ] Fast-follow: integrate credit into the Record-Sale flow (mark a sale fully/partly on credit → auto-create a deni credit entry). Currently deni uses a standalone "record credit / add client" path.
- [ ] Optional: surface deni cash-movements as line items in the linked wallet's transaction history (today they move the balance only).

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
- [x] Naming — renamed Customer → Client across the deni feature (code/UI); physical SQLite table kept, no migration (`1323c2e`).
- [x] Credit (deni) — optional business-wallet link on credit/payment + add-client; wallet balance reflects cash moved; Room v4→v5 (`0b5f666`).

---

## How to use this file

- **Start of session**: paste `reorient.md` prompt — Claude reads this file and loads task state.
- **During session**: Claude moves tasks between sections as work progresses.
- **End of session**: Claude updates In Progress, Blocked, and Next Session before closing.
- **New project**: paste `new_project.md` prompt — Claude initialises this file with Phase 1 tasks.
