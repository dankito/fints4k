package net.dankito.banking.mapper

import net.dankito.banking.ui.model.*
import net.dankito.banking.ui.model.responses.AddAccountResponse
import net.dankito.banking.ui.model.responses.BankingClientResponse
import net.dankito.banking.ui.model.responses.GetTransactionsResponse
import net.dankito.banking.ui.model.tan.*
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Sicherheitsfunktion
import net.dankito.fints.model.AccountData
import net.dankito.fints.model.BankData
import net.dankito.fints.model.CustomerData
import net.dankito.fints.response.client.FinTsClientResponse
import net.dankito.fints.response.segments.AccountType
import net.dankito.utils.exception.ExceptionHelper
import java.math.BigDecimal


open class fints4javaModelMapper {


    private val exceptionHelper = ExceptionHelper()


    open fun mapResponse(response: FinTsClientResponse): BankingClientResponse {
        return BankingClientResponse(response.isSuccessful, mapErrorToShowToUser(response), response.exception)
    }

    open fun mapResponse(account: Account, response: net.dankito.fints.response.client.AddAccountResponse): AddAccountResponse {
        val balances = response.balances.mapKeys { findMatchingBankAccount(account, it.key) }.filter { it.key != null } as Map<BankAccount, BigDecimal>

        val bookedTransactions = response.bookedTransactions.associateBy { it.account }
        val mappedBookedTransactions = mutableMapOf<BankAccount, List<AccountTransaction>>()

        bookedTransactions.keys.forEach { accountData ->
            findMatchingBankAccount(account, accountData)?.let { bankAccount ->
                mappedBookedTransactions.put(bankAccount, mapTransactions(bankAccount, response.bookedTransactions))
            }
        }

        return AddAccountResponse(response.isSuccessful, mapErrorToShowToUser(response),
            account, response.supportsRetrievingTransactionsOfLast90DaysWithoutTan,
            mappedBookedTransactions,
            mapOf(), // TODO: map unbooked transactions
            balances,
            response.exception,
            response.userCancelledAction)
    }

    open fun mapResponse(bankAccount: BankAccount, response: net.dankito.fints.response.client.GetTransactionsResponse): GetTransactionsResponse {

        return GetTransactionsResponse(response.isSuccessful, mapErrorToShowToUser(response),
            mapOf(bankAccount to mapTransactions(bankAccount, response.bookedTransactions)),
            mapOf(), // TODO: map unbooked transactions
            response.balance?.let { mapOf(bankAccount to it) } ?: mapOf(),
            response.exception, response.userCancelledAction)
    }

    open fun mapErrorToShowToUser(response: FinTsClientResponse): String? {
        val innerException = response.exception?.let { exception -> exceptionHelper.getInnerException(exception) }

        return innerException?.localizedMessage ?: response.errorsToShowToUser.joinToString("\n")
    }


    open fun mapBank(bank: BankData): Bank {
        return Bank(bank.name, bank.bankCode, bank.bic, bank.finTs3ServerAddress)
    }

    open fun mapAccount(customer: CustomerData, bank: BankData): Account {
        val mappedBank = mapBank(bank)

        val account = Account(mappedBank, customer.customerId, customer.pin, customer.name, customer.userId)

        account.bankAccounts = mapBankAccounts(account, customer.accounts)

        updateTanMediaAndProcedures(account, customer)

        return account
    }

    // TODO: move to a fints4java internal mapper
    open fun updateCustomer(customer: CustomerData, updatedCustomer: CustomerData) {
        customer.pin = updatedCustomer.pin
        customer.name = updatedCustomer.name

        customer.supportedTanProcedures = updatedCustomer.supportedTanProcedures
        customer.selectedTanProcedure = updatedCustomer.selectedTanProcedure
        customer.tanMedia = updatedCustomer.tanMedia

        customer.updVersion = updatedCustomer.updVersion
        customer.selectedLanguage = updatedCustomer.selectedLanguage
        customer.customerSystemId = updatedCustomer.customerSystemId
        customer.customerSystemStatus = updatedCustomer.customerSystemStatus

        updateBankAccounts(customer, updatedCustomer.accounts)
    }


    open fun mapBankAccounts(account: Account, accountData: List<AccountData>): List<BankAccount> {
        return accountData.map { mapBankAccount(account, it) }
    }

