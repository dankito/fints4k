import Foundation
import SwiftUI
import BankingUiSwift


extension Customer : Identifiable {

    public var id: UUID { UUID() }
    
}

extension BankAccount : Identifiable {

    public var id: UUID { UUID() }
    
}

extension AccountTransaction : Identifiable {

    public var id: UUID { UUID() }
    
}

extension Array where Element == AccountTransaction {
    
    func sumAmounts() -> CommonBigDecimal {
        return CommonBigDecimal(decimal_: self.map { $0.amount.decimal }.sum())
    }
    
}


extension BankInfo : Identifiable {

    public var id: UUID { UUID() }

}


extension TanProcedure : Identifiable {

    public var id: String { self.bankInternalProcedureCode }

}
