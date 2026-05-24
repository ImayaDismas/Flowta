# Active Tasks — Flowta

> This file is the cross-session task tracker. Claude reads it at the start of every session via `reorient.md` and updates it before ending each session. Do not summarise guidelines here — just task state.
>
> Reconstructed 2026-05-23 via `resume_project.md`. Scope = Phase 1 MVP.

---

## In Progress

<!-- Claude sets exactly one task here at a time -->

**Reconciliation (2026-05-24): SMS-paste + CSV statement import both built, verified on-device, SMS-paste committed+pushed (`77d9dbd`/`2559b68`). Statement import is NOT yet committed.**

Built this session (Clean Architecture vertical slice, mirrors deni):
- **Pluggable SMS parser engine** (concept non-negotiable): `PaymentSmsParser` interface + per-provider rules (`MpesaSmsParser`, `AirtelMoneySmsParser`, `TkashSmsParser`) bound `@IntoSet`; `PaymentSmsParserEngine` dispatches. M-Pesa rule is solid; Airtel/T-Kash seeded against canonical samples, to refine with real messages.
- **Money unit gotcha handled:** app stores KES in *whole shillings* in `Money.minorUnits` (no cents) — parser rounds "Ksh2,500.00" → 2500 (not 250000), else matching breaks.
- **Persistence:** `ReceivedPaymentEntity` (+ `received_payments` table), DAO with idempotent insert on unique (business_id, provider, reference), **Room v5→v6 migration**, mapper, datasource, 3 enum converters. `matched_transaction_id` is a soft link (indexed, no FK) like deni's wallet_id.
- **Repo + use cases:** `ReconciliationRepository`(Impl); `ParseAndStorePayments`, `SuggestMatch` (exact-amount + closest-time within 48h, excludes already-matched), `ConfirmMatch`, `IgnorePayment`, `RecordSaleFromPayment`, `GetReceivedPayment`, `ObserveReconciliationSummaryForCurrentBusiness`.
- **UI:** Reconciliation hub (summary + 4 input-method tiles + unmatched/matched lists), Paste-SMS, Match-review (confirm / not-a-match / record-as-new-sale w/ wallet pick / dismiss). Entry card on dashboard → `Destination.Reconciliation`. Methods 2–4 (camera/import/inbox) are "coming soon" shells feeding the same future pipeline. en+sw strings added.

Verification status:
- ✅ `assembleDebug` builds; Hilt graph valid; **26 reconciliation unit tests pass** (0 skipped/failed).
- ✅ **FULLY VERIFIED on-device (emulator-5554, 2026-05-24):** installed v6 with `-r` over the existing v5 DB → **v5→v6 migration ran cleanly** (SQLCipher opened, no crash, data intact: Deni 3,300, balances preserved). `received_payments` table confirmed (hub queried it). Pasted an M-Pesa SMS → parsed **KES 1,234** (whole shillings, NOT 123,400) with correct sender/ref and **1:15 PM → 13:15 Nairobi** time → stored as unmatched. Match-review showed the correct **no-suggestion** path (no 1,234 sale existed) → "Record as new sale" into M-Pesa Till → payment → Matched ("1 of 1 matched"). Side effects correct: **revenue 2,020 → 3,254 (+1,234)**, **M-Pesa Till 27,195 → 28,429 (+1,234)**, Deni/expenses unchanged. No app errors in logcat.
- Stitch screens generated (Sapphire Slate): hub, match, paste, camera, import, inbox. 2 duplicate "Match Payments Review" screens (`fea9ed06…`, `a1a538b7…`) need manual deletion in Stitch.

> **Emulator test data CHANGED by verification.** SMS-paste test: added a KES 1,234 SALE in M-Pesa Till + matched received_payment (ref SGR45TXKLP) → revenue 3,254, M-Pesa Till 28,429. CSV-import test: imported `/sdcard/Download/flowta_statement.csv` → 2 unmatched payments stored (PETER KAMAU 500 / STMNTA0001, GRACE ATIENO 750 / STMNTB0002); the withdrawal row was correctly skipped. Reconciliation hub now: **1 matched, 2 unmatched**. KCB 123,899; Cash Drawer 370; Deni owed 3,300 (Mama Achieng 2,500, Juma Test 800). (No UI to delete a received_payment yet; test sale deletable via History.)

> **Prior context (still standing):** Client rename (`1323c2e`) + wallet-linked deni (`0b5f666`) landed and fully verified on-device 2026-05-24 (v4→v5 migration clean; all picker paths; P&L isolation holds).

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

**Pick up next:** **Commit the CSV export work** (built + on-device verified, uncommitted; statement-import already committed in `39d149f`/`cfb214a`). Then remaining options: reconciliation camera OCR (method 2) / SMS inbox scan (method 4, lowest priority) — still shells — or the Phase 1 paywall.

> Uncommitted + unpushed: export feature on top of `cfb214a`. Local branch is 5 commits ahead of `origin/develop` (push pending).

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
- [ ] Fast-follow: integrate credit into the Record-Sale flow (mark a sale fully/partly on credit → auto-create a deni credit entry). Currently deni uses a standalone "record credit / add client" path.
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

---

## How to use this file

- **Start of session**: paste `reorient.md` prompt — Claude reads this file and loads task state.
- **During session**: Claude moves tasks between sections as work progresses.
- **End of session**: Claude updates In Progress, Blocked, and Next Session before closing.
- **New project**: paste `new_project.md` prompt — Claude initialises this file with Phase 1 tasks.
