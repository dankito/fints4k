import Foundation
import BankingUiSwift


let previewBanks = createPreviewBanks()

func createPreviewBanks() -> [BUCCustomer] {
    let bank1 = BUCCustomer(bankCode: "", customerId: "", password: "", finTsServerAddress: "", bankName: "Abzockbank", bic: "", customerName: "Marieke Musterfrau", userId: "", iconUrl: "", accounts: [])
    
    bank1.accounts = [
        BUCBankAccount(customer: bank1, productName: "Girokonto", identifier: "1234567890"),
        
        BUCBankAccount(customer: bank1, productName: "Tagesgeld Minus", identifier: "0987654321")
    ]
    
    
    let bank2 = BUCCustomer(bankCode: "", customerId: "", password: "", finTsServerAddress: "", bankName: "Kundenverarschebank", bic: "", customerName: "Marieke Musterfrau", userId: "", iconUrl: "", accounts: [])
    
    bank2.accounts = [
        BUCBankAccount(customer: bank2, productName: "Girokonto", identifier: "1234567890")
    ]
    
    return [ bank1, bank2 ]
}
