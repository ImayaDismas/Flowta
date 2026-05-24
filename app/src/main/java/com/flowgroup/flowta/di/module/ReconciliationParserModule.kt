package com.flowgroup.flowta.di.module

import com.flowgroup.flowta.domain.reconciliation.PaymentSmsParser
import com.flowgroup.flowta.domain.reconciliation.StatementParser
import com.flowgroup.flowta.domain.reconciliation.parser.AirtelMoneySmsParser
import com.flowgroup.flowta.domain.reconciliation.parser.MpesaSmsParser
import com.flowgroup.flowta.domain.reconciliation.parser.TkashSmsParser
import com.flowgroup.flowta.domain.reconciliation.statement.MpesaStatementCsvParser
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ReconciliationParserModule {

    @Binds
    @IntoSet
    abstract fun bindMpesaParser(impl: MpesaSmsParser): PaymentSmsParser

    @Binds
    @IntoSet
    abstract fun bindAirtelMoneyParser(impl: AirtelMoneySmsParser): PaymentSmsParser

    @Binds
    @IntoSet
    abstract fun bindTkashParser(impl: TkashSmsParser): PaymentSmsParser

    @Binds
    @IntoSet
    abstract fun bindMpesaStatementParser(impl: MpesaStatementCsvParser): StatementParser
}
