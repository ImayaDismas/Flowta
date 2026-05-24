package com.flowgroup.flowta.di.module

import com.flowgroup.flowta.data.ocr.MlKitReceiptTextRecognizer
import com.flowgroup.flowta.domain.reconciliation.ReceiptTextRecognizer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class OcrModule {

    @Binds
    abstract fun bindReceiptTextRecognizer(impl: MlKitReceiptTextRecognizer): ReceiptTextRecognizer
}
