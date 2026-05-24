package com.flowgroup.flowta.data.ocr

import android.content.Context
import androidx.core.net.toUri
import com.flowgroup.flowta.domain.common.AppException
import com.flowgroup.flowta.domain.common.Result
import com.flowgroup.flowta.domain.common.resultOf
import com.flowgroup.flowta.domain.reconciliation.OcrTextNormalizer
import com.flowgroup.flowta.domain.reconciliation.ReceiptTextRecognizer
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MlKitReceiptTextRecognizer @Inject constructor(
    @ApplicationContext private val context: Context,
) : ReceiptTextRecognizer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun recognize(imageUri: String): Result<String> = resultOf {
        val image = InputImage.fromFilePath(context, imageUri.toUri())
        val text = suspendCancellableCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { continuation.resume(it.text) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
        if (text.isBlank()) {
            throw AppException.LocalException("No text found in the image")
        }
        OcrTextNormalizer.normalize(text)
    }
}