    open fun mapBankAccount(account: Account, accountData: AccountData): BankAccount {
        return BankAccount(account, accountData.accountIdentifier, accountData.accountHolderName, accountData.iban,
            accountData.subAccountAttribute, BigDecimal.ZERO, accountData.currency ?: "EUR",
            mapBankAccountType(accountData.accountType), accountData.supportsRetrievingAccountTransactions,
            accountData.supportsRetrievingBalance, accountData.supportsTransferringMoney)
    }

    open fun mapBankAccountType(type: AccountType?): BankAccountType {
        return when (type) {
            AccountType.Girokonto -> BankAccountType.Girokonto
            AccountType.Sparkonto -> BankAccountType.Sparkonto
            AccountType.Festgeldkonto -> BankAccountType.Festgeldkonto
            AccountType.Wertpapierdepot -> BankAccountType.Wertpapierdepot
            AccountType.Darlehenskonto -> BankAccountType.Darlehenskonto
            AccountType.Kreditkartenkonto -> BankAccountType.Kreditkartenkonto
            AccountType.FondsDepot -> BankAccountType.FondsDepot
            AccountType.Bausparvertrag -> BankAccountType.Bausparvertrag
            AccountType.Versicherungsvertrag -> BankAccountType.Versicherungsvertrag
            else -> BankAccountType.Sonstige
        }
    }

    // TODO: move to a fints4java internal mapper
    open fun updateBankAccounts(customer: CustomerData, updatedAccounts: List<AccountData>) {
        val accounts = customer.accounts

        updatedAccounts.forEach { updatedAccount ->
            val matchingExistingAccount = findMatchingBankAccount(accounts, updatedAccount)

            if (matchingExistingAccount == null) {
                customer.addAccount(updatedAccount)
            }
            else {
                updateBankAccount(matchingExistingAccount, updatedAccount)
            }
        }

        customer.accounts.forEach { account ->
            val updatedAccount = findMatchingBankAccount(updatedAccounts, account)

            if (updatedAccount == null) {
                customer.removeAccount(account)
            }
        }
    }

    open fun updateBankAccount(account: AccountData, updatedAccount: AccountData) {
        account.allowedJobs = updatedAccount.allowedJobs

        account.supportsRetrievingAccountTransactions = updatedAccount.supportsRetrievingAccountTransactions
        account.supportsRetrievingBalance = updatedAccount.supportsRetrievingBalance
        account.supportsTransferringMoney = updatedAccount.supportsTransferringMoney
    }

    open fun findAccountForBankAccount(customer: CustomerData, bankAccount: BankAccount): AccountData? {
        return customer.accounts.firstOrNull { bankAccount.identifier == it.accountIdentifier }
    }

    open fun findMatchingBankAccount(account: Account, accountData: AccountData): BankAccount? {
        return account.bankAccounts.firstOrNull { it.identifier == accountData.accountIdentifier }
    }

    open fun findMatchingBankAccount(accounts: List<AccountData>, accountData: AccountData): AccountData? {
        return accounts.firstOrNull { it.accountIdentifier == accountData.accountIdentifier }
    }


    open fun mapTransactions(bankAccount: BankAccount, transactions: List<net.dankito.fints.model.AccountTransaction>): List<AccountTransaction> {
        return transactions.map { mapTransaction(bankAccount, it) }
    }

    open fun mapTransaction(bankAccount: BankAccount, transaction: net.dankito.fints.model.AccountTransaction): AccountTransaction {
        return AccountTransaction(
            transaction.amount,
            transaction.bookingDate,
            transaction.usage,
            transaction.otherPartyName,
            transaction.otherPartyBankCode,
            transaction.otherPartyAccountId,
            transaction.bookingText,
            transaction.closingBalance,
            transaction.currency,
            bankAccount
        )
    }


    open fun updateTanMediaAndProcedures(account: Account, customer: CustomerData) {
        account.supportedTanProcedures = mapTanProcedures(customer.supportedTanProcedures)

        if (customer.isTanProcedureSelected) {
            account.selectedTanProcedure = findMappedTanProcedure(account, customer.selectedTanProcedure)
        }
        else {
            account.selectedTanProcedure = null
        }

        account.tanMedia = mapTanMediums(customer.tanMedia)
    }


    open fun mapTanProcedures(tanProcedures: List<net.dankito.fints.model.TanProcedure>): List<TanProcedure> {
        return tanProcedures.map { mapTanProcedure(it) }
    }

