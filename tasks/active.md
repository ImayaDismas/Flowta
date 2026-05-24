# Active Tasks — Flowta

> This file is the cross-session task tracker. Claude reads it at the start of every session via `reorient.md` and updates it before ending each session. Do not summarise guidelines here — just task state.
>
> Reconstructed 2026-05-23 via `resume_project.md`. Scope = Phase 1 MVP.

---

## In Progress

<!-- Claude sets exactly one task here at a time -->

**Reconciliation money-flow direction (2026-05-25): built, FULLY VERIFIED on-device, committed (`9d19e36`) — not yet pushed.**

Mobile-money messages now carry a **direction (IN/OUT)** end to end, so money *leaving* a wallet reconciles as an EXPENSE instead of being silently dropped. Per user decision: **bank moves are recorded like the rest** (received-from-bank = income/SALE; sent/transferred-to-bank = EXPENSE) — note this counts own-money transfers in P&L.
- **Model + schema:** new `PaymentDirection { IN, OUT }` on `ParsedPayment` + `ReceivedPayment`; `PaymentDirectionConverter`; entity gains a `direction` column (`defaultValue = "IN"`). **Room v6→v7** migration `ALTER TABLE received_payments ADD COLUMN direction TEXT NOT NULL DEFAULT 'IN'` — existing received rows default to IN. `ReceivedPaymentDao` projections updated to include `direction`.
- **Parser (`MpesaSmsParser`):** rewritten with an `Extracted` dispatch — recognises `received` (IN) + `sent to`/`transferred to`/`paid to` (till)/`Withdraw` (OUT), extracting the counterparty (recipient for OUT). Phone optional (bank/paybill/buygoods). `canParse` broadened.
- **CSV import (`MpesaStatementCsvParser`):** `Withdrawn` rows now parse as OUT (was dropped); `Paid In` stays IN. Added a `to <name>` recipient regex for OUT rows.
- **Matching/record:** `SuggestMatchUseCase` pairs IN→SALE, OUT→EXPENSE. `RecordSaleFromPaymentUseCase` → **`RecordTransactionFromPaymentUseCase`** (records SALE for IN, EXPENSE for OUT). `MatchReviewViewModel.onRecordSale` → `onRecordTransaction`.
- **UI:** hub `PaymentRow` shows a CallReceived/CallMade arrow + signed/coloured amount (`+KES` green / `-KES` red, provider prefix in the subtitle). Match review shows "Money in/out" and a "Record as new sale/expense" / "Not a sale|expense — dismiss" action by direction. en+sw strings added.

Verification status:
- ✅ `assembleDebug` builds; Hilt graph valid; **all 135 unit tests pass** (Mpesa parser outbound cases, CSV withdrawn→OUT, SuggestMatch OUT→expense, new `RecordTransactionFromPaymentUseCaseTest`, ImportStatement inflow+outflow).
- ✅ **FULLY VERIFIED on-device (emulator-5554, 2026-05-25)** — v6→v7 migration ran clean (no crash); the 4 pre-existing received payments correctly show as `+KES` (IN). Pasted a `sent to` SMS → parsed **PETER SUPPLIER · ref QWE12RTY34 · -KES 800 · 25/5 09:00** (OUT, red). Opened it → "M-Pesa • Money out" + "Record as new expense" → recorded to M-Pesa Till. Hub **1→2 of 5 matched**; dashboard **EXPENSES 0 → KES 800**; **M-Pesa Till 28,829 → 28,029** (−800). Correct end to end.

> **Emulator test data CHANGED (2026-05-25).** Reconciliation hub now **2 matched / 3 unmatched**: unmatched JOHN OCR 1,250 / GRACE ATIENO 750 / PETER KAMAU 500; matched JANE DOE 1,234 (+) and **PETER SUPPLIER 800 (−, OUT, new)**. New EXPENSE of KES 800 on M-Pesa Till → **M-Pesa Till now 28,029** (was 28,829); Cash Drawer 370, KCB 123,899, deni 4,700 unchanged. This-week EXPENSES = 800, REVENUE = 0. No UI to delete a received_payment in v1.

> **Prior context (still standing):** Camera OCR (`5eb2dad`/`79dd08b`) verified on-device (gallery path). Credit→Sale (`61e07ca`/`bcf116e`), SMS-paste (`77d9dbd`/`2559b68`), CSV statement import (`39d149f`/`cfb214a`), CSV export (`5a22569`/`1699ae6`) committed + verified on-device. Wallet-linked deni (`0b5f666`) + Client rename (`1323c2e`) verified 2026-05-24. Reminder notification render still unconfirmed; wallet-detail/insights/transaction-detail flows still pending on-device.

