import SwiftUI
import BankingUiSwift


extension Message {
    
    static func createUnsavedChangesMessage(_ discardChanges: @escaping () -> Void) -> Message {
        return Message(title: Text("Unsaved changes"),
                       message: Text("Changed data hasn't been saved. Are you sure you want to discard them?"),
                       primaryButton: .discard(discardChanges),
                       secondaryButton: .cancel())
    }
    
    static func createAskUserToDeleteAccountMessage(_ bank: Customer, _ deleteAccount: @escaping (Customer) -> Void) -> Message {
        return Message(title: Text("Delete account?"),
                       message: Text("Really delete account '\(bank.displayName)'? This cannot be undone and data will be lost."),
                       primaryButton: .destructive(Text("Delete"), action: { deleteAccount(bank) } ),
                       secondaryButton: .cancel())
    }
    
}
