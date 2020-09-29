import Foundation
import CoreData
import BankingUiSwift


class Mapper {
    
    func map(_ bank: PersistedBankData) -> IBankData {
        let mapped = BankData(bankCode: map(bank.bankCode), userName: map(bank.userName), password: map(bank.password), finTsServerAddress: map(bank.finTsServerAddress), bankName: map(bank.bankName), bic: map(bank.bic), customerName: map(bank.customerName), userId: map(bank.userId), iconUrl: bank.iconUrl, accounts: [])
        
        mapped.countDaysForWhichTransactionsAreKept = mapToInt(bank.countDaysForWhichTransactionsAreKept)

        mapped.userSetDisplayName = bank.userSetDisplayName
        mapped.displayIndex = bank.displayIndex
        
        mapped.accounts = map(mapped, bank.accounts?.array as? [PersistedBankAccount])
        
        mapped.supportedTanMethods = map(bank.supportedTanMethods?.array as? [PersistedTanMethod])
        mapped.selectedTanMethod = mapped.supportedTanMethods.first(where: { $0.bankInternalMethodCode == bank.selectedTanMethodCode })
        
        mapped.tanMedia = map(bank.tanMedia?.array as? [PersistedTanMedium])
        
        mapped.technicalId = bank.objectIDAsString
        
        return mapped
    }
    
    func map(_ bank: IBankData, _ context: NSManagedObjectContext) -> PersistedBankData {
        let mapped = context.objectByID(bank.technicalId) ?? PersistedBankData(context: context)
        
        mapped.bankCode = bank.bankCode
        mapped.userName = bank.userName
        mapped.password = bank.password
        mapped.finTsServerAddress = bank.finTsServerAddress
        mapped.bankName = bank.bankName
        mapped.bic = bank.bic
        mapped.customerName = bank.customerName
        mapped.userId = bank.userId
        mapped.iconUrl = bank.iconUrl
        mapped.countDaysForWhichTransactionsAreKept = mapFromInt(bank.countDaysForWhichTransactionsAreKept)

        mapped.userSetDisplayName = bank.userSetDisplayName
        mapped.displayIndex = bank.displayIndex
        
        mapped.accounts = NSOrderedSet(array: map(mapped, bank.accounts, context))
        
        mapped.supportedTanMethods = NSOrderedSet(array: map(bank.supportedTanMethods, context))
        mapped.selectedTanMethodCode = bank.selectedTanMethod?.bankInternalMethodCode
        
        mapped.tanMedia = NSOrderedSet(array: map(bank.tanMedia, context))
        
        return mapped
    }
    
    
    func map(_ bank: IBankData, _ accounts: [PersistedBankAccount]?) -> [IBankAccount] {
        return accounts?.map( { map(bank, $0) } ) ?? []
    }
    
    func map(_ bank: IBankData, _ account: PersistedBankAccount) -> IBankAccount {
        let mapped = BankAccount(bank: bank, identifier: map(account.identifier), accountHolderName: map(account.accountHolderName), iban: account.iban, subAccountNumber: account.subAccountNumber, balance: map(account.balance), currency: map(account.currency), type: map(account.type), productName: account.productName, accountLimit: account.accountLimit, retrievedTransactionsFromOn: map(account.retrievedTransactionsFromOn), retrievedTransactionsUpTo: map(account.retrievedTransactionsUpTo), supportsRetrievingAccountTransactions: account.supportsRetrievingAccountTransactions, supportsRetrievingBalance: account.supportsRetrievingBalance, supportsTransferringMoney: account.supportsTransferringMoney, supportsRealTimeTransfer: account.supportsRealTimeTransfer, bookedTransactions: [], unbookedTransactions: [])
        
        mapped.haveAllTransactionsBeenRetrieved = account.haveAllTransactionsBeenRetrieved
        mapped.isAccountTypeSupportedByApplication = account.isAccountTypeSupportedByApplication
        
        mapped.userSetDisplayName = account.userSetDisplayName
        mapped.displayIndex = account.displayIndex
        
        mapped.hideAccount = account.hideAccount
        mapped.updateAccountAutomatically = account.updateAccountAutomatically
        mapped.doNotShowStrikingFetchAllTransactionsView = account.doNotShowStrikingFetchAllTransactionsView
        
        mapped.bookedTransactions = map(mapped, account.transactions as? Set<PersistedAccountTransaction>)
        
        mapped.technicalId = account.objectIDAsString
        
        return mapped
    }
    