---

## Next Session

<!-- Claude writes the next task to pick up here before closing -->

**Pick up next:** Phase 1 paywall (license state stored locally + encrypted, M-Pesa Paybill + SMS activation code), or reconciliation SMS inbox scan (method 4 — lowest priority, Play Store policy risk, still a shell). Reconciliation methods 1/2/3 are done; only method 4 remains.

> **Unpushed:** Camera OCR (`5eb2dad`/`79dd08b`) + money-flow direction (feat `9d19e36` + the `chore:` tracking this active.md) sit on local `develop`, **ahead of `origin/develop` (`bcf116e`) — not yet pushed.** Push when ready. Optional reconciliation follow-ups: extend outbound parsing to Airtel/T-Kash (currently inbound-only); "Match to a different sale/expense" manual picker; refine Airtel/T-Kash regex with real samples.

### Phase 1 remaining — by feature area

**Ledger** — core flows complete (record, history, wallet + transaction detail/edit/delete).

**Reconciliation** (mobile money — M-Pesa / Airtel / T-Kash)
- [x] Design Stitch screens (generated; delete 2 duplicate match screens).
- [x] Pluggable SMS parser engine — one rule per provider (per concept).
- [x] (1) SMS copy-paste parse → match to sales — built end-to-end; **on-device verify pending** (incl. v5→v6 migration).
- [x] (2) Camera OCR scan — built end-to-end (ML Kit on-device OCR + `OcrTextNormalizer` + real `ScanReceiptScreen` with camera/gallery) feeding `ParseAndStorePaymentsUseCase` (source CAMERA_OCR). **Verified on-device (gallery path); committed (`5eb2dad`, unpushed).** Camera path shares the same OCR code but is unverifiable on the emulator's synthetic camera.
- [x] (3) Statement import (**CSV**) — built end-to-end + **verified on-device** (pluggable `StatementParser` engine + `MpesaStatementCsvParser`, SAF file pick, `ImportStatementUseCase`, real screen). PDF deferred (M-Pesa PDFs are password-protected; needs a heavy lib).
- [ ] (4) SMS inbox scan — UI shell only; lowest priority (Play Store risk); needs permission flow + inbox read (source SMS_INBOX).
- [x] Money-flow direction (IN/OUT) — outbound messages (sent/paybill/buygoods/withdraw + bank moves) now reconcile as EXPENSE; inbound as SALE. Room v6→v7. M-Pesa only so far. Verified on-device; committed (`9d19e36`, unpushed).
- [ ] Follow-ups: extend outbound parsing to Airtel/T-Kash (inbound-only today); "Match to a different sale/expense" manual picker; refine Airtel/T-Kash regex with real samples.

**Credit (deni)** — core shipped in `fcc7f2f`; wallet-linked in `0b5f666` (built in Compose, no Stitch).
- [x] Fast-follow: integrate credit into the Record-Sale flow (mark a sale fully/partly on credit → auto-create a deni credit entry). Built + verified on-device 2026-05-24 (`RecordSaleOnCreditUseCase`); committed + pushed (`61e07ca`/`bcf116e`). The standalone "record credit / add client" path still exists alongside it.
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
- [x] Credit→Sale — record a sale fully/partly on credit from Record-Sale; full sale = revenue, unpaid portion = deni CREDIT linked to the wallet (wallet nets to cash received). Built + verified on-device; committed + pushed (`61e07ca`/`bcf116e`).
- [x] Reconciliation Camera OCR (method 2) — ML Kit on-device OCR + `OcrTextNormalizer` (digit/letter repair) + real `ScanReceiptScreen` (camera/gallery) → `ParseAndStorePaymentsUseCase` (CAMERA_OCR). Verified on-device (gallery path); committed (`5eb2dad`, unpushed).
- [x] Reconciliation money-flow direction — `PaymentDirection` (IN/OUT) through parser/model/UI; outbound (sent/paybill/buygoods/withdraw + bank moves) → EXPENSE, inbound → SALE; Room v6→v7. M-Pesa only. Verified on-device; committed (`9d19e36`, unpushed).

---

## How to use this file

- **Start of session**: paste `reorient.md` prompt — Claude reads this file and loads task state.
- **During session**: Claude moves tasks between sections as work progresses.
- **End of session**: Claude updates In Progress, Blocked, and Next Session before closing.
- **New project**: paste `new_project.md` prompt — Claude initialises this file with Phase 1 tasks.
