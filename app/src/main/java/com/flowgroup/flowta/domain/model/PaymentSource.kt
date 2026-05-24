package com.flowgroup.flowta.domain.model

/** How a received payment entered the app — one per reconciliation input method. */
enum class PaymentSource {
    SMS_PASTE,
    SMS_INBOX,
    CAMERA_OCR,
    STATEMENT_IMPORT,
}
