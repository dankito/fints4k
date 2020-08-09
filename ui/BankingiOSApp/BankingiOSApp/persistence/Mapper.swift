import Foundation
import CoreData
import BankingUiSwift


class Mapper {
    
    /*      Cache mapped object to not save them twice      */
    private var mappedBanks = [Customer:PersistedCustomer]()
    
    private var mappedAccounts = [BankAccount:PersistedBankAccount]()
    
    private var mappedTransactions = [AccountTransaction:PersistedAccountTransaction]()
    
    private var mappedTanProcedures = [TanProcedure:PersistedTanProcedure]()
    
    
    func map(_ customer: PersistedCustomer) -> Customer {
        let mapped = Customer(bankCode: map(customer.bankCode), customerId: map(customer.customerId), password: map(customer.password), finTsServerAddress: map(customer.finTsServerAddress), bankName: map(customer.bankName), bic: map(customer.bic), customerName: map(customer.customerName), userId: map(customer.userId), iconUrl: customer.iconUrl, accounts: [])

        mapped.userSetDisplayName = customer.userSetDisplayName
        
        mapped.accounts = map(mapped, customer.accounts?.array as? [PersistedBankAccount])
        
        mappedBanks[mapped] = customer
        
        mapped.supportedTanProcedures = map(customer.supportedTanProcedures?.array as? [PersistedTanProcedure])
        mapped.selectedTanProcedure = mapped.supportedTanProcedures.first(where: { $0.bankInternalProcedureCode == customer.selectedTanProcedureCode })
        
        return mapped
    }
    
    func map(_ customer: Customer, _ context: NSManagedObjectContext) -> PersistedCustomer {
        let mapped = mappedBanks[customer] ?? PersistedCustomer(context: context)
        
        mapped.bankCode = customer.bankCode
        mapped.customerId = customer.customerId
        mapped.password = customer.password
        mapped.finTsServerAddress = customer.finTsServerAddress
        mapped.bankName = customer.bankName
        mapped.bic = customer.bic
        mapped.customerName = customer.customerName
        mapped.userId = customer.userId
        mapped.iconUrl = customer.iconUrl

        mapped.userSetDisplayName = customer.userSetDisplayName
        
        mapped.accounts = NSOrderedSet(array: map(mapped, customer.accounts, context))
        
        mappedBanks[customer] = mapped
        
        mapped.supportedTanProcedures = NSOrderedSet(array: map(customer.supportedTanProcedures, context))
        mapped.selectedTanProcedureCode = customer.selectedTanProcedure?.bankInternalProcedureCode
        
        return mapped
    }
    
    
    func map(_ customer: Customer, _ accounts: [PersistedBankAccount]?) -> [BankAccount] {
        return accounts?.map( { map(customer, $0) } ) ?? []
    }
    
    func map(_ customer: Customer, _ account: PersistedBankAccount) -> BankAccount {
        let mapped = BankAccount(customer: customer, identifier: map(account.identifier), accountHolderName: map(account.accountHolderName), iban: account.iban, subAccountNumber: account.subAccountNumber, customerId: map(account.customerId), balance: map(account.balance), currency: map(account.currency), type: map(account.type), productName: account.productName, accountLimit: account.accountLimit, lastRetrievedTransactionsTimestamp: map(account.lastRetrievedTransactionsTimestamp), supportsRetrievingAccountTransactions: account.supportsRetrievingAccountTransactions, supportsRetrievingBalance: account.supportsRetrievingBalance, supportsTransferringMoney: account.supportsTransferringMoney, supportsInstantPaymentMoneyTransfer: account.supportsInstantPaymentMoneyTransfer, bookedTransactions: [], unbookedTransactions: [])
        
        mapped.haveAllTransactionsBeenFetched = account.haveAllTransactionsBeenFetched
        
        mapped.userSetDisplayName = account.userSetDisplayName
        
        mapped.bookedTransactions = map(mapped, account.transactions as? Set<PersistedAccountTransaction>)
        
        mappedAccounts[mapped] = account
        
        return mapped
    }
    
