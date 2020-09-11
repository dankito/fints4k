import Foundation
import BankingUiSwift


let previewBanks = createPreviewBanks()

let previewTanProcedures = createPreviewTanProcedures()

let previewTanMedia = createPreviewTanMedia()

let previewTanChallenge = TanChallenge(messageToShowToUser: "Hier ist eine Nachricht deiner Bank, die dir die Welt erklaert", tanProcedure: previewTanProcedures[0])

let previewImageTanChallenge = ImageTanChallenge(image: TanImage(mimeType: "image/png", imageBytes: KotlinByteArray(size: 0), decodingError: nil), messageToShowToUser: "", tanProcedure: previewTanProcedures[1])

let previewFlickerCodeTanChallenge = FlickerCodeTanChallenge(flickerCode: FlickerCode(challengeHHD_UC: "", parsedDataSet: "", decodingError: nil), messageToShowToUser: "", tanProcedure: previewTanProcedures[0])


func createPreviewBanks() -> [ICustomer] {
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


func createPreviewTanProcedures() -> [TanProcedure] {
    return [
        TanProcedure(displayName: "chipTAN optisch", type: .chiptanflickercode, bankInternalProcedureCode: "", maxTanInputLength: 6, allowedTanFormat: .numeric),
        TanProcedure(displayName: "chipTAN QR", type: .chiptanqrcode, bankInternalProcedureCode: "", maxTanInputLength: 8, allowedTanFormat: .numeric),
        TanProcedure(displayName: "Secure Super Duper Plus", type: .apptan, bankInternalProcedureCode: "", maxTanInputLength: 6, allowedTanFormat: .alphanumeric)
    ]
}

func createPreviewTanMedia() -> [TanMedium] {
    return [
        TanMedium(displayName: "EC-Karte mit Nummer 12345678", status: .available),
        TanMedium(displayName: "Handy mit Nummer 0170 / 12345678", status: .available)
    ]
}