    open fun mapTanProcedure(tanProcedure: net.dankito.fints.model.TanProcedure): TanProcedure {
        return TanProcedure(
            tanProcedure.displayName,
            mapTanProcedureType(tanProcedure.type),
            tanProcedure.securityFunction.code
        )
    }

    open fun mapTanProcedureType(type: net.dankito.fints.model.TanProcedureType): TanProcedureType {
        return when (type) {
            net.dankito.fints.model.TanProcedureType.EnterTan -> TanProcedureType.EnterTan
            net.dankito.fints.model.TanProcedureType.ChipTanManuell -> TanProcedureType.ChipTanManuell
            net.dankito.fints.model.TanProcedureType.ChipTanOptisch -> TanProcedureType.ChipTanOptisch
            net.dankito.fints.model.TanProcedureType.ChipTanQrCode -> TanProcedureType.ChipTanQrCode
            net.dankito.fints.model.TanProcedureType.PhotoTan -> TanProcedureType.PhotoTan
            net.dankito.fints.model.TanProcedureType.SmsTan -> TanProcedureType.SmsTan
            net.dankito.fints.model.TanProcedureType.PushTan -> TanProcedureType.PushTan
        }
    }

    protected open fun findMappedTanProcedure(account: Account, tanProcedure: net.dankito.fints.model.TanProcedure): TanProcedure? {
        return account.supportedTanProcedures.firstOrNull { it.bankInternalProcedureCode == tanProcedure.securityFunction.code }
    }


    open fun mapTanMediums(tanMediums: List<net.dankito.fints.messages.datenelemente.implementierte.tan.TanMedium>): List<TanMedium> {
        return tanMediums.map { mapTanMedium(it) }
    }

    open fun mapTanMedium(tanMedium: net.dankito.fints.messages.datenelemente.implementierte.tan.TanMedium): TanMedium {
        return TanMedium(
            getDisplayNameForTanMedium(tanMedium),
            mapTanMediumStatus(tanMedium)
        )
    }

    open fun mapTanMedium(tanMedium: net.dankito.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium): TanGeneratorTanMedium {
        return TanGeneratorTanMedium(
            getDisplayNameForTanMedium(tanMedium),
            mapTanMediumStatus(tanMedium),
            tanMedium.cardNumber
        )
    }

    protected open fun getDisplayNameForTanMedium(tanMedium: net.dankito.fints.messages.datenelemente.implementierte.tan.TanMedium): String {
        if (tanMedium is net.dankito.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium) {
            var cardNumber = tanMedium.cardNumber
            tanMedium.cardSequenceNumber?.let {
                cardNumber += " (Kartenfolgenummer $it)" // TODO: translate
            }

            tanMedium.mediaName?.let { mediaName ->
                return "$mediaName $cardNumber"
            }

            return "Karte $cardNumber" // TODO: translate
        }

        return tanMedium.mediumClass.name
    }

    open fun mapTanMediumStatus(tanMedium: net.dankito.fints.messages.datenelemente.implementierte.tan.TanMedium): TanMediumStatus {
        return if (tanMedium.status.name.contains("aktiv", true)) TanMediumStatus.Used else TanMediumStatus.Available
    }

    open fun mapTanMedium(tanMedium: TanMedium, customer: CustomerData): net.dankito.fints.messages.datenelemente.implementierte.tan.TanMedium {
        if (tanMedium is TanGeneratorTanMedium) {
            return mapTanMedium(tanMedium, customer)
        }

        val statusToHave = if (tanMedium.status == TanMediumStatus.Used) listOf(net.dankito.fints.messages.datenelemente.implementierte.tan.TanMediumStatus.Aktiv, net.dankito.fints.messages.datenelemente.implementierte.tan.TanMediumStatus.AktivFolgekarte)
        else listOf(net.dankito.fints.messages.datenelemente.implementierte.tan.TanMediumStatus.Verfuegbar, net.dankito.fints.messages.datenelemente.implementierte.tan.TanMediumStatus.VerfuegbarFolgekarte)

        return customer.tanMedia.first { tanMedium.displayName == it.mediumClass.name && statusToHave.contains(it.status) }
    }

    open fun mapTanMedium(tanMedium: TanGeneratorTanMedium, customer: CustomerData): net.dankito.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium {
        return customer.tanMedia.mapNotNull { it as? net.dankito.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium }
            .first { it.cardNumber == tanMedium.cardNumber
                    && (it.cardSequenceNumber == null || tanMedium.displayName.contains(it.cardSequenceNumber!!)) }
    }


