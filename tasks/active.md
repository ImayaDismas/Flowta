# Active Tasks — Flowta

> This file is the cross-session task tracker. Claude reads it at the start of every session via `reorient.md` and updates it before ending each session. Do not summarise guidelines here — just task state.
>
> Reconstructed 2026-05-23 via `resume_project.md`. Scope = Phase 1 MVP.

---

## In Progress

<!-- Claude sets exactly one task here at a time -->

**Credit→Sale integration (2026-05-24): built + FULLY VERIFIED on-device. NOT yet committed.**

Integrated credit (deni) into the Record-Sale flow — a sale can now be marked fully/partly on credit, auto-creating a deni CREDIT entry. Reuses existing primitives: **no schema change, no migration.**
- **Accounting model (the key decision):** record the **full sale** as a SALE transaction (revenue +total, wallet +total) and the unpaid portion as a deni CREDIT **linked to the same wallet** (wallet −credit). The wallet nets to cash actually received (total − credit). This is the only consistent choice because deni payments never hit the P&L — recognising the full sale up front keeps the credit portion's revenue from being lost when the client later pays.
- **Domain:** `RecordSaleOnCreditUseCase` (+ `SaleCreditClient.Existing/New`) — validates `0 < credit ≤ total`, resolves business currency, creates the client if new, then records SALE + deni CREDIT (sequential, non-atomic, mirrors `AddClientUseCase`).
- **UI:** Record-Sale screen gains an "On credit (deni)" switch (SALE only); a client dropdown (existing clients + "+ New client" → inline name/phone fields); a credit-amount field defaulting to the full amount with a derived "Paid now: KES X" hint. ViewModel combines wallets+clients flows and branches `submit()`. en+sw strings added.

Verification status:
- ✅ `assembleDebug` builds; Hilt graph valid; unit tests pass (`RecordSaleOnCreditUseCaseTest` + `RecordTransactionViewModel` credit-path tests); existing VM test updated for the new 4-arg constructor.
- ✅ **FULLY VERIFIED on-device (emulator-5554, 2026-05-24)** — both code paths, no logcat errors:
  1. **Existing client + partial credit** (M-Pesa Till): SALE 1,000 with 600 on credit to Mama Achieng → revenue **3,254→4,254** (+1,000), M-Pesa Till **28,429→28,829** (net **+400** cash), Mama Achieng **2,500→3,100**, total deni **3,300→3,900**. "Paid now: KES 400" hint correct.
  2. **New client + full credit** (Cash Drawer): SALE 800 fully on credit to a brand-new client **"Wanjiku"** → revenue **4,254→5,054** (+800), **Cash Drawer unchanged at 370** (net 0), Wanjiku owes 800, total deni **3,900→4,700**.

> **Emulator test data CHANGED by this verification (2026-05-24).** Added two credit-sales (above). Now: **revenue 5,054**, **M-Pesa Till 28,829**, Cash Drawer 370, KCB 123,899. Deni total **4,700** — Mama Achieng 3,100, Juma Test 800, **Wanjiku 800** (new). Reconciliation hub still 1 matched / 2 unmatched (PETER KAMAU 500 / GRACE ATIENO 750). No UI to delete a deni client or received_payment in v1; test sales deletable via History.

> **Design note (intentional, still standing):** deni cash-movements adjust the selected wallet's *balance* but do NOT appear as line items in that wallet's transaction history (they're not sales/expenses, and are excluded from the P&L). The credit→sale integration relies on exactly this: the deni CREDIT silently nets the wallet down without showing as a transaction. Surfacing these in wallet history is a possible follow-up.

> **Prior context (still standing):** Reconciliation SMS-paste (`77d9dbd`/`2559b68`) + CSV statement import (`39d149f`/`cfb214a`) + CSV export (`5a22569`/`1699ae6`) all committed, pushed, and verified on-device. Wallet-linked deni (`0b5f666`) + Client rename (`1323c2e`) verified 2026-05-24. Reminder notification render still unconfirmed; wallet-detail/insights/transaction-detail flows still pending on-device.

---

## Next Session

<!-- Claude writes the next task to pick up here before closing -->

**Pick up next:** **Commit the credit→Record-Sale integration** (built + on-device verified this session, uncommitted). Suggested split per repo convention: `feat:` for the feature, then `chore:` recording on-device verification. Then remaining options: reconciliation camera OCR (method 2) / SMS inbox scan (method 4, lowest priority) — still shells — or the Phase 1 paywall.

> Uncommitted: credit→Record-Sale integration (RecordSaleOnCreditUseCase + contract/VM/screen/strings/tests) on top of `1699ae6`. `develop` is currently up to date with `origin/develop`; CSV export was already committed+pushed (`5a22569`/`1699ae6`) — the prior note claiming it was uncommitted was stale.

### Phase 1 remaining — by feature area

**Ledger** — core flows complete (record, history, wallet + transaction detail/edit/delete).

**Reconciliation** (mobile money — M-Pesa / Airtel / T-Kash)
- [x] Design Stitch screens (generated; delete 2 duplicate match screens).
- [x] Pluggable SMS parser engine — one rule per provider (per concept).
- [x] (1) SMS copy-paste parse → match to sales — built end-to-end; **on-device verify pending** (incl. v5→v6 migration).
- [ ] (2) Camera OCR scan — UI shell only; needs OCR + feed `ParseAndStorePaymentsUseCase` (source CAMERA_OCR).
- [x] (3) Statement import (**CSV**) — built end-to-end + **verified on-device** (pluggable `StatementParser` engine + `MpesaStatementCsvParser`, SAF file pick, `ImportStatementUseCase`, real screen). PDF deferred (M-Pesa PDFs are password-protected; needs a heavy lib).
- [ ] (4) SMS inbox scan — UI shell only; lowest priority (Play Store risk); needs permission flow + inbox read (source SMS_INBOX).
- [ ] Follow-ups: "Match to a different sale" manual picker; refine Airtel/T-Kash regex with real samples.

**Credit (deni)** — core shipped in `fcc7f2f`; wallet-linked in `0b5f666` (built in Compose, no Stitch).
- [x] Fast-follow: integrate credit into the Record-Sale flow (mark a sale fully/partly on credit → auto-create a deni credit entry). Built + verified on-device 2026-05-24 (`RecordSaleOnCreditUseCase`); **uncommitted**. The standalone "record credit / add client" path still exists alongside it.
- [ ] Optional: surface deni cash-movements as line items in the linked wallet's transaction history (today they move the balance only).

**Export**
- [x] Design — built directly in Compose (no Stitch), per user choice.
- [x] Basic CSV export — transactions → CSV via `ExportTransactionsCsvUseCase` + SAF `CreateDocument`, dashboard entry card. Built + **verified on-device** (saved 5 txns, file well-formed, amounts in whole shillings). Exports transactions only for now; deni/received-payments export is a possible follow-up.

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
- [x] Credit→Sale — record a sale fully/partly on credit from Record-Sale; full sale = revenue, unpaid portion = deni CREDIT linked to the wallet (wallet nets to cash received). Built + verified on-device; **uncommitted**.

---

## How to use this file

- **Start of session**: paste `reorient.md` prompt — Claude reads this file and loads task state.
- **During session**: Claude moves tasks between sections as work progresses.
- **End of session**: Claude updates In Progress, Blocked, and Next Session before closing.
- **New project**: paste `new_project.md` prompt — Claude initialises this file with Phase 1 tasks.
