package com.flowgroup.flowta.domain.model

enum class DeniEntryType {
    /** Client took goods/service on credit, or borrowed money — increases what they owe. */
    CREDIT,

    /** Client paid back — decreases what they owe. */
    PAYMENT,
}