    func map(_ bank: PersistedBankData, _ accounts: [IBankAccount], _ context: NSManagedObjectContext) -> [PersistedBankAccount] {
        return accounts.map( { map(bank, $0, context) } )
    }
    
    func map(_ bank: PersistedBankData, _ account: IBankAccount, _ context: NSManagedObjectContext) -> PersistedBankAccount {
        let mapped = context.objectByID(account.technicalId) ?? PersistedBankAccount(context: context)
        
        mapped.bank = bank
        mapped.identifier = account.identifier
        mapped.accountHolderName = account.accountHolderName
        mapped.iban = account.iban
        mapped.subAccountNumber = account.subAccountNumber
        mapped.balance = account.balance.decimal
        mapped.currency = account.currency
        mapped.type = map(account.type)
        mapped.isAccountTypeSupportedByApplication = account.isAccountTypeSupportedByApplication
        mapped.productName = account.productName
        mapped.accountLimit = account.accountLimit
        mapped.retrievedTransactionsFromOn = account.retrievedTransactionsFromOn?.date
        mapped.retrievedTransactionsUpTo = account.retrievedTransactionsUpTo?.date
        mapped.supportsRetrievingAccountTransactions = account.supportsRetrievingAccountTransactions
        mapped.supportsRetrievingBalance = account.supportsRetrievingBalance
        mapped.supportsTransferringMoney = account.supportsTransferringMoney
        mapped.supportsRealTimeTransfer = account.supportsRealTimeTransfer
        
        mapped.haveAllTransactionsBeenRetrieved = account.haveAllTransactionsBeenRetrieved
        
        mapped.userSetDisplayName = account.userSetDisplayName
        mapped.displayIndex = account.displayIndex
        
        mapped.hideAccount = account.hideAccount
        mapped.updateAccountAutomatically = account.updateAccountAutomatically
        mapped.doNotShowStrikingFetchAllTransactionsView = account.doNotShowStrikingFetchAllTransactionsView
        
        mapped.transactions = NSSet(array: map(mapped, account.bookedTransactions, context))
        
        return mapped
    }
    
    
    func map(_ type: BankAccountType) -> String {
        return type.name
    }
    
    func map(_ type: String?) -> BankAccountType {
        switch type {
        case BankAccountType.girokonto.name:
            return BankAccountType.girokonto
        case BankAccountType.sparkonto.name:
            return BankAccountType.sparkonto
        case BankAccountType.festgeldkonto.name:
            return BankAccountType.festgeldkonto
        case BankAccountType.wertpapierdepot.name:
            return BankAccountType.wertpapierdepot
        case BankAccountType.darlehenskonto.name:
            return BankAccountType.darlehenskonto
        case BankAccountType.kreditkartenkonto.name:
            return BankAccountType.kreditkartenkonto
        case BankAccountType.fondsdepot.name:
            return BankAccountType.fondsdepot
        case BankAccountType.bausparvertrag.name:
            return BankAccountType.bausparvertrag
        case BankAccountType.versicherungsvertrag.name:
            return BankAccountType.versicherungsvertrag
        case BankAccountType.sonstige.name:
            return BankAccountType.sonstige
        default:
            return BankAccountType.girokonto
        }
    }
    
    
    func map(_ account: IBankAccount, _ transactions: Set<PersistedAccountTransaction>?) -> [IAccountTransaction] {
        return transactions?.map( {map(account, $0) } ) ?? []
    }
    
