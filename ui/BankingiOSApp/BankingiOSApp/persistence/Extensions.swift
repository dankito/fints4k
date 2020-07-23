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
