package com.flowgroup.flowta.domain.reconciliation

import com.flowgroup.flowta.domain.common.Result

/**
 * Reads text from an image on-device, so the same parse→match pipeline used for pasted SMS can run
 * on a photographed payment message or receipt. The image is identified by its content/file URI as
 * a string, keeping callers free of Android types.
 */
interface ReceiptTextRecognizer {
    suspend fun recognize(imageUri: String): Result<String>
}
