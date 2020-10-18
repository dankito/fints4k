import Foundation
import BankingUiSwift


class EnterTanState : Identifiable {
    
    let id: Foundation.UUID = UUID()
    
    let bank: IBankData
    
    let tanChallenge: TanChallenge
    
    let callback: (EnterTanResult) -> Void


    init(_ bank: IBankData, _ tanChallenge: TanChallenge, _ callback: @escaping (EnterTanResult) -> Void) {
        self.bank = bank
        self.tanChallenge = tanChallenge
        self.callback = callback
    }
    
}