    func map(_ customer: PersistedCustomer, _ accounts: [BankAccount], _ context: NSManagedObjectContext) -> [PersistedBankAccount] {
        return accounts.map( { map(customer, $0, context) } )
    }
    
    func map(_ customer: PersistedCustomer, _ account: BankAccount, _ context: NSManagedObjectContext) -> PersistedBankAccount {
        let mapped = mappedAccounts[account] ?? PersistedBankAccount(context: context)
        
        mapped.customer = customer
        mapped.identifier = account.identifier
        mapped.accountHolderName = account.accountHolderName
        mapped.iban = account.iban
        mapped.subAccountNumber = account.subAccountNumber
        mapped.customerId = account.customerId
        mapped.balance = account.balance.decimal
        mapped.currency = account.currency
        mapped.type = map(account.type)
        mapped.productName = account.productName
        mapped.accountLimit = account.accountLimit
        mapped.lastRetrievedTransactionsTimestamp = account.lastRetrievedTransactionsTimestamp?.date
        mapped.supportsRetrievingAccountTransactions = account.supportsRetrievingAccountTransactions
        mapped.supportsRetrievingBalance = account.supportsRetrievingBalance
        mapped.supportsTransferringMoney = account.supportsTransferringMoney
        mapped.supportsInstantPaymentMoneyTransfer = account.supportsInstantPaymentMoneyTransfer
        
        mapped.haveAllTransactionsBeenFetched = account.haveAllTransactionsBeenFetched
        
        mapped.userSetDisplayName = account.userSetDisplayName
        
        mapped.transactions = NSSet(array: map(mapped, account.bookedTransactions, context))
        
        mappedAccounts[account] = mapped
        
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
    
    
    func map(_ account: BankAccount, _ transactions: Set<PersistedAccountTransaction>?) -> [AccountTransaction] {
        return transactions?.map( {map(account, $0) } ) ?? []
    }
    
    func map(_ account: BankAccount, _ transaction: PersistedAccountTransaction) -> AccountTransaction {
        let mapped = AccountTransaction(bankAccount: account, amount: map(transaction.amount), currency: map(transaction.currency), unparsedUsage: map(transaction.unparsedUsage), bookingDate: map(transaction.bookingDate), otherPartyName: transaction.otherPartyName, otherPartyBankCode: transaction.otherPartyBankCode, otherPartyAccountId: transaction.otherPartyAccountId, bookingText: transaction.bookingText, valueDate: map(transaction.valueDate), statementNumber: Int32(transaction.statementNumber), sequenceNumber: map(transaction.sequenceNumber), openingBalance: map(transaction.openingBalance), closingBalance: map(transaction.closingBalance), endToEndReference: transaction.endToEndReference, customerReference: transaction.customerReference, mandateReference: transaction.mandateReference, creditorIdentifier: transaction.creditorIdentifier, originatorsIdentificationCode: transaction.originatorsIdentificationCode, compensationAmount: transaction.compensationAmount, originalAmount: transaction.originalAmount, sepaUsage: transaction.sepaUsage, deviantOriginator: transaction.deviantOriginator, deviantRecipient: transaction.deviantRecipient, usageWithNoSpecialType: transaction.usageWithNoSpecialType, primaNotaNumber: transaction.primaNotaNumber, textKeySupplement: transaction.textKeySupplement, currencyType: transaction.currencyType, bookingKey: map(transaction.bookingKey), referenceForTheAccountOwner: map(transaction.referenceForTheAccountOwner), referenceOfTheAccountServicingInstitution: transaction.referenceOfTheAccountServicingInstitution, supplementaryDetails: transaction.supplementaryDetails, transactionReferenceNumber: map(transaction.transactionReferenceNumber), relatedReferenceNumber: transaction.relatedReferenceNumber)
        
        mappedTransactions[mapped] = transaction
        
        return mapped
    }
    
    
    func map(_ account: PersistedBankAccount, _ transactions: [AccountTransaction], _ context: NSManagedObjectContext) -> [PersistedAccountTransaction] {
        return transactions.map( {map(account, $0, context) } )
    }
    
    func map(_ account: PersistedBankAccount, _ transaction: AccountTransaction, _ context: NSManagedObjectContext) -> PersistedAccountTransaction {
        let mapped = mappedTransactions[transaction] ?? PersistedAccountTransaction(context: context)
        
        mapped.account = account
        
        mapped.amount = map(transaction.amount)
        mapped.currency = transaction.currency
        mapped.unparsedUsage = transaction.unparsedUsage
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
        mapped.sepaUsage = transaction.sepaUsage
        mapped.deviantOriginator = transaction.deviantOriginator
        mapped.deviantRecipient = transaction.deviantRecipient
        mapped.usageWithNoSpecialType = transaction.usageWithNoSpecialType
        mapped.primaNotaNumber = transaction.primaNotaNumber
        mapped.textKeySupplement = transaction.textKeySupplement
        
        mapped.currencyType = transaction.currencyType
        mapped.bookingKey = transaction.bookingKey
        mapped.referenceForTheAccountOwner = transaction.referenceForTheAccountOwner
        mapped.referenceOfTheAccountServicingInstitution = transaction.referenceOfTheAccountServicingInstitution
        mapped.supplementaryDetails = transaction.supplementaryDetails
        
        mapped.transactionReferenceNumber = transaction.transactionReferenceNumber
        mapped.relatedReferenceNumber = transaction.relatedReferenceNumber
        
        mappedTransactions[transaction] = mapped
        
        return mapped
    }
    
    
    func map(_ tanProcedures: [PersistedTanProcedure]?) -> [TanProcedure] {
        return tanProcedures?.map { map($0) } ?? []
    }
    
    func map(_ tanProcedure: PersistedTanProcedure) -> TanProcedure {
        let mapped = TanProcedure(
            displayName: map(tanProcedure.displayName),
            type: mapTanProcedureType(tanProcedure.type),
            bankInternalProcedureCode: map(tanProcedure.bankInternalProcedureCode)
        )
        
        mappedTanProcedures[mapped] = tanProcedure
        
        return mapped
    }
    
    func map(_ tanProcedures: [TanProcedure], _ context: NSManagedObjectContext) -> [PersistedTanProcedure] {
        return tanProcedures.map { map($0, context) }
    }
    
    func map(_ tanProcedure: TanProcedure, _ context: NSManagedObjectContext) -> PersistedTanProcedure {
        let mapped = mappedTanProcedures[tanProcedure] ?? PersistedTanProcedure(context: context)
        
        mapped.displayName = tanProcedure.displayName
        mapped.type = tanProcedure.type.name
        mapped.bankInternalProcedureCode = tanProcedure.bankInternalProcedureCode
        
        mappedTanProcedures[tanProcedure] = mapped
        
        return mapped
    }
    
    func mapTanProcedureType(_ type: String?) -> TanProcedureType {
        switch type {
        case TanProcedureType.entertan.name:
            return TanProcedureType.entertan
        case TanProcedureType.chiptanmanuell.name:
            return TanProcedureType.chiptanmanuell
        case TanProcedureType.chiptanflickercode.name:
            return TanProcedureType.chiptanflickercode
        case TanProcedureType.chiptanusb.name:
            return TanProcedureType.chiptanusb
        case TanProcedureType.chiptanqrcode.name:
            return TanProcedureType.chiptanqrcode
        case TanProcedureType.chiptanphototanmatrixcode.name:
            return TanProcedureType.chiptanphototanmatrixcode
        case TanProcedureType.smstan.name:
            return TanProcedureType.smstan
        case TanProcedureType.apptan.name:
            return TanProcedureType.apptan
        case TanProcedureType.phototan.name:
            return TanProcedureType.phototan
        case TanProcedureType.qrcode.name:
            return TanProcedureType.qrcode
        default:
            return TanProcedureType.entertan
        }
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
    
    func map(_ string: String?) -> String {
        return string ?? ""
    }
    
}
