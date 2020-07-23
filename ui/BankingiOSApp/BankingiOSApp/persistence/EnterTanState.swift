import Foundation
import BankingUiSwift


class EnterTanState : Identifiable {
    
    let id: Foundation.UUID = UUID()
    
    let customer: Customer
    
    let tanChallenge: TanChallenge
    
    let callback: (EnterTanResult) -> Void


    init(_ customer: Customer, _ tanChallenge: TanChallenge, _ callback: @escaping (EnterTanResult) -> Void) {
        self.customer = customer
        self.tanChallenge = tanChallenge
        self.callback = callback
    }
    
}
