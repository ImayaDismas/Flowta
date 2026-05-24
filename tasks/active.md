# Active Tasks — Flowta

> This file is the cross-session task tracker. Claude reads it at the start of every session via `reorient.md` and updates it before ending each session. Do not summarise guidelines here — just task state.
>
> Reconstructed 2026-05-23 via `resume_project.md`. Scope = Phase 1 MVP.

---

## In Progress

<!-- Claude sets exactly one task here at a time -->

**Manual transaction picker in MatchReviewScreen (2026-05-25): built, FULLY VERIFIED on-device, committed (`5d7ce34`). Picker opens bottom sheet with all unmatched transactions of the right type; selecting one confirms the match.**

> Prior: **Airtel/T-Kash outbound parsing (2026-05-25): built, all 16 new tests pass, committed + pushed (`6788430`). No on-device verify needed — pure parsing logic covered by unit tests.**

30-day free trial tracked in encrypted DataStore from first post-onboard launch. On expiry, `SplashViewModel` routes to a hard-gate `PaywallScreen` that blocks the full app. User pays M-Pesa Paybill, receives an SMS activation code, enters it in-app; verified on-device via HMAC-SHA256(secret, businessId) — no server required. Activation persists in DataStore across restarts.

- **Domain:** `LicenseState` (Trial/Active/Expired), `LicenseRepository`, `GetLicenseStateUseCase`, `ActivateLicenseUseCase`, `InitLicenseTrialUseCase`.
- **Data:** `LicenseLocalDataSource` (DataStore keys `trial_start_epoch` + `license_activated`), `LicenseCodeValidator` (HMAC-SHA256, 10-char hex code), `LicenseRepositoryImpl`.
- **UI:** `PaywallScreen` (payment instructions card + code entry field + Activate button), `PaywallViewModel` (Idle/Loading/Activated/Error states). en + sw strings.
- **Navigation:** `Destination.Paywall`; `SplashViewModel` calls `initLicenseTrial()` then checks license state — expired → `Paywall`, otherwise → PinUnlock/SetPin as before. After activation → Splash re-check → PinUnlock.
- **Paybill number:** placeholder `000000` in `strings.xml` — update before release.
- **HMAC key:** `flt_mvp_2026_ke` in `LicenseCodeValidator`; generate codes with `python3 -c "import hmac,hashlib; h=hmac.new(b'flt_mvp_2026_ke',<BUSINESS_ID>.upper().encode(),hashlib.sha256).digest(); print(''.join(f'{b:02X}' for b in h[:5]))"`.

Verification status:
- ✅ `assembleDebug` builds; all unit tests pass.
- ✅ **FULLY VERIFIED on-device (emulator-5554, 2026-05-25)** — TRIAL_DAYS=0 forced expiry; paywall screen appeared on launch with all payment-instruction text. Entered code `0CC10C0F90` (generated for businessId `f87b5b3d-cbcd-4182-a2fe-295d10ba811d`) → navigated to PinUnlock. Force-stopped + restarted → routes to PinUnlock (not paywall) confirming persistence. TRIAL_DAYS restored to 30 before final commit.

> **Prior context (still standing):** Reconciliation money-flow direction (`9d19e36`) verified on-device 2026-05-25. Camera OCR (`5eb2dad`/`79dd08b`) verified on-device (gallery path). Credit→Sale (`61e07ca`/`bcf116e`), SMS-paste, CSV import/export committed + verified on-device. Wallet-linked deni (`0b5f666`) + Client rename (`1323c2e`) verified 2026-05-24.

---

## Next Session

<!-- Claude writes the next task to pick up here before closing -->

**Pick up next:** Pre-release polish (set real Paybill number in strings.xml), or refine Airtel/T-Kash regex with real samples when available.

> **Unpushed:** `5d7ce34` (manual picker) is on `develop` but not yet pushed to `origin/develop`.

### Phase 1 remaining — by feature area

**Ledger** — core flows complete (record, history, wallet + transaction detail/edit/delete).

