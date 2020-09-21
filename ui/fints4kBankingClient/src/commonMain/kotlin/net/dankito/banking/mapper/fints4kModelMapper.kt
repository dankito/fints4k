package net.dankito.banking.mapper

import net.dankito.banking.extensions.toBigDecimal
import net.dankito.banking.ui.model.*
import net.dankito.banking.ui.model.responses.AddAccountResponse
import net.dankito.banking.ui.model.responses.BankingClientResponse
import net.dankito.banking.ui.model.responses.GetTransactionsResponse
import net.dankito.banking.ui.model.tan.*
import net.dankito.banking.fints.messages.datenelemente.implementierte.signatur.Sicherheitsfunktion
import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanMediumKlasse
import net.dankito.banking.fints.model.AccountData
import net.dankito.banking.fints.model.AccountFeature
import net.dankito.banking.fints.model.BankData
import net.dankito.banking.fints.response.client.FinTsClientResponse
import net.dankito.banking.fints.response.segments.AccountType
import net.dankito.banking.ui.model.mapper.IModelCreator
import net.dankito.utils.multiplatform.log.LoggerFactory


open class fints4kModelMapper(protected val modelCreator: IModelCreator) {

    companion object {
        private val log = LoggerFactory.getLogger(fints4kModelMapper::class)
    }


    open fun mapResponse(response: FinTsClientResponse): BankingClientResponse {
        return BankingClientResponse(response.successful, mapErrorToShowToUser(response), response.userCancelledAction)
    }

    open fun mapResponse(customer: TypedCustomer, response: net.dankito.banking.fints.response.client.AddAccountResponse): AddAccountResponse {

        return AddAccountResponse(customer, map(customer, response.retrievedData), mapErrorToShowToUser(response), response.userCancelledAction)
    }

    open fun mapResponse(bankAccount: TypedBankAccount, response: net.dankito.banking.fints.response.client.GetTransactionsResponse): GetTransactionsResponse {

        return GetTransactionsResponse(map(bankAccount.customer as TypedCustomer, response.retrievedData),
            mapErrorToShowToUser(response), response.userCancelledAction, response.tanRequiredButWeWereToldToAbortIfSo)
    }

    open fun map(customer: TypedCustomer, retrievedData: List<net.dankito.banking.fints.model.RetrievedAccountData>): List<RetrievedAccountData> {
        return retrievedData.mapNotNull { map(customer, it) }
    }

    open fun map(customer: TypedCustomer, retrievedData: net.dankito.banking.fints.model.RetrievedAccountData): RetrievedAccountData? {
        val account = findMatchingBankAccount(customer, retrievedData.accountData)

        if (account == null) {
            log.error("No matching account found for ${retrievedData.accountData}. Has there an account been added we didn't map yet?")
            return null
        }

        return RetrievedAccountData(
            account,
            retrievedData.successfullyRetrievedData,
            retrievedData.balance?.toBigDecimal(),
            mapTransactions(account, retrievedData.bookedTransactions),
            listOf() // TODO: map unbooked transactions
        )
    }

    open fun mapErrorToShowToUser(response: FinTsClientResponse): String? {
        val errorMessage = response.errorMessage

        return errorMessage ?:
        if (response.errorsToShowToUser.isEmpty()) null else response.errorsToShowToUser.joinToString("\n") // TODO: find a better way to choose which of these error messages to show
    }


    open fun mapBank(customer: TypedCustomer, bank: BankData) {
        customer.bankCode = bank.bankCode
        customer.customerId = bank.customerId
        customer.password = bank.pin
        customer.finTsServerAddress = bank.finTs3ServerAddress
        customer.bankName = bank.bankName
        customer.bic = bank.bic
        customer.customerName = bank.customerName
        customer.countDaysForWhichTransactionsAreKept = bank.countDaysForWhichTransactionsAreKept
        customer.userId = bank.userId

        customer.accounts = mapBankAccounts(customer, bank.accounts)

        updateTanMediaAndMethods(customer, bank)
    }

