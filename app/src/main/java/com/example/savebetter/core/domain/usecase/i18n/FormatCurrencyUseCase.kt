package com.example.savebetter.core.domain.usecase.i18n

import com.example.savebetter.core.i18n.AppLanguage
import com.example.savebetter.core.i18n.CurrencyFormatter
import javax.inject.Inject

/**
 * Use case to format raw monetary numeric values into the localized BDT (৳) currency representation.
 */
class FormatCurrencyUseCase @Inject constructor() {

    operator fun invoke(
        amount: Double,
        language: AppLanguage = AppLanguage.ENGLISH,
        includeDecimals: Boolean = true,
        showSign: Boolean = false
    ): String {
        return if (showSign) {
            CurrencyFormatter.formatSignedAmount(amount, language, includeDecimals)
        } else {
            CurrencyFormatter.formatAmount(amount, language, includeDecimals)
        }
    }
}