    func map(_ account: IBankAccount, _ transaction: PersistedAccountTransaction) -> IAccountTransaction {
        let mapped = AccountTransaction(account: account, amount: map(transaction.amount), currency: map(transaction.currency), unparsedReference: map(transaction.unparsedReference), bookingDate: map(transaction.bookingDate), otherPartyName: transaction.otherPartyName, otherPartyBankCode: transaction.otherPartyBankCode, otherPartyAccountId: transaction.otherPartyAccountId, bookingText: transaction.bookingText, valueDate: map(transaction.valueDate), statementNumber: Int32(transaction.statementNumber), sequenceNumber: map(transaction.sequenceNumber), openingBalance: map(transaction.openingBalance), closingBalance: map(transaction.closingBalance), endToEndReference: transaction.endToEndReference, customerReference: transaction.customerReference, mandateReference: transaction.mandateReference, creditorIdentifier: transaction.creditorIdentifier, originatorsIdentificationCode: transaction.originatorsIdentificationCode, compensationAmount: transaction.compensationAmount, originalAmount: transaction.originalAmount, sepaReference: transaction.sepaReference, deviantOriginator: transaction.deviantOriginator, deviantRecipient: transaction.deviantRecipient, referenceWithNoSpecialType: transaction.referenceWithNoSpecialType, primaNotaNumber: transaction.primaNotaNumber, textKeySupplement: transaction.textKeySupplement, currencyType: transaction.currencyType, bookingKey: map(transaction.bookingKey), referenceForTheAccountOwner: map(transaction.referenceForTheAccountOwner), referenceOfTheAccountServicingInstitution: transaction.referenceOfTheAccountServicingInstitution, supplementaryDetails: transaction.supplementaryDetails, transactionReferenceNumber: map(transaction.transactionReferenceNumber), relatedReferenceNumber: transaction.relatedReferenceNumber)
        
        mapped.technicalId = transaction.objectIDAsString
        
        return mapped
    }
    
    
    func map(_ account: PersistedBankAccount, _ transactions: [IAccountTransaction], _ context: NSManagedObjectContext) -> [PersistedAccountTransaction] {
        return transactions.map( {map(account, $0, context) } )
    }
    
    func map(_ account: PersistedBankAccount, _ transaction: IAccountTransaction, _ context: NSManagedObjectContext) -> PersistedAccountTransaction {
        let mapped = context.objectByID(transaction.technicalId) ?? PersistedAccountTransaction(context: context)
        
        mapped.account = account
        
        mapped.amount = map(transaction.amount)
        mapped.currency = transaction.currency
        mapped.unparsedReference = transaction.unparsedReference
        mapped.bookingDate = map(transaction.bookingDate)
        mapped.otherPartyName = transaction.otherPartyName
        mapped.otherPartyBankCode = transaction.otherPartyBankCode
        mapped.otherPartyAccountId = transaction.otherPartyAccountId
        mapped.bookingText = transaction.bookingText
        mapped.valueDate = map(transaction.valueDate)
        
        mapped.statementNumber = transaction.statementNumber
        mapped.sequenceNumber = map(transaction.sequenceNumber) ?? 0 // TODO: why doesn't it accept Int32? ?
        mapped.openingBalance = mapOptional(transaction.openingBalance)
        mapped.closingBalance = mapOptional(transaction.closingBalance)
        
        mapped.endToEndReference = transaction.endToEndReference
        mapped.customerReference = transaction.customerReference
        mapped.mandateReference = transaction.mandateReference
        mapped.creditorIdentifier = transaction.creditorIdentifier
        mapped.originatorsIdentificationCode = transaction.originatorsIdentificationCode
        mapped.compensationAmount = transaction.compensationAmount
        mapped.originalAmount = transaction.originalAmount
        mapped.sepaReference = transaction.sepaReference
        mapped.deviantOriginator = transaction.deviantOriginator
        mapped.deviantRecipient = transaction.deviantRecipient
        mapped.referenceWithNoSpecialType = transaction.referenceWithNoSpecialType
        mapped.primaNotaNumber = transaction.primaNotaNumber
        mapped.textKeySupplement = transaction.textKeySupplement
        
        mapped.currencyType = transaction.currencyType
        mapped.bookingKey = transaction.bookingKey
        mapped.referenceForTheAccountOwner = transaction.referenceForTheAccountOwner
        mapped.referenceOfTheAccountServicingInstitution = transaction.referenceOfTheAccountServicingInstitution
        mapped.supplementaryDetails = transaction.supplementaryDetails
        
        mapped.transactionReferenceNumber = transaction.transactionReferenceNumber
        mapped.relatedReferenceNumber = transaction.relatedReferenceNumber
        
        return mapped
    }
    
    
    func map(_ tanMethods: [PersistedTanMethod]?) -> [TanMethod] {
        return tanMethods?.map { map($0) } ?? []
    }
    