**Reconciliation** (mobile money — M-Pesa / Airtel / T-Kash)
- [x] Design Stitch screens (generated; delete 2 duplicate match screens).
- [x] Pluggable SMS parser engine — one rule per provider (per concept).
- [x] (1) SMS copy-paste parse → match to sales — built end-to-end; **on-device verify pending** (incl. v5→v6 migration).
- [x] (2) Camera OCR scan — built end-to-end (ML Kit on-device OCR + `OcrTextNormalizer` + real `ScanReceiptScreen` with camera/gallery) feeding `ParseAndStorePaymentsUseCase` (source CAMERA_OCR). **Verified on-device (gallery path); committed + pushed (`5eb2dad`).** Camera path shares the same OCR code but is unverifiable on the emulator's synthetic camera.
- [x] (3) Statement import (**CSV**) — built end-to-end + **verified on-device** (pluggable `StatementParser` engine + `MpesaStatementCsvParser`, SAF file pick, `ImportStatementUseCase`, real screen). PDF deferred (M-Pesa PDFs are password-protected; needs a heavy lib).
- [ ] (4) SMS inbox scan — UI shell only; lowest priority (Play Store risk); needs permission flow + inbox read (source SMS_INBOX).
- [x] Money-flow direction (IN/OUT) — outbound messages (sent/paybill/buygoods/withdraw + bank moves) now reconcile as EXPENSE; inbound as SALE. Room v6→v7. M-Pesa only so far. Verified on-device; committed + pushed (`9d19e36`).
- [x] Airtel Money + T-Kash outbound (OUT) parsing — sent/paid/withdrawn patterns; Extracted-dispatch mirror of MpesaSmsParser; 16 new unit tests. Committed + pushed (`6788430`).
- [x] "Match to a different sale/expense" manual picker — in MatchReviewScreen, let user pick from a list instead of only the auto-suggestion. Built + verified on-device; committed (`5d7ce34`).
- [ ] Refine Airtel/T-Kash regex with real samples when available.

**Credit (deni)** — core shipped in `fcc7f2f`; wallet-linked in `0b5f666` (built in Compose, no Stitch).
- [x] Fast-follow: integrate credit into the Record-Sale flow (mark a sale fully/partly on credit → auto-create a deni credit entry). Built + verified on-device 2026-05-24 (`RecordSaleOnCreditUseCase`); committed + pushed (`61e07ca`/`bcf116e`). The standalone "record credit / add client" path still exists alongside it.
- [ ] Optional: surface deni cash-movements as line items in the linked wallet's transaction history (today they move the balance only).

**Export**
- [x] Design — built directly in Compose (no Stitch), per user choice.
- [x] Basic CSV export — transactions → CSV via `ExportTransactionsCsvUseCase` + SAF `CreateDocument`, dashboard entry card. Built + **verified on-device** (saved 5 txns, file well-formed, amounts in whole shillings). Exports transactions only for now; deni/received-payments export is a possible follow-up.

**Security / licensing**
- [x] PIN lock + SQLCipher encryption — done.
- [x] Phase 1 paywall: 30-day trial + M-Pesa Paybill + SMS activation code. On-device verified; committed + pushed (`a25565d`). Paybill number placeholder in strings.xml — update before release.

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
- [x] Reconciliation Camera OCR (method 2) — ML Kit on-device OCR + `OcrTextNormalizer` (digit/letter repair) + real `ScanReceiptScreen` (camera/gallery) → `ParseAndStorePaymentsUseCase` (CAMERA_OCR). Verified on-device (gallery path); committed + pushed (`5eb2dad`).
- [x] Reconciliation money-flow direction — `PaymentDirection` (IN/OUT) through parser/model/UI; outbound (sent/paybill/buygoods/withdraw + bank moves) → EXPENSE, inbound → SALE; Room v6→v7. M-Pesa only. Verified on-device; committed + pushed (`9d19e36`).
- [x] Phase 1 paywall — 30-day trial + M-Pesa Paybill SMS activation code; HMAC on-device verification; hard gate on expiry. Verified on-device; committed + pushed (`a25565d`).
- [x] Trial-remaining banner on Home — `TrialBanner` above tab content; amber >7 days, red ≤7 days; "Activate" CTA to PaywallScreen. Verified on-device (31-day trial showed banner); committed + pushed (`7b68bdb`).

---

## How to use this file

- **Start of session**: paste `reorient.md` prompt — Claude reads this file and loads task state.
- **During session**: Claude moves tasks between sections as work progresses.
- **End of session**: Claude updates In Progress, Blocked, and Next Session before closing.
- **New project**: paste `new_project.md` prompt — Claude initialises this file with Phase 1 tasks.
