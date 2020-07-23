import Foundation
import BankingUiSwift


let previewBanks = createPreviewBanks()

func createPreviewBanks() -> [Customer] {
    let bank1 = Customer(bankCode: "", customerId: "", password: "", finTsServerAddress: "", bankName: "Abzockbank", bic: "", customerName: "Marieke Musterfrau", userId: "", iconUrl: "", accounts: [])
    
    bank1.accounts = [
        BankAccount(customer: bank1, productName: "Girokonto", identifier: "1234567890"),
        
        BankAccount(customer: bank1, productName: "Tagesgeld Minus", identifier: "0987654321")
    ]
    
    
    let bank2 = Customer(bankCode: "", customerId: "", password: "", finTsServerAddress: "", bankName: "Kundenverarschebank", bic: "", customerName: "Marieke Musterfrau", userId: "", iconUrl: "", accounts: [])
    
    bank2.accounts = [
        BankAccount(customer: bank2, productName: "Girokonto", identifier: "1234567890")
    ]
    
    return [ bank1, bank2 ]
}