    func map(_ tanMethod: PersistedTanMethod) -> TanMethod {
        let mapped = TanMethod(
            displayName: map(tanMethod.displayName),
            type: mapTanMethodType(tanMethod.type),
            bankInternalMethodCode: map(tanMethod.bankInternalMethodCode),
            maxTanInputLength: map(tanMethod.maxTanInputLength),
            allowedTanFormat: tanMethod.allowedTanFormat == "numeric" ? .numeric : .alphanumeric
        )
        
        mapped.technicalId = tanMethod.objectIDAsString
        
        return mapped
    }
    
    func map(_ tanMethods: [TanMethod], _ context: NSManagedObjectContext) -> [PersistedTanMethod] {
        return tanMethods.map { map($0, context) }
    }
    
    func map(_ tanMethod: TanMethod, _ context: NSManagedObjectContext) -> PersistedTanMethod {
        let mapped = context.objectByID(tanMethod.technicalId) ?? PersistedTanMethod(context: context)
        
        mapped.displayName = tanMethod.displayName
        mapped.type = tanMethod.type.name
        mapped.bankInternalMethodCode = tanMethod.bankInternalMethodCode
        
        mapped.maxTanInputLength = map(tanMethod.maxTanInputLength) ?? -1
        mapped.allowedTanFormat = tanMethod.allowedTanFormat.name
        
        return mapped
    }
    
    func mapTanMethodType(_ type: String?) -> TanMethodType {
        switch type {
        case TanMethodType.entertan.name:
            return TanMethodType.entertan
        case TanMethodType.chiptanmanuell.name:
            return TanMethodType.chiptanmanuell
        case TanMethodType.chiptanflickercode.name:
            return TanMethodType.chiptanflickercode
        case TanMethodType.chiptanusb.name:
            return TanMethodType.chiptanusb
        case TanMethodType.chiptanqrcode.name:
            return TanMethodType.chiptanqrcode
        case TanMethodType.chiptanphototanmatrixcode.name:
            return TanMethodType.chiptanphototanmatrixcode
        case TanMethodType.smstan.name:
            return TanMethodType.smstan
        case TanMethodType.apptan.name:
            return TanMethodType.apptan
        case TanMethodType.phototan.name:
            return TanMethodType.phototan
        case TanMethodType.qrcode.name:
            return TanMethodType.qrcode
        default:
            return TanMethodType.entertan
        }
    }
    
    
    func map(_ tanMedia: [PersistedTanMedium]?) -> [TanMedium] {
        return tanMedia?.map { map($0) } ?? []
    }
    
    func map(_ tanMedium: PersistedTanMedium) -> TanMedium {
        let mapped = TanMedium(
            displayName: map(tanMedium.displayName),
            status: mapTanMediumStatus(tanMedium.status)
        )
        
        mapped.technicalId = tanMedium.objectIDAsString
        
        return mapped
    }
    
    func map(_ tanMedia: [TanMedium], _ context: NSManagedObjectContext) -> [PersistedTanMedium] {
        return tanMedia.map { map($0, context) }
    }
    
    func map(_ tanMedium: TanMedium, _ context: NSManagedObjectContext) -> PersistedTanMedium {
        let mapped = context.objectByID(tanMedium.technicalId) ?? PersistedTanMedium(context: context)
        
        mapped.displayName = tanMedium.displayName
        mapped.status = tanMedium.status.name
        
        return mapped
    }
    
