package net.dankito.banking.ui.util


interface ICurrencyInfoProvider {

    val userDefaultCurrencyInfo: CurrencyInfo

    val currencyInfos: List<CurrencyInfo>

    fun getInfoForIsoCode(isoCode: String): CurrencyInfo?


    fun getCurrencySymbolForIsoCode(isoCode: String): String?

    fun getCurrencySymbolForIsoCodeOr(isoCode: String, defaultValue: String): String

    fun getCurrencySymbolForIsoCodeOrEuro(isoCode: String): String

}