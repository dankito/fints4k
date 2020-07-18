import Foundation
import CoreData
import BankingUiSwift


class Mapper {
    
    func map(_ customer: Customer) -> BUCCustomer {
        let mapped = BUCCustomer(bankCode: map(customer.bankCode), customerId: map(customer.customerId), password: map(customer.password), finTsServerAddress: map(customer.finTsServerAddress), bankName: map(customer.bankName), bic: map(customer.bic), customerName: map(customer.customerName), userId: map(customer.userId), iconUrl: customer.iconUrl, accounts: [])
        
        mapped.accounts = map(mapped, customer.accounts as? Set<BankAccount>)
        
        return mapped
    }
    
    func map(_ customer: BUCCustomer, _ context: NSManagedObjectContext) -> Customer {
        let mapped = Customer(context: context)
        
        mapped.bankCode = customer.bankCode
        mapped.customerId = customer.customerId
        mapped.password = customer.password
        mapped.finTsServerAddress = customer.finTsServerAddress
        mapped.bankName = customer.bankName
        mapped.bic = customer.bic
        mapped.customerName = customer.customerName
        mapped.userId = customer.userId
        mapped.iconUrl = customer.iconUrl
        
        mapped.accounts = NSSet(array: map(mapped, customer.accounts, context))
        
        return mapped
    }
    
    
    func map(_ customer: BUCCustomer, _ accounts: Set<BankAccount>?) -> [BUCBankAccount] {
        return accounts?.map( { map(customer, $0) } ) ?? []
    }
    
    func map(_ customer: BUCCustomer, _ account: BankAccount) -> BUCBankAccount {
        let mapped = BUCBankAccount(customer: customer, identifier: map(account.identifier), accountHolderName: map(account.accountHolderName), iban: account.iban, subAccountNumber: account.subAccountNumber, customerId: map(account.customerId), balance: map(account.balance), currency: map(account.currency), type: map(account.type), productName: account.productName, accountLimit: account.accountLimit, lastRetrievedTransactionsTimestamp: map(account.lastRetrievedTransactionsTimestamp), supportsRetrievingAccountTransactions: account.supportsRetrievingAccountTransactions, supportsRetrievingBalance: account.supportsRetrievingBalance, supportsTransferringMoney: account.supportsTransferringMoney, supportsInstantPaymentMoneyTransfer: account.supportsInstantPaymentMoneyTransfer, bookedAccountTransactions: [])
        
        mapped.bookedTransactions = map(mapped, account.transactions as? Set<AccountTransaction>)
        
        return mapped
    }
    
    func map(_ customer: Customer, _ accounts: [BUCBankAccount], _ context: NSManagedObjectContext) -> [BankAccount] {
        return accounts.map( { map(customer, $0, context) } )
    }
    
    func map(_ customer: Customer, _ account: BUCBankAccount, _ context: NSManagedObjectContext) -> BankAccount {
        let mapped = BankAccount(context: context)
        
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
        
        mapped.transactions = NSSet(array: map(mapped, account.bookedTransactions, context))
        
        return mapped
    }
    
    
    func map(_ type: BUCBankAccountType) -> String {
        return type.name
    }
    
    func map(_ type: String?) -> BUCBankAccountType {
        switch type {
        case BUCBankAccountType.girokonto.name:
            return BUCBankAccountType.girokonto
        case BUCBankAccountType.sparkonto.name:
            return BUCBankAccountType.sparkonto
        case BUCBankAccountType.festgeldkonto.name:
            return BUCBankAccountType.festgeldkonto
        case BUCBankAccountType.wertpapierdepot.name:
            return BUCBankAccountType.wertpapierdepot
        case BUCBankAccountType.darlehenskonto.name:
            return BUCBankAccountType.darlehenskonto
        case BUCBankAccountType.kreditkartenkonto.name:
            return BUCBankAccountType.kreditkartenkonto
        case BUCBankAccountType.fondsdepot.name:
            return BUCBankAccountType.fondsdepot
        case BUCBankAccountType.bausparvertrag.name:
            return BUCBankAccountType.bausparvertrag
        case BUCBankAccountType.versicherungsvertrag.name:
            return BUCBankAccountType.versicherungsvertrag
        case BUCBankAccountType.sonstige.name:
            return BUCBankAccountType.sonstige
        default:
            return BUCBankAccountType.girokonto
        }
    }
    
    
    func map(_ account: BUCBankAccount, _ transactions: Set<AccountTransaction>?) -> [BUCAccountTransaction] {
        return transactions?.map( {map(account, $0) } ) ?? []
    }
    
    func map(_ account: BUCBankAccount, _ transaction: AccountTransaction) -> BUCAccountTransaction {
        return BUCAccountTransaction(bankAccount: account, amount: map(transaction.amount), currency: map(transaction.currency), unparsedUsage: map(transaction.unparsedUsage), bookingDate: map(transaction.bookingDate), otherPartyName: transaction.otherPartyName, otherPartyBankCode: transaction.otherPartyBankCode, otherPartyAccountId: transaction.otherPartyAccountId, bookingText: transaction.bookingText, valueDate: map(transaction.valueDate), statementNumber: Int32(transaction.statementNumber), sequenceNumber: map(transaction.sequenceNumber), openingBalance: map(transaction.openingBalance), closingBalance: map(transaction.closingBalance), endToEndReference: transaction.endToEndReference, customerReference: transaction.customerReference, mandateReference: transaction.mandateReference, creditorIdentifier: transaction.creditorIdentifier, originatorsIdentificationCode: transaction.originatorsIdentificationCode, compensationAmount: transaction.compensationAmount, originalAmount: transaction.originalAmount, sepaUsage: transaction.sepaUsage, deviantOriginator: transaction.deviantOriginator, deviantRecipient: transaction.deviantRecipient, usageWithNoSpecialType: transaction.usageWithNoSpecialType, primaNotaNumber: transaction.primaNotaNumber, textKeySupplement: transaction.textKeySupplement, currencyType: transaction.currencyType, bookingKey: map(transaction.bookingKey), referenceForTheAccountOwner: map(transaction.referenceForTheAccountOwner), referenceOfTheAccountServicingInstitution: transaction.referenceOfTheAccountServicingInstitution, supplementaryDetails: transaction.supplementaryDetails, transactionReferenceNumber: map(transaction.transactionReferenceNumber), relatedReferenceNumber: transaction.relatedReferenceNumber)
    }
    
    
    func map(_ account: BankAccount, _ transactions: [BUCAccountTransaction], _ context: NSManagedObjectContext) -> [AccountTransaction] {
        return transactions.map( {map(account, $0, context) } )
    }
    
    func map(_ account: BankAccount, _ transaction: BUCAccountTransaction, _ context: NSManagedObjectContext) -> AccountTransaction {
        let mapped = AccountTransaction(context: context)
        
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
    
    func map(_ string: String?) -> String {
        return string ?? ""
    }
    
}