    func mapTanMediumStatus(_ status: String?) -> TanMediumStatus {
        switch status {
        case TanMediumStatus.used.name:
            return TanMediumStatus.used
        case TanMediumStatus.available.name:
            return TanMediumStatus.available
        default:
            return TanMediumStatus.available
        }
    }
    
    
    func map(_ settings: PersistedAppSettings) -> AppSettings {
        let mapped = AppSettings(
            updateAccountsAutomatically: settings.updateAccountsAutomatically,
            refreshAccountsAfterMinutes: settings.refreshAccountsAfterMinutes,
            flickerCodeSettings: map(settings.flickerCodeSettings),
            qrCodeSettings: map(settings.qrCodeSettings),
            photoTanSettings: map(settings.photoTanSettings))
        
        mapped.technicalId = settings.objectIDAsString
        
        return mapped
    }
    
    func map(_ settings: AppSettings, _ context: NSManagedObjectContext) -> PersistedAppSettings {
        let mapped = context.objectByID(settings.technicalId) ?? PersistedAppSettings(context: context)
        
        mapped.updateAccountsAutomatically = settings.updateAccountsAutomatically
        mapped.refreshAccountsAfterMinutes = settings.refreshAccountsAfterMinutes
        
        mapped.flickerCodeSettings = map(settings.flickerCodeSettings, context)
        mapped.qrCodeSettings = map(settings.qrCodeSettings, context)
        mapped.photoTanSettings = map(settings.photoTanSettings, context)
        
        return mapped
    }
    
    
    func map(_ settings: PersistedTanMethodSettings?) -> TanMethodSettings? {
        guard let settings = settings else {
            return nil
        }
        
        let mapped = TanMethodSettings(width: settings.width, height: settings.height, space: settings.space, frequency: settings.frequency)
        
        mapped.technicalId = settings.objectIDAsString
        
        return mapped
    }
    
    func map(_ settings: TanMethodSettings?, _ context: NSManagedObjectContext) -> PersistedTanMethodSettings? {
        guard let settings = settings else {
            return nil
        }
        
        let mapped = context.objectByID(settings.technicalId) ?? PersistedTanMethodSettings(context: context)
        
        mapped.width = settings.width
        mapped.height = settings.height
        mapped.space = settings.space
        mapped.frequency = settings.frequency
        
        return mapped
    }
    
    
    func map(_ date: Date?) -> CommonDate {
        if let date = date {
            return CommonDate(date: date)
        }
        
        return CommonDate(millisSinceEpoch: 0)
    }
    
    func map(_ date: CommonDate) -> Date {
        return date.date
    }
    
    
    func map(_ decimal: NSDecimalNumber?) -> CommonBigDecimal {
        if let decimal = decimal {
            return map(decimal)
        }
        
        return CommonBigDecimal(double: 0)
    }
    
    func map(_ decimal: NSDecimalNumber) -> CommonBigDecimal {
        return CommonBigDecimal(decimal_: decimal)
    }
    
    func mapOptional(_ decimal: CommonBigDecimal?) -> NSDecimalNumber? {
        if let decimal = decimal {
            return map(decimal)
        }
        
        return nil
    }
    
    func map(_ decimal: CommonBigDecimal) -> NSDecimalNumber {
        return decimal.decimal
    }
    
    
    func map(_ int: Int32?) -> KotlinInt? {
        if let int = int {
            return KotlinInt(int: int)
        }
        
        return nil
    }
    
    func map(_ int: KotlinInt?) -> Int32? {
        if let int = int {
            return Int32(int)
        }
        
        return nil
    }
    
    func mapToInt(_ int: NSNumber?) -> KotlinInt? {
        if let int = int {
            return KotlinInt(int: int.int32Value)
        }
        
        return nil
    }
    
    func mapFromInt(_ int: KotlinInt?) -> NSNumber? {
        if let int = map(int) {
            return NSNumber(value: int)
        }
        
        return nil
    }
    
    func map(_ string: String?) -> String {
        return string ?? ""
    }
    
}
