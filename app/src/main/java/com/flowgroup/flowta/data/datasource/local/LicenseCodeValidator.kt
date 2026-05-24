package com.flowgroup.flowta.data.datasource.local

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

internal object LicenseCodeValidator {
    // Obfuscated by R8 in release builds; split across two parts to hinder grep
    private val S = "flt_mvp_" + "2026_ke"

    fun generate(businessId: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(S.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        val hash = mac.doFinal(businessId.trim().uppercase().toByteArray(Charsets.UTF_8))
        return hash.take(5).joinToString("") { "%02X".format(it) }
    }

    fun verify(businessId: String, code: String): Boolean =
        generate(businessId).equals(code.trim(), ignoreCase = true)
}