    /**
     * In UI only customerId, password, (bankCode,) and selected TAN method can be set
     */
    open fun mapChangesFromUiToClientModel(customer: TypedCustomer, bank: BankData) {
        bank.customerId = customer.customerId
        bank.pin = customer.password

        bank.bankCode = customer.bankCode

        bank.selectedTanMethod = findTanMethod(bank, customer.selectedTanMethod) ?: bank.selectedTanMethod
    }


    open fun mapBankAccounts(customer: TypedCustomer, accountData: List<AccountData>): List<TypedBankAccount> {
        return accountData.mapIndexed { index, account ->
            val mappedAccount = customer.accounts.firstOrNull { it.identifier == account.accountIdentifier }
                ?: modelCreator.createBankAccount(customer, account.productName, account.accountIdentifier)

            mapBankAccount(mappedAccount, account)

            mappedAccount.displayIndex = index

            mappedAccount
        }
    }

    open fun mapBankAccount(account: TypedBankAccount, accountData: AccountData) {
        account.identifier = accountData.accountIdentifier
        account.accountHolderName = accountData.accountHolderName
        account.iban = accountData.iban
        account.subAccountNumber = accountData.subAccountAttribute
        account.customerId = accountData.customerId

        account.currency = accountData.currency ?: "EUR"
        account.type = mapBankAccountType(accountData.accountType)
        account.isAccountTypeSupported = accountData.isAccountTypeSupported
        account.productName = accountData.productName
        account.accountLimit = accountData.accountLimit

        account.supportsRetrievingBalance = accountData.supportsFeature(AccountFeature.RetrieveBalance)
        account.supportsRetrievingAccountTransactions = accountData.supportsFeature(AccountFeature.RetrieveAccountTransactions)
        account.supportsTransferringMoney = accountData.supportsFeature(AccountFeature.TransferMoney)
        account.supportsInstantPaymentMoneyTransfer = accountData.supportsFeature(AccountFeature.InstantPayment)
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

    // TODO: move to a fints4k internal mapper
    open fun updateBankAccounts(bank: BankData, updatedAccounts: List<AccountData>) {
        val accounts = bank.accounts

        updatedAccounts.forEach { updatedAccount ->
            val matchingExistingAccount = findMatchingBankAccount(accounts, updatedAccount)

            if (matchingExistingAccount == null) {
                bank.addAccount(updatedAccount)
            }
            else {
                updateBankAccount(matchingExistingAccount, updatedAccount)
            }
        }

        bank.accounts.forEach { account ->
            val updatedAccount = findMatchingBankAccount(updatedAccounts, account)

            if (updatedAccount == null) {
                bank.removeAccount(account)
            }
        }
    }

    open fun updateBankAccount(account: AccountData, updatedAccount: AccountData) {
        account.allowedJobs = updatedAccount.allowedJobs

        AccountFeature.values().forEach { feature ->
            account.setSupportsFeature(feature, updatedAccount.supportsFeature(feature))
        }
    }

    open fun findAccountForBankAccount(bank: BankData, bankAccount: TypedBankAccount): AccountData? {
        return bank.accounts.firstOrNull { bankAccount.identifier == it.accountIdentifier }
    }

    open fun findMatchingBankAccount(customer: TypedCustomer, accountData: AccountData): TypedBankAccount? {
        return customer.accounts.firstOrNull { it.identifier == accountData.accountIdentifier }
    }

    open fun findMatchingBankAccount(accounts: List<AccountData>, accountData: AccountData): AccountData? {
        return accounts.firstOrNull { it.accountIdentifier == accountData.accountIdentifier }
    }


    open fun mapTransactions(bankAccount: TypedBankAccount, transactions: Collection<net.dankito.banking.fints.model.AccountTransaction>): List<IAccountTransaction> {
        return transactions.map { mapTransaction(bankAccount, it) }
    }

    open fun mapTransaction(bankAccount: TypedBankAccount, transaction: net.dankito.banking.fints.model.AccountTransaction): IAccountTransaction {
        return modelCreator.createTransaction(
            bankAccount,
            transaction.amount.toBigDecimal(),
            transaction.amount.currency.code,
            transaction.unparsedUsage,
            transaction.bookingDate,
            transaction.otherPartyName,
            transaction.otherPartyBankCode,
            transaction.otherPartyAccountId,
            transaction.bookingText,
            transaction.valueDate,
            transaction.statementNumber,
            transaction.sequenceNumber,
            transaction.openingBalance?.toBigDecimal(),
            transaction.closingBalance?.toBigDecimal(),

            transaction.endToEndReference,
            transaction.customerReference,
            transaction.mandateReference,
            transaction.creditorIdentifier,
            transaction.originatorsIdentificationCode,
            transaction.compensationAmount,
            transaction.originalAmount,
            transaction.sepaUsage,
            transaction.deviantOriginator,
            transaction.deviantRecipient,
            transaction.usageWithNoSpecialType,
            transaction.primaNotaNumber,
            transaction.textKeySupplement,

            transaction.currencyType,
            transaction.bookingKey,
            transaction.referenceForTheAccountOwner,
            transaction.referenceOfTheAccountServicingInstitution,
            transaction.supplementaryDetails,

            transaction.transactionReferenceNumber,
            transaction.relatedReferenceNumber
        )
    }


    open fun updateTanMediaAndMethods(account: TypedCustomer, bank: BankData) {
        account.supportedTanMethods = bank.tanMethodsAvailableForUser.map { tanMethod ->
            findMappedTanMethod(account, tanMethod) ?: mapTanMethod(tanMethod)
        }

        if (bank.isTanMethodSelected) {
            account.selectedTanMethod = findMappedTanMethod(account, bank.selectedTanMethod)
        }
        else {
            account.selectedTanMethod = null
        }

        account.tanMedia = bank.tanMedia.map { tanMedium ->
            findMappedTanMedium(account, tanMedium) ?: mapTanMedium(tanMedium)
        }
    }


    open fun mapTanMethods(tanMethods: List<net.dankito.banking.fints.model.TanMethod>): List<TanMethod> {
        return tanMethods.map { mapTanMethod(it) }
    }

    open fun mapTanMethod(tanMethod: net.dankito.banking.fints.model.TanMethod): TanMethod {
        return modelCreator.createTanMethod(
            tanMethod.displayName,
            mapTanMethodType(tanMethod.type),
            tanMethod.securityFunction.code,
            tanMethod.maxTanInputLength,
            mapAllowedTanFormat(tanMethod.allowedTanFormat)
        )
    }

    open fun mapTanMethodType(type: net.dankito.banking.fints.model.TanMethodType): TanMethodType {
        return when (type) {
            net.dankito.banking.fints.model.TanMethodType.EnterTan -> TanMethodType.EnterTan
            net.dankito.banking.fints.model.TanMethodType.ChipTanManuell -> TanMethodType.ChipTanManuell
            net.dankito.banking.fints.model.TanMethodType.ChipTanFlickercode -> TanMethodType.ChipTanFlickercode
            net.dankito.banking.fints.model.TanMethodType.ChipTanUsb -> TanMethodType.ChipTanUsb
            net.dankito.banking.fints.model.TanMethodType.ChipTanQrCode -> TanMethodType.ChipTanQrCode
            net.dankito.banking.fints.model.TanMethodType.ChipTanPhotoTanMatrixCode -> TanMethodType.ChipTanPhotoTanMatrixCode
            net.dankito.banking.fints.model.TanMethodType.SmsTan -> TanMethodType.SmsTan
            net.dankito.banking.fints.model.TanMethodType.AppTan -> TanMethodType.AppTan
            net.dankito.banking.fints.model.TanMethodType.photoTan -> TanMethodType.photoTan
            net.dankito.banking.fints.model.TanMethodType.QrCode -> TanMethodType.QrCode
        }
    }

    open fun mapAllowedTanFormat(allowedTanFormat: net.dankito.banking.fints.messages.datenelemente.implementierte.tan.AllowedTanFormat): AllowedTanFormat {
        return when (allowedTanFormat) {
            net.dankito.banking.fints.messages.datenelemente.implementierte.tan.AllowedTanFormat.Alphanumeric -> AllowedTanFormat.Alphanumeric
            net.dankito.banking.fints.messages.datenelemente.implementierte.tan.AllowedTanFormat.Numeric -> AllowedTanFormat.Numeric
        }
    }

    protected open fun findMappedTanMethod(customer: TypedCustomer, tanMethod: net.dankito.banking.fints.model.TanMethod): TanMethod? {
        return customer.supportedTanMethods.firstOrNull { it.bankInternalMethodCode == tanMethod.securityFunction.code }
    }

    protected open fun findTanMethod(bank: BankData, tanMethod: TanMethod?): net.dankito.banking.fints.model.TanMethod? {
        if (tanMethod == null) {
            return null
        }

        return bank.tanMethodsAvailableForUser.firstOrNull { it.securityFunction.code == tanMethod.bankInternalMethodCode }
    }

    protected open fun findMappedTanMedium(customer: TypedCustomer, tanMedium: net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanMedium): TanMedium? {
        return customer.tanMedia.firstOrNull { doesMatchTanMedium(tanMedium, it) }
    }

    protected open fun doesMatchTanMedium(fintsTanMedium: net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanMedium, tanMedium: TanMedium): Boolean {
        return when (fintsTanMedium.mediumClass) {
            TanMediumKlasse.TanGenerator -> tanMedium is TanGeneratorTanMedium && tanMedium.cardNumber == (fintsTanMedium as? net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium)?.cardNumber
            TanMediumKlasse.MobiltelefonMitMobileTan -> tanMedium is MobilePhoneTanMedium && (tanMedium.phoneNumber == (fintsTanMedium as? net.dankito.banking.fints.messages.datenelemente.implementierte.tan.MobilePhoneTanMedium)?.phoneNumber ||
                    tanMedium.phoneNumber == (fintsTanMedium as? net.dankito.banking.fints.messages.datenelemente.implementierte.tan.MobilePhoneTanMedium)?.concealedPhoneNumber)
            else -> tanMedium.displayName == fintsTanMedium.mediumClass.name
        }
    }


    open fun mapTanMedia(tanMediums: List<net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanMedium>): List<TanMedium> {
        return tanMediums.map { mapTanMedium(it) }
    }

    open fun mapTanMedium(tanMedium: net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanMedium): TanMedium {
        val displayName = getDisplayNameForTanMedium(tanMedium)
        val status = mapTanMediumStatus(tanMedium)

        // TODO: irgendwas ging hier schief
        if (tanMedium is net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium) {
            return mapTanMedium(tanMedium, displayName, status)
        }

        if (tanMedium is net.dankito.banking.fints.messages.datenelemente.implementierte.tan.MobilePhoneTanMedium) {
            return modelCreator.createMobilePhoneTanMedium(displayName, status, tanMedium.phoneNumber ?: tanMedium.concealedPhoneNumber)
        }

        return modelCreator.createTanMedium(displayName, status)
    }

    open fun mapTanMedium(tanMedium: net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium): TanGeneratorTanMedium {
        return mapTanMedium(tanMedium, getDisplayNameForTanMedium(tanMedium), mapTanMediumStatus(tanMedium))
    }

    protected open fun mapTanMedium(tanMedium: net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium,
                          displayName: String, status: TanMediumStatus): TanGeneratorTanMedium {

        return modelCreator.createTanGeneratorTanMedium(displayName, status, tanMedium.cardNumber)
    }

    protected open fun getDisplayNameForTanMedium(tanMedium: net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanMedium): String {
        if (tanMedium is net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium) {
            var cardNumber = tanMedium.cardNumber
            tanMedium.cardSequenceNumber?.let {
                cardNumber += " (Kartenfolgenummer $it)" // TODO: translate
            }

            tanMedium.mediumName?.let { mediaName ->
                return "$mediaName $cardNumber"
            }

            return "Karte $cardNumber" // TODO: translate
        }
        else if (tanMedium is net.dankito.banking.fints.messages.datenelemente.implementierte.tan.MobilePhoneTanMedium) {
            val mediumName =  tanMedium.mediumName

            (tanMedium.phoneNumber ?: tanMedium.concealedPhoneNumber)?.let { phoneNumber ->
                return "$mediumName ($phoneNumber)"
            }

            return mediumName
        }

        return tanMedium.mediumClass.name
    }

    open fun mapTanMediumStatus(tanMedium: net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanMedium): TanMediumStatus {
        return if (tanMedium.status.name.contains("aktiv", true)) TanMediumStatus.Used else TanMediumStatus.Available
    }

    open fun mapTanMedium(tanMedium: TanMedium, bank: BankData): net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanMedium {
        if (tanMedium is TanGeneratorTanMedium) {
            return mapTanMedium(tanMedium, bank)
        }

        val statusToHave = if (tanMedium.status == TanMediumStatus.Used) listOf(net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanMediumStatus.Aktiv, net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanMediumStatus.AktivFolgekarte)
        else listOf(net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanMediumStatus.Verfuegbar, net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanMediumStatus.VerfuegbarFolgekarte)

        return bank.tanMedia.first { tanMedium.displayName == it.mediumClass.name && statusToHave.contains(it.status) }
    }

    open fun mapTanMedium(tanMedium: TanGeneratorTanMedium, bank: BankData): net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium {
        return bank.tanMedia.mapNotNull { it as? net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium }
            .first { it.cardNumber == tanMedium.cardNumber
                    && (it.cardSequenceNumber == null || tanMedium.displayName.contains(it.cardSequenceNumber!!)) }
    }


    open fun mapTanMethod(tanMethod: TanMethod): net.dankito.banking.fints.model.TanMethod {
        return net.dankito.banking.fints.model.TanMethod(
            tanMethod.displayName,
            Sicherheitsfunktion.values().first { it.code == tanMethod.bankInternalMethodCode },
            mapTanMethodType(tanMethod.type),
            null, // TODO: where to get HDD Version from?
            tanMethod.maxTanInputLength,
            mapAllowedTanFormat(tanMethod.allowedTanFormat)
        )
    }

    open fun mapTanMethodType(type: TanMethodType): net.dankito.banking.fints.model.TanMethodType {
        return when (type) {
            TanMethodType.EnterTan -> net.dankito.banking.fints.model.TanMethodType.EnterTan
            TanMethodType.ChipTanManuell -> net.dankito.banking.fints.model.TanMethodType.ChipTanManuell
            TanMethodType.ChipTanFlickercode -> net.dankito.banking.fints.model.TanMethodType.ChipTanFlickercode
            TanMethodType.ChipTanUsb -> net.dankito.banking.fints.model.TanMethodType.ChipTanUsb
            TanMethodType.ChipTanQrCode -> net.dankito.banking.fints.model.TanMethodType.ChipTanQrCode
            TanMethodType.ChipTanPhotoTanMatrixCode -> net.dankito.banking.fints.model.TanMethodType.ChipTanPhotoTanMatrixCode
            TanMethodType.SmsTan -> net.dankito.banking.fints.model.TanMethodType.SmsTan
            TanMethodType.AppTan -> net.dankito.banking.fints.model.TanMethodType.AppTan
            TanMethodType.photoTan -> net.dankito.banking.fints.model.TanMethodType.photoTan
            TanMethodType.QrCode -> net.dankito.banking.fints.model.TanMethodType.QrCode
        }
    }

    open fun mapAllowedTanFormat(allowedTanFormat: AllowedTanFormat): net.dankito.banking.fints.messages.datenelemente.implementierte.tan.AllowedTanFormat {
        return when (allowedTanFormat) {
            AllowedTanFormat.Alphanumeric -> net.dankito.banking.fints.messages.datenelemente.implementierte.tan.AllowedTanFormat.Alphanumeric
            AllowedTanFormat.Numeric -> net.dankito.banking.fints.messages.datenelemente.implementierte.tan.AllowedTanFormat.Numeric
        }
    }

    open fun mapEnterTanResult(result: EnterTanResult, bank: BankData): net.dankito.banking.fints.model.EnterTanResult {
        result.changeTanMethodTo?.let { changeTanMethodTo ->
            return net.dankito.banking.fints.model.EnterTanResult.userAsksToChangeTanMethod(mapTanMethod(changeTanMethodTo))
        }

        result.changeTanMediumTo?.let { changeTanMediumTo ->
            val callback: ((FinTsClientResponse) -> Unit)? = if (result.changeTanMediumResultCallback == null) null
            else { response -> result.changeTanMediumResultCallback?.invoke(mapResponse(response)) }
            return net.dankito.banking.fints.model.EnterTanResult.userAsksToChangeTanMedium(mapTanMedium(changeTanMediumTo, bank), callback)
        }

        result.enteredTan?.let { enteredTan ->
            return net.dankito.banking.fints.model.EnterTanResult.userEnteredTan(enteredTan)
        }

        return net.dankito.banking.fints.model.EnterTanResult.userDidNotEnterTan()
    }

    open fun mapEnterTanGeneratorAtcResult(result: EnterTanGeneratorAtcResult): net.dankito.banking.fints.model.EnterTanGeneratorAtcResult {
        if (result.hasAtcBeenEntered) {
            return net.dankito.banking.fints.model.EnterTanGeneratorAtcResult.userEnteredAtc(result.tan!!, result.atc!!)
        }

        return net.dankito.banking.fints.model.EnterTanGeneratorAtcResult.userDidNotEnterAtc()
    }

    open fun mapTanChallenge(tanChallenge: net.dankito.banking.fints.model.TanChallenge): TanChallenge {
        if (tanChallenge is net.dankito.banking.fints.model.FlickerCodeTanChallenge) {
            return mapTanChallenge(tanChallenge)
        }

        if (tanChallenge is net.dankito.banking.fints.model.ImageTanChallenge) {
            return mapTanChallenge(tanChallenge)
        }

        return TanChallenge(tanChallenge.messageToShowToUser,
            mapTanMethod(tanChallenge.tanMethod)
        )
    }

    open fun mapTanChallenge(tanChallenge: net.dankito.banking.fints.model.FlickerCodeTanChallenge): FlickerCodeTanChallenge {
        return FlickerCodeTanChallenge(mapFlickerCode(tanChallenge.flickerCode), tanChallenge.messageToShowToUser,
            mapTanMethod(tanChallenge.tanMethod)
        )
    }

    open fun mapFlickerCode(flickerCode: net.dankito.banking.fints.tan.FlickerCode): FlickerCode {
        return FlickerCode(flickerCode.challengeHHD_UC, flickerCode.parsedDataSet, flickerCode.decodingError)
    }

    open fun mapTanChallenge(tanChallenge: net.dankito.banking.fints.model.ImageTanChallenge): ImageTanChallenge {
        return ImageTanChallenge(mapTanImage(tanChallenge.image), tanChallenge.messageToShowToUser,
            mapTanMethod(tanChallenge.tanMethod)
        )
    }

    open fun mapTanImage(image: net.dankito.banking.fints.tan.TanImage): TanImage {
        return TanImage(image.mimeType, image.imageBytes, image.decodingError)
    }

}