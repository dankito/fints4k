import Foundation
import BankingUiSwift


let previewBanks = createPreviewBanks()

let previewTanMethods = createPreviewTanMethod()

let previewTanMedia = createPreviewTanMedia()

let previewTanChallenge = TanChallenge(messageToShowToUser: "Hier ist eine Nachricht deiner Bank, die dir die Welt erklaert", tanMethod: previewTanMethods[0])

let previewImageTanChallenge = ImageTanChallenge(image: TanImage(mimeType: "image/png", imageBytes: KotlinByteArray(size: 0), decodingError: nil), messageToShowToUser: "", tanMethod: previewTanMethods[1])

let previewFlickerCodeTanChallenge = FlickerCodeTanChallenge(flickerCode: FlickerCode(challengeHHD_UC: "", parsedDataSet: "", decodingError: nil), messageToShowToUser: "", tanMethod: previewTanMethods[0])


func createPreviewBanks() -> [IBankData] {
    let bank1 = BankData(bankCode: "", userName: "", password: "", finTsServerAddress: "", bankName: "Abzockbank", bic: "", customerName: "Marieke Musterfrau", userId: "", iconUrl: "", accounts: [])
    
    bank1.accounts = [
        BankAccount(bank: bank1, productName: "Girokonto", identifier: "1234567890"),
        
        BankAccount(bank: bank1, productName: "Tagesgeld Minus", identifier: "0987654321")
    ]
    
    
    let bank2 = BankData(bankCode: "", userName: "", password: "", finTsServerAddress: "", bankName: "Kundenverarschebank", bic: "", customerName: "Marieke Musterfrau", userId: "", iconUrl: "", accounts: [])
    
    bank2.accounts = [
        BankAccount(bank: bank2, productName: "Girokonto", identifier: "1234567890")
    ]
    
    return [ bank1, bank2 ]
}


func createPreviewTanMethod() -> [TanMethod] {
    return [
        TanMethod(displayName: "chipTAN optisch", type: .chiptanflickercode, bankInternalMethodCode: "", maxTanInputLength: 6, allowedTanFormat: .numeric),
        TanMethod(displayName: "chipTAN QR", type: .chiptanqrcode, bankInternalMethodCode: "", maxTanInputLength: 8, allowedTanFormat: .numeric),
        TanMethod(displayName: "Secure Super Duper Plus", type: .apptan, bankInternalMethodCode: "", maxTanInputLength: 6, allowedTanFormat: .alphanumeric)
    ]
}

func createPreviewTanMedia() -> [TanMedium] {
    return [
        TanMedium(displayName: "EC-Karte mit Nummer 12345678", status: .available),
        TanMedium(displayName: "Handy mit Nummer 0170 / 12345678", status: .available)
    ]
}
