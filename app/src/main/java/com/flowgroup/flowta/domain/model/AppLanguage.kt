package com.flowgroup.flowta.domain.model

enum class AppLanguage(val code: String) {
    Swahili("sw"),
    English("en");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: Swahili
    }
}