    open fun mapTanProcedure(tanProcedure: TanProcedure): net.dankito.fints.model.TanProcedure {
        return net.dankito.fints.model.TanProcedure(
            tanProcedure.displayName,
            Sicherheitsfunktion.values().first { it.code == tanProcedure.bankInternalProcedureCode },
            mapTanProcedureType(tanProcedure.type)
        )
    }

    open fun mapTanProcedureType(type: TanProcedureType): net.dankito.fints.model.TanProcedureType {
        return when (type) {
            TanProcedureType.EnterTan -> net.dankito.fints.model.TanProcedureType.EnterTan
            TanProcedureType.ChipTanManuell -> net.dankito.fints.model.TanProcedureType.ChipTanManuell
            TanProcedureType.ChipTanOptisch -> net.dankito.fints.model.TanProcedureType.ChipTanOptisch
            TanProcedureType.ChipTanQrCode -> net.dankito.fints.model.TanProcedureType.ChipTanQrCode
            TanProcedureType.PhotoTan -> net.dankito.fints.model.TanProcedureType.PhotoTan
            TanProcedureType.SmsTan -> net.dankito.fints.model.TanProcedureType.SmsTan
            TanProcedureType.PushTan -> net.dankito.fints.model.TanProcedureType.PushTan
        }
    }

    open fun mapEnterTanResult(result: EnterTanResult, customer: CustomerData): net.dankito.fints.model.EnterTanResult {
        result.changeTanProcedureTo?.let { changeTanProcedureTo ->
            return net.dankito.fints.model.EnterTanResult.userAsksToChangeTanProcedure(mapTanProcedure(changeTanProcedureTo))
        }

        result.changeTanMediumTo?.let { changeTanMediumTo ->
            val callback: ((FinTsClientResponse) -> Unit)? = if (result.changeTanMediumResultCallback == null) null
            else { response -> result.changeTanMediumResultCallback?.invoke(mapResponse(response)) }
            return net.dankito.fints.model.EnterTanResult.userAsksToChangeTanMedium(mapTanMedium(changeTanMediumTo, customer), callback)
        }

        result.enteredTan?.let { enteredTan ->
            return net.dankito.fints.model.EnterTanResult.userEnteredTan(enteredTan)
        }

        return net.dankito.fints.model.EnterTanResult.userDidNotEnterTan()
    }

    open fun mapEnterTanGeneratorAtcResult(result: EnterTanGeneratorAtcResult): net.dankito.fints.model.EnterTanGeneratorAtcResult {
        if (result.hasAtcBeenEntered) {
            return net.dankito.fints.model.EnterTanGeneratorAtcResult.userEnteredAtc(result.tan!!, result.atc!!)
        }

        return net.dankito.fints.model.EnterTanGeneratorAtcResult.userDidNotEnterTan()
    }

    open fun mapTanChallenge(tanChallenge: net.dankito.fints.model.TanChallenge): TanChallenge {
        if (tanChallenge is net.dankito.fints.model.FlickerCodeTanChallenge) {
            return mapTanChallenge(tanChallenge)
        }

        if (tanChallenge is net.dankito.fints.model.ImageTanChallenge) {
            return mapTanChallenge(tanChallenge)
        }

        return TanChallenge(tanChallenge.messageToShowToUser,
            mapTanProcedure(tanChallenge.tanProcedure)
        )
    }

    open fun mapTanChallenge(tanChallenge: net.dankito.fints.model.FlickerCodeTanChallenge): FlickerCodeTanChallenge {
        return FlickerCodeTanChallenge(mapFlickerCode(tanChallenge.flickerCode), tanChallenge.messageToShowToUser,
            mapTanProcedure(tanChallenge.tanProcedure)
        )
    }

    open fun mapFlickerCode(flickerCode: net.dankito.fints.tan.FlickerCode): FlickerCode {
        return FlickerCode(flickerCode.challengeHHD_UC, flickerCode.parsedDataSet, flickerCode.decodingError)
    }

    open fun mapTanChallenge(tanChallenge: net.dankito.fints.model.ImageTanChallenge): ImageTanChallenge {
        return ImageTanChallenge(mapTanImage(tanChallenge.image), tanChallenge.messageToShowToUser,
            mapTanProcedure(tanChallenge.tanProcedure)
        )
    }

    open fun mapTanImage(image: net.dankito.fints.tan.TanImage): TanImage {
        return TanImage(image.mimeType, image.imageBytes, image.decodingError)
    }

}