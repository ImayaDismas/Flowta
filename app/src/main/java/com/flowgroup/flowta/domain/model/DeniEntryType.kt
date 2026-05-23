package com.flowgroup.flowta.domain.model

enum class DeniEntryType {
    /** Customer took goods/service on credit, or borrowed money — increases what they owe. */
    CREDIT,

    /** Customer paid back — decreases what they owe. */
    PAYMENT,
}
