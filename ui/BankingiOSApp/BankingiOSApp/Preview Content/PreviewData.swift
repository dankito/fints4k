import Foundation
import BankingUiSwift


let previewBanks = createPreviewBanks()

func createPreviewBanks() -> [BUCCustomer] {
    let bank1 = BUCCustomer(bankCode: "", customerId: "", password: "", finTsServerAddress: "", bankName: "Abzockbank", bic: "", customerName: "", userId: "", iconUrl: "", accounts: [])
    
    bank1.accounts = [
        BUCBankAccount(customer: bank1, identifier: "id", accountHolderName: "Marieke Musterfrau", iban: nil, subAccountNumber: nil, customerId: "", balance: CommonBigDecimal(double: 17.0), currency: "EUR", type: .girokonto, productName: "Girokonto", accountLimit: nil, lastRetrievedTransactionsTimestamp: nil, supportsRetrievingAccountTransactions: true, supportsRetrievingBalance: true, supportsTransferringMoney: true, supportsInstantPaymentMoneyTransfer: true, bookedAccountTransactions: []),
        
        BUCBankAccount(customer: bank1, identifier: "id", accountHolderName: "Marieke Musterfrau", iban: nil, subAccountNumber: nil, customerId: "", balance: CommonBigDecimal(double: 17.0), currency: "EUR", type: .festgeldkonto, productName: "Tagesgeld Minus", accountLimit: nil, lastRetrievedTransactionsTimestamp: nil, supportsRetrievingAccountTransactions: true, supportsRetrievingBalance: true, supportsTransferringMoney: true, supportsInstantPaymentMoneyTransfer: true, bookedAccountTransactions: [])
    ]
    
    let bank2 = BUCCustomer(bankCode: "", customerId: "", password: "", finTsServerAddress: "", bankName: "Kundenverarschebank", bic: "", customerName: "", userId: "", iconUrl: "", accounts: [])
    
    bank2.accounts = [
        BUCBankAccount(customer: bank1, identifier: "id", accountHolderName: "Marieke Musterfrau", iban: nil, subAccountNumber: nil, customerId: "", balance: CommonBigDecimal(double: 17.0), currency: "EUR", type: .girokonto, productName: "Girokonto", accountLimit: nil, lastRetrievedTransactionsTimestamp: nil, supportsRetrievingAccountTransactions: true, supportsRetrievingBalance: true, supportsTransferringMoney: true, supportsInstantPaymentMoneyTransfer: true, bookedAccountTransactions: [])
    ]
    
    return [ bank1, bank2 ]
}